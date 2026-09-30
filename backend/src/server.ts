import { app } from './app';
import { env } from './config/env';
import { connectDatabase, disconnectDatabase } from './config/db';
import { logger } from './utils/logger';

const startServer = async (): Promise<void> => {
  logger.info(`Starting OneCampus AI Backend in [${env.NODE_ENV}] mode...`);

  // Connect to MongoDB
  const dbConnected = await connectDatabase();
  if (dbConnected) {
    logger.info('Database connection established successfully.');
  } else {
    logger.warn('Server starting with database disconnected. Check MONGODB_URI and ensure MongoDB instance is reachable.');
  }

  // Start HTTP Server
  const server = app.listen(env.PORT, () => {
    logger.info(`🚀 OneCampus AI API Server is live at http://localhost:${env.PORT}`);
    logger.info(`🏥 Health Check endpoint: http://localhost:${env.PORT}/api/v1/health`);
    logger.info(`📢 Announcements endpoint: http://localhost:${env.PORT}/api/v1/announcements`);
  });

  // Graceful Shutdown Handlers
  const handleShutdown = async (signal: string) => {
    logger.info(`[Process] ${signal} signal received. Closing HTTP server and database connections...`);
    server.close(async () => {
      logger.info('[Process] HTTP server closed.');
      await disconnectDatabase();
      logger.info('[Process] Graceful shutdown completed.');
      process.exit(0);
    });

    // Force exit after 10 seconds if not shut down cleanly
    setTimeout(() => {
      logger.error('[Process] Forcefully terminating after timeout.');
      process.exit(1);
    }, 10000);
  };

  process.on('SIGTERM', () => handleShutdown('SIGTERM'));
  process.on('SIGINT', () => handleShutdown('SIGINT'));
};

startServer().catch((error) => {
  logger.error('[Fatal Startup Error]', error);
  process.exit(1);
});
