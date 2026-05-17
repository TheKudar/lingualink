import { api } from "@/lib/api";
import type { ReportCreateRequest, ReportResponse } from "@/types/api";

export const reportService = {
  create: async (request: ReportCreateRequest): Promise<ReportResponse> =>
    (await api.post<ReportResponse>("/api/reports", request)).data,

  listAll: async (): Promise<ReportResponse[]> =>
    (await api.get<ReportResponse[]>("/api/reports")).data,

  banCourse: async (reportId: number): Promise<void> => {
    await api.post(`/api/reports/${reportId}/ban-course`);
  },

  keepCourse: async (reportId: number): Promise<void> => {
    await api.post(`/api/reports/${reportId}/keep-course`);
  },
};
