import { BaseRepository } from './base.repository';
import { StudentProfile, IStudentProfileDocument } from '../models/StudentProfile';

export class StudentProfileRepository extends BaseRepository<IStudentProfileDocument> {
  constructor() {
    super(StudentProfile);
  }

  async findByUserId(userId: string): Promise<IStudentProfileDocument | null> {
    return this.model.findOne({ userId }).exec();
  }

  async existsByRegisterNumber(registerNumber: string): Promise<boolean> {
    const count = await this.model.countDocuments({ registerNumber: registerNumber.toUpperCase().trim() });
    return count > 0;
  }
}
