import { Router } from 'express';
import { AnnouncementController } from '../../controllers/announcement.controller';
import { PersonalizationController } from '../../controllers/personalization.controller';
import { asyncHandler } from '../../middleware/asyncHandler';
import { authenticate, authorizeRoles } from '../../middleware/auth.middleware';
import {
  validateCreateAnnouncement,
  validateUpdateAnnouncement
} from '../../validators/announcement.validator';
import { UserRole } from '../../types/user.types';

const router = Router();
const announcementController = new AnnouncementController();
const personalizationController = new PersonalizationController();

// GET /api/v1/announcements/feed/personalized - Protected personalized feed for STUDENT
router.get(
  '/feed/personalized',
  authenticate,
  authorizeRoles(UserRole.STUDENT),
  asyncHandler(personalizationController.getPersonalizedFeed)
);

// GET /api/v1/announcements - Authenticated feed (students only see PUBLISHED)
router.get('/', authenticate, asyncHandler(announcementController.getFeedAnnouncements));

// GET /api/v1/announcements/my - FACULTY & ADMIN management of their own circulars (drafts, published, archived)
router.get(
  '/my',
  authenticate,
  authorizeRoles(UserRole.FACULTY, UserRole.ADMIN),
  asyncHandler(announcementController.getMyAnnouncements)
);

// POST /api/v1/announcements - FACULTY or ADMIN only: create announcement (DRAFT or PUBLISHED)
router.post(
  '/',
  authenticate,
  authorizeRoles(UserRole.FACULTY, UserRole.ADMIN),
  validateCreateAnnouncement,
  asyncHandler(announcementController.createAnnouncement)
);

// GET /api/v1/announcements/:id/preview - Preview announcement (Owner FACULTY or ADMIN only)
router.get(
  '/:id/preview',
  authenticate,
  authorizeRoles(UserRole.FACULTY, UserRole.ADMIN),
  asyncHandler(announcementController.previewAnnouncement)
);

// POST /api/v1/announcements/:id/publish - Publish a DRAFT announcement
router.post(
  '/:id/publish',
  authenticate,
  authorizeRoles(UserRole.FACULTY, UserRole.ADMIN),
  asyncHandler(announcementController.publishAnnouncement)
);

// POST /api/v1/announcements/:id/analyze - Trigger AI analysis (Publisher FACULTY or ADMIN)
router.post(
  '/:id/analyze',
  authenticate,
  authorizeRoles(UserRole.FACULTY, UserRole.ADMIN),
  asyncHandler(announcementController.analyzeAnnouncement)
);

// POST /api/v1/announcements/:id/archive - Archive a published/draft announcement
router.post(
  '/:id/archive',
  authenticate,
  authorizeRoles(UserRole.FACULTY, UserRole.ADMIN),
  asyncHandler(announcementController.archiveAnnouncement)
);

// GET /api/v1/announcements/:id - View announcement details (Student restricted to PUBLISHED; Faculty restricted to PUBLISHED or own)
router.get('/:id', authenticate, asyncHandler(announcementController.getAnnouncementById));

// POST /api/v1/announcements/:id/bookmark - Bookmark/unbookmark announcement
router.post('/:id/bookmark', authenticate, asyncHandler(announcementController.toggleBookmark));

// PUT /api/v1/announcements/:id - Publisher FACULTY or ADMIN only
router.put(
  '/:id',
  authenticate,
  authorizeRoles(UserRole.FACULTY, UserRole.ADMIN),
  validateUpdateAnnouncement,
  asyncHandler(announcementController.updateAnnouncement)
);

// DELETE /api/v1/announcements/:id - Hard delete DRAFT announcements (Publisher FACULTY or ADMIN only)
router.delete(
  '/:id',
  authenticate,
  authorizeRoles(UserRole.FACULTY, UserRole.ADMIN),
  asyncHandler(announcementController.deleteAnnouncement)
);

export const announcementRoutes = router;

