export type AIAnalysisStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';

export interface IEligibilityData {
  departments?: string[];
  years?: number[];
  requirements?: string[];
}

export interface IGeminiStructuredAnnouncement {
  summary: string;
  keyActions: string[];
  deadline: string | null;
  eligibility: IEligibilityData | string | null;
  keywords: string[];
  eventType: string;
  urgencyIndicators: string[];
  requiredDocuments: string[];
  locationOrPlatform: string | null;
  confidence: number;
}

export interface IPriorityBreakdown {
  deadlineScore: number;
  categoryScore: number;
  urgencyScore: number;
  actionScore: number;
  totalScore: number;
}

export interface IPriorityResult {
  score: number;
  level: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  explanation: string[];
  breakdown: IPriorityBreakdown;
}

export interface IAnnouncementAIAnalysisResult {
  status: AIAnalysisStatus;
  structuredData?: IGeminiStructuredAnnouncement;
  priority?: IPriorityResult;
  deadlineConflict?: boolean;
  effectiveDeadline?: Date;
  error?: string;
}
