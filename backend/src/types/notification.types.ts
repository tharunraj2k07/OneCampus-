import { Types } from 'mongoose';
import { AnnouncementPriority } from './announcement.types';

export enum NotificationType {
  CRITICAL_ALERT = 'CRITICAL_ALERT',
  DEADLINE = 'DEADLINE',
  PLACEMENT = 'PLACEMENT',
  ASSIGNMENT = 'ASSIGNMENT',
  EVENT = 'EVENT',
  AI_SUMMARY = 'AI_SUMMARY',
  GENERAL = 'GENERAL'
}

export interface INotification {
  userId: Types.ObjectId;
  title: string;
  body: string;
  type: NotificationType;
  announcementId?: Types.ObjectId;
  taskId?: Types.ObjectId;
  category?: string;
  priority?: AnnouncementPriority;
  isRead: boolean;
  createdAt: Date;
}

export interface INotificationPreferences {
  userId: Types.ObjectId;
  pushEnabled: boolean;
  criticalAlerts: boolean;
  placementAlerts: boolean;
  assignmentAlerts: boolean;
  eventAlerts: boolean;
  deadlineReminders: boolean;
  updatedAt: Date;
}

export interface IUpdateNotificationPreferencesDTO {
  pushEnabled?: boolean;
  criticalAlerts?: boolean;
  placementAlerts?: boolean;
  assignmentAlerts?: boolean;
  eventAlerts?: boolean;
  deadlineReminders?: boolean;
}
