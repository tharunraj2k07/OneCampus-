import { Request, Response } from 'express';
import { AuthService } from '../services/auth.service';
import { ApiResponse } from '../utils/apiResponse';
import { ApiError } from '../utils/apiError';

export class AuthController {
  private authService: AuthService;

  constructor(authService: AuthService = new AuthService()) {
    this.authService = authService;
  }

  register = async (req: Request, res: Response): Promise<Response> => {
    const result = await this.authService.register(req.body);
    return ApiResponse.created(res, 'Account registered successfully', result);
  };

  login = async (req: Request, res: Response): Promise<Response> => {
    const result = await this.authService.login(req.body);
    return ApiResponse.success(res, 'Login successful', result);
  };

  getMe = async (req: Request, res: Response): Promise<Response> => {
    if (!req.user || !req.user.userId) {
      throw ApiError.unauthorized('Authentication required', 'UNAUTHORIZED');
    }
    const result = await this.authService.getCurrentUser(req.user.userId);
    return ApiResponse.success(res, 'Current user profile retrieved successfully', result);
  };

  logout = async (_req: Request, res: Response): Promise<Response> => {
    return ApiResponse.success(res, 'Logged out successfully');
  };
}
