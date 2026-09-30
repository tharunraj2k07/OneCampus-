export enum UserRole {
  STUDENT = 'STUDENT',
  FACULTY = 'FACULTY',
  ADMIN = 'ADMIN'
}

export interface IUser {
  name: string;
  email: string;
  passwordHash: string;
  role: UserRole;
  profileImage?: string;
  isActive: boolean;
  createdAt: Date;
  updatedAt: Date;
}
