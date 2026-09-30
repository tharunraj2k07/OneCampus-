import { Router } from 'express';
import { v1Routes } from './v1';
import { HealthController } from '../controllers/health.controller';

const router = Router();

// Version 1 Routes
router.use('/v1', v1Routes);

// Direct /api/health alias for convenience & reverse proxy health-checks
router.get('/health', HealthController.getHealth);

export const rootApiRouter = router;
