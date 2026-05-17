"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useState } from "react";
import { useQuery, useMutation } from "@tanstack/react-query";
import { Navbar } from "@/components/layout/Navbar";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { courseService } from "@/services/courseService";
import { reportService } from "@/services/reportService";
import { extractErrorMessage } from "@/lib/api";

export default function CoursePage() {
  const params = useParams<{ id: string }>();
  const courseId = Number(params.id);
  const [reportOpen, setReportOpen] = useState(false);
  const [reportMessage, setReportMessage] = useState("");

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

  const reportMutation = useMutation({
    mutationFn: () =>
      reportService.create({
        courseId,
        message: reportMessage,
      }),
    onSuccess: () => {
      setReportMessage("");
      setReportOpen(false);
    },
  });

  const enrolled = !progressQuery.isError && progressQuery.data != null;
  const reportMessageValid = reportMessage.trim().length > 0;

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
                Прогресс: {progressQuery.data?.progressPercentage ?? 0}%
              </div>
            )}
          </div>

          {courseQuery.data?.description && (
            <p className="mt-4 text-base text-foreground/80 max-w-3xl">
              {courseQuery.data.description}
            </p>
          )}

          <div className="mt-6 max-w-2xl">
            {!reportOpen ? (
              <Button variant="outline" onClick={() => setReportOpen(true)}>
                Пожаловаться на курс
              </Button>
            ) : (
              <form
                className="space-y-3 rounded-lg border border-border p-4"
                onSubmit={(event) => {
                  event.preventDefault();
                  if (reportMessageValid) reportMutation.mutate();
                }}
              >
                <Textarea
                  value={reportMessage}
                  onChange={(event) => setReportMessage(event.target.value)}
                  placeholder="Опишите проблему с курсом"
                  rows={4}
                />
                <div className="flex flex-wrap items-center gap-3">
                  <Button
                    type="submit"
                    disabled={!reportMessageValid || reportMutation.isPending}
                  >
                    {reportMutation.isPending ? "Отправка..." : "Отправить жалобу"}
                  </Button>
                  <Button
                    type="button"
                    variant="ghost"
                    onClick={() => {
                      setReportOpen(false);
                      setReportMessage("");
                    }}
                  >
                    Отмена
                  </Button>
                </div>
                {reportMutation.isSuccess && (
                  <p className="text-sm text-foreground/70">Жалоба отправлена.</p>
                )}
                {reportMutation.isError && (
                  <p className="text-sm text-destructive">
                    {extractErrorMessage(reportMutation.error)}
                  </p>
                )}
              </form>
            )}
          </div>

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
