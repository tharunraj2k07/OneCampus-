import { Response } from 'express';
import { IApiResponse } from '../types/api.types';

export class ApiResponse {
  static success<T>(
    res: Response,
    message: string = 'Operation successful',
    data?: T,
    statusCode: number = 200
  ): Response {
    const payload: IApiResponse<T> = {
      success: true,
      message,
      ...(data !== undefined && { data })
    };
    return res.status(statusCode).json(payload);
  }

  static created<T>(
    res: Response,
    message: string = 'Resource created successfully',
    data?: T
  ): Response {
    return this.success(res, message, data, 201);
  }
}
