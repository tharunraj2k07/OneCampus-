import { Types } from 'mongoose';

export type DevicePlatform = 'ANDROID' | 'IOS' | 'WEB';

export interface IDeviceToken {
  userId: Types.ObjectId;
  token: string;
  platform: DevicePlatform;
  deviceName?: string;
  lastUsedAt: Date;
  createdAt: Date;
}

export interface IRegisterDeviceTokenDTO {
  token: string;
  platform?: DevicePlatform;
  deviceName?: string;
}
