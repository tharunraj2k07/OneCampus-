import mongoose, { Schema, Document, Model, Types } from 'mongoose';
import { IStudentProfile } from '../types/profile.types';

export interface IStudentProfileDocument extends IStudentProfile, Document {}

const studentProfileSchema = new Schema<IStudentProfileDocument>(
  {
    userId: {
      type: Schema.Types.ObjectId,
      ref: 'User',
      required: [true, 'User ID is required'],
      unique: true,
      index: true
    },
    department: {
      type: String,
      required: [true, 'Department is required'],
      uppercase: true,
      trim: true,
      index: true
    },
    year: {
      type: Number,
      required: [true, 'Academic year is required'],
      min: [1, 'Year must be at least 1'],
      max: [5, 'Year cannot exceed 5'],
      index: true
    },
    section: {
      type: String,
      required: [true, 'Section is required'],
      uppercase: true,
      trim: true
    },
    registerNumber: {
      type: String,
      required: [true, 'Register number is required'],
      unique: true,
      uppercase: true,
      trim: true,
      index: true
    },
    interests: {
      type: [String],
      default: []
    },
    focusAreas: {
      type: [String],
      default: []
    },
    preferredCategories: {
      type: [String],
      default: []
    },
    profileCompletion: {
      type: Number,
      default: 0,
      min: 0,
      max: 100
    },
    cgpa: {
      type: Number,
      min: [0, 'CGPA cannot be negative'],
      max: [10, 'CGPA cannot exceed 10.0']
    }
  },
  {
    timestamps: true
  }
);

studentProfileSchema.index({ department: 1, year: 1 });

export const StudentProfile: Model<IStudentProfileDocument> = mongoose.model<IStudentProfileDocument>(
  'StudentProfile',
  studentProfileSchema
);
