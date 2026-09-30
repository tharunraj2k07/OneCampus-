import { Router } from 'express';
import { NotificationController } from '../../controllers/notification.controller';
import { asyncHandler } from '../../middleware/asyncHandler';
import { authenticate } from '../../middleware/auth.middleware';

const router = Router();
const notificationController = new NotificationController();

// Protected: all routes require authenticated user
router.use(authenticate);

// GET /api/v1/notifications - Paginated in-app notifications
router.get('/', asyncHandler(notificationController.getNotifications));

// PATCH /api/v1/notifications/read-all - Mark all as read
router.patch('/read-all', asyncHandler(notificationController.markAllAsRead));

// PATCH /api/v1/notifications/:id/read - Mark single notification as read
router.patch('/:id/read', asyncHandler(notificationController.markAsRead));

// GET /api/v1/notifications/preferences - Get notification preferences
router.get('/preferences', asyncHandler(notificationController.getPreferences));

// PUT /api/v1/notifications/preferences - Update notification preferences
router.put('/preferences', asyncHandler(notificationController.updatePreferences));

// POST /api/v1/notifications/reminders/check - Check & trigger deadline reminders
router.post('/reminders/check', asyncHandler(notificationController.checkReminders));

export const notificationRoutes = router;
