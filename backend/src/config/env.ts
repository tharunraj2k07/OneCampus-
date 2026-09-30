import dotenv from 'dotenv';
import path from 'path';

// Load .env file from backend root or parent if present
dotenv.config({ path: path.resolve(process.cwd(), '.env') });

export interface IEnvironmentConfig {
  PORT: number;
  NODE_ENV: 'development' | 'production' | 'test';
  MONGODB_URI: string;
  CORS_ORIGIN: string | string[];
  JWT_SECRET: string;
  JWT_EXPIRES_IN: string;
  GEMINI_API_KEY?: string;
  FCM_CONFIGURATION?: string;
}

const parseCorsOrigin = (originStr?: string): string | string[] => {
  if (!originStr || originStr === '*') return '*';
  if (originStr.includes(',')) {
    return originStr.split(',').map((item) => item.trim());
  }
  return originStr.trim();
};

export const env: IEnvironmentConfig = {
  PORT: parseInt(process.env.PORT || '5000', 10),
  NODE_ENV: (process.env.NODE_ENV as 'development' | 'production' | 'test') || 'development',
  MONGODB_URI: process.env.MONGODB_URI || 'mongodb://localhost:27017/onecampus_ai',
  CORS_ORIGIN: parseCorsOrigin(process.env.CORS_ORIGIN),
  JWT_SECRET: process.env.JWT_SECRET || 'onecampus_ai_jwt_dev_secret_key_super_secure',
  JWT_EXPIRES_IN: process.env.JWT_EXPIRES_IN || '7d',
  GEMINI_API_KEY: process.env.GEMINI_API_KEY,
  FCM_CONFIGURATION: process.env.FCM_CONFIGURATION
};
