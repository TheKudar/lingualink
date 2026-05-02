import { api, unwrap } from "@/lib/api";
import type {
  ApiResponse,
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  UserMeResponse,
} from "@/types/api";

export const authService = {
  register: async (request: RegisterRequest): Promise<AuthResponse> =>
    unwrap(await api.post<ApiResponse<AuthResponse>>("/api/auth/register", request)),

  login: async (request: LoginRequest): Promise<AuthResponse> =>
    unwrap(await api.post<ApiResponse<AuthResponse>>("/api/auth/login", request)),

  me: async (): Promise<UserMeResponse> =>
    unwrap(await api.get<ApiResponse<UserMeResponse>>("/api/auth/me")),
};
