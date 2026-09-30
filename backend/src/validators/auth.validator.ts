import { Request, Response, NextFunction } from 'express';
import { ApiError } from '../utils/apiError';
import { IApiErrorDetail } from '../types/api.types';
import { UserRole } from '../types/user.types';

const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

export const validateRegister = (req: Request, _res: Response, next: NextFunction): void => {
  const { name, email, password, role = UserRole.STUDENT } = req.body;
  const errors: IApiErrorDetail[] = [];

  // Common User fields
  if (!name || typeof name !== 'string' || name.trim().length < 2) {
    errors.push({ field: 'name', message: 'Name is required and must be at least 2 characters long' });
  }

  if (!email || typeof email !== 'string' || !EMAIL_REGEX.test(email.trim())) {
    errors.push({ field: 'email', message: 'A valid college email address is required' });
  }

  if (!password || typeof password !== 'string' || password.length < 6) {
    errors.push({ field: 'password', message: 'Password is required and must be at least 6 characters long' });
  }

  if (role && !Object.values(UserRole).includes(role)) {
    errors.push({ field: 'role', message: `Invalid role specified. Must be one of: ${Object.values(UserRole).join(', ')}` });
  }

  // Student specific validation
  if (role === UserRole.STUDENT) {
    const { department, year, section, registerNumber } = req.body;

    if (!department || typeof department !== 'string' || department.trim().length === 0) {
      errors.push({ field: 'department', message: 'Department is required for student registration' });
    }

    if (year === undefined || typeof year !== 'number' || year < 1 || year > 5) {
      errors.push({ field: 'year', message: 'Academic year must be an integer between 1 and 5' });
    }

    if (!section || typeof section !== 'string' || section.trim().length === 0) {
      errors.push({ field: 'section', message: 'Section is required for student registration' });
    }

    if (!registerNumber || typeof registerNumber !== 'string' || registerNumber.trim().length === 0) {
      errors.push({ field: 'registerNumber', message: 'Register number is required for student registration' });
    }
  }

  // Faculty specific validation
  if (role === UserRole.FACULTY) {
    const { department, designation } = req.body;

    if (!department || typeof department !== 'string' || department.trim().length === 0) {
      errors.push({ field: 'department', message: 'Department is required for faculty registration' });
    }

    if (!designation || typeof designation !== 'string' || designation.trim().length === 0) {
      errors.push({ field: 'designation', message: 'Designation is required for faculty registration' });
    }
  }

  if (errors.length > 0) {
    throw ApiError.badRequest('Validation failed on registration payload', errors);
  }

  next();
};

export const validateLogin = (req: Request, _res: Response, next: NextFunction): void => {
  const { email, password } = req.body;
  const errors: IApiErrorDetail[] = [];

  if (!email || typeof email !== 'string' || !EMAIL_REGEX.test(email.trim())) {
    errors.push({ field: 'email', message: 'Please provide a valid email address' });
  }

  if (!password || typeof password !== 'string' || password.length === 0) {
    errors.push({ field: 'password', message: 'Password is required' });
  }

  if (errors.length > 0) {
    throw ApiError.badRequest('Validation failed on login credentials', errors);
  }

  next();
};
