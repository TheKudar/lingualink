"use client";

import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { CourseSummaryResponse } from "@/types/api";

interface FavoriteCoursesState {
  coursesById: Record<number, CourseSummaryResponse>;
  toggleCourse: (course: CourseSummaryResponse) => void;
  isFavorite: (courseId: number) => boolean;
}

export const useFavoriteCoursesStore = create<FavoriteCoursesState>()(
  persist(
    (set, get) => ({
      coursesById: {},
      toggleCourse: (course) => {
        set((state) => {
          const next = { ...state.coursesById };

          if (next[course.id]) {
            delete next[course.id];
          } else {
            next[course.id] = course;
          }

          return { coursesById: next };
        });
      },
      isFavorite: (courseId) => Boolean(get().coursesById[courseId]),
    }),
    {
      name: "lingualink-favorite-courses",
    }
  )
);
