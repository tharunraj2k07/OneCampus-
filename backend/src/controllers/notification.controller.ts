import { Request, Response, NextFunction } from 'express';
import { NotificationService } from '../services/notification/notification.service';
import { ApiResponse } from '../utils/apiResponse';

export class NotificationController {
  private notificationService: NotificationService;

  constructor() {
    this.notificationService = new NotificationService();
  }

  getNotifications = async (req: Request, res: Response, Next: NextFunction) => {
    try {
      const user = (req as any).user;
      const page = parseInt(req.query.page as string, 10) || 1;
      const limit = parseInt(req.query.limit as string, 10) || 30;
      const unreadOnly = req.query.unread === 'true';

      const data = await this.notificationService.getUserNotifications(user.userId, page, limit, unreadOnly);
      return ApiResponse.success(res, 'Notifications retrieved successfully', data);
    } catch (error) {
      Next(error);
    }
  };

  markAsRead = async (req: Request, res: Response, Next: NextFunction) => {
    try {
      const user = (req as any).user;
      const notificationId = req.params.id;

      await this.notificationService.markAsRead(user.userId, notificationId);
      return ApiResponse.success(res, 'Notification marked as read', { id: notificationId, isRead: true });
    } catch (error) {
      Next(error);
    }
  };

  markAllAsRead = async (req: Request, res: Response, Next: NextFunction) => {
    try {
      const user = (req as any).user;
      const count = await this.notificationService.markAllAsRead(user.userId);
      return ApiResponse.success(res, 'All notifications marked as read', { updatedCount: count });
    } catch (error) {
      Next(error);
    }
  };

  getPreferences = async (req: Request, res: Response, Next: NextFunction) => {
    try {
      const user = (req as any).user;
      const prefs = await this.notificationService.getPreferences(user.userId);
      return ApiResponse.success(res, 'Notification preferences retrieved', prefs);
    } catch (error) {
      Next(error);
    }
  };

  updatePreferences = async (req: Request, res: Response, Next: NextFunction) => {
    try {
      const user = (req as any).user;
      const updated = await this.notificationService.updatePreferences(user.userId, req.body);
      return ApiResponse.success(res, 'Notification preferences updated successfully', updated);
    } catch (error) {
      Next(error);
    }
  };

  checkReminders = async (req: Request, res: Response, Next: NextFunction) => {
    try {
      const result = await this.notificationService.checkDeadlineReminders();
      return ApiResponse.success(res, 'Deadline reminder check completed', result);
    } catch (error) {
      Next(error);
    }
  };
}
