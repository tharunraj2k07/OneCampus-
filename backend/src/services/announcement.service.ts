import { AnnouncementRepository, IAnnouncementFilterOptions } from '../repositories/announcement.repository';
import { FacultyProfileRepository } from '../repositories/facultyProfile.repository';
import { IAnnouncementDocument } from '../models/Announcement';
import { IPaginatedData } from '../types/api.types';
import { ApiError } from '../utils/apiError';
import { IAuthUser } from '../types/auth.types';
import { UserRole } from '../types/user.types';
import {
  AnnouncementStatus,
  AnnouncementPriority,
  ICreateAnnouncementDTO,
  IUpdateAnnouncementDTO,
  IAnnouncementResponse
} from '../types/announcement.types';
import { AnnouncementAIService } from './ai/announcementAI.service';
import mongoose from 'mongoose';

export const toAnnouncementResponse = (doc: IAnnouncementDocument): IAnnouncementResponse => {
  const priorityScore = doc.priorityScore ?? (doc.aiPriorityScore !== undefined ? Math.round(doc.aiPriorityScore * 100) : 50);
  const priorityLevel = doc.priorityLevel || (priorityScore >= 80 ? AnnouncementPriority.CRITICAL : priorityScore >= 60 ? AnnouncementPriority.HIGH : priorityScore >= 30 ? AnnouncementPriority.MEDIUM : AnnouncementPriority.LOW);
  const priorityExplanation = doc.priorityExplanation && doc.priorityExplanation.length > 0
    ? doc.priorityExplanation
    : (doc.aiExplanation ? [doc.aiExplanation] : []);

  const keyActions = doc.keyActions && doc.keyActions.length > 0
    ? doc.keyActions
    : (doc.extractedActions || []);

  const aiStatus = doc.aiAnalysisStatus || 'PENDING';

  return {
    id: doc._id ? doc._id.toString() : '',
    title: doc.title,
    originalContent: doc.originalContent,
    summary: doc.summary,
    category: doc.category,
    targetAudience: {
      departments: doc.targetAudience?.departments || ['ALL'],
      years: doc.targetAudience?.years || [1, 2, 3, 4],
      sections: doc.targetAudience?.sections || []
    },
    deadline: doc.deadline,
    eligibility: doc.eligibility,
    externalLink: doc.externalLink,
    status: doc.status,
    publisher: {
      id: doc.publisherId ? doc.publisherId.toString() : '',
      name: doc.publisherName,
      department: doc.publisherDepartment,
      designation: doc.publisherDesignation,
      verified: doc.isVerifiedPublisher !== false
    },
    publishedAt: doc.publishedAt,
    archivedAt: doc.archivedAt,

    // Phase 7 AI Structured Intelligence & Priority
    keyActions,
    extractedActions: keyActions,
    extractedDeadline: doc.extractedDeadline,
    extractedEligibility: doc.extractedEligibility,
    keywords: doc.keywords || [],
    eventType: doc.eventType,
    urgencyIndicators: doc.urgencyIndicators || [],
    requiredDocuments: doc.requiredDocuments || [],
    locationOrPlatform: doc.locationOrPlatform,
    aiAnalysisStatus: aiStatus,
    aiConfidence: doc.aiConfidence,
    deadlineConflict: doc.deadlineConflict || false,
    priorityScore,
    priorityLevel,
    priorityExplanation,
    priority: {
      score: priorityScore,
      level: priorityLevel,
      explanation: priorityExplanation
    },
    ai: {
      status: aiStatus,
      confidence: doc.aiConfidence,
      eventType: doc.eventType,
      keywords: doc.keywords || [],
      requiredDocuments: doc.requiredDocuments || [],
      locationOrPlatform: doc.locationOrPlatform
    },
    aiPriorityScore: doc.aiPriorityScore ?? (priorityScore / 100),
    aiExplanation: doc.aiExplanation || priorityExplanation.join(' • '),

    createdAt: doc.createdAt,
    updatedAt: doc.updatedAt
  };
};

export class AnnouncementService {
  private announcementRepository: AnnouncementRepository;
  private facultyProfileRepository: FacultyProfileRepository;
  private announcementAIService: AnnouncementAIService;

  constructor(
    announcementRepository: AnnouncementRepository = new AnnouncementRepository(),
    facultyProfileRepository: FacultyProfileRepository = new FacultyProfileRepository(),
    announcementAIService: AnnouncementAIService = new AnnouncementAIService()
  ) {
    this.announcementRepository = announcementRepository;
    this.facultyProfileRepository = facultyProfileRepository;
    this.announcementAIService = announcementAIService;
  }

  async getFeedAnnouncements(
    options: IAnnouncementFilterOptions,
    user: IAuthUser
  ): Promise<IPaginatedData<IAnnouncementResponse>> {
    // If student or default feed, only return PUBLISHED announcements
    const filterOptions: IAnnouncementFilterOptions = { ...options };
    if (user.role === UserRole.STUDENT) {
      filterOptions.status = AnnouncementStatus.PUBLISHED;
    }

    const paginatedDocs = await this.announcementRepository.findFeed(filterOptions);
    return {
      items: paginatedDocs.items.map(toAnnouncementResponse),
      pagination: paginatedDocs.pagination
    };
  }

  async getMyAnnouncements(
    user: IAuthUser,
    status?: AnnouncementStatus | 'ALL',
    page = 1,
    limit = 20
  ): Promise<IPaginatedData<IAnnouncementResponse>> {
    if (user.role === UserRole.STUDENT) {
      throw ApiError.forbidden('Students do not have access to publisher management endpoints');
    }

    const paginatedDocs = await this.announcementRepository.findByPublisher(user.userId, status, page, limit);
    return {
      items: paginatedDocs.items.map(toAnnouncementResponse),
      pagination: paginatedDocs.pagination
    };
  }

  async getAnnouncementDetails(id: string, user: IAuthUser): Promise<IAnnouncementResponse> {
    const announcement = await this.announcementRepository.findById(id);
    if (!announcement) {
      throw ApiError.notFound(`Announcement with ID '${id}' not found`);
    }

    const publisherIdStr = announcement.publisherId ? announcement.publisherId.toString() : '';

    // Access control rule for details:
    if (user.role === UserRole.STUDENT) {
      if (announcement.status !== AnnouncementStatus.PUBLISHED) {
        throw ApiError.notFound(`Announcement with ID '${id}' not found`);
      }
    } else if (user.role === UserRole.FACULTY) {
      if (announcement.status !== AnnouncementStatus.PUBLISHED && publisherIdStr !== user.userId) {
        throw ApiError.forbidden(
          'You are not authorized to view this unpublished announcement',
          'OWNERSHIP_REQUIRED'
        );
      }
    }

    return toAnnouncementResponse(announcement);
  }

  async previewAnnouncement(id: string, user: IAuthUser): Promise<IAnnouncementResponse> {
    const announcement = await this.announcementRepository.findById(id);
    if (!announcement) {
      throw ApiError.notFound(`Announcement with ID '${id}' not found`);
    }

    const publisherIdStr = announcement.publisherId ? announcement.publisherId.toString() : '';

    if (user.role !== UserRole.ADMIN && publisherIdStr !== user.userId) {
      throw ApiError.forbidden(
        'You are not authorized to preview this announcement. Only the owner or an admin can preview.',
        'OWNERSHIP_REQUIRED'
      );
    }

    return toAnnouncementResponse(announcement);
  }

  async createAnnouncement(data: ICreateAnnouncementDTO, user: IAuthUser): Promise<IAnnouncementResponse> {
    const initialStatus = data.status || AnnouncementStatus.DRAFT;

    // Retrieve faculty profile to populate official department & designation
    let facultyDepartment: string | undefined;
    let facultyDesignation: string | undefined;

    try {
      const facultyProfile = await this.facultyProfileRepository.findByUserId(user.userId);
      if (facultyProfile) {
        facultyDepartment = facultyProfile.department;
        facultyDesignation = facultyProfile.designation;
      }
    } catch {
      // Non-blocking fallback
    }

    const announcementToCreate: Partial<IAnnouncementDocument> = {
      title: data.title.trim(),
      originalContent: data.originalContent.trim(),
      category: data.category.trim(),
      targetAudience: {
        departments: data.targetAudience?.departments?.length ? data.targetAudience.departments : ['ALL'],
        years: data.targetAudience?.years?.length ? data.targetAudience.years : [1, 2, 3, 4],
        sections: data.targetAudience?.sections || []
      },
      deadline: data.deadline ? new Date(data.deadline) : undefined,
      eligibility: data.eligibility?.trim(),
      externalLink: data.externalLink?.trim(),
      publisherId: mongoose.Types.ObjectId.isValid(user.userId)
        ? new mongoose.Types.ObjectId(user.userId)
        : (user.userId as any),
      publisherName: user.name,
      publisherDepartment: facultyDepartment,
      publisherDesignation: facultyDesignation,
      isVerifiedPublisher: true,
      status: initialStatus,
      publishedAt: initialStatus === AnnouncementStatus.PUBLISHED ? new Date() : undefined
    };

    const created = await this.announcementRepository.create(announcementToCreate);
    if (initialStatus === AnnouncementStatus.PUBLISHED && created._id) {
      this.announcementAIService.triggerAsyncAnalysis(created._id.toString());
    }
    return toAnnouncementResponse(created);
  }

  async publishAnnouncement(id: string, user: IAuthUser): Promise<IAnnouncementResponse> {
    const existing = await this.announcementRepository.findById(id);
    if (!existing) {
      throw ApiError.notFound(`Announcement with ID '${id}' not found`);
    }

    const publisherIdStr = existing.publisherId ? existing.publisherId.toString() : '';

    if (user.role !== UserRole.ADMIN && publisherIdStr !== user.userId) {
      throw ApiError.forbidden(
        'You are not authorized to publish this announcement. Only the owner or an admin can publish.',
        'OWNERSHIP_REQUIRED'
      );
    }

    if (existing.status === AnnouncementStatus.PUBLISHED) {
      return toAnnouncementResponse(existing);
    }

    const updated = await this.announcementRepository.updateById(id, {
      status: AnnouncementStatus.PUBLISHED,
      publishedAt: new Date()
    });

    if (!updated) {
      throw ApiError.internal('Failed to publish announcement');
    }

    // Trigger non-blocking asynchronous AI analysis pipeline
    this.announcementAIService.triggerAsyncAnalysis(id);

    return toAnnouncementResponse(updated);
  }

  async updateAnnouncement(
    id: string,
    updateData: IUpdateAnnouncementDTO,
    user: IAuthUser
  ): Promise<IAnnouncementResponse> {
    const existing = await this.announcementRepository.findById(id);
    if (!existing) {
      throw ApiError.notFound(`Announcement with ID '${id}' not found`);
    }

    const publisherIdStr = existing.publisherId ? existing.publisherId.toString() : '';

    if (user.role !== UserRole.ADMIN && publisherIdStr !== user.userId) {
      throw ApiError.forbidden(
        'You are not authorized to update this announcement. Only the publisher faculty or an admin can edit it.',
        'OWNERSHIP_REQUIRED'
      );
    }

    const payloadToUpdate: Partial<IAnnouncementDocument> = {};

    if (updateData.title !== undefined) payloadToUpdate.title = updateData.title.trim();
    if (updateData.originalContent !== undefined) payloadToUpdate.originalContent = updateData.originalContent.trim();
    if (updateData.category !== undefined) payloadToUpdate.category = updateData.category.trim();
    if (updateData.eligibility !== undefined) payloadToUpdate.eligibility = updateData.eligibility.trim();
    if (updateData.externalLink !== undefined) payloadToUpdate.externalLink = updateData.externalLink.trim();

    if (updateData.targetAudience !== undefined) {
      payloadToUpdate.targetAudience = {
        departments: updateData.targetAudience.departments || existing.targetAudience?.departments || ['ALL'],
        years: updateData.targetAudience.years || existing.targetAudience?.years || [1, 2, 3, 4],
        sections: updateData.targetAudience.sections || existing.targetAudience?.sections || []
      };
    }

    if (updateData.deadline !== undefined) {
      payloadToUpdate.deadline = updateData.deadline ? new Date(updateData.deadline) : undefined;
    }

    if (updateData.status !== undefined) {
      payloadToUpdate.status = updateData.status;
      if (updateData.status === AnnouncementStatus.PUBLISHED && !existing.publishedAt) {
        payloadToUpdate.publishedAt = new Date();
      } else if (updateData.status === AnnouncementStatus.ARCHIVED && !existing.archivedAt) {
        payloadToUpdate.archivedAt = new Date();
      }
    }

    const updated = await this.announcementRepository.updateById(id, payloadToUpdate);
    if (!updated) {
      throw ApiError.internal('Failed to update announcement');
    }

    // If published or content updated, re-trigger intelligence
    if (
      payloadToUpdate.status === AnnouncementStatus.PUBLISHED ||
      (existing.status === AnnouncementStatus.PUBLISHED && (updateData.title !== undefined || updateData.originalContent !== undefined || updateData.deadline !== undefined))
    ) {
      this.announcementAIService.triggerAsyncAnalysis(id);
    }

    return toAnnouncementResponse(updated);
  }

  /**
   * Synchronously runs or re-runs AI analysis on an announcement (e.g. for testing or on-demand trigger)
   */
  async analyzeAnnouncementNow(id: string, user: IAuthUser): Promise<IAnnouncementResponse> {
    const existing = await this.announcementRepository.findById(id);
    if (!existing) {
      throw ApiError.notFound(`Announcement with ID '${id}' not found`);
    }

    const publisherIdStr = existing.publisherId ? existing.publisherId.toString() : '';
    if (user.role !== UserRole.ADMIN && publisherIdStr !== user.userId) {
      throw ApiError.forbidden('You are not authorized to trigger AI analysis on this announcement');
    }

    await this.announcementAIService.analyzeAnnouncement(id);
    const updated = await this.announcementRepository.findById(id);
    return toAnnouncementResponse(updated || existing);
  }

  async archiveAnnouncement(id: string, user: IAuthUser): Promise<IAnnouncementResponse> {
    const existing = await this.announcementRepository.findById(id);
    if (!existing) {
      throw ApiError.notFound(`Announcement with ID '${id}' not found`);
    }

    const publisherIdStr = existing.publisherId ? existing.publisherId.toString() : '';

    if (user.role !== UserRole.ADMIN && publisherIdStr !== user.userId) {
      throw ApiError.forbidden(
        'You are not authorized to archive this announcement. Only the publisher faculty or an admin can archive it.',
        'OWNERSHIP_REQUIRED'
      );
    }

    const updated = await this.announcementRepository.updateById(id, {
      status: AnnouncementStatus.ARCHIVED,
      archivedAt: new Date()
    });

    if (!updated) {
      throw ApiError.internal('Failed to archive announcement');
    }

    return toAnnouncementResponse(updated);
  }

  async deleteAnnouncement(id: string, user: IAuthUser): Promise<{ id: string; deleted: boolean }> {
    const existing = await this.announcementRepository.findById(id);
    if (!existing) {
      throw ApiError.notFound(`Announcement with ID '${id}' not found`);
    }

    const publisherIdStr = existing.publisherId ? existing.publisherId.toString() : '';

    if (user.role !== UserRole.ADMIN) {
      if (publisherIdStr !== user.userId) {
        throw ApiError.forbidden(
          'You are not authorized to delete this announcement. Only the publisher faculty or an admin can delete it.',
          'OWNERSHIP_REQUIRED'
        );
      }

      // Hard rule: Faculty can only delete DRAFT announcements. Published/Archived announcements cannot be deleted.
      if (existing.status !== AnnouncementStatus.DRAFT) {
        throw ApiError.badRequest(
          'Published or archived announcements cannot be deleted by faculty. Please use the archive feature instead.',
          [{ field: 'status', message: `Current status is ${existing.status}. Only DRAFT announcements can be deleted.` }]
        );
      }
    }

    await this.announcementRepository.deleteById(id);
    return { id, deleted: true };
  }
}

