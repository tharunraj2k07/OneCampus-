import { Request, Response, NextFunction } from 'express';
import { IApiErrorResponse } from '../types/api.types';

export const notFoundHandler = (req: Request, res: Response, _next: NextFunction): Response => {
  const response: IApiErrorResponse = {
    success: false,
    message: `Cannot ${req.method} ${req.originalUrl} - Route not found`,
    error: {
      code: 'ROUTE_NOT_FOUND',
      details: `The requested path '${req.originalUrl}' does not exist on this server.`
    }
  };
  return res.status(404).json(response);
};
