import mongoose, { Schema, Document, Model } from 'mongoose';
import { IBookmark } from '../types/bookmark.types';

export interface IBookmarkDocument extends IBookmark, Document {}

const bookmarkSchema = new Schema<IBookmarkDocument>(
  {
    userId: {
      type: Schema.Types.ObjectId,
      ref: 'User',
      required: [true, 'User ID is required'],
      index: true
    },
    announcementId: {
      type: Schema.Types.ObjectId,
      ref: 'Announcement',
      required: [true, 'Announcement ID is required'],
      index: true
    }
  },
  {
    timestamps: { createdAt: true, updatedAt: false }
  }
);

// Compound unique index ensuring a user cannot bookmark the same announcement twice
bookmarkSchema.index({ userId: 1, announcementId: 1 }, { unique: true });

export const Bookmark: Model<IBookmarkDocument> = mongoose.model<IBookmarkDocument>(
  'Bookmark',
  bookmarkSchema
);
