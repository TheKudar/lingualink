// API types matching backend DTOs (com.lingualink.*)

export interface ApiResponse<T> {
  success: boolean;
  data: T | null;
  message: string | null;
}

// ===== Auth =====
export type UserRole = "STUDENT" | "CREATOR" | "ADMIN";
export type UserStatus = "ACTIVE" | "BLOCKED";

export interface RegisterRequest {
  email: string;
  username: string;
  firstName: string;
  lastName: string;
  password: string;
  role?: UserRole;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
}

export interface UserMeResponse {
  id: number;
  email: string;
  role: UserRole;
  status: UserStatus;
}

// ===== User =====
export interface UserDto {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  status: UserStatus;
  avatarUrl: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface UserUpdateRequest {
  firstName?: string;
  lastName?: string;
  avatarUrl?: string;
}

export interface ChatUserSearchResponse {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  avatarUrl: string | null;
}

export interface PublicUserProfileResponse {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  avatarUrl: string | null;
  role: UserRole;
}

// ===== Courses =====
export type CourseLevel = "A1" | "A2" | "B1" | "B2" | "C1" | "C2";
export type CourseStatus = "DRAFT" | "PENDING_REVIEW" | "PUBLISHED" | "REJECTED" | "ARCHIVED";

export type CourseLanguage =
  | "ENGLISH"
  | "FRENCH"
  | "GERMAN"
  | "CHINESE"
  | "SPANISH"
  | "JAPANESE"
  | "ITALIAN"
  | "RUSSIAN"
  | "KAZAKH"
  | "FINNISH"
  | "SERBIAN";

export interface CourseSummaryResponse {
  id: number;
  title: string;
  language: CourseLanguage;
  level: CourseLevel;
  price: number;
  rating: number;
  reviewsCount: number;
  totalStudents: number;
  coverImageUrl: string | null;
  createdAt: string;
}

export interface CourseResponse {
  id: number;
  title: string;
  description: string;
  language: CourseLanguage;
  level: CourseLevel;
  creatorId: number;
  creatorName: string;
  creatorAvatarUrl: string | null;
  price: number;
  rating: number;
  reviewsCount: number;
  totalStudents: number;
  coverImageUrl: string | null;
  status: CourseStatus;
  rejectionReason: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CourseFilters {
  keyword?: string;
  language?: CourseLanguage | "ANY";
  level?: CourseLevel;
  minPrice?: number;
  maxPrice?: number;
  minRating?: number;
  freeOnly?: boolean;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // current page (0-indexed)
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// ===== Course mutations =====
export interface CourseCreateRequest {
  title: string;
  description: string;
  language: CourseLanguage;
  level: CourseLevel;
  price: number;
  coverImageUrl?: string | null;
}

export type CourseUpdateRequest = Partial<CourseCreateRequest>;

// ===== Modules / Lessons / Exercises =====
export interface ModuleResponse {
  id: number;
  title: string;
  description: string | null;
  orderIndex: number;
  lessons: LessonResponse[];
}

export interface ModuleCreateRequest {
  title: string;
  description?: string;
  orderIndex: number;
}

export interface LessonResponse {
  id: number;
  title: string;
  content: string;
  orderIndex: number;
}

export interface LessonCreateRequest {
  title: string;
  content?: string;
  orderIndex: number;
}

export interface ExerciseCreateRequest {
  type: ExerciseType;
  question: string;
  options?: string[];
  correctAnswer: string;
  explanation?: string;
  orderIndex: number;
}

export type ExerciseType = "MULTIPLE_CHOICE" | "SHORT_ANSWER" | "FILL_IN_THE_BLANK";

export interface ExerciseResponse {
  id: number;
  question: string;
  type: ExerciseType;
  options: string[] | null;
  correctAnswer?: string;
  explanation?: string | null;
  orderIndex: number;
  lessonId: number;
}

export interface ExerciseAttemptResponse {
  id: number;
  exerciseId: number;
  answer: string;
  correct: boolean;
  feedback: string | null;
  attemptedAt: string;
}

export interface CourseProgressResponse {
  courseId: number;
  totalLessons: number;
  completedLessons: number;
  progressPercent: number;
}

export interface EnrolledCourseResponse {
  id: number;
  courseId: number;
  courseTitle: string;
  coverImageUrl: string | null;
  language: CourseLanguage;
  level: CourseLevel;
  enrolledAt: string;
  progressPercent: number;
}

// ===== Chat =====
export interface ConversationResponse {
  id: number;
  participantId: number;
  participantUsername: string;
  participantFirstName: string;
  participantLastName: string;
  participantAvatarUrl: string | null;
  lastMessageAt: string | null;
}

export interface MessageResponse {
  id: number;
  conversationId: number;
  senderId: number;
  content: string;
  sentAt: string;
}

// ===== Reading materials =====
export type ReadingLevel = CourseLevel;

export interface ReadingMaterialResponse {
  id: number;
  title: string;
  content: string;
  language: CourseLanguage;
  level: ReadingLevel;
  authorName: string | null;
  createdAt: string;
}

// ===== Helper labels =====
export const LANGUAGE_LABELS: Record<CourseLanguage, string> = {
  ENGLISH: "Английский язык",
  FRENCH: "Французский язык",
  GERMAN: "Немецкий язык",
  CHINESE: "Китайский язык",
  SPANISH: "Испанский язык",
  JAPANESE: "Японский язык",
  ITALIAN: "Итальянский язык",
  RUSSIAN: "Русский язык",
  KAZAKH: "Казахский язык",
  FINNISH: "Финский язык",
  SERBIAN: "Сербский язык",
};
