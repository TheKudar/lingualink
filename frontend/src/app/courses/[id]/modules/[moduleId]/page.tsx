"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { ArrowLeft } from "lucide-react";
import { Navbar } from "@/components/layout/Navbar";
import { courseService } from "@/services/courseService";
import { extractErrorMessage } from "@/lib/api";

export default function CourseModulePage() {
  const params = useParams<{ id: string; moduleId: string }>();
  const courseId = Number(params.id);
  const moduleId = Number(params.moduleId);

  const courseQuery = useQuery({
    queryKey: ["course", courseId],
    queryFn: () => courseService.getById(courseId),
    enabled: Number.isFinite(courseId),
  });

  const modulesQuery = useQuery({
    queryKey: ["course", courseId, "modules"],
    queryFn: () => courseService.listModules(courseId),
    enabled: Number.isFinite(courseId),
  });

  const courseModule = modulesQuery.data?.find((m) => m.id === moduleId);

  return (
    <>
      <Navbar />
      <main className="mx-auto max-w-5xl px-6 py-6">
        <Link
          href={`/courses/${courseId}`}
          className="inline-flex items-center gap-2 text-sm text-foreground/70 hover:text-foreground"
        >
          <ArrowLeft className="h-4 w-4" /> К курсу
        </Link>

        <section className="mt-4 rounded-3xl bg-white p-8 shadow-sm">
          {modulesQuery.isLoading && (
            <div className="space-y-4">
              <div className="h-8 w-2/3 animate-pulse rounded bg-muted" />
              <div className="h-12 animate-pulse rounded-lg bg-muted" />
              <div className="h-12 animate-pulse rounded-lg bg-muted" />
            </div>
          )}

          {modulesQuery.isError && (
            <p className="text-sm text-destructive">
              {extractErrorMessage(modulesQuery.error)}
            </p>
          )}

          {modulesQuery.data && !courseModule && (
            <p className="text-sm text-muted-foreground">Модуль не найден.</p>
          )}

          {courseModule && (
            <>
              <p className="text-sm text-foreground/60">
                {courseQuery.data?.title ?? "Курс"}
              </p>
              <h1 className="mt-2 text-2xl font-semibold leading-tight">
                {courseModule.title}
              </h1>
              {courseModule.description && (
                <p className="mt-3 max-w-3xl text-base text-foreground/75">
                  {courseModule.description}
                </p>
              )}

              <div className="mt-8 flex flex-col gap-3">
                {courseModule.lessons?.map((lesson, idx) => (
                  <Link
                    key={lesson.id}
                    href={`/courses/${courseId}/modules/${moduleId}/lessons/${lesson.id}`}
                    className="block rounded-lg bg-muted px-5 py-3 text-base font-medium transition-colors hover:bg-muted/70"
                  >
                    Урок {idx + 1}: {lesson.title}
                  </Link>
                ))}

                {(!courseModule.lessons || courseModule.lessons.length === 0) && (
                  <p className="text-sm text-muted-foreground">
                    В этом модуле пока нет уроков.
                  </p>
                )}
              </div>
            </>
          )}
        </section>
      </main>
    </>
  );
}
