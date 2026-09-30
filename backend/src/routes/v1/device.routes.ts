import { Router } from 'express';
import { DeviceController } from '../../controllers/device.controller';
import { asyncHandler } from '../../middleware/asyncHandler';
import { authenticate } from '../../middleware/auth.middleware';

const router = Router();
const deviceController = new DeviceController();

// Protected: any authenticated user (student, faculty, admin) can register their device
router.use(authenticate);

// POST /api/v1/devices/token - Register or refresh device token
router.post('/token', asyncHandler(deviceController.registerToken));

// DELETE /api/v1/devices/token/:token - Remove device token (e.g. on logout)
router.delete('/token/:token', asyncHandler(deviceController.unregisterToken));

export const deviceRoutes = router;
