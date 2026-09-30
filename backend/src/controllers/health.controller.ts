import { Request, Response } from 'express';
import { HealthService } from '../services/health.service';
import { ApiResponse } from '../utils/apiResponse';

export class HealthController {
  public static getHealth(_req: Request, res: Response): Response {
    const healthData = HealthService.getSystemHealth();
    return ApiResponse.success(res, 'OneCampus AI API is running', healthData);
  }
}
