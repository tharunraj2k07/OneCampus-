import { IAnnouncementResponse } from '../../types/announcement.types';

export enum RelevanceLevel {
  VERY_HIGH = 'VERY_HIGH',
  HIGH = 'HIGH',
  MEDIUM = 'MEDIUM',
  LOW = 'LOW'
}

export interface IRelevanceInfo {
  score: number;
  level: RelevanceLevel;
  explanation: string[];
}

export interface IRelevanceScoreResult {
  relevanceScore: number;
  relevanceLevel: RelevanceLevel;
  relevanceExplanation: string[];
  isEligible: boolean;
  isMandatory: boolean;
  academicMatchScore: number;
  interestMatchScore: number;
  categoryAffinityScore: number;
  keywordMatchScore: number;
}

export interface IPersonalizedAnnouncementResponse extends IAnnouncementResponse {
  relevance: IRelevanceInfo;
}

export interface IStudentPreferencesInput {
  interests?: string[];
  focusAreas?: string[];
  preferredCategories?: string[];
}
