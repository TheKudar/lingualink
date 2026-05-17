import { api } from "@/lib/api";
import type {
  ChatUserSearchResponse,
  CourseLanguage,
  CourseLevel,
  PublicUserProfileResponse,
  UserDto,
  UserUpdateRequest,
} from "@/types/api";

interface UserSearchParams {
  query?: string;
  language?: CourseLanguage;
  level?: CourseLevel;
}

export const userService = {
  getMe: async (): Promise<UserDto> => (await api.get<UserDto>("/api/users/me")).data,

  updateMe: async (request: UserUpdateRequest): Promise<UserDto> =>
    (await api.patch<UserDto>("/api/users/me", request)).data,

  uploadAvatar: async (file: File): Promise<UserDto> => {
    const formData = new FormData();
    formData.append("file", file);
    return (
      await api.post<UserDto>("/api/users/me/avatar", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      })
    ).data;
  },

  search: async (params: UserSearchParams = {}): Promise<ChatUserSearchResponse[]> =>
    (
      await api.get<ChatUserSearchResponse[]>("/api/users/search", {
        params: { ...params, excludeCurrentUser: true },
      })
    ).data,

  getById: async (id: number): Promise<PublicUserProfileResponse> =>
    (await api.get<PublicUserProfileResponse>(`/api/users/${id}`)).data,

  // ===== Admin =====
  block: async (id: number): Promise<UserDto> =>
    (await api.post<UserDto>(`/api/users/${id}/block`)).data,

  unblock: async (id: number): Promise<UserDto> =>
    (await api.post<UserDto>(`/api/users/${id}/unblock`)).data,
};
