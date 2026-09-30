import { Request, Response, NextFunction } from 'express';
import { ApiError } from '../utils/apiError';
import { JwtUtil } from '../utils/jwt';
import { UserRole } from '../types/user.types';
import { User } from '../models/User';

export const authenticate = async (req: Request, _res: Response, next: NextFunction): Promise<void> => {
  const authHeader = req.headers.authorization;

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    throw ApiError.unauthorized('Authentication token is missing or malformed. Expected Bearer token.', 'TOKEN_MISSING');
  }

  const token = authHeader.split(' ')[1];
  if (!token || token.trim().length === 0) {
    throw ApiError.unauthorized('Authentication token is missing.', 'TOKEN_MISSING');
  }

  const decoded = JwtUtil.verifyToken(token);

  // Verify that the user exists and is active
  const user = await User.findById(decoded.userId).exec();
  if (!user) {
    throw ApiError.unauthorized('The user associated with this token no longer exists.', 'USER_NOT_FOUND');
  }

  if (!user.isActive) {
    throw ApiError.forbidden('User account has been deactivated. Please contact college administration.', 'ACCOUNT_DEACTIVATED');
  }

  req.user = {
    userId: user._id.toString(),
    name: user.name,
    email: user.email,
    role: user.role
  };

  next();
};

export const authorizeRoles = (...allowedRoles: UserRole[]) => {
  return (req: Request, _res: Response, next: NextFunction): void => {
    if (!req.user) {
      throw ApiError.unauthorized('Authentication required to access this resource.', 'UNAUTHORIZED');
    }

    if (!allowedRoles.includes(req.user.role)) {
      throw ApiError.forbidden(
        `Access denied. Role '${req.user.role}' is not authorized to access this resource.`,
        'INSUFFICIENT_PERMISSIONS'
      );
    }

    next();
  };
};
