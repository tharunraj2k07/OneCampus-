import { Request, Response, NextFunction } from 'express';
import { ApiError } from '../utils/apiError';
import { IApiErrorResponse, IApiErrorDetail } from '../types/api.types';
import { env } from '../config/env';
import { logger } from '../utils/logger';

export const errorHandler = (
  err: Error | ApiError,
  req: Request,
  res: Response,
  _next: NextFunction
): Response => {
  let statusCode = 500;
  let message = 'Internal server error occurred';
  let errorCode = 'INTERNAL_ERROR';
  let details: IApiErrorDetail[] | string | Record<string, unknown> | undefined;

  // 1. Custom ApiError
  if (err instanceof ApiError) {
    statusCode = err.statusCode;
    message = err.message;
    errorCode = err.errorCode || 'API_ERROR';
    details = err.details;
  }
  // 2. Mongoose Validation Error
  else if (err.name === 'ValidationError') {
    statusCode = 400;
    message = 'Validation failed on submitted payload';
    errorCode = 'VALIDATION_ERROR';
    const validationErrors = err as unknown as { errors: Record<string, { message: string; path: string }> };
    details = Object.values(validationErrors.errors).map((e) => ({
      field: e.path,
      message: e.message
    }));
  }
  // 3. Mongoose Duplicate Key Error (E11000)
  else if ('code' in err && (err as { code: number }).code === 11000) {
    statusCode = 409;
    message = 'Duplicate field value entered';
    errorCode = 'DUPLICATE_KEY_ERROR';
    const keyValue = (err as unknown as { keyValue: Record<string, unknown> }).keyValue;
    details = keyValue;
  }
  // 4. Mongoose Cast Error (Invalid ObjectId)
  else if (err.name === 'CastError') {
    statusCode = 400;
    message = 'Invalid resource identifier format';
    errorCode = 'INVALID_ID';
    const castErr = err as unknown as { path: string; value: unknown };
    details = `Invalid ${castErr.path}: ${castErr.value}`;
  }
  // 5. Syntax Error (e.g. malformed JSON body)
  else if (err instanceof SyntaxError && 'body' in err) {
    statusCode = 400;
    message = 'Malformed JSON in request body';
    errorCode = 'MALFORMED_JSON';
  } else {
    // Unhandled error logging
    logger.error(`[Unhandled Error] ${err.message}`, {
      stack: err.stack,
      url: req.originalUrl,
      method: req.method
    });
  }

  const responsePayload: IApiErrorResponse = {
    success: false,
    message,
    error: {
      code: errorCode,
      ...(details !== undefined && { details }),
      ...(env.NODE_ENV === 'development' && { stack: err.stack })
    }
  };

  return res.status(statusCode).json(responsePayload);
};
