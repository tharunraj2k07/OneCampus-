import { Types } from 'mongoose';

export interface IBookmark {
  userId: Types.ObjectId;
  announcementId: Types.ObjectId;
  createdAt: Date;
}
