import { api, unwrap } from "@/lib/api";
import type { ApiResponse, CourseLanguage } from "@/types/api";

export interface PracticeTurn {
  user: string;
  assistant: string;
}

export const languageToolsService = {
  chat: async (language: CourseLanguage, message: string, history: PracticeTurn[]) =>
    unwrap(await api.post<ApiResponse<string>>("/api/ai/chat", {
      language, message, history: history.slice(-6),
    })),

  translate: async (targetLanguage: CourseLanguage, text: string) =>
    unwrap(await api.post<ApiResponse<string>>("/api/ai/translate", { targetLanguage, text })),
};
