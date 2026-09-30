import { Announcement, IAnnouncementDocument } from '../../models/Announcement';
import { GeminiService } from './gemini.service';
import { PriorityService } from './priority.service';
import { IAnnouncementAIAnalysisResult, IGeminiStructuredAnnouncement } from './ai.types';

export class AnnouncementAIService {
  private geminiService: GeminiService;
  private priorityService: PriorityService;

  constructor(
    geminiService: GeminiService = new GeminiService(),
    priorityService: PriorityService = new PriorityService()
  ) {
    this.geminiService = geminiService;
    this.priorityService = priorityService;
  }

  /**
   * Asynchronously triggers announcement intelligence without blocking callers.
   */
  public triggerAsyncAnalysis(announcementId: string): void {
    setImmediate(async () => {
      try {
        await this.analyzeAnnouncement(announcementId);
      } catch (err: any) {
        console.error(`[AnnouncementAIService] Unhandled error during async analysis for ${announcementId}:`, err.message);
      }
    });
  }

  /**
   * Performs the full hybrid analysis pipeline:
   * 1. Gemini structured information extraction
   * 2. Strict JSON validation
   * 3. Authoritative deadline reconciliation
   * 4. Deterministic rule-based priority calculation
   * 5. Persistence to MongoDB
   */
  public async analyzeAnnouncement(announcementId: string): Promise<IAnnouncementAIAnalysisResult> {
    const doc = await Announcement.findById(announcementId);
    if (!doc) {
      return {
        status: 'FAILED',
        error: `Announcement ${announcementId} not found`
      };
    }

    // Set status to PROCESSING
    await Announcement.findByIdAndUpdate(announcementId, {
      aiAnalysisStatus: 'PROCESSING'
    });

    let structuredData: IGeminiStructuredAnnouncement | undefined;
    let geminiSuccess = false;

    // Step 1: Attempt Gemini extraction
    try {
      if (this.geminiService.isAvailable()) {
        structuredData = await this.geminiService.analyzeAnnouncementContent({
          title: doc.title,
          originalContent: doc.originalContent,
          category: doc.category,
          targetDepartments: doc.targetAudience?.departments || ['ALL'],
          targetYears: doc.targetAudience?.years || [1, 2, 3, 4],
          deadline: doc.deadline,
          eligibility: doc.eligibility
        });
        geminiSuccess = true;
      } else {
        console.warn('[AnnouncementAIService] Gemini API key not present. Falling back to deterministic pipeline.');
      }
    } catch (geminiError: any) {
      console.warn(`[AnnouncementAIService] Gemini analysis failed for ${announcementId}: ${geminiError.message}. Proceeding to fallback priority calculation.`);
    }

    // Step 2: Deadline reconciliation
    // Rule 1: Faculty-entered deadline is authoritative.
    // Rule 2: AI-extracted deadline is supplementary.
    // Rule 3: If both exist and differ by > 1 hour, flag deadlineConflict.
    let extractedDeadlineDate: Date | undefined;
    let hasConflict = false;

    if (structuredData?.deadline) {
      const parsedAiMs = Date.parse(structuredData.deadline);
      if (!isNaN(parsedAiMs)) {
        extractedDeadlineDate = new Date(parsedAiMs);
      }
    }

    if (doc.deadline && extractedDeadlineDate) {
      const diffMs = Math.abs(doc.deadline.getTime() - extractedDeadlineDate.getTime());
      if (diffMs > 3600 * 1000) {
        hasConflict = true;
      }
    }

    // Effective deadline for priority calculation
    const effectiveDeadline = doc.deadline || extractedDeadlineDate || null;

    // Step 3: Hybrid Deterministic Priority Engine
    const keyActions = structuredData?.keyActions || (doc.extractedActions && doc.extractedActions.length > 0 ? doc.extractedActions : []);
    const urgencyIndicators = structuredData?.urgencyIndicators || [];

    const priorityResult = this.priorityService.calculatePriority({
      category: doc.category,
      deadline: effectiveDeadline,
      title: doc.title,
      originalContent: doc.originalContent,
      keyActions,
      urgencyIndicators
    });

    // Step 4: Prepare updates for MongoDB
    const updatePayload: Partial<IAnnouncementDocument> = {
      priorityScore: priorityResult.score,
      priorityLevel: priorityResult.level,
      priorityExplanation: priorityResult.explanation,
      aiPriorityScore: priorityResult.score / 100, // Normalized 0-1 backward-compatibility
      aiExplanation: priorityResult.explanation.join(' • ')
    };

    if (geminiSuccess && structuredData) {
      updatePayload.aiAnalysisStatus = 'COMPLETED';
      updatePayload.summary = structuredData.summary || doc.summary;
      updatePayload.keyActions = structuredData.keyActions;
      updatePayload.extractedActions = structuredData.keyActions;
      updatePayload.extractedDeadline = extractedDeadlineDate;
      updatePayload.extractedEligibility = typeof structuredData.eligibility === 'string'
        ? structuredData.eligibility
        : structuredData.eligibility
          ? JSON.stringify(structuredData.eligibility)
          : doc.eligibility;
      updatePayload.keywords = structuredData.keywords;
      updatePayload.eventType = structuredData.eventType;
      updatePayload.urgencyIndicators = structuredData.urgencyIndicators;
      updatePayload.requiredDocuments = structuredData.requiredDocuments;
      updatePayload.locationOrPlatform = structuredData.locationOrPlatform || undefined;
      updatePayload.aiConfidence = structuredData.confidence;
      updatePayload.deadlineConflict = hasConflict;
    } else {
      // Graceful fallback without blocking or crashing
      updatePayload.aiAnalysisStatus = 'FAILED';
      if (!doc.summary) {
        // Safe heuristic summary fallback
        updatePayload.summary = doc.originalContent.slice(0, 160) + (doc.originalContent.length > 160 ? '...' : '');
      }
    }

    await Announcement.findByIdAndUpdate(announcementId, updatePayload);

    // Non-blocking trigger of actionable task synchronization and smart push notifications
    setImmediate(async () => {
      try {
        const { taskService } = await import('../personalization/task.service');
        await taskService.syncTasksForAnnouncement(announcementId);
      } catch (err: any) {
        console.warn(`[AnnouncementAIService] Task synchronization error for ${announcementId}:`, err.message);
      }

      try {
        const { NotificationService } = await import('../notification/notification.service');
        const notifService = new NotificationService();
        await notifService.sendAnnouncementNotifications(announcementId);
      } catch (err: any) {
        console.warn(`[AnnouncementAIService] Notification dispatch error for ${announcementId}:`, err.message);
      }
    });

    return {
      status: geminiSuccess ? 'COMPLETED' : 'FAILED',
      structuredData,
      priority: priorityResult,
      deadlineConflict: hasConflict,
      effectiveDeadline: effectiveDeadline || undefined
    };
  }
}
