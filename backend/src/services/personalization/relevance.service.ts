import { IStudentProfile } from '../../types/profile.types';
import { IAnnouncement } from '../../types/announcement.types';
import { RelevanceLevel, IRelevanceScoreResult } from './personalization.types';

export class RelevanceService {
  /**
   * Check if a student is eligible to receive an announcement.
   * If any target criteria is empty or contains 'ALL', it is treated as universal.
   * Mandatory announcements are ALWAYS eligible.
   */
  public checkEligibility(student: IStudentProfile, announcement: IAnnouncement): {
    isEligible: boolean;
    isMandatory: boolean;
    deptMatch: boolean;
    yearMatch: boolean;
    sectionMatch: boolean;
  } {
    const isMandatory = this.isMandatoryAnnouncement(announcement);

    const targetDepts = (announcement.targetAudience?.departments || []).map(d => d.toUpperCase().trim());
    const targetYears = announcement.targetAudience?.years || [];
    const targetSections = (announcement.targetAudience?.sections || []).map(s => s.toUpperCase().trim());

    const studentDept = (student.department || '').toUpperCase().trim();
    const studentYear = Number(student.year);
    const studentSection = (student.section || '').toUpperCase().trim();

    const deptMatch =
      targetDepts.length === 0 ||
      targetDepts.includes('ALL') ||
      targetDepts.includes(studentDept);

    const yearMatch =
      targetYears.length === 0 ||
      targetYears.includes(0) ||
      targetYears.includes(studentYear);

    const sectionMatch =
      targetSections.length === 0 ||
      targetSections.includes('ALL') ||
      targetSections.includes(studentSection);

    const isEligible = isMandatory || (deptMatch && yearMatch && sectionMatch);

    return {
      isEligible,
      isMandatory,
      deptMatch,
      yearMatch,
      sectionMatch
    };
  }

  /**
   * Evaluates whether an announcement has mandatory status
   */
  public isMandatoryAnnouncement(announcement: IAnnouncement): boolean {
    const category = (announcement.category || '').toUpperCase().trim();
    const title = (announcement.title || '').toLowerCase();
    const content = (announcement.originalContent || '').toLowerCase();

    if (category === 'EXAMINATION' || category === 'CIRCULAR') {
      return true;
    }

    if (
      title.includes('mandatory') ||
      title.includes('compulsory') ||
      content.includes('mandatory for all') ||
      content.includes('attendance is compulsory')
    ) {
      return true;
    }

    return false;
  }

  /**
   * Calculates comprehensive relevance score (0-100) and human-readable explanation
   */
  public calculateRelevance(
    student: IStudentProfile,
    announcement: IAnnouncement
  ): IRelevanceScoreResult {
    const eligibility = this.checkEligibility(student, announcement);
    const explanation: string[] = [];

    // 1. ACADEMIC MATCH (Up to 40 Points)
    let academicMatchScore = 0;
    const targetDepts = (announcement.targetAudience?.departments || []).map(d => d.toUpperCase().trim());
    const targetYears = announcement.targetAudience?.years || [];
    const targetSections = (announcement.targetAudience?.sections || []).map(s => s.toUpperCase().trim());

    const studentDept = (student.department || '').toUpperCase().trim();
    const studentYear = Number(student.year);
    const studentSection = (student.section || '').toUpperCase().trim();

    // Department match (+15 for direct, +10 for ALL)
    if (targetDepts.includes(studentDept)) {
      academicMatchScore += 15;
      explanation.push(`Matches your department: ${studentDept}`);
    } else if (targetDepts.length === 0 || targetDepts.includes('ALL')) {
      academicMatchScore += 10;
      explanation.push(`Open to all departments including ${studentDept}`);
    }

    // Year match (+15 for direct, +10 for ALL)
    if (targetYears.includes(studentYear)) {
      academicMatchScore += 15;
      explanation.push(`Targeted for Year ${studentYear} students`);
    } else if (targetYears.length === 0 || targetYears.includes(0)) {
      academicMatchScore += 10;
    }

    // Section match (+10 for direct, +5 for ALL)
    if (studentSection && targetSections.includes(studentSection)) {
      academicMatchScore += 10;
      explanation.push(`Specific to Section ${studentSection}`);
    } else if (targetSections.length === 0 || targetSections.includes('ALL')) {
      academicMatchScore += 5;
    }

    academicMatchScore = Math.min(40, academicMatchScore);

    // Collect all student interest tokens (interests + focus areas)
    const studentInterests = [
      ...(student.interests || []),
      ...(student.focusAreas || [])
    ]
      .map(i => i.toLowerCase().trim())
      .filter(Boolean);

    // 2. INTEREST MATCH (Up to 25 Points)
    let interestMatchScore = 0;
    const matchedInterests: string[] = [];
    const searchTarget = [
      announcement.title || '',
      announcement.category || '',
      announcement.eventType || '',
      ...(announcement.keywords || [])
    ]
      .join(' ')
      .toLowerCase();

    for (const interest of studentInterests) {
      if (interest.length >= 2 && searchTarget.includes(interest)) {
        matchedInterests.push(interest);
      }
    }

    if (matchedInterests.length > 0) {
      // First match +15, subsequent +5 each up to 25
      interestMatchScore = Math.min(25, 15 + (matchedInterests.length - 1) * 5);
      const displayNames = matchedInterests.slice(0, 2).map(i => this.capitalize(i)).join(', ');
      explanation.push(`Matches your interest in: ${displayNames}`);
    }

    // 3. CATEGORY AFFINITY (Up to 15 Points)
    let categoryAffinityScore = 0;
    const category = (announcement.category || '').toUpperCase().trim();
    const preferredCats = (student.preferredCategories || []).map(c => c.toUpperCase().trim());

    if (preferredCats.includes(category)) {
      categoryAffinityScore = 15;
      explanation.push(`Related to your preferred category: ${this.formatCategory(category)}`);
    } else {
      // Semantic category affinity mappings
      const hasTechInterest = studentInterests.some(i =>
        ['ai', 'coding', 'programming', 'hackathon', 'web', 'app', 'machine learning', 'cybersecurity'].some(k => i.includes(k))
      );
      const hasCareerInterest = studentInterests.some(i =>
        ['internship', 'placement', 'job', 'career', 'resume', 'interview'].some(k => i.includes(k))
      );
      const hasSportsInterest = studentInterests.some(i =>
        ['sports', 'cricket', 'football', 'badminton', 'athletics', 'fitness'].some(k => i.includes(k))
      );
      const hasCulturalInterest = studentInterests.some(i =>
        ['music', 'dance', 'drama', 'cultural', 'art', 'theatre', 'club'].some(k => i.includes(k))
      );

      if (hasTechInterest && (category === 'HACKATHON' || category === 'WORKSHOP' || category === 'TECHNICAL')) {
        categoryAffinityScore = 15;
        explanation.push(`Category aligns with your technical profile: ${this.formatCategory(category)}`);
      } else if (hasCareerInterest && (category === 'PLACEMENT' || category === 'INTERNSHIP')) {
        categoryAffinityScore = 15;
        explanation.push(`Directly relates to your career and placement goals`);
      } else if (hasSportsInterest && category === 'SPORTS') {
        categoryAffinityScore = 15;
        explanation.push(`Matches your sports and athletics activity`);
      } else if (hasCulturalInterest && (category === 'CULTURAL' || category === 'CLUB')) {
        categoryAffinityScore = 15;
        explanation.push(`Matches your cultural and club activity`);
      }
    }

    // 4. AI KEYWORD MATCH (Up to 20 Points)
    let keywordMatchScore = 0;
    const aiKeywords = (announcement.keywords || []).map(k => k.toLowerCase().trim());
    const matchedAiKeywords: string[] = [];

    for (const interest of studentInterests) {
      for (const kw of aiKeywords) {
        if (kw.includes(interest) || interest.includes(kw)) {
          if (!matchedAiKeywords.includes(kw)) {
            matchedAiKeywords.push(kw);
          }
        }
      }
    }

    if (matchedAiKeywords.length > 0) {
      keywordMatchScore = Math.min(20, matchedAiKeywords.length * 10);
      explanation.push(`Topic aligns with AI tags: #${matchedAiKeywords.slice(0, 2).join(', #')}`);
    }

    // Total raw relevance calculation
    let totalScore = academicMatchScore + interestMatchScore + categoryAffinityScore + keywordMatchScore;

    // If Mandatory, guarantee high visibility
    if (eligibility.isMandatory) {
      totalScore = Math.max(totalScore, 90);
      explanation.unshift(`Mandatory official campus notice`);
    }

    // Strict clamping 0 - 100
    const finalScore = Math.max(0, Math.min(100, Math.round(totalScore)));

    // Map to Relevance Level
    let level = RelevanceLevel.LOW;
    if (finalScore >= 80) {
      level = RelevanceLevel.VERY_HIGH;
    } else if (finalScore >= 60) {
      level = RelevanceLevel.HIGH;
    } else if (finalScore >= 30) {
      level = RelevanceLevel.MEDIUM;
    }

    if (explanation.length === 0) {
      explanation.push('Relevant for general campus community');
    }

    return {
      relevanceScore: finalScore,
      relevanceLevel: level,
      relevanceExplanation: explanation,
      isEligible: eligibility.isEligible,
      isMandatory: eligibility.isMandatory,
      academicMatchScore,
      interestMatchScore,
      categoryAffinityScore,
      keywordMatchScore
    };
  }

  private capitalize(str: string): string {
    if (!str) return '';
    return str.charAt(0).toUpperCase() + str.slice(1);
  }

  private formatCategory(category: string): string {
    return category.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase());
  }
}

export const relevanceService = new RelevanceService();
