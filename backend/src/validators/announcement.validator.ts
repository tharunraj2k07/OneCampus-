import { Request, Response, NextFunction } from 'express';
import { ApiError } from '../utils/apiError';
import { IApiErrorDetail } from '../types/api.types';
import { AnnouncementStatus } from '../types/announcement.types';

const VALID_CATEGORIES = [
  'Placement',
  'Assignment',
  'Internship',
  'Examination',
  'Workshop',
  'Event',
  'Coding Contest',
  'Club Activity',
  'Registration',
  'General Announcement',
  'Scholarship',
  'Competition',
  'Academic Notice'
];

const VALID_DEPARTMENTS = ['ALL', 'CSE', 'IT', 'ECE', 'EEE', 'MECH', 'CIVIL', 'AI&DS', 'CSBS'];

const isValidUrl = (urlString: string): boolean => {
  try {
    const url = new URL(urlString);
    return url.protocol === 'http:' || url.protocol === 'https:';
  } catch {
    return false;
  }
};

export const validateCreateAnnouncement = (req: Request, _res: Response, next: NextFunction): void => {
  const { title, originalContent, category, targetAudience, deadline, externalLink, status } = req.body;
  const errors: IApiErrorDetail[] = [];

  // Title validation
  if (!title || typeof title !== 'string' || title.trim().length === 0) {
    errors.push({ field: 'title', message: 'Announcement title is required' });
  } else if (title.trim().length < 3) {
    errors.push({ field: 'title', message: 'Announcement title must be at least 3 characters long' });
  } else if (title.trim().length > 200) {
    errors.push({ field: 'title', message: 'Announcement title cannot exceed 200 characters' });
  }

  // Original content validation
  if (!originalContent || typeof originalContent !== 'string' || originalContent.trim().length === 0) {
    errors.push({ field: 'originalContent', message: 'Announcement content is required' });
  } else if (originalContent.trim().length < 5) {
    errors.push({ field: 'originalContent', message: 'Announcement content must be at least 5 characters long' });
  }

  // Category validation
  if (!category || typeof category !== 'string' || category.trim().length === 0) {
    errors.push({ field: 'category', message: 'Category is required' });
  } else if (!VALID_CATEGORIES.includes(category.trim())) {
    errors.push({
      field: 'category',
      message: `Invalid category. Must be one of: ${VALID_CATEGORIES.join(', ')}`
    });
  }

  // Status validation on creation (only DRAFT or PUBLISHED allowed)
  if (status !== undefined) {
    if (status !== AnnouncementStatus.DRAFT && status !== AnnouncementStatus.PUBLISHED) {
      errors.push({
        field: 'status',
        message: 'Status on creation must be either DRAFT or PUBLISHED. ARCHIVED cannot be selected directly.'
      });
    }
  }

  // Target audience validation (optional)
  if (targetAudience !== undefined) {
    if (typeof targetAudience !== 'object' || targetAudience === null) {
      errors.push({ field: 'targetAudience', message: 'targetAudience must be an object' });
    } else {
      if (targetAudience.departments !== undefined) {
        if (!Array.isArray(targetAudience.departments)) {
          errors.push({ field: 'targetAudience.departments', message: 'departments must be an array of strings' });
        } else {
          for (const dept of targetAudience.departments) {
            if (typeof dept !== 'string' || (!VALID_DEPARTMENTS.includes(dept.toUpperCase()) && dept.trim().length === 0)) {
              errors.push({ field: 'targetAudience.departments', message: `Invalid department: ${dept}` });
              break;
            }
          }
        }
      }

      if (targetAudience.years !== undefined) {
        if (!Array.isArray(targetAudience.years)) {
          errors.push({ field: 'targetAudience.years', message: 'years must be an array of numbers' });
        } else {
          for (const yr of targetAudience.years) {
            if (typeof yr !== 'number' || yr < 1 || yr > 5) {
              errors.push({ field: 'targetAudience.years', message: `Invalid academic year: ${yr}. Must be 1-5.` });
              break;
            }
          }
        }
      }

      if (targetAudience.sections !== undefined && !Array.isArray(targetAudience.sections)) {
        errors.push({ field: 'targetAudience.sections', message: 'sections must be an array of strings' });
      }
    }
  }

  // Deadline validation (optional)
  if (deadline !== undefined && deadline !== null && deadline !== '') {
    const parsedDate = new Date(deadline);
    if (isNaN(parsedDate.getTime())) {
      errors.push({ field: 'deadline', message: 'Deadline must be a valid date or ISO-8601 string' });
    }
  }

  // External link validation (optional)
  if (externalLink !== undefined && externalLink !== null && externalLink.trim() !== '') {
    if (typeof externalLink !== 'string' || !isValidUrl(externalLink.trim())) {
      errors.push({ field: 'externalLink', message: 'External link must be a valid HTTP or HTTPS URL' });
    }
  }

  if (errors.length > 0) {
    throw ApiError.badRequest('Validation failed on announcement creation payload', errors);
  }

  next();
};

export const validateUpdateAnnouncement = (req: Request, _res: Response, next: NextFunction): void => {
  const { title, originalContent, category, targetAudience, deadline, externalLink, status } = req.body;
  const errors: IApiErrorDetail[] = [];

  // Title validation if provided
  if (title !== undefined) {
    if (typeof title !== 'string' || title.trim().length === 0) {
      errors.push({ field: 'title', message: 'Title must be a non-empty string' });
    } else if (title.trim().length < 3) {
      errors.push({ field: 'title', message: 'Title must be at least 3 characters long' });
    } else if (title.trim().length > 200) {
      errors.push({ field: 'title', message: 'Title cannot exceed 200 characters' });
    }
  }

  // Content validation if provided
  if (originalContent !== undefined) {
    if (typeof originalContent !== 'string' || originalContent.trim().length === 0) {
      errors.push({ field: 'originalContent', message: 'Original content cannot be empty' });
    } else if (originalContent.trim().length < 5) {
      errors.push({ field: 'originalContent', message: 'Original content must be at least 5 characters long' });
    }
  }

  // Category validation if provided
  if (category !== undefined) {
    if (typeof category !== 'string' || !VALID_CATEGORIES.includes(category.trim())) {
      errors.push({
        field: 'category',
        message: `Invalid category. Must be one of: ${VALID_CATEGORIES.join(', ')}`
      });
    }
  }

  // Status validation if provided
  if (status !== undefined) {
    if (!Object.values(AnnouncementStatus).includes(status)) {
      errors.push({
        field: 'status',
        message: `Invalid status. Must be one of: ${Object.values(AnnouncementStatus).join(', ')}`
      });
    }
  }

  // Target audience validation if provided
  if (targetAudience !== undefined) {
    if (typeof targetAudience !== 'object' || targetAudience === null) {
      errors.push({ field: 'targetAudience', message: 'targetAudience must be an object' });
    } else {
      if (targetAudience.departments !== undefined && !Array.isArray(targetAudience.departments)) {
        errors.push({ field: 'targetAudience.departments', message: 'departments must be an array of strings' });
      }
      if (targetAudience.years !== undefined && !Array.isArray(targetAudience.years)) {
        errors.push({ field: 'targetAudience.years', message: 'years must be an array of numbers' });
      }
      if (targetAudience.sections !== undefined && !Array.isArray(targetAudience.sections)) {
        errors.push({ field: 'targetAudience.sections', message: 'sections must be an array of strings' });
      }
    }
  }

  // Deadline validation if provided
  if (deadline !== undefined && deadline !== null && deadline !== '') {
    const parsedDate = new Date(deadline);
    if (isNaN(parsedDate.getTime())) {
      errors.push({ field: 'deadline', message: 'Deadline must be a valid date or ISO-8601 string' });
    }
  }

  // External link validation if provided
  if (externalLink !== undefined && externalLink !== null && externalLink.trim() !== '') {
    if (typeof externalLink !== 'string' || !isValidUrl(externalLink.trim())) {
      errors.push({ field: 'externalLink', message: 'External link must be a valid HTTP or HTTPS URL' });
    }
  }

  if (errors.length > 0) {
    throw ApiError.badRequest('Validation failed on announcement update payload', errors);
  }

  next();
};

