"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Star, Trash2 } from "lucide-react";
import { Navbar } from "@/components/layout/Navbar";
import { RichTextContent } from "@/components/editor/RichTextContent";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogTitle,
} from "@/components/ui/dialog";
import { Textarea } from "@/components/ui/textarea";
import { courseService } from "@/services/courseService";
import { reportService } from "@/services/reportService";
import { extractErrorMessage } from "@/lib/api";
import { useAuthStore } from "@/lib/auth-store";
import { cn } from "@/lib/utils";

export default function CoursePage() {
  const params = useParams<{ id: string }>();
  const courseId = Number(params.id);
  const queryClient = useQueryClient();
  const user = useAuthStore((state) => state.user);
  const [reportOpen, setReportOpen] = useState(false);
  const [reportMessage, setReportMessage] = useState("");
  const [rating, setRating] = useState(5);
  const [reviewComment, setReviewComment] = useState("");
  const [unenrollOpen, setUnenrollOpen] = useState(false);
  const [unenrollSuccess, setUnenrollSuccess] = useState(false);

  const courseQuery = useQuery({
    queryKey: ["course", courseId],
    queryFn: () => courseService.getById(courseId),
    enabled: Number.isFinite(courseId),
  });

  const progressQuery = useQuery({
    queryKey: ["course", courseId, "progress"],
    queryFn: () => courseService.getProgress(courseId),
    enabled: Number.isFinite(courseId),
    retry: false,
  });

  const enrolled = !progressQuery.isError && progressQuery.data != null;

  const modulesQuery = useQuery({
    queryKey: ["course", courseId, "modules"],
    queryFn: () => courseService.listModules(courseId),
    enabled: Number.isFinite(courseId) && enrolled,
  });

  const reviewsQuery = useQuery({
    queryKey: ["course", courseId, "reviews"],
    queryFn: () => courseService.listReviews(courseId),
    enabled: Number.isFinite(courseId),
  });

  const enrollMutation = useMutation({
    mutationFn: () => courseService.enroll(courseId),
    onSuccess: () => {
      setUnenrollSuccess(false);
      queryClient.invalidateQueries({ queryKey: ["course", courseId, "progress"] });
      queryClient.invalidateQueries({ queryKey: ["course", courseId, "modules"] });
      queryClient.invalidateQueries({ queryKey: ["my-enrollments"] });
    },
  });

  const unenrollMutation = useMutation({
    mutationFn: () => courseService.unenroll(courseId),
    onSuccess: () => {
      setUnenrollOpen(false);
      setUnenrollSuccess(true);
      queryClient.removeQueries({ queryKey: ["course", courseId, "modules"] });
      queryClient.invalidateQueries({ queryKey: ["course", courseId, "progress"] });
      queryClient.invalidateQueries({ queryKey: ["my-enrollments"] });
    },
  });

  const reviewMutation = useMutation({
    mutationFn: () =>
      courseService.createReview(courseId, {
        rating,
        comment: reviewComment.trim() || null,
      }),
    onSuccess: () => {
      setRating(5);
      setReviewComment("");
      queryClient.invalidateQueries({ queryKey: ["course", courseId] });
      queryClient.invalidateQueries({ queryKey: ["course", courseId, "reviews"] });
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

  const reportMessageValid = reportMessage.trim().length > 0;
  const myReview = reviewsQuery.data?.content.find((review) => review.studentId === user?.id);
  const canReview = enrolled && !myReview;

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
            {enrolled && (
              <Button
                type="button"
                variant="outline"
                onClick={() => {
                  unenrollMutation.reset();
                  setUnenrollOpen(true);
                }}
              >
                <Trash2 className="mr-2 h-4 w-4" />
                Отписаться от курса
              </Button>
            )}
          </div>

          {courseQuery.data?.description && (
            <RichTextContent
              value={courseQuery.data.description}
              className="mt-4 max-w-3xl text-base text-foreground/80"
            />
          )}

          {courseQuery.data && (
            <div className="mt-4 flex flex-wrap items-center gap-3 text-sm text-muted-foreground">
              <span className="flex items-center gap-1 text-base font-medium text-foreground">
                <Star className="h-5 w-5 fill-yellow-400 text-yellow-400" />
                {courseQuery.data.rating.toFixed(1)}
              </span>
              <span>
                {courseQuery.data.reviewsCount} оценок
              </span>
            </div>
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
              {unenrollSuccess && (
                <p className="mt-2 text-sm text-foreground/70">
                  Курс успешно удалён из списка ваших курсов
                </p>
              )}
            </div>
          )}

          {enrolled && <div className="mt-8 flex flex-col gap-3">
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
          </div>}

          <section className="mt-10 border-t border-border pt-6">
            <h2 className="text-2xl font-semibold">Отзывы и оценки</h2>

            {canReview && (
              <form
                className="mt-4 space-y-4 rounded-lg border border-border p-4"
                onSubmit={(event) => {
                  event.preventDefault();
                  reviewMutation.mutate();
                }}
              >
                <div>
                  <p className="mb-2 text-sm font-medium">Ваша оценка</p>
                  <div className="flex gap-1">
                    {Array.from({ length: 5 }).map((_, index) => {
                      const value = index + 1;
                      return (
                        <button
                          key={value}
                          type="button"
                          onClick={() => setRating(value)}
                          className="rounded-md p-1 focus:outline-none focus:ring-2 focus:ring-primary"
                          aria-label={`Оценить на ${value}`}
                        >
                          <Star
                            className={cn(
                              "h-7 w-7 text-yellow-400",
                              value <= rating ? "fill-yellow-400" : "fill-transparent"
                            )}
                          />
                        </button>
                      );
                    })}
                  </div>
                </div>
                <Textarea
                  value={reviewComment}
                  onChange={(event) => setReviewComment(event.target.value)}
                  placeholder="Комментарий к курсу (необязательно)"
                  rows={4}
                  maxLength={1000}
                />
                <Button type="submit" disabled={reviewMutation.isPending}>
                  {reviewMutation.isPending ? "Сохранение..." : "Оценить курс"}
                </Button>
                {reviewMutation.isError && (
                  <p className="text-sm text-destructive">
                    {extractErrorMessage(reviewMutation.error)}
                  </p>
                )}
              </form>
            )}

            {myReview && (
              <p className="mt-4 rounded-lg bg-muted px-4 py-3 text-sm text-muted-foreground">
                Вы уже оценили этот курс на {myReview.rating} из 5.
              </p>
            )}

            {!enrolled && (
              <p className="mt-4 text-sm text-muted-foreground">
                Запишитесь на курс, чтобы оставить оценку.
              </p>
            )}

            <div className="mt-5 space-y-3">
              {reviewsQuery.isLoading &&
                Array.from({ length: 2 }).map((_, index) => (
                  <div key={index} className="h-24 animate-pulse rounded-lg bg-muted" />
                ))}
              {reviewsQuery.data?.content.length === 0 && (
                <p className="text-sm text-muted-foreground">Пока нет отзывов.</p>
              )}
              {reviewsQuery.data?.content.map((review) => (
                <article key={review.id} className="rounded-lg border border-border p-4">
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <div>
                      <p className="font-medium">
                        {review.studentFirstName || review.studentUsername || "Пользователь"}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        {new Date(review.createdAt).toLocaleDateString("ru-RU")}
                      </p>
                    </div>
                    <div className="flex items-center gap-1">
                      {Array.from({ length: 5 }).map((_, index) => (
                        <Star
                          key={index}
                          className={cn(
                            "h-4 w-4 text-yellow-400",
                            index < review.rating ? "fill-yellow-400" : "fill-transparent"
                          )}
                        />
                      ))}
                    </div>
                  </div>
                  {review.comment && (
                    <p className="mt-3 whitespace-pre-line text-sm text-foreground/80">
                      {review.comment}
                    </p>
                  )}
                </article>
              ))}
            </div>
          </section>
        </div>
      </main>
      <Dialog open={unenrollOpen} onOpenChange={setUnenrollOpen}>
        <DialogContent className="p-6">
          <DialogTitle>Отписаться от курса</DialogTitle>
          <DialogDescription>
            Вы уверены, что хотите отписаться от курса?
          </DialogDescription>
          <div className="flex justify-end gap-3">
            <Button
              type="button"
              variant="ghost"
              onClick={() => setUnenrollOpen(false)}
              disabled={unenrollMutation.isPending}
            >
              Отмена
            </Button>
            <Button
              type="button"
              variant="outline"
              className="border-destructive text-destructive hover:bg-destructive/10"
              onClick={() => unenrollMutation.mutate()}
              disabled={unenrollMutation.isPending}
            >
              {unenrollMutation.isPending ? "Удаление..." : "Удалить курс из моих курсов"}
            </Button>
          </div>
          {unenrollMutation.isError && (
            <p className="text-sm text-destructive">
              {extractErrorMessage(unenrollMutation.error)}
            </p>
          )}
        </DialogContent>
      </Dialog>
    </>
  );
}
