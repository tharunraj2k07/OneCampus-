import mongoose, { Schema, Document, Model } from 'mongoose';
import { IDeviceToken, DevicePlatform } from '../types/device.types';

export interface IDeviceTokenDocument extends IDeviceToken, Document {}

const deviceTokenSchema = new Schema<IDeviceTokenDocument>(
  {
    userId: {
      type: Schema.Types.ObjectId,
      ref: 'User',
      required: [true, 'User ID is required'],
      index: true
    },
    token: {
      type: String,
      required: [true, 'FCM device token is required'],
      trim: true
    },
    platform: {
      type: String,
      enum: ['ANDROID', 'IOS', 'WEB'],
      default: 'ANDROID'
    },
    deviceName: {
      type: String,
      trim: true
    },
    lastUsedAt: {
      type: Date,
      default: Date.now
    }
  },
  {
    timestamps: { createdAt: true, updatedAt: false }
  }
);

// Prevent duplicate tokens for the same user
deviceTokenSchema.index({ userId: 1, token: 1 }, { unique: true });
// Fast lookup of token for unregistering/updating
deviceTokenSchema.index({ token: 1 });

export const DeviceToken: Model<IDeviceTokenDocument> = mongoose.model<IDeviceTokenDocument>(
  'DeviceToken',
  deviceTokenSchema
);
