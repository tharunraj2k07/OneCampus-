import { BaseRepository } from './base.repository';
import { FacultyProfile, IFacultyProfileDocument } from '../models/FacultyProfile';

export class FacultyProfileRepository extends BaseRepository<IFacultyProfileDocument> {
  constructor() {
    super(FacultyProfile);
  }

  async findByUserId(userId: string): Promise<IFacultyProfileDocument | null> {
    return this.model.findOne({ userId }).exec();
  }
}
