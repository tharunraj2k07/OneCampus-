import bcrypt from 'bcryptjs';

const SALT_ROUNDS = 10;

export class PasswordUtil {
  /**
   * Hashes a plain-text password using bcrypt.
   */
  static async hash(password: string): Promise<string> {
    const salt = await bcrypt.genSalt(SALT_ROUNDS);
    return bcrypt.hash(password, salt);
  }

  /**
   * Compares a plain-text password against a bcrypt hash.
   */
  static async compare(plainPassword: string, passwordHash: string): Promise<boolean> {
    return bcrypt.compare(plainPassword, passwordHash);
  }
}
