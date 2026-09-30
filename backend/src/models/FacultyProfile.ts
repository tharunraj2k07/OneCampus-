import mongoose, { Schema, Document, Model } from 'mongoose';
import { IFacultyProfile } from '../types/profile.types';

export interface IFacultyProfileDocument extends IFacultyProfile, Document {}

const facultyProfileSchema = new Schema<IFacultyProfileDocument>(
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
    designation: {
      type: String,
      required: [true, 'Designation is required'],
      trim: true
    }
  },
  {
    timestamps: true
  }
);

export const FacultyProfile: Model<IFacultyProfileDocument> = mongoose.model<IFacultyProfileDocument>(
  'FacultyProfile',
  facultyProfileSchema
);
