import { Types } from 'mongoose';

export enum AnnouncementStatus {
  DRAFT = 'DRAFT',
  PUBLISHED = 'PUBLISHED',
  ARCHIVED = 'ARCHIVED'
}

export enum AnnouncementPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL'
}

export interface ITargetAudience {
  departments: string[];
  years: number[];
  sections?: string[];
}

export interface IPublisherInfo {
  id: string;
  name: string;
  department?: string;
  designation?: string;
  verified: boolean;
}

export interface IPriorityInfo {
  score: number;
  level: AnnouncementPriority | string;
  explanation: string[];
}

export interface IAIInfo {
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  confidence?: number;
  eventType?: string;
  keywords?: string[];
  requiredDocuments?: string[];
  locationOrPlatform?: string;
}

export interface IAIExtractedFields {
  aiAnalysisStatus?: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  keyActions?: string[];
  extractedActions?: string[];
  extractedDeadline?: Date;
  extractedEligibility?: string;
  keywords?: string[];
  eventType?: string;
  urgencyIndicators?: string[];
  requiredDocuments?: string[];
  locationOrPlatform?: string;
  aiConfidence?: number;
  deadlineConflict?: boolean;
  priorityScore?: number;
  priorityLevel?: AnnouncementPriority | string;
  priorityExplanation?: string[];
  aiPriorityScore?: number;
  aiExplanation?: string;
}

export interface IAnnouncement {
  title: string;
  originalContent: string;
  summary?: string;
  category: string;
  targetAudience: ITargetAudience;
  deadline?: Date;
  eligibility?: string;
  externalLink?: string;
  publisherId: Types.ObjectId;
  publisherName: string;
  publisherDepartment?: string;
  publisherDesignation?: string;
  isVerifiedPublisher: boolean;
  status: AnnouncementStatus;
  publishedAt?: Date;
  archivedAt?: Date;

  // AI Structured Fields & Priority
  aiAnalysisStatus?: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  keyActions?: string[];
  extractedActions?: string[];
  extractedDeadline?: Date;
  extractedEligibility?: string;
  keywords?: string[];
  eventType?: string;
  urgencyIndicators?: string[];
  requiredDocuments?: string[];
  locationOrPlatform?: string;
  aiConfidence?: number;
  deadlineConflict?: boolean;
  priorityScore?: number;
  priorityLevel?: AnnouncementPriority | string;
  priorityExplanation?: string[];
  aiPriorityScore?: number;
  aiExplanation?: string;

  createdAt: Date;
  updatedAt: Date;
}

export interface ICreateAnnouncementDTO {
  title: string;
  originalContent: string;
  category: string;
  targetAudience?: {
    departments?: string[];
    years?: number[];
    sections?: string[];
  };
  deadline?: string | Date;
  eligibility?: string;
  externalLink?: string;
  status?: AnnouncementStatus;
}

export interface IUpdateAnnouncementDTO {
  title?: string;
  originalContent?: string;
  category?: string;
  targetAudience?: {
    departments?: string[];
    years?: number[];
    sections?: string[];
  };
  deadline?: string | Date;
  eligibility?: string;
  externalLink?: string;
  status?: AnnouncementStatus;
}

export interface IAnnouncementResponse {
  id: string;
  title: string;
  originalContent: string;
  summary?: string;
  category: string;
  targetAudience: ITargetAudience;
  deadline?: Date;
  eligibility?: string;
  externalLink?: string;
  status: AnnouncementStatus;
  publisher: IPublisherInfo;
  publishedAt?: Date;
  archivedAt?: Date;
  keyActions?: string[];
  extractedActions?: string[];
  extractedDeadline?: Date;
  extractedEligibility?: string;
  keywords?: string[];
  eventType?: string;
  urgencyIndicators?: string[];
  requiredDocuments?: string[];
  locationOrPlatform?: string;
  aiAnalysisStatus?: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  aiConfidence?: number;
  deadlineConflict?: boolean;
  priorityScore?: number;
  priorityLevel?: AnnouncementPriority | string;
  priorityExplanation?: string[];
  priority?: IPriorityInfo;
  ai?: IAIInfo;
  aiPriorityScore?: number;
  aiExplanation?: string;
  createdAt: Date;
  updatedAt: Date;
}

export interface IAnnouncementFilters {
  category?: string;
  department?: string;
  year?: number;
  priority?: string;
  search?: string;
  status?: string;
  page?: number;
  limit?: number;
}


