import { Types } from 'mongoose';
import { AnnouncementPriority } from './announcement.types';

export enum TaskStatus {
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  DISMISSED = 'DISMISSED'
}

export interface ITask {
  studentId: Types.ObjectId;
  announcementId?: Types.ObjectId;
  actionIdentifier?: string;
  title: string;
  description?: string;
  deadline?: Date;
  priority: AnnouncementPriority;
  status: TaskStatus;
  createdAt: Date;
  completedAt?: Date;
}

export interface ITaskResponse {
  id: string;
  studentId: string;
  announcementId?: string;
  actionIdentifier?: string;
  title: string;
  description?: string;
  deadline?: string;
  deadlineEpochMs?: number;
  priority: AnnouncementPriority;
  status: TaskStatus;
  createdAt: string;
  completedAt?: string;
}

export interface ITaskFilters {
  status?: TaskStatus;
  announcementId?: string;
  page?: number;
  limit?: number;
}
