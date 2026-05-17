"use client";

import { useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ArrowLeft, CheckCircle2, XCircle } from "lucide-react";
import { Navbar } from "@/components/layout/Navbar";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { courseService } from "@/services/courseService";
import { extractErrorMessage } from "@/lib/api";

export default function ExercisePage() {
  const params = useParams<{
    id: string;
    moduleId: string;
    lessonId: string;
    exerciseId: string;
  }>();
  const courseId = Number(params.id);
  const moduleId = Number(params.moduleId);
  const lessonId = Number(params.lessonId);
  const exerciseId = Number(params.exerciseId);
  const queryClient = useQueryClient();
  const [answer, setAnswer] = useState("");

  const exerciseQuery = useQuery({
    queryKey: ["exercise", courseId, moduleId, lessonId, exerciseId],
    queryFn: () =>
      courseService.getExercise(courseId, moduleId, lessonId, exerciseId),
    enabled:
      Number.isFinite(courseId) &&
      Number.isFinite(moduleId) &&
      Number.isFinite(lessonId) &&
      Number.isFinite(exerciseId),
  });

  const submitMutation = useMutation({
    mutationFn: () =>
      courseService.submitExercise(courseId, moduleId, lessonId, exerciseId, answer),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["course", courseId, "progress"] });
      queryClient.invalidateQueries({ queryKey: ["my-enrollments"] });
    },
  });

  const exercise = exerciseQuery.data;
  const isMultipleChoice = exercise?.type === "MULTIPLE_CHOICE";
  const result = submitMutation.data;

  return (
    <>
      <Navbar />
      <main className="mx-auto max-w-4xl px-6 py-6">
        <Link
          href={`/courses/${courseId}/modules/${moduleId}/lessons/${lessonId}`}
          className="inline-flex items-center gap-2 text-sm text-foreground/70 hover:text-foreground"
        >
          <ArrowLeft className="h-4 w-4" /> К уроку
        </Link>

        <section className="mt-4 rounded-3xl bg-white p-8 shadow-sm">
          {exerciseQuery.isLoading && (
            <div className="space-y-4">
              <div className="h-8 w-2/3 animate-pulse rounded bg-muted" />
              <div className="h-24 animate-pulse rounded-lg bg-muted" />
            </div>
          )}

          {exerciseQuery.isError && (
            <p className="text-sm text-destructive">
              {extractErrorMessage(exerciseQuery.error)}
            </p>
          )}

          {exercise && (
            <>
              <p className="text-sm text-foreground/60">Упражнение</p>
              <h1 className="mt-2 text-2xl font-semibold leading-tight">
                {exercise.question}
              </h1>

              <form
                className="mt-8 space-y-5"
                onSubmit={(event) => {
                  event.preventDefault();
                  if (answer.trim()) submitMutation.mutate();
                }}
              >
                {isMultipleChoice ? (
                  <div className="space-y-2">
                    {(exercise.options ?? []).map((option) => {
                      const selected = answer === option;
                      return (
                        <button
                          key={option}
                          type="button"
                          onClick={() => setAnswer(option)}
                          className={`w-full rounded-lg border px-4 py-3 text-left text-sm transition-colors ${
                            selected
                              ? "border-primary bg-primary/10 text-primary"
                              : "border-border bg-muted/30 hover:bg-muted"
                          }`}
                        >
                          {option}
                        </button>
                      );
                    })}
                  </div>
                ) : (
                  <Textarea
                    value={answer}
                    onChange={(event) => setAnswer(event.target.value)}
                    rows={5}
                    maxLength={1000}
                    placeholder="Введите ответ"
                  />
                )}

                <Button
                  type="submit"
                  disabled={!answer.trim() || submitMutation.isPending}
                >
                  {submitMutation.isPending ? "Проверка..." : "Ответить"}
                </Button>
              </form>

              {submitMutation.isError && (
                <p className="mt-4 text-sm text-destructive">
                  {extractErrorMessage(submitMutation.error)}
                </p>
              )}

              {result && (
                <div
                  className={`mt-6 rounded-lg p-4 ${
                    result.correct
                      ? "bg-emerald-50 text-emerald-800"
                      : "bg-destructive/10 text-destructive"
                  }`}
                >
                  <div className="flex items-center gap-2 font-medium">
                    {result.correct ? (
                      <CheckCircle2 className="h-5 w-5" />
                    ) : (
                      <XCircle className="h-5 w-5" />
                    )}
                    {result.correct ? "Верно" : "Неверно"}
                  </div>

                  {!result.correct && result.correctAnswer && (
                    <p className="mt-2 text-sm">
                      Правильный ответ: {result.correctAnswer}
                    </p>
                  )}

                  {result.explanation && (
                    <p className="mt-2 text-sm">{result.explanation}</p>
                  )}

                  <p className="mt-3 text-sm">
                    Прогресс: {result.progressPercentage}%
                  </p>
                </div>
              )}
            </>
          )}
        </section>
      </main>
    </>
  );
}
