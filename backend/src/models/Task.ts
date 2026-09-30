import mongoose, { Schema, Document, Model } from 'mongoose';
import { ITask, TaskStatus } from '../types/task.types';
import { AnnouncementPriority } from '../types/announcement.types';

export interface ITaskDocument extends ITask, Document {}

const taskSchema = new Schema<ITaskDocument>(
  {
    studentId: {
      type: Schema.Types.ObjectId,
      ref: 'User',
      required: [true, 'Student ID is required'],
      index: true
    },
    announcementId: {
      type: Schema.Types.ObjectId,
      ref: 'Announcement',
      index: true
    },
    actionIdentifier: {
      type: String,
      trim: true,
      index: true
    },
    title: {
      type: String,
      required: [true, 'Task title is required'],
      trim: true,
      maxlength: [200, 'Task title cannot exceed 200 characters']
    },
    description: {
      type: String,
      trim: true,
      maxlength: [1000, 'Description cannot exceed 1000 characters']
    },
    deadline: {
      type: Date,
      index: true
    },
    priority: {
      type: String,
      enum: Object.values(AnnouncementPriority),
      default: AnnouncementPriority.MEDIUM
    },
    status: {
      type: String,
      enum: Object.values(TaskStatus),
      default: TaskStatus.PENDING,
      index: true
    },
    completedAt: {
      type: Date
    }
  },
  {
    timestamps: true
  }
);

// Compound index for querying student tasks
taskSchema.index({ studentId: 1, status: 1, deadline: 1 });

// Deduplication compound index: studentId + announcementId + actionIdentifier
taskSchema.index(
  { studentId: 1, announcementId: 1, actionIdentifier: 1 },
  { unique: true, sparse: true }
);

export const Task: Model<ITaskDocument> = mongoose.model<ITaskDocument>('Task', taskSchema);
