import express, { Application, Request, Response, NextFunction } from 'express';
import cors from 'cors';
import { env } from './config/env';
import { rootApiRouter } from './routes';
import { errorHandler, notFoundHandler } from './middleware';
import { logger } from './utils/logger';

export const createApp = (): Application => {
  const app: Application = express();

  // 1. Security & CORS Configuration
  const corsOptions: cors.CorsOptions = {
    origin: (origin, callback) => {
      // Allow requests with no origin (like mobile apps, curl, Postman)
      if (!origin) return callback(null, true);

      if (env.CORS_ORIGIN === '*') {
        return callback(null, true);
      }

      const allowedOrigins = Array.isArray(env.CORS_ORIGIN) ? env.CORS_ORIGIN : [env.CORS_ORIGIN];
      if (allowedOrigins.indexOf(origin) !== -1 || env.NODE_ENV === 'development') {
        return callback(null, true);
      }

      return callback(new Error('Not allowed by CORS configuration'));
    },
    credentials: true,
    methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'Authorization', 'X-Requested-With', 'Accept']
  };

  app.use(cors(corsOptions));

  // 2. Request Parsing Middleware
  app.use(express.json({ limit: '10mb' }));
  app.use(express.urlencoded({ extended: true, limit: '10mb' }));

  // 3. HTTP Request Logging Middleware
  app.use((req: Request, _res: Response, next: NextFunction) => {
    logger.debug(`[HTTP] ${req.method} ${req.originalUrl}`);
    next();
  });

  // 4. Mount API Routes (/api)
  app.use('/api', rootApiRouter);

  // 5. 404 Not Found Middleware
  app.use(notFoundHandler);

  // 6. Global Error Handling Middleware
  app.use(errorHandler);

  return app;
};

export const app = createApp();
