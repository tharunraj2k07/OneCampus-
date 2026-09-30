import mongoose from 'mongoose';
import { env } from './env';
import { logger } from '../utils/logger';

export const connectDatabase = async (): Promise<boolean> => {
  try {
    // Avoid re-connecting if already connected
    if (mongoose.connection.readyState === 1) {
      logger.info('MongoDB is already connected.');
      return true;
    }

    mongoose.connection.on('connected', () => {
      logger.info(`[MongoDB] Connected successfully to host: ${mongoose.connection.host}`);
    });

    mongoose.connection.on('error', (err) => {
      logger.error(`[MongoDB] Runtime connection error: ${err.message}`);
    });

    mongoose.connection.on('disconnected', () => {
      logger.warn('[MongoDB] Disconnected from database.');
    });

    logger.info(`[MongoDB] Attempting connection to: ${env.MONGODB_URI}`);
    await mongoose.connect(env.MONGODB_URI, {
      serverSelectionTimeoutMS: 5000,
      autoIndex: env.NODE_ENV !== 'production'
    });

    return true;
  } catch (error) {
    const errMessage = error instanceof Error ? error.message : String(error);
    logger.error(`[MongoDB] Initial connection failed: ${errMessage}`);
    return false;
  }
};

export const disconnectDatabase = async (): Promise<void> => {
  if (mongoose.connection.readyState !== 0) {
    await mongoose.disconnect();
    logger.info('[MongoDB] Connection closed gracefully.');
  }
};

export const isDatabaseConnected = (): boolean => {
  return mongoose.connection.readyState === 1;
};
