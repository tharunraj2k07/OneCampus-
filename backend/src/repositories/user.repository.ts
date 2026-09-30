import { BaseRepository } from './base.repository';
import { User, IUserDocument } from '../models/User';

export class UserRepository extends BaseRepository<IUserDocument> {
  constructor() {
    super(User);
  }

  async findByEmail(email: string, includePassword = false): Promise<IUserDocument | null> {
    const query = this.model.findOne({ email: email.toLowerCase().trim() });
    if (includePassword) {
      query.select('+passwordHash');
    }
    return query.exec();
  }

  async existsByEmail(email: string): Promise<boolean> {
    const count = await this.model.countDocuments({ email: email.toLowerCase().trim() });
    return count > 0;
  }
}
