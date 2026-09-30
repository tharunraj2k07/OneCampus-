import { cert, initializeApp, getApps, App } from 'firebase-admin/app';
import { getMessaging } from 'firebase-admin/messaging';

let firebaseApp: App | null = null;

export function initializeFirebaseAdmin(): App | null {
  if (firebaseApp) {
    return firebaseApp;
  }

  const existingApps = getApps();
  if (existingApps.length > 0) {
    firebaseApp = existingApps[0];
    return firebaseApp;
  }

  const projectId = process.env.FIREBASE_PROJECT_ID;
  const clientEmail = process.env.FIREBASE_CLIENT_EMAIL;
  let privateKey = process.env.FIREBASE_PRIVATE_KEY;

  if (projectId && clientEmail && privateKey) {
    try {
      if (privateKey.includes('\\n')) {
        privateKey = privateKey.replace(/\\n/g, '\n');
      }

      firebaseApp = initializeApp({
        credential: cert({
          projectId,
          clientEmail,
          privateKey
        })
      });
      console.log('✅ [Firebase Admin] Initialized successfully for project:', projectId);
      return firebaseApp;
    } catch (error) {
      console.warn('⚠️ [Firebase Admin] Failed to initialize credentials, running in simulation mode:', error);
      return null;
    }
  } else {
    console.log('ℹ️ [Firebase Admin] Credentials not configured. FCM running in simulation/mock mode.');
    return null;
  }
}

export interface IFcmPayload {
  title: string;
  body: string;
  data?: Record<string, string>;
  priority?: 'high' | 'normal';
}

export async function sendMulticastPushNotification(
  tokens: string[],
  payload: IFcmPayload
): Promise<{ successCount: number; failureCount: number }> {
  if (!tokens || tokens.length === 0) {
    return { successCount: 0, failureCount: 0 };
  }

  const app = initializeFirebaseAdmin();
  if (!app) {
    console.log(`[FCM Simulation] Sent notification to ${tokens.length} tokens: "${payload.title}" - ${payload.body}`);
    return { successCount: tokens.length, failureCount: 0 };
  }

  try {
    const messaging = getMessaging(app);
    const response = await messaging.sendEachForMulticast({
      tokens,
      notification: {
        title: payload.title,
        body: payload.body
      },
      data: payload.data || {},
      android: {
        priority: payload.priority === 'high' ? 'high' : 'normal',
        notification: {
          channelId: payload.priority === 'high' ? 'onecampus_critical' : 'onecampus_general'
        }
      }
    });

    return {
      successCount: response.successCount,
      failureCount: response.failureCount
    };
  } catch (error) {
    console.error('❌ [FCM] Error sending multicast notification:', error);
    return { successCount: 0, failureCount: tokens.length };
  }
}
