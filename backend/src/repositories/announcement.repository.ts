import { BaseRepository } from './base.repository';
import { Announcement, IAnnouncementDocument } from '../models/Announcement';
import { IPaginatedData, IPaginationMeta } from '../types/api.types';
import { AnnouncementStatus } from '../types/announcement.types';
import { Types } from 'mongoose';

export interface IAnnouncementFilterOptions {
  category?: string;
  department?: string;
  year?: number;
  status?: AnnouncementStatus;
  search?: string;
  page?: number;
  limit?: number;
}

export class AnnouncementRepository extends BaseRepository<IAnnouncementDocument> {
  constructor() {
    super(Announcement);
  }

  async findFeed(options: IAnnouncementFilterOptions): Promise<IPaginatedData<IAnnouncementDocument>> {
    const page = Math.max(1, options.page || 1);
    const limit = Math.min(100, Math.max(1, options.limit || 20));
    const skip = (page - 1) * limit;

    const filter: Record<string, unknown> = {
      status: AnnouncementStatus.PUBLISHED
    };

    if (options.category && options.category !== 'ALL') {
      filter.category = options.category;
    }

    if (options.department && options.department !== 'ALL') {
      filter['targetAudience.departments'] = { $in: [options.department, 'ALL'] };
    }

    if (options.year) {
      filter['targetAudience.years'] = { $in: [options.year] };
    }

    if (options.search && options.search.trim().length > 0) {
      filter.$text = { $search: options.search.trim() };
    }

    const [items, total] = await Promise.all([
      this.model
        .find(filter)
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(limit)
        .exec(),
      this.model.countDocuments(filter).exec()
    ]);

    const totalPages = Math.ceil(total / limit) || 1;

    const pagination: IPaginationMeta = {
      total,
      page,
      limit,
      totalPages
    };

    return { items, pagination };
  }

  async findByPublisher(
    publisherId: string,
    status?: AnnouncementStatus | 'ALL',
    page = 1,
    limit = 20
  ): Promise<IPaginatedData<IAnnouncementDocument>> {
    const safePage = Math.max(1, page);
    const safeLimit = Math.min(100, Math.max(1, limit));
    const skip = (safePage - 1) * safeLimit;

    const filter: Record<string, unknown> = {
      publisherId: Types.ObjectId.isValid(publisherId) ? new Types.ObjectId(publisherId) : publisherId
    };

    if (status && status !== 'ALL') {
      filter.status = status;
    }

    const [items, total] = await Promise.all([
      this.model
        .find(filter)
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(safeLimit)
        .exec(),
      this.model.countDocuments(filter).exec()
    ]);

    const totalPages = Math.ceil(total / safeLimit) || 1;

    return {
      items,
      pagination: {
        total,
        page: safePage,
        limit: safeLimit,
        totalPages
      }
    };
  }

  async findWithFilters(options: IAnnouncementFilterOptions): Promise<IPaginatedData<IAnnouncementDocument>> {
    const page = Math.max(1, options.page || 1);
    const limit = Math.min(100, Math.max(1, options.limit || 20));
    const skip = (page - 1) * limit;

    const filter: Record<string, unknown> = {};

    if (options.status) {
      filter.status = options.status;
    } else {
      filter.status = AnnouncementStatus.PUBLISHED;
    }

    if (options.category && options.category !== 'ALL') {
      filter.category = options.category;
    }

    if (options.department && options.department !== 'ALL') {
      filter['targetAudience.departments'] = { $in: [options.department, 'ALL'] };
    }

    if (options.year) {
      filter['targetAudience.years'] = { $in: [options.year] };
    }

    if (options.search && options.search.trim().length > 0) {
      filter.$text = { $search: options.search.trim() };
    }

    const [items, total] = await Promise.all([
      this.model
        .find(filter)
        .sort({ createdAt: -1 })
        .skip(skip)
        .limit(limit)
        .exec(),
      this.model.countDocuments(filter).exec()
    ]);

    const totalPages = Math.ceil(total / limit) || 1;

    const pagination: IPaginationMeta = {
      total,
      page,
      limit,
      totalPages
    };

    return { items, pagination };
  }
}

