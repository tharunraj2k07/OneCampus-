import { Router } from 'express';
import { TaskController } from '../../controllers/task.controller';
import { asyncHandler } from '../../middleware/asyncHandler';
import { authenticate, authorizeRoles } from '../../middleware/auth.middleware';
import { UserRole } from '../../types/user.types';

const router = Router();
const taskController = new TaskController();

// All task routes are strictly protected and restricted to authenticated STUDENTS
router.use(authenticate, authorizeRoles(UserRole.STUDENT));

// GET /api/v1/tasks - Get tasks for authenticated student
router.get('/', asyncHandler(taskController.getTasks));

// POST /api/v1/tasks/sync - Trigger sync with current eligible announcements
router.post('/sync', asyncHandler(taskController.syncTasks));

// GET /api/v1/tasks/:id - Get specific task
router.get('/:id', asyncHandler(taskController.getTaskById));

// PUT /api/v1/tasks/:id - Update task details
router.put('/:id', asyncHandler(taskController.updateTask));

// PATCH /api/v1/tasks/:id/status - Update task status (e.g. COMPLETED)
router.patch('/:id/status', asyncHandler(taskController.updateTaskStatus));

export const taskRoutes = router;
