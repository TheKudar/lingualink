"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Search } from "lucide-react";
import { Navbar } from "@/components/layout/Navbar";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { CourseCard } from "@/components/courses/CourseCard";
import { CourseFiltersDialog } from "@/components/courses/CourseFilters";
import { courseService } from "@/services/courseService";
import type { CourseFilters } from "@/types/api";

export default function CoursesPage() {
  const [keyword, setKeyword] = useState("");
  const [filters, setFilters] = useState<CourseFilters>({});
  const [filtersOpen, setFiltersOpen] = useState(false);

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

        {/* Big search row */}
        <div className="mt-4 flex flex-wrap items-center gap-4">
          <div className="relative flex-1 min-w-72">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-muted-foreground" />
            <input
              type="text"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              placeholder="Введите запрос..."
              className="h-14 w-full rounded-xl bg-input-soft pl-12 pr-4 text-base placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>
          <Button variant="outline" size="lg" onClick={() => setFiltersOpen(true)}>
            Фильтры
          </Button>
          <Button size="lg" onClick={() => query.refetch()}>
            Поиск
          </Button>
        </div>

        {/* Results */}
        <h1 className="mt-8 text-3xl font-bold">Все курсы</h1>

        <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {query.isLoading &&
            Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="h-44 animate-pulse rounded-2xl bg-muted" />
            ))}
          {query.data?.content.map((course) => (
            <CourseCard key={course.id} course={course} />
          ))}
          {query.isError && (
            <p className="col-span-full text-sm text-muted-foreground">
              Не удалось загрузить курсы.
            </p>
          )}
          {query.data?.empty && (
            <p className="col-span-full text-sm text-muted-foreground">
              Курсы не найдены.
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
