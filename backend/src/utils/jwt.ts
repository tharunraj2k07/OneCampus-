import jwt from 'jsonwebtoken';
import { env } from '../config/env';
import { IJwtPayload } from '../types/auth.types';
import { ApiError } from './apiError';

export class JwtUtil {
  /**
   * Generates a signed JWT token for an authenticated user.
   */
  static generateToken(payload: IJwtPayload): string {
    return jwt.sign(
      {
        userId: payload.userId,
        email: payload.email,
        role: payload.role
      },
      env.JWT_SECRET,
      {
        expiresIn: env.JWT_EXPIRES_IN as jwt.SignOptions['expiresIn']
      }
    );
  }

  /**
   * Verifies and decodes a JWT token.
   */
  static verifyToken(token: string): IJwtPayload {
    try {
      const decoded = jwt.verify(token, env.JWT_SECRET) as IJwtPayload;
      return decoded;
    } catch (error) {
      if (error instanceof jwt.TokenExpiredError) {
        throw ApiError.unauthorized('Authentication token has expired. Please login again.', 'TOKEN_EXPIRED');
      }
      if (error instanceof jwt.JsonWebTokenError) {
        throw ApiError.unauthorized('Invalid authentication token provided.', 'INVALID_TOKEN');
      }
      throw ApiError.unauthorized('Authentication failed.', 'AUTH_FAILED');
    }
  }
}
