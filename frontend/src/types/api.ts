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
  nativeLanguage: CourseLanguage | null;
  targetLanguage: CourseLanguage | null;
  level: CourseLevel | null;
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
  nativeLanguage: CourseLanguage | null;
  targetLanguage: CourseLanguage | null;
  level: CourseLevel | null;
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
  nativeLanguage?: CourseLanguage;
  targetLanguage?: CourseLanguage;
  level?: CourseLevel;
}

export interface ChatUserSearchResponse {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  avatarUrl: string | null;
  role: UserRole;
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
export type EnrollmentStatus = "ACTIVE" | "COMPLETED" | "CANCELLED";

export type CourseLanguage =
  | "ENGLISH"
  | "FRENCH"
  | "SPANISH"
  | "GERMANY"
  | "ITALIAN"
  | "RUSSIAN"
  | "JAPANESE"
  | "KOREAN"
  | "ARABIC"
  | "BULGARIAN"
  | "UKRAINIAN"
  | "DUTCH"
  | "DANISH"
  | "PORTUGUESE"
  | "ARMENIAN"
  | "KAZAKH"
  | "BELARUSIAN"
  | "VIETNAMESE"
  | "SWEDISH"
  | "FINNISH"
  | "CHINESE"
  | "CROATIAN"
  | "HINDI"
  | "HUNGARIAN"
  | "POLISH";

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
  completed?: boolean;
  completedAt?: string | null;
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

export type ExerciseType = "MULTIPLE_CHOICE" | "TEXT_INPUT";

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
  attemptId: number;
  exerciseId: number;
  courseId: number;
  answer: string;
  correct: boolean;
  correctAnswer: string;
  explanation: string | null;
  attemptedAt: string;
  totalLessons: number;
  completedLessons: number;
  totalExercises: number;
  completedExercises: number;
  exerciseAttempts: number;
  totalItems: number;
  completedItems: number;
  progressPercentage: number;
}

export interface LessonCompletionResponse {
  lessonId: number;
  moduleId: number;
  courseId: number;
  completed: boolean;
  completedAt: string;
  totalLessons: number;
  completedLessons: number;
  totalExercises: number;
  completedExercises: number;
  exerciseAttempts: number;
  totalItems: number;
  completedItems: number;
  progressPercentage: number;
}

export interface CourseProgressResponse {
  courseId: number;
  enrollmentStatus: EnrollmentStatus;
  enrolledAt: string;
  completedAt: string | null;
  totalLessons: number;
  completedLessons: number;
  totalExercises: number;
  completedExercises: number;
  exerciseAttempts: number;
  totalItems: number;
  completedItems: number;
  progressPercentage: number;
}

// ===== Reports =====
export interface ReportCreateRequest {
  courseId: number;
  message: string;
}

export interface ReportResponse {
  id: number;
  userId: number;
  courseId: number;
  message: string;
  createdAt: string;
}

export interface EnrolledCourseResponse {
  enrollmentId: number;
  courseId: number;
  title: string;
  creatorName: string | null;
  creatorAvatarUrl: string | null;
  coverImageUrl: string | null;
  language: CourseLanguage;
  level: CourseLevel;
  price: number;
  rating: number;
  reviewsCount: number;
  totalStudents: number;
  enrollmentStatus: EnrollmentStatus;
  enrolledAt: string;
  completedAt: string | null;
  totalLessons: number;
  completedLessons: number;
  totalExercises: number;
  completedExercises: number;
  exerciseAttempts: number;
  totalItems: number;
  completedItems: number;
  progressPercentage: number;
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

// ===== Personal dictionaries =====
export interface DictionaryEntryResponse {
  id: number;
  sourceWord: string;
  targetWord: string;
  createdAt: string;
  updatedAt: string;
}

export interface DictionaryResponse {
  id: number;
  name: string;
  entries: DictionaryEntryResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface DictionaryCreateRequest {
  name: string;
}

export interface DictionaryEntryRequest {
  sourceWord: string;
  targetWord: string;
}

// ===== Helper labels =====
export const LANGUAGE_LABELS: Record<CourseLanguage, string> = {
  ENGLISH: "Английский язык",
  FRENCH: "Французский язык",
  SPANISH: "Испанский язык",
  GERMANY: "Немецкий язык",
  ITALIAN: "Итальянский язык",
  RUSSIAN: "Русский язык",
  JAPANESE: "Японский язык",
  KOREAN: "Корейский язык",
  ARABIC: "Арабский язык",
  BULGARIAN: "Болгарский язык",
  UKRAINIAN: "Украинский язык",
  DUTCH: "Голландский язык",
  DANISH: "Датский язык",
  PORTUGUESE: "Португальский язык",
  ARMENIAN: "Армянский язык",
  KAZAKH: "Казахский язык",
  BELARUSIAN: "Белорусский язык",
  VIETNAMESE: "Вьетнамский язык",
  SWEDISH: "Шведский язык",
  FINNISH: "Финский язык",
  CHINESE: "Китайский язык",
  CROATIAN: "Хорватский язык",
  HINDI: "Хинди",
  HUNGARIAN: "Венгерский язык",
  POLISH: "Польский язык",
};
