import { UserRepository } from '../repositories/user.repository';
import { StudentProfileRepository } from '../repositories/studentProfile.repository';
import { FacultyProfileRepository } from '../repositories/facultyProfile.repository';
import { PasswordUtil } from '../utils/password';
import { JwtUtil } from '../utils/jwt';
import { ApiError } from '../utils/apiError';
import { UserRole, IUser } from '../types/user.types';
import {
  IRegisterDTO,
  IStudentRegisterDTO,
  IFacultyRegisterDTO,
  ILoginDTO,
  IAuthResponseData,
  ICurrentUserResponseData,
  ISafeUser
} from '../types/auth.types';
import { IUserDocument } from '../models/User';

export class AuthService {
  private userRepository: UserRepository;
  private studentProfileRepository: StudentProfileRepository;
  private facultyProfileRepository: FacultyProfileRepository;

  constructor(
    userRepository: UserRepository = new UserRepository(),
    studentProfileRepository: StudentProfileRepository = new StudentProfileRepository(),
    facultyProfileRepository: FacultyProfileRepository = new FacultyProfileRepository()
  ) {
    this.userRepository = userRepository;
    this.studentProfileRepository = studentProfileRepository;
    this.facultyProfileRepository = facultyProfileRepository;
  }

  private toSafeUser(user: IUserDocument): ISafeUser {
    return {
      id: user._id.toString(),
      name: user.name,
      email: user.email,
      role: user.role,
      profileImage: user.profileImage,
      isActive: user.isActive,
      createdAt: user.createdAt,
      updatedAt: user.updatedAt
    };
  }

  async register(dto: IRegisterDTO): Promise<IAuthResponseData> {
    const email = dto.email.toLowerCase().trim();
    const role = dto.role || UserRole.STUDENT;

    // 1. Check if email already registered
    const emailExists = await this.userRepository.existsByEmail(email);
    if (emailExists) {
      throw ApiError.conflict('An account with this email address is already registered', 'EMAIL_ALREADY_EXISTS');
    }

    // 2. Role specific pre-checks
    if (role === UserRole.STUDENT) {
      const studentDto = dto as IStudentRegisterDTO;
      const regNumber = studentDto.registerNumber.toUpperCase().trim();
      const regExists = await this.studentProfileRepository.existsByRegisterNumber(regNumber);
      if (regExists) {
        throw ApiError.conflict('A student account with this register number is already registered', 'REGISTER_NUMBER_EXISTS');
      }
    }

    // 3. Hash password securely
    const passwordHash = await PasswordUtil.hash(dto.password);

    // 4. Create User entity
    const newUser = await this.userRepository.create({
      name: dto.name.trim(),
      email,
      passwordHash,
      role,
      isActive: true
    } as Partial<IUserDocument>);

    // 5. Create Profile entity based on role
    let profile = null;

    if (role === UserRole.STUDENT) {
      const studentDto = dto as IStudentRegisterDTO;
      let completion = 70;
      if (studentDto.interests && studentDto.interests.length > 0) completion += 15;
      if (studentDto.cgpa !== undefined && studentDto.cgpa !== null) completion += 15;

      profile = await this.studentProfileRepository.create({
        userId: newUser._id,
        department: studentDto.department.toUpperCase().trim(),
        year: studentDto.year,
        section: studentDto.section.toUpperCase().trim(),
        registerNumber: studentDto.registerNumber.toUpperCase().trim(),
        interests: studentDto.interests || [],
        profileCompletion: Math.min(100, completion),
        cgpa: studentDto.cgpa
      });
    } else if (role === UserRole.FACULTY) {
      const facultyDto = dto as IFacultyRegisterDTO;
      profile = await this.facultyProfileRepository.create({
        userId: newUser._id,
        department: facultyDto.department.toUpperCase().trim(),
        designation: facultyDto.designation.trim()
      });
    }

    // 6. Generate JWT Token
    const token = JwtUtil.generateToken({
      userId: newUser._id.toString(),
      email: newUser.email,
      role: newUser.role
    });

    return {
      token,
      user: this.toSafeUser(newUser),
      profile
    };
  }

  async login(dto: ILoginDTO): Promise<IAuthResponseData> {
    const email = dto.email.toLowerCase().trim();

    // 1. Find user by email including hidden passwordHash
    const user = await this.userRepository.findByEmail(email, true);
    if (!user) {
      throw ApiError.unauthorized('Invalid email or password', 'INVALID_CREDENTIALS');
    }

    // 2. Check account status
    if (!user.isActive) {
      throw ApiError.forbidden('Your account has been deactivated. Please contact college administration.', 'ACCOUNT_DEACTIVATED');
    }

    // 3. Verify password
    const isPasswordValid = await PasswordUtil.compare(dto.password, user.passwordHash);
    if (!isPasswordValid) {
      throw ApiError.unauthorized('Invalid email or password', 'INVALID_CREDENTIALS');
    }

    // 4. Fetch corresponding profile
    let profile = null;
    if (user.role === UserRole.STUDENT) {
      profile = await this.studentProfileRepository.findByUserId(user._id.toString());
    } else if (user.role === UserRole.FACULTY) {
      profile = await this.facultyProfileRepository.findByUserId(user._id.toString());
    }

    // 5. Generate JWT Token
    const token = JwtUtil.generateToken({
      userId: user._id.toString(),
      email: user.email,
      role: user.role
    });

    return {
      token,
      user: this.toSafeUser(user),
      profile
    };
  }

  async getCurrentUser(userId: string): Promise<ICurrentUserResponseData> {
    const user = await this.userRepository.findById(userId);
    if (!user) {
      throw ApiError.notFound('User account not found', 'USER_NOT_FOUND');
    }

    let profile = null;
    if (user.role === UserRole.STUDENT) {
      profile = await this.studentProfileRepository.findByUserId(user._id.toString());
    } else if (user.role === UserRole.FACULTY) {
      profile = await this.facultyProfileRepository.findByUserId(user._id.toString());
    }

    return {
      user: this.toSafeUser(user),
      profile
    };
  }
}
