import { Router } from 'express';
import { healthRoutes } from './health.routes';
import { announcementRoutes } from './announcement.routes';
import authRoutes from './auth.routes';
import { taskRoutes } from './task.routes';
import { personalizationRoutes } from './personalization.routes';
import { deviceRoutes } from './device.routes';
import { notificationRoutes } from './notification.routes';

const router = Router();

router.use('/health', healthRoutes);
router.use('/auth', authRoutes);
router.use('/announcements', announcementRoutes);
router.use('/tasks', taskRoutes);
router.use('/personalization', personalizationRoutes);
router.use('/devices', deviceRoutes);
router.use('/notifications', notificationRoutes);

export const v1Routes = router;
