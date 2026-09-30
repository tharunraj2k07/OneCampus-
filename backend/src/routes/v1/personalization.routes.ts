import { Router } from 'express';
import { PersonalizationController } from '../../controllers/personalization.controller';
import { asyncHandler } from '../../middleware/asyncHandler';
import { authenticate, authorizeRoles } from '../../middleware/auth.middleware';
import { UserRole } from '../../types/user.types';

const router = Router();
const personalizationController = new PersonalizationController();

// Protected routes for STUDENT
router.use(authenticate, authorizeRoles(UserRole.STUDENT));

// PUT /api/v1/personalization/preferences - Update interests and preferences
router.put('/preferences', asyncHandler(personalizationController.updatePreferences));

export const personalizationRoutes = router;
