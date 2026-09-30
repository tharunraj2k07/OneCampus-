import { Request, Response } from 'express';
import { AnnouncementService } from '../services/announcement.service';
import { ApiResponse } from '../utils/apiResponse';
import { AnnouncementStatus } from '../types/announcement.types';
import { IAuthUser } from '../types/auth.types';

export class AnnouncementController {
  private announcementService: AnnouncementService;

  constructor(announcementService: AnnouncementService = new AnnouncementService()) {
    this.announcementService = announcementService;
  }

  getFeedAnnouncements = async (req: Request, res: Response): Promise<Response> => {
    const { category, department, year, status, search, page, limit } = req.query;

    const result = await this.announcementService.getFeedAnnouncements(
      {
        category: category as string,
        department: department as string,
        year: year ? parseInt(year as string, 10) : undefined,
        status: status as AnnouncementStatus,
        search: search as string,
        page: page ? parseInt(page as string, 10) : 1,
        limit: limit ? parseInt(limit as string, 10) : 20
      },
      req.user as IAuthUser
    );

    return ApiResponse.success(res, 'Announcements retrieved successfully', {
      announcements: result.items,
      pagination: result.pagination
    });
  };

  getMyAnnouncements = async (req: Request, res: Response): Promise<Response> => {
    const { status, page, limit } = req.query;

    const result = await this.announcementService.getMyAnnouncements(
      req.user as IAuthUser,
      status as AnnouncementStatus | 'ALL',
      page ? parseInt(page as string, 10) : 1,
      limit ? parseInt(limit as string, 10) : 20
    );

    return ApiResponse.success(res, 'Faculty announcements retrieved successfully', {
      announcements: result.items,
      pagination: result.pagination
    });
  };

  getAnnouncementById = async (req: Request, res: Response): Promise<Response> => {
    const { id } = req.params;
    const announcement = await this.announcementService.getAnnouncementDetails(id, req.user as IAuthUser);
    return ApiResponse.success(res, 'Announcement details retrieved successfully', announcement);
  };

  previewAnnouncement = async (req: Request, res: Response): Promise<Response> => {
    const { id } = req.params;
    const preview = await this.announcementService.previewAnnouncement(id, req.user as IAuthUser);
    return ApiResponse.success(res, 'Announcement preview retrieved successfully', preview);
  };

  createAnnouncement = async (req: Request, res: Response): Promise<Response> => {
    const created = await this.announcementService.createAnnouncement(req.body, req.user as IAuthUser);
    const message = created.status === AnnouncementStatus.PUBLISHED
      ? 'Announcement published successfully'
      : 'Announcement draft saved successfully';
    return ApiResponse.created(res, message, created);
  };

  publishAnnouncement = async (req: Request, res: Response): Promise<Response> => {
    const { id } = req.params;
    const published = await this.announcementService.publishAnnouncement(id, req.user as IAuthUser);
    return ApiResponse.success(res, 'Announcement published successfully', published);
  };

  updateAnnouncement = async (req: Request, res: Response): Promise<Response> => {
    const { id } = req.params;
    const updated = await this.announcementService.updateAnnouncement(id, req.body, req.user as IAuthUser);
    return ApiResponse.success(res, 'Announcement updated successfully', updated);
  };

  archiveAnnouncement = async (req: Request, res: Response): Promise<Response> => {
    const { id } = req.params;
    const archived = await this.announcementService.archiveAnnouncement(id, req.user as IAuthUser);
    return ApiResponse.success(res, 'Announcement archived successfully', archived);
  };

  deleteAnnouncement = async (req: Request, res: Response): Promise<Response> => {
    const { id } = req.params;
    const result = await this.announcementService.deleteAnnouncement(id, req.user as IAuthUser);
    return ApiResponse.success(res, 'Announcement deleted successfully', result);
  };

  analyzeAnnouncement = async (req: Request, res: Response): Promise<Response> => {
    const { id } = req.params;
    const analyzed = await this.announcementService.analyzeAnnouncementNow(id, req.user as IAuthUser);
    return ApiResponse.success(res, 'Announcement analyzed successfully by OneCampus AI', analyzed);
  };

  toggleBookmark = async (req: Request, res: Response): Promise<Response> => {
    const { id } = req.params;
    return ApiResponse.success(res, 'Bookmark toggled', { id, bookmarked: true });
  };
}

