import { api } from "@/lib/api";
import type {
  CourseCreateRequest,
  CourseFilters,
  CourseProgressResponse,
  CourseResponse,
  CourseSummaryResponse,
  CourseUpdateRequest,
  EnrolledCourseResponse,
  ExerciseAttemptResponse,
  ExerciseCreateRequest,
  ExerciseResponse,
  LessonCreateRequest,
  LessonResponse,
  ModuleCreateRequest,
  ModuleResponse,
  PageResponse,
} from "@/types/api";

interface PageParams {
  page?: number;
  size?: number;
  sort?: string;
}

const buildCoursesParams = (filters?: CourseFilters, page?: PageParams) => {
  const params: Record<string, string | number | boolean | undefined> = {
    page: page?.page ?? 0,
    size: page?.size ?? 12,
  };
  if (page?.sort) params.sort = page.sort;
  if (filters?.keyword) params.keyword = filters.keyword;
  if (filters?.language && filters.language !== "ANY") params.language = filters.language;
  if (filters?.level) params.level = filters.level;
  if (filters?.minRating) params.minRating = filters.minRating;
  if (filters?.freeOnly) {
    params.maxPrice = 0;
  } else {
    if (filters?.minPrice != null) params.minPrice = filters.minPrice;
    if (filters?.maxPrice != null) params.maxPrice = filters.maxPrice;
  }
  return params;
};

export const courseService = {
  listPublished: async (
    filters?: CourseFilters,
    page?: PageParams
  ): Promise<PageResponse<CourseSummaryResponse>> =>
    (
      await api.get<PageResponse<CourseSummaryResponse>>("/api/courses/published", {
        params: buildCoursesParams(filters, page),
      })
    ).data,

  getById: async (id: number): Promise<CourseResponse> =>
    (await api.get<CourseResponse>(`/api/courses/${id}`)).data,

  // ===== Creator endpoints =====
  myCourses: async (page = 0, size = 20): Promise<PageResponse<CourseSummaryResponse>> =>
    (
      await api.get<PageResponse<CourseSummaryResponse>>("/api/courses/my-courses", {
        params: { page, size, sort: "createdAt,desc" },
      })
    ).data,

  create: async (request: CourseCreateRequest): Promise<CourseResponse> =>
    (await api.post<CourseResponse>("/api/courses", request)).data,

  update: async (id: number, request: CourseUpdateRequest): Promise<CourseResponse> =>
    (await api.put<CourseResponse>(`/api/courses/${id}`, request)).data,

  uploadCover: async (id: number, file: File): Promise<CourseResponse> => {
    const formData = new FormData();
    formData.append("file", file);
    return (
      await api.post<CourseResponse>(`/api/courses/${id}/cover`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
      })
    ).data;
  },

  remove: async (id: number): Promise<void> => {
    await api.delete(`/api/courses/${id}`);
  },

  submitForReview: async (id: number): Promise<CourseResponse> =>
    (await api.post<CourseResponse>(`/api/courses/${id}/submit-for-review`)).data,

  archive: async (id: number): Promise<CourseResponse> =>
    (await api.post<CourseResponse>(`/api/courses/${id}/archive`)).data,

  // ===== Admin moderation =====
  pendingForModeration: async (
    page = 0,
    size = 20
  ): Promise<PageResponse<CourseResponse>> =>
    (
      await api.get<PageResponse<CourseResponse>>("/api/courses/moderation/pending", {
        params: { page, size, sort: "createdAt,asc" },
      })
    ).data,

  approve: async (id: number): Promise<CourseResponse> =>
    (await api.post<CourseResponse>(`/api/courses/${id}/approve`)).data,

  reject: async (id: number, reason: string): Promise<CourseResponse> =>
    (await api.post<CourseResponse>(`/api/courses/${id}/reject`, { reason })).data,

  enroll: async (id: number): Promise<EnrolledCourseResponse> =>
    (await api.post<EnrolledCourseResponse>(`/api/courses/${id}/enroll`)).data,

  myEnrollments: async (page?: PageParams): Promise<PageResponse<EnrolledCourseResponse>> =>
    (
      await api.get<PageResponse<EnrolledCourseResponse>>("/api/courses/my-enrollments", {
        params: { page: page?.page ?? 0, size: page?.size ?? 20 },
      })
    ).data,

  getProgress: async (id: number): Promise<CourseProgressResponse> =>
    (await api.get<CourseProgressResponse>(`/api/courses/${id}/progress`)).data,

  // Modules
  listModules: async (courseId: number): Promise<ModuleResponse[]> =>
    (await api.get<ModuleResponse[]>(`/api/courses/${courseId}/modules`)).data,

  createModule: async (courseId: number, request: ModuleCreateRequest): Promise<ModuleResponse> =>
    (await api.post<ModuleResponse>(`/api/courses/${courseId}/modules`, request)).data,

  updateModule: async (
    courseId: number,
    moduleId: number,
    request: ModuleCreateRequest
  ): Promise<ModuleResponse> =>
    (
      await api.put<ModuleResponse>(
        `/api/courses/${courseId}/modules/${moduleId}`,
        request
      )
    ).data,

  removeModule: async (courseId: number, moduleId: number): Promise<void> => {
    await api.delete(`/api/courses/${courseId}/modules/${moduleId}`);
  },

  // Lessons
  getLesson: async (
    courseId: number,
    moduleId: number,
    lessonId: number
  ): Promise<LessonResponse> =>
    (
      await api.get<LessonResponse>(
        `/api/courses/${courseId}/modules/${moduleId}/lessons/${lessonId}`
      )
    ).data,

  createLesson: async (
    courseId: number,
    moduleId: number,
    request: LessonCreateRequest
  ): Promise<LessonResponse> =>
    (
      await api.post<LessonResponse>(
        `/api/courses/${courseId}/modules/${moduleId}/lessons`,
        request
      )
    ).data,

  updateLesson: async (
    courseId: number,
    moduleId: number,
    lessonId: number,
    request: LessonCreateRequest
  ): Promise<LessonResponse> =>
    (
      await api.put<LessonResponse>(
        `/api/courses/${courseId}/modules/${moduleId}/lessons/${lessonId}`,
        request
      )
    ).data,

  removeLesson: async (
    courseId: number,
    moduleId: number,
    lessonId: number
  ): Promise<void> => {
    await api.delete(
      `/api/courses/${courseId}/modules/${moduleId}/lessons/${lessonId}`
    );
  },

  createExercise: async (
    courseId: number,
    moduleId: number,
    lessonId: number,
    request: ExerciseCreateRequest
  ): Promise<ExerciseResponse> =>
    (
      await api.post<ExerciseResponse>(
        `/api/courses/${courseId}/modules/${moduleId}/lessons/${lessonId}/exercises`,
        request
      )
    ).data,

  removeExercise: async (
    courseId: number,
    moduleId: number,
    lessonId: number,
    exerciseId: number
  ): Promise<void> => {
    await api.delete(
      `/api/courses/${courseId}/modules/${moduleId}/lessons/${lessonId}/exercises/${exerciseId}`
    );
  },

  completeLesson: async (
    courseId: number,
    moduleId: number,
    lessonId: number
  ): Promise<void> => {
    await api.post(
      `/api/courses/${courseId}/modules/${moduleId}/lessons/${lessonId}/complete`
    );
  },

  // Exercises
  listExercises: async (
    courseId: number,
    moduleId: number,
    lessonId: number
  ): Promise<ExerciseResponse[]> =>
    (
      await api.get<ExerciseResponse[]>(
        `/api/courses/${courseId}/modules/${moduleId}/lessons/${lessonId}/exercises`
      )
    ).data,

  submitExercise: async (
    courseId: number,
    moduleId: number,
    lessonId: number,
    exerciseId: number,
    answer: string
  ): Promise<ExerciseAttemptResponse> =>
    (
      await api.post<ExerciseAttemptResponse>(
        `/api/courses/${courseId}/modules/${moduleId}/lessons/${lessonId}/exercises/${exerciseId}/submit`,
        { answer }
      )
    ).data,
};
