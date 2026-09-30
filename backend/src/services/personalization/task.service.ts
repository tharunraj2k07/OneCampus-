import { Types } from 'mongoose';
import { Task } from '../../models/Task';
import { ITask, ITaskFilters, ITaskResponse, TaskStatus } from '../../types/task.types';
import { AnnouncementPriority, IAnnouncement } from '../../types/announcement.types';
import { IAnnouncementDocument } from '../../models/Announcement';

export class TaskService {
  /**
   * Synchronizes actionable tasks from relevant announcements for an eligible student.
   * Strictly prevents duplicate tasks using the compound key (studentId, announcementId, actionIdentifier).
   */
  public async syncTasksForStudent(
    studentUserId: string | Types.ObjectId,
    announcements: (IAnnouncementDocument | IAnnouncement & { _id: Types.ObjectId })[]
  ): Promise<number> {
    const studentId = typeof studentUserId === 'string' ? new Types.ObjectId(studentUserId) : studentUserId;
    let createdCount = 0;

    for (const ann of announcements) {
      const actions = ann.keyActions || ann.extractedActions || [];
      if (!actions || actions.length === 0) continue;

      const annId = ann._id;
      const annPriority = (ann.priorityLevel || (ann as any).priority?.level || AnnouncementPriority.MEDIUM) as AnnouncementPriority;
      const deadline = ann.deadline || ann.extractedDeadline;

      for (let i = 0; i < actions.length; i++) {
        const actionText = actions[i].trim();
        if (!actionText) continue;

        // Unique action identifier for deterministic deduplication
        const normalizedActionId = `action-${i}-${actionText.slice(0, 40).replace(/[^a-zA-Z0-9]/g, '_').toLowerCase()}`;

        // Check for existing task
        const existing = await Task.findOne({
          studentId,
          announcementId: annId,
          actionIdentifier: normalizedActionId
        });

        if (!existing) {
          // Determine priority with deadline proximity boost if < 24h
          let taskPriority = annPriority;
          if (deadline) {
            const msUntilDeadline = new Date(deadline).getTime() - Date.now();
            if (msUntilDeadline > 0 && msUntilDeadline <= 24 * 60 * 60 * 1000) {
              if (taskPriority === AnnouncementPriority.LOW || taskPriority === AnnouncementPriority.MEDIUM) {
                taskPriority = AnnouncementPriority.HIGH;
              }
            }
          }

          try {
            await Task.create({
              studentId,
              announcementId: annId,
              actionIdentifier: normalizedActionId,
              title: actionText,
              description: `Action item from: ${ann.title}`,
              deadline: deadline || undefined,
              priority: taskPriority,
              status: TaskStatus.PENDING
            });
            createdCount++;
          } catch (err: any) {
            // Ignore duplicate key race condition error
            if (err.code !== 11000) {
              console.error('Failed to create task item:', err);
            }
          }
        }
      }
    }

    return createdCount;
  }

  /**
   * Retrieves tasks for a student with filtering and pagination.
   * Strictly scoped to the authenticated student's userId.
   */
  public async getStudentTasks(
    studentUserId: string,
    filters: ITaskFilters = {}
  ): Promise<{
    tasks: ITaskResponse[];
    total: number;
    completedCount: number;
    pendingCount: number;
    page: number;
    limit: number;
  }> {
    const studentId = new Types.ObjectId(studentUserId);
    const query: any = { studentId };

    if (filters.status) {
      query.status = filters.status;
    }
    if (filters.announcementId) {
      query.announcementId = new Types.ObjectId(filters.announcementId);
    }

    const page = Math.max(1, filters.page || 1);
    const limit = Math.min(100, Math.max(1, filters.limit || 20));
    const skip = (page - 1) * limit;

    const [rawTasks, total, completedCount, pendingCount] = await Promise.all([
      Task.find(query)
        .populate('announcementId', 'title category deadline priorityScore priorityLevel')
        .sort({
          // PENDING & IN_PROGRESS first, COMPLETED & DISMISSED last
          status: 1,
          deadline: 1,
          createdAt: -1
        })
        .skip(skip)
        .limit(limit)
        .lean(),
      Task.countDocuments(query),
      Task.countDocuments({ studentId, status: TaskStatus.COMPLETED }),
      Task.countDocuments({ studentId, status: { $in: [TaskStatus.PENDING, TaskStatus.IN_PROGRESS] } })
    ]);

    const tasks: ITaskResponse[] = rawTasks.map((t: any) => ({
      id: t._id.toString(),
      studentId: t.studentId.toString(),
      announcementId: t.announcementId?._id?.toString() || t.announcementId?.toString(),
      actionIdentifier: t.actionIdentifier,
      title: t.title,
      description: t.description,
      deadline: t.deadline ? t.deadline.toISOString() : undefined,
      deadlineEpochMs: t.deadline ? new Date(t.deadline).getTime() : undefined,
      priority: t.priority,
      status: t.status,
      createdAt: t.createdAt.toISOString(),
      completedAt: t.completedAt ? t.completedAt.toISOString() : undefined
    }));

    return {
      tasks,
      total,
      completedCount,
      pendingCount,
      page,
      limit
    };
  }

  /**
   * Retrieves a single task ensuring it belongs to the authenticated student.
   */
  public async getTaskById(taskId: string, studentUserId: string): Promise<ITaskResponse | null> {
    const task = await Task.findOne({
      _id: new Types.ObjectId(taskId),
      studentId: new Types.ObjectId(studentUserId)
    }).lean();

    if (!task) return null;

    return {
      id: task._id.toString(),
      studentId: task.studentId.toString(),
      announcementId: task.announcementId?.toString(),
      actionIdentifier: task.actionIdentifier,
      title: task.title,
      description: task.description,
      deadline: task.deadline ? task.deadline.toISOString() : undefined,
      deadlineEpochMs: task.deadline ? new Date(task.deadline).getTime() : undefined,
      priority: task.priority,
      status: task.status,
      createdAt: task.createdAt.toISOString(),
      completedAt: task.completedAt ? task.completedAt.toISOString() : undefined
    };
  }

  /**
   * Updates task status (PENDING, IN_PROGRESS, COMPLETED, DISMISSED)
   * Strictly scoped to authenticated student.
   */
  public async updateTaskStatus(
    taskId: string,
    studentUserId: string,
    status: TaskStatus
  ): Promise<ITaskResponse | null> {
    const updateData: any = { status };
    if (status === TaskStatus.COMPLETED) {
      updateData.completedAt = new Date();
    } else {
      updateData.completedAt = null;
    }

    const updated = await Task.findOneAndUpdate(
      {
        _id: new Types.ObjectId(taskId),
        studentId: new Types.ObjectId(studentUserId)
      },
      { $set: updateData },
      { new: true }
    ).lean();

    if (!updated) return null;

    return {
      id: updated._id.toString(),
      studentId: updated.studentId.toString(),
      announcementId: updated.announcementId?.toString(),
      actionIdentifier: updated.actionIdentifier,
      title: updated.title,
      description: updated.description,
      deadline: updated.deadline ? updated.deadline.toISOString() : undefined,
      deadlineEpochMs: updated.deadline ? new Date(updated.deadline).getTime() : undefined,
      priority: updated.priority,
      status: updated.status,
      createdAt: updated.createdAt.toISOString(),
      completedAt: updated.completedAt ? updated.completedAt.toISOString() : undefined
    };
  }

  /**
   * Updates task details (title, description, deadline, priority)
   */
  public async updateTask(
    taskId: string,
    studentUserId: string,
    updates: Partial<ITask>
  ): Promise<ITaskResponse | null> {
    const updated = await Task.findOneAndUpdate(
      {
        _id: new Types.ObjectId(taskId),
        studentId: new Types.ObjectId(studentUserId)
      },
      { $set: updates },
      { new: true }
    ).lean();

    if (!updated) return null;

    return {
      id: updated._id.toString(),
      studentId: updated.studentId.toString(),
      announcementId: updated.announcementId?.toString(),
      actionIdentifier: updated.actionIdentifier,
      title: updated.title,
      description: updated.description,
      deadline: updated.deadline ? updated.deadline.toISOString() : undefined,
      deadlineEpochMs: updated.deadline ? new Date(updated.deadline).getTime() : undefined,
      priority: updated.priority,
      status: updated.status,
      createdAt: updated.createdAt.toISOString(),
      completedAt: updated.completedAt ? updated.completedAt.toISOString() : undefined
    };
  }

  /**
   * Synchronize actionable tasks for all eligible students of a newly published announcement.
   */
  public async syncTasksForAnnouncement(announcementId: string): Promise<number> {
    const { Announcement } = await import('../../models/Announcement');
    const { StudentProfile } = await import('../../models/StudentProfile');
    const ann = await Announcement.findById(announcementId);
    if (!ann) return 0;

    const studentQuery: any = {};
    const audience = ann.targetAudience;
    if (audience) {
      if (audience.departments && audience.departments.length > 0 && !audience.departments.includes('ALL')) {
        studentQuery.department = { $in: audience.departments };
      }
      if (audience.years && audience.years.length > 0) {
        studentQuery.year = { $in: audience.years };
      }
      if (audience.sections && audience.sections.length > 0) {
        studentQuery.section = { $in: audience.sections };
      }
    }

    const eligibleStudents = await StudentProfile.find(studentQuery).lean();
    let totalCreated = 0;
    for (const student of eligibleStudents) {
      const created = await this.syncTasksForStudent(student.userId, [ann as any]);
      totalCreated += created;
    }
    return totalCreated;
  }
}

export const taskService = new TaskService();
