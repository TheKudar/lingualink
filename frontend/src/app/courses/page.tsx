"use client";

import { useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Search } from "lucide-react";
import { Navbar } from "@/components/layout/Navbar";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { CourseCard } from "@/components/courses/CourseCard";
import { CourseFiltersDialog } from "@/components/courses/CourseFilters";
import { courseService } from "@/services/courseService";
import { useFavoriteCoursesStore } from "@/lib/favorite-courses-store";
import { cn } from "@/lib/utils";
import type { CourseFilters } from "@/types/api";

export default function CoursesPage() {
  const [keyword, setKeyword] = useState("");
  const [filters, setFilters] = useState<CourseFilters>({});
  const [filtersOpen, setFiltersOpen] = useState(false);
  const [activeSection, setActiveSection] = useState<"all" | "favorites">("all");
  const favoriteCoursesById = useFavoriteCoursesStore((state) => state.coursesById);
  const favoriteCourses = useMemo(() => Object.values(favoriteCoursesById), [favoriteCoursesById]);

  // Top filter bar (mini): language placeholder + free-only
  const [freeOnly, setFreeOnly] = useState(false);

  const query = useQuery({
    queryKey: ["courses", "list", { ...filters, keyword, freeOnly }],
    queryFn: () =>
      courseService.listPublished({ ...filters, keyword, freeOnly }, { page: 0, size: 24 }),
  });

  const submit = (e: React.FormEvent) => {
    e.preventDefault();
    query.refetch();
  };

  return (
    <>
      <Navbar />

      <main className="mx-auto max-w-7xl px-6 py-6">
        {/* Top compact filter */}
        <form
          onSubmit={submit}
          className="flex flex-wrap items-center gap-4 rounded-2xl bg-card/40 p-3 ring-1 ring-border"
        >
          <div className="relative flex-1 min-w-64">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <input
              type="text"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="Название курса, автор или предмет"
              className="h-10 w-full rounded-lg bg-white pl-10 pr-3 text-sm placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>

          <button
            type="button"
            onClick={() => setFiltersOpen(true)}
            className="h-10 rounded-lg bg-white px-3 text-sm hover:bg-muted"
          >
            На любом языке ▾
          </button>

          <label className="flex items-center gap-2 text-sm">
            <Checkbox
              checked={freeOnly}
              onCheckedChange={(v) => setFreeOnly(v === true)}
            />
            Бесплатные
          </label>

          <Button type="submit" variant="success" size="sm">
            Искать
          </Button>
        </form>

        {/* Results */}
        <div className="mt-8 flex flex-wrap items-center gap-6">
          <button
            type="button"
            onClick={() => setActiveSection("all")}
            className={cn(
              "text-3xl font-bold transition-colors",
              activeSection === "all" ? "text-foreground" : "text-foreground/45 hover:text-foreground"
            )}
          >
            Все курсы
          </button>
          <button
            type="button"
            onClick={() => setActiveSection("favorites")}
            className={cn(
              "text-3xl font-bold transition-colors",
              activeSection === "favorites" ? "text-foreground" : "text-foreground/45 hover:text-foreground"
            )}
          >
            Избранные курсы
          </button>
        </div>

        <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {activeSection === "all" && query.isLoading &&
            Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="h-44 animate-pulse rounded-2xl bg-muted" />
            ))}
          {activeSection === "all" && query.data?.content.map((course) => (
            <CourseCard key={course.id} course={course} />
          ))}
          {activeSection === "favorites" && favoriteCourses.map((course) => (
            <CourseCard key={course.id} course={course} />
          ))}
          {activeSection === "all" && query.isError && (
            <p className="col-span-full text-sm text-muted-foreground">
              Не удалось загрузить курсы.
            </p>
          )}
          {activeSection === "all" && query.data?.empty && (
            <p className="col-span-full text-sm text-muted-foreground">
              Курсы не найдены.
            </p>
          )}
          {activeSection === "favorites" && favoriteCourses.length === 0 && (
            <p className="col-span-full text-sm text-muted-foreground">
              В избранном пока нет курсов.
            </p>
          )}
        </div>
      </main>

      <CourseFiltersDialog
        open={filtersOpen}
        onOpenChange={setFiltersOpen}
        initial={filters}
        onApply={setFilters}
      />
    </>
  );
}
