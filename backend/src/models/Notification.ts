import mongoose, { Schema, Document, Model } from 'mongoose';
import { INotification, NotificationType } from '../types/notification.types';
import { AnnouncementPriority } from '../types/announcement.types';

export interface INotificationDocument extends INotification, Document {}

const notificationSchema = new Schema<INotificationDocument>(
  {
    userId: {
      type: Schema.Types.ObjectId,
      ref: 'User',
      required: [true, 'Target user ID is required'],
      index: true
    },
    title: {
      type: String,
      required: [true, 'Notification title is required'],
      trim: true
    },
    body: {
      type: String,
      required: [true, 'Notification body is required']
    },
    type: {
      type: String,
      enum: Object.values(NotificationType),
      default: NotificationType.GENERAL
    },
    announcementId: {
      type: Schema.Types.ObjectId,
      ref: 'Announcement',
      index: true
    },
    taskId: {
      type: Schema.Types.ObjectId,
      ref: 'Task',
      index: true
    },
    category: {
      type: String,
      default: 'GENERAL'
    },
    priority: {
      type: String,
      enum: Object.values(AnnouncementPriority),
      default: AnnouncementPriority.MEDIUM
    },
    isRead: {
      type: Boolean,
      default: false,
      index: true
    }
  },
  {
    timestamps: { createdAt: true, updatedAt: false }
  }
);

// Compound index for user unread notifications
notificationSchema.index({ userId: 1, isRead: 1, createdAt: -1 });

export const Notification: Model<INotificationDocument> = mongoose.model<INotificationDocument>(
  'Notification',
  notificationSchema
);
