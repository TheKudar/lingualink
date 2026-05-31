import { api } from "@/lib/api";
import type {
  CourseAnalyticsOverviewResponse,
  DropoffLessonAnalyticsResponse,
  QuestionAnalyticsResponse,
} from "@/types/api";

export const analyticsService = {
  overview: async (courseId: number): Promise<CourseAnalyticsOverviewResponse> =>
    (await api.get<CourseAnalyticsOverviewResponse>(`/api/analytics/course/${courseId}/overview`)).data,

  questions: async (courseId: number): Promise<QuestionAnalyticsResponse[]> =>
    (await api.get<QuestionAnalyticsResponse[]>(`/api/analytics/course/${courseId}/questions`)).data,

  dropoff: async (courseId: number): Promise<DropoffLessonAnalyticsResponse[]> =>
    (await api.get<DropoffLessonAnalyticsResponse[]>(`/api/analytics/course/${courseId}/dropoff`)).data,
};
