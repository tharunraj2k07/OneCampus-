import { IApiErrorDetail } from '../types/api.types';

export class ApiError extends Error {
  public readonly statusCode: number;
  public readonly isOperational: boolean;
  public readonly errorCode?: string;
  public readonly details?: IApiErrorDetail[] | string | Record<string, unknown>;

  constructor(
    message: string,
    statusCode: number = 500,
    errorCode?: string,
    details?: IApiErrorDetail[] | string | Record<string, unknown>,
    isOperational: boolean = true
  ) {
    super(message);
    this.statusCode = statusCode;
    this.errorCode = errorCode;
    this.details = details;
    this.isOperational = isOperational;

    Error.captureStackTrace(this, this.constructor);
  }

  static badRequest(message: string, details?: IApiErrorDetail[] | string | Record<string, unknown>, code: string = 'BAD_REQUEST'): ApiError {
    return new ApiError(message, 400, code, details);
  }

  static unauthorized(message: string = 'Unauthorized access', code: string = 'UNAUTHORIZED'): ApiError {
    return new ApiError(message, 401, code);
  }

  static forbidden(message: string = 'Access forbidden', code: string = 'FORBIDDEN'): ApiError {
    return new ApiError(message, 403, code);
  }

  static notFound(message: string = 'Resource not found', code: string = 'NOT_FOUND'): ApiError {
    return new ApiError(message, 404, code);
  }

  static conflict(message: string, code: string = 'CONFLICT'): ApiError {
    return new ApiError(message, 409, code);
  }

  static internal(message: string = 'Internal server error', code: string = 'INTERNAL_ERROR'): ApiError {
    return new ApiError(message, 500, code, undefined, false);
  }
}
