import { Request, Response } from 'express';
import { personalizationService } from '../services/personalization/personalization.service';

export class PersonalizationController {
  /**
   * GET /api/v1/announcements/feed/personalized
   * Retrieve personalized, ranked announcements for the authenticated student.
   */
  public async getPersonalizedFeed(req: Request, res: Response): Promise<void> {
    const studentUserId = (req as any).user?.id;
    if (!studentUserId) {
      res.status(401).json({ success: false, message: 'Authentication required' });
      return;
    }

    const { category, priority, search, page, limit } = req.query;

    const result = await personalizationService.getPersonalizedFeed(studentUserId, {
      category: category as string,
      priority: priority as any,
      search: search as string,
      page: page ? parseInt(page as string, 10) : 1,
      limit: limit ? parseInt(limit as string, 10) : 20
    });

    res.status(200).json({
      success: true,
      data: result
    });
  }

  /**
   * PUT /api/v1/personalization/preferences
   * Update student interests and personalization preferences.
   */
  public async updatePreferences(req: Request, res: Response): Promise<void> {
    const studentUserId = (req as any).user?.id;
    if (!studentUserId) {
      res.status(401).json({ success: false, message: 'Authentication required' });
      return;
    }

    const { interests, focusAreas, preferredCategories } = req.body;

    const updated = await personalizationService.updateStudentPreferences(studentUserId, {
      interests,
      focusAreas,
      preferredCategories
    });

    res.status(200).json({
      success: true,
      message: 'Personalization preferences updated successfully',
      data: {
        interests: updated?.interests || [],
        focusAreas: updated?.focusAreas || [],
        preferredCategories: updated?.preferredCategories || []
      }
    });
  }
}
