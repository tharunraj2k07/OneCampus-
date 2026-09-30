import mongoose, { Schema, Document, Model } from 'mongoose';
import { IAnnouncement, AnnouncementStatus } from '../types/announcement.types';

export interface IAnnouncementDocument extends IAnnouncement, Document {}

const targetAudienceSchema = new Schema(
  {
    departments: {
      type: [String],
      required: true,
      default: ['ALL']
    },
    years: {
      type: [Number],
      required: true,
      default: [1, 2, 3, 4]
    },
    sections: {
      type: [String],
      default: []
    }
  },
  { _id: false }
);

const announcementSchema = new Schema<IAnnouncementDocument>(
  {
    title: {
      type: String,
      required: [true, 'Announcement title is required'],
      trim: true,
      maxlength: [200, 'Title cannot exceed 200 characters']
    },
    originalContent: {
      type: String,
      required: [true, 'Original circular content is required']
    },
    summary: {
      type: String,
      trim: true
    },
    category: {
      type: String,
      required: [true, 'Category is required'],
      trim: true,
      index: true
    },
    targetAudience: {
      type: targetAudienceSchema,
      required: true,
      default: () => ({
        departments: ['ALL'],
        years: [1, 2, 3, 4],
        sections: []
      })
    },
    deadline: {
      type: Date,
      index: true
    },
    eligibility: {
      type: String,
      trim: true
    },
    externalLink: {
      type: String,
      trim: true
    },
    publisherId: {
      type: Schema.Types.ObjectId,
      ref: 'User',
      required: [true, 'Publisher user ID is required'],
      index: true
    },
    publisherName: {
      type: String,
      required: [true, 'Publisher name is required'],
      trim: true
    },
    publisherDepartment: {
      type: String,
      trim: true
    },
    publisherDesignation: {
      type: String,
      trim: true
    },
    isVerifiedPublisher: {
      type: Boolean,
      default: true
    },
    status: {
      type: String,
      enum: Object.values(AnnouncementStatus),
      default: AnnouncementStatus.DRAFT,
      index: true
    },
    publishedAt: {
      type: Date,
      index: true
    },
    archivedAt: {
      type: Date,
      index: true
    },

    // AI Extracted Fields (Phase 7 Gemini Integration & Hybrid Priority Engine)
    aiAnalysisStatus: {
      type: String,
      enum: ['PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'],
      default: 'PENDING',
      index: true
    },
    keyActions: {
      type: [String],
      default: []
    },
    extractedActions: {
      type: [String],
      default: []
    },
    extractedDeadline: {
      type: Date
    },
    extractedEligibility: {
      type: String
    },
    keywords: {
      type: [String],
      default: []
    },
    eventType: {
      type: String
    },
    urgencyIndicators: {
      type: [String],
      default: []
    },
    requiredDocuments: {
      type: [String],
      default: []
    },
    locationOrPlatform: {
      type: String
    },
    aiConfidence: {
      type: Number,
      min: 0,
      max: 1
    },
    deadlineConflict: {
      type: Boolean,
      default: false
    },
    priorityScore: {
      type: Number,
      min: 0,
      max: 100,
      index: true
    },
    priorityLevel: {
      type: String,
      enum: ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'],
      default: 'MEDIUM',
      index: true
    },
    priorityExplanation: {
      type: [String],
      default: []
    },
    aiPriorityScore: {
      type: Number,
      min: 0,
      max: 1
    },
    aiExplanation: {
      type: String
    }
  },
  {
    timestamps: true
  }
);

// Compound and search indexes
announcementSchema.index({ 'targetAudience.departments': 1, 'targetAudience.years': 1 });
announcementSchema.index({ createdAt: -1 });
announcementSchema.index({ title: 'text', originalContent: 'text', summary: 'text' });

export const Announcement: Model<IAnnouncementDocument> = mongoose.model<IAnnouncementDocument>(
  'Announcement',
  announcementSchema
);
