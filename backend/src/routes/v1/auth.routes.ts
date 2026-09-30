import { Router } from 'express';
import { AuthController } from '../../controllers/auth.controller';
import { validateRegister, validateLogin } from '../../validators/auth.validator';
import { authenticate } from '../../middleware/auth.middleware';
import { asyncHandler } from '../../middleware/asyncHandler';

const router = Router();
const authController = new AuthController();

/**
 * @route   POST /api/v1/auth/register
 * @desc    Register a new Student or Faculty member
 * @access  Public
 */
router.post('/register', validateRegister, asyncHandler(authController.register));

/**
 * @route   POST /api/v1/auth/login
 * @desc    Authenticate user and get token
 * @access  Public
 */
router.post('/login', validateLogin, asyncHandler(authController.login));

/**
 * @route   GET /api/v1/auth/me
 * @desc    Get current authenticated user profile
 * @access  Private (STUDENT, FACULTY, ADMIN)
 */
router.get('/me', authenticate, asyncHandler(authController.getMe));

/**
 * @route   POST /api/v1/auth/logout
 * @desc    Logout user (stateless JWT acknowledgment)
 * @access  Public / Private
 */
router.post('/logout', asyncHandler(authController.logout));

export default router;
