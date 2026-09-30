import { Types } from 'mongoose';
import {
  Notification,
  DeviceToken,
  NotificationPreference,
  Announcement,
  StudentProfile,
  Task
} from '../../models';
import {
  NotificationType,
  INotificationPreferences,
  IUpdateNotificationPreferencesDTO
} from '../../types/notification.types';
import { IRegisterDeviceTokenDTO } from '../../types/device.types';
import { AnnouncementPriority, AnnouncementStatus } from '../../types/announcement.types';
import { RelevanceService } from '../personalization/relevance.service';
import { sendMulticastPushNotification } from '../../config/firebase.config';
import { ApiError } from '../../utils/apiError';

export class NotificationService {
  private relevanceService: RelevanceService;

  constructor() {
    this.relevanceService = new RelevanceService();
  }

  /**
   * Register or update FCM device token for authenticated user
   */
  async registerDeviceToken(
    userId: string,
    data: IRegisterDeviceTokenDTO
  ): Promise<{ token: string; platform: string; lastUsedAt: Date }> {
    if (!data.token || data.token.trim().length === 0) {
      throw ApiError.badRequest('FCM token is required');
    }

    const userObjId = new Types.ObjectId(userId);
    const platform = data.platform || 'ANDROID';

    const updated = await DeviceToken.findOneAndUpdate(
      { userId: userObjId, token: data.token.trim() },
      {
        userId: userObjId,
        token: data.token.trim(),
        platform,
        deviceName: data.deviceName,
        lastUsedAt: new Date()
      },
      { upsert: true, new: true, setDefaultsOnInsert: true }
    );

    return {
      token: updated.token,
      platform: updated.platform,
      lastUsedAt: updated.lastUsedAt
    };
  }

  /**
   * Unregister FCM device token
   */
  async unregisterDeviceToken(userId: string, token: string): Promise<boolean> {
    const result = await DeviceToken.deleteOne({
      userId: new Types.ObjectId(userId),
      token: token.trim()
    });
    return (result.deletedCount || 0) > 0;
  }

  /**
   * Get notification preferences for user
   */
  async getPreferences(userId: string): Promise<INotificationPreferences> {
    const userObjId = new Types.ObjectId(userId);
    let prefs = await NotificationPreference.findOne({ userId: userObjId });
    if (!prefs) {
      prefs = await NotificationPreference.create({
        userId: userObjId,
        pushEnabled: true,
        criticalAlerts: true,
        placementAlerts: true,
        assignmentAlerts: true,
        eventAlerts: true,
        deadlineReminders: true
      });
    }
    return prefs;
  }

  /**
   * Update notification preferences for user
   */
  async updatePreferences(
    userId: string,
    data: IUpdateNotificationPreferencesDTO
  ): Promise<INotificationPreferences> {
    const userObjId = new Types.ObjectId(userId);
    const updated = await NotificationPreference.findOneAndUpdate(
      { userId: userObjId },
      { $set: data },
      { new: true, upsert: true, setDefaultsOnInsert: true }
    );
    return updated!;
  }

  /**
   * Get paginated in-app notifications for authenticated user
   */
  async getUserNotifications(
    userId: string,
    page = 1,
    limit = 30,
    unreadOnly = false
  ) {
    const userObjId = new Types.ObjectId(userId);
    const filter: any = { userId: userObjId };
    if (unreadOnly) {
      filter.isRead = false;
    }

    const skip = (Math.max(1, page) - 1) * limit;

    const [items, total, unreadCount] = await Promise.all([
      Notification.find(filter)
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(limit)
        .lean(),
      Notification.countDocuments(filter),
      Notification.countDocuments({ userId: userObjId, isRead: false })
    ]);

    return {
      notifications: items.map((item: any) => ({
        id: item._id.toString(),
        userId: item.userId.toString(),
        title: item.title,
        body: item.body,
        type: item.type,
        announcementId: item.announcementId?.toString(),
        taskId: item.taskId?.toString(),
        category: item.category,
        priority: item.priority,
        isRead: item.isRead,
        createdAt: item.createdAt
      })),
      pagination: {
        page,
        limit,
        total,
        totalPages: Math.ceil(total / limit)
      },
      unreadCount
    };
  }

  /**
   * Mark single notification as read
   */
  async markAsRead(userId: string, notificationId: string): Promise<boolean> {
    const res = await Notification.updateOne(
      { _id: new Types.ObjectId(notificationId), userId: new Types.ObjectId(userId) },
      { $set: { isRead: true } }
    );
    return (res.modifiedCount || 0) > 0;
  }

  /**
   * Mark all notifications as read for user
   */
  async markAllAsRead(userId: string): Promise<number> {
    const res = await Notification.updateMany(
      { userId: new Types.ObjectId(userId), isRead: false },
      { $set: { isRead: true } }
    );
    return res.modifiedCount || 0;
  }

  /**
   * SMART NOTIFICATION ENGINE:
   * Called when announcement is published and AI analyzed.
   * Evaluates audience, computes personalization match, respects notification rules,
   * creates in-app notifications and delivers personalized FCM push messages.
   */
  async sendAnnouncementNotifications(
    announcementId: string
  ): Promise<{ eligibleStudents: number; notificationsSent: number }> {
    const ann = await Announcement.findById(announcementId);
    if (!ann || ann.status !== AnnouncementStatus.PUBLISHED) {
      return { eligibleStudents: 0, notificationsSent: 0 };
    }

    const priority = (ann.priorityLevel || (ann as any).priority?.level || AnnouncementPriority.MEDIUM) as AnnouncementPriority;
    const isCritical = priority === AnnouncementPriority.CRITICAL;
    const isHigh = priority === AnnouncementPriority.HIGH;
    const isLow = priority === AnnouncementPriority.LOW;

    // 1. Build eligibility filter for student profiles
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
    if (eligibleStudents.length === 0) {
      return { eligibleStudents: 0, notificationsSent: 0 };
    }

    // 2. Fetch notification preferences for all eligible students in bulk
    const userIds = eligibleStudents.map((s: any) => s.userId);
    const prefDocs = await NotificationPreference.find({ userId: { $in: userIds } }).lean();
    const prefMap = new Map<string, any>();
    prefDocs.forEach((p: any) => prefMap.set(p.userId.toString(), p));

    // 3. Determine notification type based on category / priority
    let notifType = NotificationType.GENERAL;
    if (isCritical) {
      notifType = NotificationType.CRITICAL_ALERT;
    } else if (ann.category?.toUpperCase().includes('PLACEMENT') || ann.category?.toUpperCase().includes('CAREER')) {
      notifType = NotificationType.PLACEMENT;
    } else if (ann.category?.toUpperCase().includes('ASSIGNMENT') || ann.category?.toUpperCase().includes('EXAM')) {
      notifType = NotificationType.ASSIGNMENT;
    } else if (ann.category?.toUpperCase().includes('EVENT') || ann.category?.toUpperCase().includes('HACKATHON') || ann.category?.toUpperCase().includes('CLUB')) {
      notifType = NotificationType.EVENT;
    }

    // 4. Fetch device tokens in bulk for push notification delivery
    const deviceTokens = await DeviceToken.find({ userId: { $in: userIds } }).lean();
    const tokenMap = new Map<string, string[]>();
    deviceTokens.forEach((dt: any) => {
      const uStr = dt.userId.toString();
      const existing = tokenMap.get(uStr) || [];
      existing.push(dt.token);
      tokenMap.set(uStr, existing);
    });

    const notificationsToInsert: any[] = [];
    const tokensToSendPush: string[] = [];

    const summaryText = ann.summary || (ann.originalContent.slice(0, 120) + '...');

    for (const student of eligibleStudents) {
      const sUserIdStr = (student as any).userId.toString();
      const prefs = prefMap.get(sUserIdStr) || {
        pushEnabled: true,
        criticalAlerts: true,
        placementAlerts: true,
        assignmentAlerts: true,
        eventAlerts: true,
        deadlineReminders: true
      };

      // Check category preferences (Critical notices override category preferences)
      if (!isCritical) {
        if (!prefs.pushEnabled) continue;
        if (notifType === NotificationType.PLACEMENT && !prefs.placementAlerts) continue;
        if (notifType === NotificationType.ASSIGNMENT && !prefs.assignmentAlerts) continue;
        if (notifType === NotificationType.EVENT && !prefs.eventAlerts) continue;
      }

      // Calculate personal relevance
      const relevance = this.relevanceService.calculateRelevance(student as any, ann as any);

      // Low priority notification rule: do not spam students with low relevance
      if (isLow && relevance.relevanceScore < 75) {
        continue;
      }

      // Format personalized title and message
      let title: string;
      let body: string;

      if (isCritical) {
        title = `🔴 CRITICAL: ${ann.title}`;
        body = summaryText;
      } else if (relevance.relevanceScore >= 70) {
        title = `🎯 Relevant for you: ${ann.title}`;
        const matchReason = relevance.relevanceExplanation[0] || `High match for ${(student as any).department} Year ${(student as any).year}`;
        body = `${matchReason} • ${summaryText}`;
      } else if (isHigh) {
        title = `⚡ High Priority: ${ann.title}`;
        body = summaryText;
      } else {
        title = ann.title;
        body = summaryText;
      }

      // Queue in-app notification
      notificationsToInsert.push({
        userId: (student as any).userId,
        title,
        body,
        type: notifType,
        announcementId: ann._id,
        category: ann.category || 'GENERAL',
        priority,
        isRead: false,
        createdAt: new Date()
      });

      // Collect FCM tokens
      const studentTokens = tokenMap.get(sUserIdStr);
      if (studentTokens && studentTokens.length > 0) {
        tokensToSendPush.push(...studentTokens);
      }
    }

    // Insert in-app notifications
    if (notificationsToInsert.length > 0) {
      await Notification.insertMany(notificationsToInsert, { ordered: false }).catch((err: any) => {
        console.warn('⚠️ [NotificationService] Partial error inserting notifications:', err);
      });
    }

    // Send FCM push notifications in batches (max 500 per FCM batch)
    if (tokensToSendPush.length > 0) {
      const uniqueTokens = Array.from(new Set(tokensToSendPush));
      const fcmPayload = {
        title: isCritical ? `🔴 Critical College Notice: ${ann.title}` : ann.title,
        body: summaryText,
        priority: isCritical ? ('high' as const) : ('normal' as const),
        data: {
          announcementId: ann._id.toString(),
          priority,
          type: notifType,
          category: ann.category || 'GENERAL'
        }
      };

      // Chunk in blocks of 500 tokens
      const chunkSize = 500;
      for (let i = 0; i < uniqueTokens.length; i += chunkSize) {
        const chunk = uniqueTokens.slice(i, i + chunkSize);
        await sendMulticastPushNotification(chunk, fcmPayload);
      }
    }

    return {
      eligibleStudents: eligibleStudents.length,
      notificationsSent: notificationsToInsert.length
    };
  }

  /**
   * DEADLINE REMINDERS:
   * Checks upcoming tasks due within 24h, 6h, and 1h.
   * Only sends if task is incomplete and reminder not already sent.
   */
  async checkDeadlineReminders(): Promise<{ remindersChecked: number; remindersSent: number }> {
    const now = new Date();
    const nowMs = now.getTime();

    // Look for pending/in-progress tasks with deadlines in next 25 hours
    const maxWindow = new Date(nowMs + 25 * 3600 * 1000);

    const pendingTasks = await Task.find({
      status: { $in: ['PENDING', 'IN_PROGRESS'] },
      deadline: { $gt: now, $lte: maxWindow }
    }).lean();

    let remindersSent = 0;

    for (const task of pendingTasks) {
      if (!task.deadline) continue;

      const deadlineMs = new Date(task.deadline).getTime();
      const diffMs = deadlineMs - nowMs;
      const hoursRemaining = diffMs / (3600 * 1000);

      // Determine reminder tier: 24h (22-25h), 6h (5-7h), 1h (0.5-1.5h)
      let tierTag: string | null = null;
      let urgencyText = '';

      if (hoursRemaining <= 1.5 && hoursRemaining > 0.2) {
        tierTag = '1H';
        urgencyText = 'in 1 hour';
      } else if (hoursRemaining <= 7 && hoursRemaining >= 5) {
        tierTag = '6H';
        urgencyText = 'in 6 hours';
      } else if (hoursRemaining <= 25 && hoursRemaining >= 23) {
        tierTag = '24H';
        urgencyText = 'tomorrow';
      }

      if (!tierTag) continue;

      // Check if reminder was already sent for this task and tier
      const existingReminder = await Notification.findOne({
        userId: task.studentId,
        taskId: task._id,
        body: { $regex: new RegExp(urgencyText, 'i') }
      });

      if (existingReminder) continue;

      // Verify student's preference for deadline reminders
      const prefs = await NotificationPreference.findOne({ userId: task.studentId }).lean();
      if (prefs && (!prefs.pushEnabled || !prefs.deadlineReminders)) {
        continue;
      }

      const title = `⏰ Deadline Reminder: ${task.title}`;
      const body = `Action required ${urgencyText}: ${task.description || task.title}`;

      // Create in-app notification
      await Notification.create({
        userId: task.studentId,
        title,
        body,
        type: NotificationType.DEADLINE,
        announcementId: task.announcementId,
        taskId: task._id,
        category: 'Action Required',
        priority: hoursRemaining <= 6 ? AnnouncementPriority.CRITICAL : AnnouncementPriority.HIGH,
        isRead: false
      });

      // Send FCM push to device tokens
      const userTokens = await DeviceToken.find({ userId: task.studentId }).lean();
      if (userTokens.length > 0) {
        const tokens = userTokens.map((t: any) => t.token);
        await sendMulticastPushNotification(tokens, {
          title,
          body,
          priority: hoursRemaining <= 6 ? 'high' : 'normal',
          data: {
            taskId: task._id.toString(),
            announcementId: task.announcementId?.toString() || '',
            type: NotificationType.DEADLINE
          }
        });
      }

      remindersSent++;
    }

    return {
      remindersChecked: pendingTasks.length,
      remindersSent
    };
  }
}
