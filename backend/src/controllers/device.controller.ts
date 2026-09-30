import { Request, Response, NextFunction } from 'express';
import { NotificationService } from '../services/notification/notification.service';
import { ApiResponse } from '../utils/apiResponse';
import { ApiError } from '../utils/apiError';

export class DeviceController {
  private notificationService: NotificationService;

  constructor() {
    this.notificationService = new NotificationService();
  }

  registerToken = async (req: Request, res: Response, Next: NextFunction) => {
    try {
      const user = (req as any).user;
      const { token, platform, deviceName } = req.body;

      const result = await this.notificationService.registerDeviceToken(user.userId, {
        token,
        platform,
        deviceName
      });

      return ApiResponse.success(res, 'FCM device token registered successfully', result);
    } catch (error) {
      Next(error);
    }
  };

  unregisterToken = async (req: Request, res: Response, Next: NextFunction) => {
    try {
      const user = (req as any).user;
      const token = req.params.token || req.body.token;

      if (!token) {
        throw ApiError.badRequest('Token is required');
      }

      await this.notificationService.unregisterDeviceToken(user.userId, token);
      return ApiResponse.success(res, 'Device token removed successfully', { unregistered: true });
    } catch (error) {
      Next(error);
    }
  };
}
