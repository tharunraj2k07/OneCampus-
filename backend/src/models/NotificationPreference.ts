import mongoose, { Schema, Document, Model } from 'mongoose';
import { INotificationPreferences } from '../types/notification.types';

export interface INotificationPreferenceDocument extends INotificationPreferences, Document {}

const notificationPreferenceSchema = new Schema<INotificationPreferenceDocument>(
  {
    userId: {
      type: Schema.Types.ObjectId,
      ref: 'User',
      required: [true, 'User ID is required'],
      unique: true,
      index: true
    },
    pushEnabled: {
      type: Boolean,
      default: true
    },
    criticalAlerts: {
      type: Boolean,
      default: true
    },
    placementAlerts: {
      type: Boolean,
      default: true
    },
    assignmentAlerts: {
      type: Boolean,
      default: true
    },
    eventAlerts: {
      type: Boolean,
      default: true
    },
    deadlineReminders: {
      type: Boolean,
      default: true
    }
  },
  {
    timestamps: true
  }
);

export const NotificationPreference: Model<INotificationPreferenceDocument> = mongoose.model<INotificationPreferenceDocument>(
  'NotificationPreference',
  notificationPreferenceSchema
);
