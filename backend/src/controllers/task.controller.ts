import { Request, Response } from 'express';
import { taskService } from '../services/personalization/task.service';
import { TaskStatus } from '../types/task.types';
import { Announcement } from '../models/Announcement';
import { AnnouncementStatus } from '../types/announcement.types';

export class TaskController {
  /**
   * GET /api/v1/tasks
   * Retrieve task list for authenticated student
   */
  public async getTasks(req: Request, res: Response): Promise<void> {
    const studentUserId = (req as any).user?.id;
    if (!studentUserId) {
      res.status(401).json({ success: false, message: 'Authentication required' });
      return;
    }

    const { status, announcementId, page, limit } = req.query;

    const result = await taskService.getStudentTasks(studentUserId, {
      status: status as TaskStatus,
      announcementId: announcementId as string,
      page: page ? parseInt(page as string, 10) : 1,
      limit: limit ? parseInt(limit as string, 10) : 20
    });

    res.status(200).json({
      success: true,
      data: result
    });
  }

  /**
   * GET /api/v1/tasks/:id
   * Retrieve specific task by ID
   */
  public async getTaskById(req: Request, res: Response): Promise<void> {
    const studentUserId = (req as any).user?.id;
    const { id } = req.params;

    const task = await taskService.getTaskById(id, studentUserId);
    if (!task) {
      res.status(404).json({ success: false, message: 'Task not found or unauthorized' });
      return;
    }

    res.status(200).json({
      success: true,
      data: { task }
    });
  }

  /**
   * PUT /api/v1/tasks/:id
   * Update task details
   */
  public async updateTask(req: Request, res: Response): Promise<void> {
    const studentUserId = (req as any).user?.id;
    const { id } = req.params;
    const { title, description, deadline, priority } = req.body;

    const task = await taskService.updateTask(id, studentUserId, {
      title,
      description,
      deadline: deadline ? new Date(deadline) : undefined,
      priority
    });

    if (!task) {
      res.status(404).json({ success: false, message: 'Task not found or unauthorized' });
      return;
    }

    res.status(200).json({
      success: true,
      message: 'Task updated successfully',
      data: { task }
    });
  }

  /**
   * PATCH /api/v1/tasks/:id/status
   * Update task status (PENDING, IN_PROGRESS, COMPLETED, DISMISSED)
   */
  public async updateTaskStatus(req: Request, res: Response): Promise<void> {
    const studentUserId = (req as any).user?.id;
    const { id } = req.params;
    const { status } = req.body;

    if (!status || !Object.values(TaskStatus).includes(status)) {
      res.status(400).json({
        success: false,
        message: `Invalid status. Valid values: ${Object.values(TaskStatus).join(', ')}`
      });
      return;
    }

    const task = await taskService.updateTaskStatus(id, studentUserId, status);
    if (!task) {
      res.status(404).json({ success: false, message: 'Task not found or unauthorized' });
      return;
    }

    res.status(200).json({
      success: true,
      message: 'Task status updated successfully',
      data: { task }
    });
  }

  /**
   * POST /api/v1/tasks/sync
   * Manually synchronize tasks from published announcements
   */
  public async syncTasks(req: Request, res: Response): Promise<void> {
    const studentUserId = (req as any).user?.id;
    if (!studentUserId) {
      res.status(401).json({ success: false, message: 'Authentication required' });
      return;
    }

    const published = await Announcement.find({
      status: AnnouncementStatus.PUBLISHED,
      $or: [
        { keyActions: { $exists: true, $ne: [] } },
        { extractedActions: { $exists: true, $ne: [] } }
      ]
    }).lean();

    const createdCount = await taskService.syncTasksForStudent(studentUserId, published as any);

    res.status(200).json({
      success: true,
      message: `Task synchronization complete. ${createdCount} new task(s) generated.`,
      data: { createdCount }
    });
  }
}
