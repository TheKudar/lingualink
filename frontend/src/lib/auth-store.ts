"use client";

import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { UserDto } from "@/types/api";
import { clearStoredToken, setStoredToken } from "./api";

interface AuthState {
  token: string | null;
  user: UserDto | null;
  isHydrated: boolean;
  setAuth: (token: string, user: UserDto) => void;
  setUser: (user: UserDto) => void;
  logout: () => void;
  setHydrated: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      user: null,
      isHydrated: false,
      setAuth: (token, user) => {
        setStoredToken(token);
        set({ token, user });
      },
      setUser: (user) => set({ user }),
      logout: () => {
        clearStoredToken();
        set({ token: null, user: null });
      },
      setHydrated: () => set({ isHydrated: true }),
    }),
    {
      name: "lingualink-auth",
      onRehydrateStorage: () => (state) => {
        state?.setHydrated();
      },
    }
  )
);

export const isAuthenticated = (state: AuthState) => Boolean(state.token);
