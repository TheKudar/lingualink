"use client";

import { useEffect } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import {
  AlertCircle,
  ArrowLeft,
  BarChart3,
  Clock,
  HelpCircle,
  LineChart,
  Target,
  Users,
} from "lucide-react";
import { Navbar } from "@/components/layout/Navbar";
import { Button } from "@/components/ui/button";
import { courseService } from "@/services/courseService";
import { analyticsService } from "@/services/analyticsService";
import { useAuthStore } from "@/lib/auth-store";
import type {
  CourseAnalyticsOverviewResponse,
  DropoffLessonAnalyticsResponse,
  LessonTimeAnalyticsResponse,
  QuestionAnalyticsResponse,
} from "@/types/api";

export default function CreatorCourseAnalyticsPage() {
  const router = useRouter();
  const params = useParams<{ id: string }>();
  const courseId = Number(params.id);
  const { user, isHydrated } = useAuthStore();
  const canViewCreatorTools = user?.role === "CREATOR" || user?.role === "ADMIN";
  const enabled = Boolean(canViewCreatorTools && Number.isFinite(courseId));

  useEffect(() => {
    if (isHydrated && (!user || !canViewCreatorTools)) {
      router.replace("/");
    }
  }, [canViewCreatorTools, isHydrated, router, user]);

  const courseQuery = useQuery({
    queryKey: ["course", courseId],
    queryFn: () => courseService.getById(courseId),
    enabled,
  });

  const overviewQuery = useQuery({
    queryKey: ["analytics", courseId, "overview"],
    queryFn: () => analyticsService.overview(courseId),
    enabled,
  });

  const questionsQuery = useQuery({
    queryKey: ["analytics", courseId, "questions"],
    queryFn: () => analyticsService.questions(courseId),
    enabled,
  });

  const dropoffQuery = useQuery({
    queryKey: ["analytics", courseId, "dropoff"],
    queryFn: () => analyticsService.dropoff(courseId),
    enabled,
  });

  const isLoading =
    courseQuery.isLoading ||
    overviewQuery.isLoading ||
    questionsQuery.isLoading ||
    dropoffQuery.isLoading;
  const error =
    courseQuery.error ??
    overviewQuery.error ??
    questionsQuery.error ??
    dropoffQuery.error;

  if (!user || !canViewCreatorTools) {
    return <Navbar />;
  }

  return (
    <>
      <Navbar />
      <main className="mx-auto max-w-7xl px-6 py-6 space-y-6">
        <Link
          href={`/studio/courses/${courseId}`}
          className="inline-flex items-center gap-2 text-sm text-foreground/70 hover:text-foreground"
        >
          <ArrowLeft className="h-4 w-4" /> Назад к редактору курса
        </Link>

        <section className="rounded-3xl bg-white p-8 shadow-sm">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div>
              <p className="text-sm font-medium text-primary">Аналитика автора</p>
              <h1 className="mt-1 text-3xl font-bold">
                {courseQuery.data?.title ?? "Аналитика курса"}
              </h1>
              <p className="mt-2 max-w-2xl text-sm text-foreground/60">
                Активность, завершение, отток, сложность вопросов и время прохождения уроков.
              </p>
            </div>
            <Button
              variant="outline"
              onClick={() => {
                overviewQuery.refetch();
                questionsQuery.refetch();
                dropoffQuery.refetch();
              }}
              disabled={isLoading}
            >
              <LineChart className="mr-2 h-4 w-4" />
              Обновить
            </Button>
          </div>

          {isLoading && <AnalyticsLoading />}

          {error && (
            <div className="mt-6 flex items-start gap-3 rounded-lg border border-destructive/30 bg-destructive/5 p-4 text-sm text-destructive">
              <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" />
              <span>{formatAnalyticsError(error)}</span>
            </div>
          )}
        </section>

        {overviewQuery.data && !error && (
          <>
            <OverviewSection overview={overviewQuery.data} />
            <DropoffSection dropoff={dropoffQuery.data ?? []} />
            <QuestionsSection questions={questionsQuery.data ?? []} />
            <LessonTimesSection lessons={overviewQuery.data.lessonTimes} />
          </>
        )}
      </main>
    </>
  );
}

function OverviewSection({ overview }: { overview: CourseAnalyticsOverviewResponse }) {
  const activeUserMax = Math.max(overview.weeklyActiveUsers, overview.enrolledUsers, 1);

  return (
    <section className="rounded-3xl bg-white p-8 shadow-sm">
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <MetricTile
          icon={Clock}
          label="Средняя сессия"
          value={formatDuration(overview.averageSessionDurationSeconds)}
          detail="По событиям выхода из курса"
        />
        <MetricTile
          icon={Target}
          label="Процент завершения"
          value={`${formatPercent(overview.completionRatePercent)}%`}
          detail={`${overview.completedUsers} из ${overview.enrolledUsers} записанных`}
        />
        <MetricTile
          icon={Clock}
          label="Среднее время завершения"
          value={formatDuration(overview.averageTimeToCompleteSeconds)}
          detail="От записи до завершения"
        />
        <MetricTile
          icon={Users}
          label="Среднее время на пользователя"
          value={formatDuration(overview.averageTotalTimeSpentSecondsPerUser)}
          detail="Учтено время в курсе и уроках"
        />
      </div>

      <div className="mt-8 grid gap-6 lg:grid-cols-2">
        <ActivityChart
          title="Активные пользователи за день"
          subtitle="Последние 24 часа"
          value={overview.dailyActiveUsers}
          max={activeUserMax}
        />
        <ActivityChart
          title="Активные пользователи за неделю"
          subtitle="Последние 7 дней"
          value={overview.weeklyActiveUsers}
          max={activeUserMax}
        />
      </div>
    </section>
  );
}

function DropoffSection({ dropoff }: { dropoff: DropoffLessonAnalyticsResponse[] }) {
  const maxStopped = Math.max(...dropoff.map((lesson) => lesson.stoppedUsers), 1);

  return (
    <section className="rounded-3xl bg-white p-8 shadow-sm">
      <div className="flex items-center gap-2">
        <BarChart3 className="h-5 w-5 text-primary" />
        <h2 className="text-xl font-semibold">Отток по урокам</h2>
      </div>

      <div className="mt-5 space-y-4">
        {dropoff.length === 0 && (
          <p className="text-sm text-muted-foreground">Данных о прохождении уроков пока нет.</p>
        )}

        {dropoff.map((lesson) => (
          <div key={lesson.lessonId} className="rounded-lg border border-border p-4">
            <div className="flex flex-wrap items-baseline justify-between gap-2">
              <div className="min-w-0">
                <p className="truncate font-medium">
                  {lesson.lessonOrder}. {lesson.lessonTitle}
                </p>
                <p className="mt-1 text-xs text-muted-foreground">
                  Начали: {lesson.startedUsers} · завершили: {lesson.completedUsers}
                </p>
              </div>
              <p className="text-sm font-semibold text-foreground">
                Остановились: {lesson.stoppedUsers} · {formatPercent(lesson.stopRatePercent)}%
              </p>
            </div>
            <div className="mt-3 h-3 overflow-hidden rounded-full bg-muted">
              <div
                className="h-full rounded-full bg-destructive"
                style={{ width: `${barPercent(lesson.stoppedUsers, maxStopped)}%` }}
              />
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}

function QuestionsSection({ questions }: { questions: QuestionAnalyticsResponse[] }) {
  return (
    <section className="rounded-3xl bg-white p-8 shadow-sm">
      <div className="flex items-center gap-2">
        <HelpCircle className="h-5 w-5 text-primary" />
        <h2 className="text-xl font-semibold">Самые сложные вопросы</h2>
      </div>

      <div className="mt-5 overflow-x-auto">
        {questions.length === 0 ? (
          <p className="text-sm text-muted-foreground">Попыток ответить на вопросы пока нет.</p>
        ) : (
          <table className="w-full min-w-[760px] text-left text-sm">
            <thead className="border-b border-border text-xs uppercase text-muted-foreground">
              <tr>
                <th className="py-3 pr-4 font-medium">Вопрос</th>
                <th className="py-3 px-4 font-medium">Ответы</th>
                <th className="py-3 px-4 font-medium">Верно</th>
                <th className="py-3 px-4 font-medium">Неверно</th>
                <th className="py-3 pl-4 font-medium">Доля ошибок</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {questions.map((question) => (
                <tr key={question.questionId}>
                  <td className="max-w-[420px] py-4 pr-4 font-medium">
                    <span className="line-clamp-2">{question.question}</span>
                  </td>
                  <td className="py-4 px-4">{question.totalAnswers}</td>
                  <td className="py-4 px-4 text-success">{question.correctAnswers}</td>
                  <td className="py-4 px-4 text-destructive">{question.incorrectAnswers}</td>
                  <td className="py-4 pl-4">
                    <div className="flex min-w-32 items-center gap-3">
                      <div className="h-2 flex-1 overflow-hidden rounded-full bg-muted">
                        <div
                          className="h-full rounded-full bg-destructive"
                          style={{ width: `${barPercent(question.errorRatePercent, 100)}%` }}
                        />
                      </div>
                      <span className="w-12 text-right font-semibold">
                        {formatPercent(question.errorRatePercent)}%
                      </span>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </section>
  );
}

function LessonTimesSection({ lessons }: { lessons: LessonTimeAnalyticsResponse[] }) {
  const maxTotal = Math.max(
    ...lessons.map((lesson) => lesson.totalTimeSpentSeconds ?? 0),
    1
  );

  return (
    <section className="rounded-3xl bg-white p-8 shadow-sm">
      <div className="flex items-center gap-2">
        <Clock className="h-5 w-5 text-primary" />
        <h2 className="text-xl font-semibold">Время по урокам</h2>
      </div>

      <div className="mt-5 space-y-4">
        {lessons.length === 0 && (
          <p className="text-sm text-muted-foreground">В этом курсе пока нет уроков.</p>
        )}

        {lessons.map((lesson) => (
          <div key={lesson.lessonId} className="rounded-lg border border-border p-4">
            <div className="flex flex-wrap items-baseline justify-between gap-2">
              <p className="min-w-0 truncate font-medium">{lesson.lessonTitle}</p>
              <p className="text-sm text-muted-foreground">
                В среднем: {formatDuration(lesson.averageTimeSpentSeconds)} · всего:{" "}
                {formatDuration(lesson.totalTimeSpentSeconds)}
              </p>
            </div>
            <div className="mt-3 h-3 overflow-hidden rounded-full bg-muted">
              <div
                className="h-full rounded-full bg-primary"
                style={{
                  width: `${barPercent(lesson.totalTimeSpentSeconds ?? 0, maxTotal)}%`,
                }}
              />
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}

function ActivityChart({
  title,
  subtitle,
  value,
  max,
}: {
  title: string;
  subtitle: string;
  value: number;
  max: number;
}) {
  return (
    <div className="rounded-lg border border-border p-5">
      <div className="flex items-baseline justify-between gap-3">
        <div>
          <h2 className="font-semibold">{title}</h2>
          <p className="mt-1 text-xs text-muted-foreground">{subtitle}</p>
        </div>
        <p className="text-3xl font-bold">{value}</p>
      </div>
      <div className="mt-5 h-20 rounded-lg bg-muted p-3">
        <div className="flex h-full items-end">
          <div
            className="w-full rounded-md bg-primary transition-all"
            style={{ height: `${Math.max(barPercent(value, max), value > 0 ? 8 : 0)}%` }}
            aria-label={`${title}: ${value}`}
          />
        </div>
      </div>
    </div>
  );
}

function MetricTile({
  icon: Icon,
  label,
  value,
  detail,
}: {
  icon: typeof Clock;
  label: string;
  value: string;
  detail: string;
}) {
  return (
    <div className="rounded-lg border border-border p-5">
      <div className="flex items-center gap-2 text-sm font-medium text-muted-foreground">
        <Icon className="h-4 w-4 text-primary" />
        {label}
      </div>
      <p className="mt-3 text-2xl font-bold">{value}</p>
      <p className="mt-1 text-xs text-muted-foreground">{detail}</p>
    </div>
  );
}

function AnalyticsLoading() {
  return (
    <div className="mt-8 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
      {Array.from({ length: 4 }).map((_, index) => (
        <div key={index} className="h-28 animate-pulse rounded-lg bg-muted" />
      ))}
    </div>
  );
}

function formatAnalyticsError(error: unknown): string {
  if (error instanceof Error && error.message.trim()) {
    if (error.message === "Network Error") {
      return "Не удалось подключиться к серверу аналитики. Проверьте соединение и попробуйте снова.";
    }
  }

  return "Не удалось загрузить аналитику курса. Попробуйте обновить данные.";
}

function formatDuration(seconds: number | null | undefined): string {
  if (seconds == null || !Number.isFinite(seconds)) return "Нет данных";
  if (seconds < 60) return `${Math.round(seconds)} сек`;

  const totalMinutes = Math.round(seconds / 60);
  if (totalMinutes < 60) return `${totalMinutes} мин`;

  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;
  if (hours < 24) return minutes > 0 ? `${hours} ч ${minutes} мин` : `${hours} ч`;

  const days = Math.floor(hours / 24);
  const remainingHours = hours % 24;
  return remainingHours > 0 ? `${days} д ${remainingHours} ч` : `${days} д`;
}

function formatPercent(value: number): string {
  return value.toLocaleString("ru-RU", {
    maximumFractionDigits: 1,
  });
}

function barPercent(value: number, max: number): number {
  if (max <= 0) return 0;
  return Math.max(0, Math.min(100, (value / max) * 100));
}
