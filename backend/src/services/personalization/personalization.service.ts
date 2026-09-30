import { Types } from 'mongoose';
import { Announcement, IAnnouncementDocument } from '../../models/Announcement';
import { StudentProfile, IStudentProfileDocument } from '../../models/StudentProfile';
import { AnnouncementStatus, IAnnouncementFilters, AnnouncementPriority } from '../../types/announcement.types';
import { relevanceService } from './relevance.service';
import { taskService } from './task.service';
import {
  IPersonalizedAnnouncementResponse,
  IStudentPreferencesInput,
  IRelevanceScoreResult
} from './personalization.types';
import { IStudentProfile } from '../../types/profile.types';

export class PersonalizationService {
  /**
   * Retrieves a personalized feed for the authenticated student.
   * Filters by eligibility while strictly preserving mandatory announcements.
   * Sorts using multi-tier hybrid ranking: Mandatory > Priority > Deadline > Relevance > Date.
   * Triggers asynchronous background task synchronization.
   */
  public async getPersonalizedFeed(
    studentUserId: string,
    filters: IAnnouncementFilters = {}
  ): Promise<{
    announcements: IPersonalizedAnnouncementResponse[];
    total: number;
    page: number;
    limit: number;
  }> {
    const studentUserObjectId = new Types.ObjectId(studentUserId);

    // 1. Load StudentProfile
    let studentProfile = await StudentProfile.findOne({ userId: studentUserObjectId }).lean();

    if (!studentProfile) {
      // Fallback default student profile for new or uncompleted profiles
      studentProfile = {
        userId: studentUserObjectId,
        department: 'CSE',
        year: 3,
        section: 'A',
        registerNumber: 'REG_DEFAULT',
        interests: ['AI', 'Coding', 'Hackathon'],
        profileCompletion: 50,
        createdAt: new Date(),
        updatedAt: new Date()
      } as any;
    }

    // 2. Fetch all published announcements matching category or search filters
    const query: any = {
      status: AnnouncementStatus.PUBLISHED
    };

    if (filters.category) {
      query.category = filters.category;
    }
    if (filters.priority) {
      query.priorityLevel = filters.priority;
    }
    if (filters.search) {
      query.$or = [
        { title: { $regex: filters.search, $options: 'i' } },
        { originalContent: { $regex: filters.search, $options: 'i' } },
        { keywords: { $regex: filters.search, $options: 'i' } }
      ];
    }

    const announcements = await Announcement.find(query)
      .populate('publisherId', 'name department designation role isVerified')
      .lean();

    // 3. Evaluate audience eligibility and calculate relevance for each announcement
    interface ScoredAnnouncement {
      doc: any;
      relevance: IRelevanceScoreResult;
    }

    const scoredAnnouncements: ScoredAnnouncement[] = [];
    const eligibleForTasks: any[] = [];

    for (const ann of announcements) {
      const relevance = relevanceService.calculateRelevance(studentProfile as unknown as IStudentProfile, ann as any);

      // Strictly retain announcement if eligible OR mandatory (NEVER hide mandatory notices)
      if (relevance.isEligible || relevance.isMandatory) {
        scoredAnnouncements.push({
          doc: ann,
          relevance
        });
        eligibleForTasks.push(ann);
      }
    }

    // 4. Asynchronously synchronize actionable tasks for eligible announcements (Non-blocking)
    setImmediate(() => {
      taskService.syncTasksForStudent(studentUserObjectId, eligibleForTasks).catch(err => {
        console.error('Async task synchronization error:', err);
      });
    });

    // 5. Multi-tier Personalized Feed Sorting
    // Tier 1: Mandatory status (mandatory notices at the very top)
    // Tier 2: Priority level (CRITICAL > HIGH > MEDIUM > LOW)
    // Tier 3: Deadline urgency (deadlines within 48 hours prioritized)
    // Tier 4: Relevance score (0 - 100 descending)
    // Tier 5: Published date (newest first)
    const priorityWeight: Record<string, number> = {
      CRITICAL: 4,
      HIGH: 3,
      MEDIUM: 2,
      LOW: 1
    };

    const now = Date.now();

    scoredAnnouncements.sort((a, b) => {
      // 1. Mandatory status
      if (a.relevance.isMandatory && !b.relevance.isMandatory) return -1;
      if (!a.relevance.isMandatory && b.relevance.isMandatory) return 1;

      // 2. Priority level
      const aPriorityVal = priorityWeight[a.doc.priorityLevel || 'MEDIUM'] || 2;
      const bPriorityVal = priorityWeight[b.doc.priorityLevel || 'MEDIUM'] || 2;
      if (aPriorityVal !== bPriorityVal) {
        return bPriorityVal - aPriorityVal;
      }

      // 3. Deadline urgency (within 48 hours)
      const aDeadline = a.doc.deadline ? new Date(a.doc.deadline).getTime() : Infinity;
      const bDeadline = b.doc.deadline ? new Date(b.doc.deadline).getTime() : Infinity;

      const aIsNear = aDeadline > now && aDeadline - now <= 48 * 60 * 60 * 1000;
      const bIsNear = bDeadline > now && bDeadline - now <= 48 * 60 * 60 * 1000;

      if (aIsNear && !bIsNear) return -1;
      if (!aIsNear && bIsNear) return 1;

      // 4. Relevance score (descending)
      if (a.relevance.relevanceScore !== b.relevance.relevanceScore) {
        return b.relevance.relevanceScore - a.relevance.relevanceScore;
      }

      // 5. Published date (newest first)
      const aDate = a.doc.publishedAt ? new Date(a.doc.publishedAt).getTime() : new Date(a.doc.createdAt).getTime();
      const bDate = b.doc.publishedAt ? new Date(b.doc.publishedAt).getTime() : new Date(b.doc.createdAt).getTime();
      return bDate - aDate;
    });

    // 6. Pagination
    const total = scoredAnnouncements.length;
    const page = Math.max(1, filters.page || 1);
    const limit = Math.min(100, Math.max(1, filters.limit || 20));
    const startIndex = (page - 1) * limit;
    const paginated = scoredAnnouncements.slice(startIndex, startIndex + limit);

    // 7. Map to response DTO
    const result: IPersonalizedAnnouncementResponse[] = paginated.map(item => {
      const ann = item.doc;
      const publisherInfo = ann.publisherId && typeof ann.publisherId === 'object'
        ? {
            id: ann.publisherId._id.toString(),
            name: ann.publisherId.name,
            department: ann.publisherId.department,
            designation: ann.publisherId.designation,
            role: ann.publisherId.role,
            verified: ann.publisherId.isVerified !== false
          }
        : {
            id: ann.publisherId?.toString() || '',
            name: ann.publisherName || 'Official Faculty',
            department: ann.publisherDepartment,
            designation: ann.publisherDesignation,
            role: 'FACULTY',
            verified: ann.isVerifiedPublisher !== false
          };

      return {
        id: ann._id.toString(),
        title: ann.title,
        originalContent: ann.originalContent,
        summary: ann.summary,
        category: ann.category,
        targetAudience: ann.targetAudience || { departments: [], years: [], sections: [] },
        deadline: ann.deadline,
        eligibility: ann.eligibility,
        externalLink: ann.externalLink,
        status: ann.status,
        publisher: publisherInfo,
        publishedAt: ann.publishedAt,
        archivedAt: ann.archivedAt,
        keyActions: ann.keyActions || [],
        extractedActions: ann.extractedActions || [],
        extractedDeadline: ann.extractedDeadline,
        extractedEligibility: ann.extractedEligibility,
        keywords: ann.keywords || [],
        eventType: ann.eventType,
        urgencyIndicators: ann.urgencyIndicators || [],
        requiredDocuments: ann.requiredDocuments || [],
        locationOrPlatform: ann.locationOrPlatform,
        aiAnalysisStatus: ann.aiAnalysisStatus || 'PENDING',
        aiConfidence: ann.aiConfidence,
        deadlineConflict: ann.deadlineConflict,
        priorityScore: ann.priorityScore,
        priorityLevel: ann.priorityLevel,
        priorityExplanation: ann.priorityExplanation || [],
        priority: {
          score: ann.priorityScore || 50,
          level: ann.priorityLevel || AnnouncementPriority.MEDIUM,
          explanation: ann.priorityExplanation || []
        },
        ai: {
          status: ann.aiAnalysisStatus || 'PENDING',
          confidence: ann.aiConfidence,
          eventType: ann.eventType,
          keywords: ann.keywords || [],
          requiredDocuments: ann.requiredDocuments || [],
          locationOrPlatform: ann.locationOrPlatform
        },
        createdAt: ann.createdAt,
        updatedAt: ann.updatedAt,
        relevance: {
          score: item.relevance.relevanceScore,
          level: item.relevance.relevanceLevel,
          explanation: item.relevance.relevanceExplanation
        }
      };
    });

    return {
      announcements: result,
      total,
      page,
      limit
    };
  }

  /**
   * Updates student interests and personalization preferences.
   * Changes dynamically affect subsequent feed ranking without re-running Gemini AI.
   */
  public async updateStudentPreferences(
    studentUserId: string,
    input: IStudentPreferencesInput
  ): Promise<IStudentProfileDocument | null> {
    const update: any = {};
    if (input.interests) update.interests = input.interests;
    if (input.focusAreas) update.focusAreas = input.focusAreas;
    if (input.preferredCategories) update.preferredCategories = input.preferredCategories;

    const updated = await StudentProfile.findOneAndUpdate(
      { userId: new Types.ObjectId(studentUserId) },
      { $set: update },
      { new: true }
    );

    return updated;
  }
}

export const personalizationService = new PersonalizationService();
