import { IPriorityResult, IPriorityBreakdown } from './ai.types';

export class PriorityService {
  /**
   * Deterministically calculates priority score, level, and human-readable explanation
   * according to hybrid multi-factor weighting rules.
   */
  public calculatePriority(params: {
    category: string;
    deadline?: Date | null;
    title: string;
    originalContent: string;
    keyActions?: string[];
    urgencyIndicators?: string[];
    now?: Date;
  }): IPriorityResult {
    const now = params.now || new Date();
    const explanation: string[] = [];

    // 1. DEADLINE URGENCY (Max 40 Points)
    let deadlineScore = 0;
    if (params.deadline) {
      const deadlineMs = params.deadline.getTime();
      const diffMs = deadlineMs - now.getTime();
      const diffHours = diffMs / (1000 * 60 * 60);

      if (diffHours <= 0) {
        // Expired or expiring right now
        deadlineScore = 40;
        explanation.push('⚠️ Deadline has arrived or is expiring immediately');
      } else if (diffHours <= 24) {
        deadlineScore = 40;
        explanation.push('🔴 Critical deadline: Due within 24 hours');
      } else if (diffHours <= 72) {
        deadlineScore = 30;
        const days = Math.ceil(diffHours / 24);
        explanation.push(`⏰ High urgency: Deadline in ${days} days`);
      } else if (diffHours <= 168) {
        deadlineScore = 20;
        explanation.push('📅 Upcoming deadline within this week (4–7 days)');
      } else {
        deadlineScore = 10;
        explanation.push('🗓️ Extended deadline (> 7 days remaining)');
      }
    } else {
      deadlineScore = 0;
    }

    // 2. CATEGORY IMPORTANCE (Max 25 Points)
    const categoryLower = params.category.trim().toLowerCase();
    let categoryScore = 5;
    let categoryExplanation = 'General campus notice';

    if (categoryLower.includes('placement')) {
      categoryScore = 25;
      categoryExplanation = '💼 Placement & career opportunity (Highest institutional priority)';
    } else if (categoryLower.includes('exam')) {
      categoryScore = 25;
      categoryExplanation = '📝 University Examination official schedule';
    } else if (categoryLower.includes('assign')) {
      categoryScore = 22;
      categoryExplanation = '📚 Academic coursework / Continuous assessment';
    } else if (categoryLower.includes('intern')) {
      categoryScore = 22;
      categoryExplanation = '🏢 Industry Internship opportunity';
    } else if (categoryLower.includes('regist')) {
      categoryScore = 20;
      categoryExplanation = '✍️ Mandatory portal registration process';
    } else if (categoryLower.includes('scholar')) {
      categoryScore = 20;
      categoryExplanation = '🎓 Financial aid / Scholarship grant program';
    } else if (categoryLower.includes('contest') || categoryLower.includes('coding')) {
      categoryScore = 15;
      categoryExplanation = '💻 Technical coding contest & challenge';
    } else if (categoryLower.includes('compet')) {
      categoryScore = 15;
      categoryExplanation = '🏆 Inter-college academic competition';
    } else if (categoryLower.includes('work') || categoryLower.includes('hack')) {
      categoryScore = 10;
      categoryExplanation = '🛠️ Skill development workshop';
    } else if (categoryLower.includes('event') || categoryLower.includes('seminar')) {
      categoryScore = 8;
      categoryExplanation = '🎪 Campus seminar / technical event';
    } else if (categoryLower.includes('club') || categoryLower.includes('cultur')) {
      categoryScore = 5;
      categoryExplanation = '🎨 Student club activity';
    }

    explanation.push(categoryExplanation);

    // 3. EXPLICIT URGENCY (Max 20 Points)
    const urgencyKeywords = [
      'urgent',
      'important',
      'last date',
      'immediate action',
      'immediate',
      'mandatory',
      'final call',
      'critical',
      'expiring',
      'strict',
      'compulsory',
      'attention'
    ];

    const combinedText = `${params.title} ${params.originalContent}`.toLowerCase();
    const matchedKeywords: string[] = [];

    for (const kw of urgencyKeywords) {
      if (combinedText.includes(kw)) {
        matchedKeywords.push(kw.toUpperCase());
      }
    }

    // Include indicators identified by AI
    if (params.urgencyIndicators && params.urgencyIndicators.length > 0) {
      for (const ind of params.urgencyIndicators) {
        if (!matchedKeywords.includes(ind.toUpperCase())) {
          matchedKeywords.push(ind);
        }
      }
    }

    let urgencyScore = 0;
    if (matchedKeywords.length >= 2) {
      urgencyScore = 20;
      explanation.push(`⚡ High urgency wording detected: ${matchedKeywords.slice(0, 3).join(', ')}`);
    } else if (matchedKeywords.length === 1) {
      urgencyScore = 14;
      explanation.push(`⚠️ Explicit urgency notice: "${matchedKeywords[0]}"`);
    }

    // 4. REQUIRED STUDENT ACTION (Max 15 Points)
    const actionKeywords = [
      'submit',
      'register',
      'upload',
      'apply',
      'attendance',
      'attend',
      'fill',
      'fee',
      'pay',
      'verify',
      'report',
      'enroll'
    ];

    const actionsList = params.keyActions || [];
    let detectedActionsCount = 0;

    for (const act of actionsList) {
      const actLower = act.toLowerCase();
      if (actionKeywords.some((ak) => actLower.includes(ak))) {
        detectedActionsCount++;
      }
    }

    // Also check combined text if actions list was empty
    if (detectedActionsCount === 0) {
      for (const ak of actionKeywords) {
        if (combinedText.includes(ak)) {
          detectedActionsCount++;
        }
      }
    }

    let actionScore = 0;
    if (detectedActionsCount >= 2) {
      actionScore = 15;
      explanation.push('📋 Immediate student action required (form submission / application)');
    } else if (detectedActionsCount === 1) {
      actionScore = 10;
      explanation.push('📌 Student follow-up action required');
    }

    // TOTAL CALCULATION
    const totalScore = Math.min(100, Math.max(0, deadlineScore + categoryScore + urgencyScore + actionScore));

    // PRIORITY LEVEL MAPPING
    let level: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
    if (totalScore >= 80) {
      level = 'CRITICAL';
    } else if (totalScore >= 60) {
      level = 'HIGH';
    } else if (totalScore >= 30) {
      level = 'MEDIUM';
    } else {
      level = 'LOW';
    }

    const breakdown: IPriorityBreakdown = {
      deadlineScore,
      categoryScore,
      urgencyScore,
      actionScore,
      totalScore
    };

    return {
      score: totalScore,
      level,
      explanation,
      breakdown
    };
  }
}
