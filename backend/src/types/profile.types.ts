import { Types } from 'mongoose';

export interface IStudentProfile {
  userId: Types.ObjectId;
  department: string;
  year: number;
  section: string;
  registerNumber: string;
  interests: string[];
  focusAreas?: string[];
  preferredCategories?: string[];
  profileCompletion: number;
  cgpa?: number;
  createdAt: Date;
  updatedAt: Date;
}

export interface IFacultyProfile {
  userId: Types.ObjectId;
  department: string;
  designation: string;
  createdAt: Date;
  updatedAt: Date;
}
