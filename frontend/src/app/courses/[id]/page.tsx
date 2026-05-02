"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useQuery, useMutation } from "@tanstack/react-query";
import { Navbar } from "@/components/layout/Navbar";
import { Button } from "@/components/ui/button";
import { courseService } from "@/services/courseService";
import { extractErrorMessage } from "@/lib/api";

export default function CoursePage() {
  const params = useParams<{ id: string }>();
  const courseId = Number(params.id);

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

  const progressQuery = useQuery({
    queryKey: ["course", courseId, "progress"],
    queryFn: () => courseService.getProgress(courseId),
    enabled: Number.isFinite(courseId),
    retry: false,
  });

  const enrollMutation = useMutation({
    mutationFn: () => courseService.enroll(courseId),
    onSuccess: () => {
      progressQuery.refetch();
    },
  });

  const enrolled = !progressQuery.isError && progressQuery.data != null;

  return (
    <>
      <Navbar />
      <main className="mx-auto max-w-7xl px-6 py-6">
        <div className="rounded-3xl bg-white p-8 shadow-sm">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <h1 className="text-3xl font-semibold leading-tight max-w-3xl">
              {courseQuery.data?.title ?? "Загрузка..."}
            </h1>
            {enrolled && (
              <div className="text-2xl font-semibold">
                Прогресс: {progressQuery.data?.progressPercent ?? 0}%
              </div>
            )}
          </div>

          {courseQuery.data?.description && (
            <p className="mt-4 text-base text-foreground/80 max-w-3xl">
              {courseQuery.data.description}
            </p>
          )}

          {!enrolled && (
            <div className="mt-6">
              <Button
                onClick={() => enrollMutation.mutate()}
                disabled={enrollMutation.isPending}
              >
                {enrollMutation.isPending ? "Запись..." : "Записаться на курс"}
              </Button>
              {enrollMutation.isError && (
                <p className="mt-2 text-sm text-destructive">
                  {extractErrorMessage(enrollMutation.error)}
                </p>
              )}
            </div>
          )}

          <div className="mt-8 flex flex-col gap-3">
            {modulesQuery.isLoading &&
              Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="h-12 animate-pulse rounded-lg bg-muted" />
              ))}
            {modulesQuery.data?.map((m, idx) => (
              <Link
                key={m.id}
                href={`/courses/${courseId}/modules/${m.id}`}
                className="block rounded-lg bg-muted px-5 py-3 text-base font-medium hover:bg-muted/70 transition-colors"
              >
                Модуль {idx + 1}: {m.title}
              </Link>
            ))}
            {modulesQuery.data?.length === 0 && (
              <p className="text-sm text-muted-foreground">У этого курса пока нет модулей.</p>
            )}
          </div>
        </div>
      </main>
    </>
  );
}
