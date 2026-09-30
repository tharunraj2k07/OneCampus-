import { env } from '../config/env';
import { isDatabaseConnected } from '../config/db';

export interface IHealthStatus {
  status: 'healthy' | 'degraded';
  environment: string;
  timestamp: string;
  uptimeSeconds: number;
  database: {
    connected: boolean;
    status: string;
  };
  version: string;
}

export class HealthService {
  public static getSystemHealth(): IHealthStatus {
    const dbConnected = isDatabaseConnected();

    return {
      status: dbConnected ? 'healthy' : 'degraded',
      environment: env.NODE_ENV,
      timestamp: new Date().toISOString(),
      uptimeSeconds: Math.floor(process.uptime()),
      database: {
        connected: dbConnected,
        status: dbConnected ? 'CONNECTED' : 'DISCONNECTED_OR_CONNECTING'
      },
      version: '1.0.0'
    };
  }
}
