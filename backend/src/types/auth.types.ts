import { UserRole } from './user.types';
import { IStudentProfile, IFacultyProfile } from './profile.types';

export interface IAuthUser {
  userId: string;
  name: string;
  email: string;
  role: UserRole;
}

export interface IJwtPayload {
  userId: string;
  email: string;
  role: UserRole;
  iat?: number;
  exp?: number;
}

export interface ISafeUser {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  profileImage?: string;
  isActive: boolean;
  createdAt: Date;
  updatedAt: Date;
}

export interface IStudentRegisterDTO {
  name: string;
  email: string;
  password: string;
  role?: UserRole.STUDENT;
  department: string;
  year: number;
  section: string;
  registerNumber: string;
  interests?: string[];
  cgpa?: number;
}

export interface IFacultyRegisterDTO {
  name: string;
  email: string;
  password: string;
  role?: UserRole.FACULTY;
  department: string;
  designation: string;
}

export type IRegisterDTO = IStudentRegisterDTO | IFacultyRegisterDTO;

export interface ILoginDTO {
  email: string;
  password: string;
}

export interface IAuthResponseData {
  token: string;
  user: ISafeUser;
  profile: IStudentProfile | IFacultyProfile | null;
}

export interface ICurrentUserResponseData {
  user: ISafeUser;
  profile: IStudentProfile | IFacultyProfile | null;
}
