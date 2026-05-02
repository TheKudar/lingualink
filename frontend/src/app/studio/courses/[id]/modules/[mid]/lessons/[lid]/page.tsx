"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { ArrowLeft, Plus, Trash2 } from "lucide-react";
import { Navbar } from "@/components/layout/Navbar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { NativeSelect } from "@/components/ui/native-select";
import { courseService } from "@/services/courseService";
import { useAuthStore } from "@/lib/auth-store";
import { extractErrorMessage } from "@/lib/api";
import type { ExerciseCreateRequest, ExerciseType } from "@/types/api";

const TYPES: { value: ExerciseType; label: string }[] = [
  { value: "MULTIPLE_CHOICE", label: "Множественный выбор" },
  { value: "SHORT_ANSWER", label: "Короткий ответ" },
  { value: "FILL_IN_THE_BLANK", label: "Заполнить пропуск" },
];

export default function LessonEditorPage() {
  const router = useRouter();
  const params = useParams<{ id: string; mid: string; lid: string }>();
  const courseId = Number(params.id);
  const moduleId = Number(params.mid);
  const lessonId = Number(params.lid);
  const queryClient = useQueryClient();
  const { user, isHydrated } = useAuthStore();

  useEffect(() => {
    if (isHydrated && !user) router.replace("/");
  }, [isHydrated, user, router]);

  const lessonQuery = useQuery({
    queryKey: ["lesson", lessonId],
    queryFn: () => courseService.getLesson(courseId, moduleId, lessonId),
    enabled: !!user,
  });

  const exercisesQuery = useQuery({
    queryKey: ["lesson", lessonId, "exercises"],
    queryFn: () => courseService.listExercises(courseId, moduleId, lessonId),
    enabled: !!user,
  });

  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");

  useEffect(() => {
    if (lessonQuery.data) {
      setTitle(lessonQuery.data.title);
      setContent(lessonQuery.data.content ?? "");
    }
  }, [lessonQuery.data]);

  const updateLessonMutation = useMutation({
    mutationFn: () =>
      courseService.updateLesson(courseId, moduleId, lessonId, {
        title,
        content,
        orderIndex: lessonQuery.data?.orderIndex ?? 0,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["lesson", lessonId] });
      queryClient.invalidateQueries({ queryKey: ["course", courseId, "modules"] });
    },
  });

  // ===== Exercise form =====
  const [exType, setExType] = useState<ExerciseType>("MULTIPLE_CHOICE");
  const [exQuestion, setExQuestion] = useState("");
  const [exOptions, setExOptions] = useState<string[]>(["", ""]);
  const [exAnswer, setExAnswer] = useState("");
  const [exExplain, setExExplain] = useState("");
  const [showAddEx, setShowAddEx] = useState(false);

  const resetExerciseForm = () => {
    setExType("MULTIPLE_CHOICE");
    setExQuestion("");
    setExOptions(["", ""]);
    setExAnswer("");
    setExExplain("");
    setShowAddEx(false);
  };

  const createExerciseMutation = useMutation({
    mutationFn: () => {
      const payload: ExerciseCreateRequest = {
        type: exType,
        question: exQuestion,
        correctAnswer: exAnswer,
        explanation: exExplain || undefined,
        orderIndex: exercisesQuery.data?.length ?? 0,
        options:
          exType === "MULTIPLE_CHOICE"
            ? exOptions.filter((o) => o.trim())
            : undefined,
      };
      return courseService.createExercise(courseId, moduleId, lessonId, payload);
    },
    onSuccess: () => {
      resetExerciseForm();
      queryClient.invalidateQueries({ queryKey: ["lesson", lessonId, "exercises"] });
    },
  });

  const deleteExerciseMutation = useMutation({
    mutationFn: (exerciseId: number) =>
      courseService.removeExercise(courseId, moduleId, lessonId, exerciseId),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: ["lesson", lessonId, "exercises"] }),
  });

  if (!user) return <Navbar />;

  return (
    <>
      <Navbar />
      <main className="mx-auto max-w-4xl px-6 py-6 space-y-6">
        <Link
          href={`/studio/courses/${courseId}/modules/${moduleId}`}
          className="inline-flex items-center gap-2 text-sm text-foreground/70 hover:text-foreground"
        >
          <ArrowLeft className="h-4 w-4" /> К модулю
        </Link>

        {/* Lesson editor */}
        <section className="rounded-3xl bg-white p-8 shadow-sm">
          <h1 className="text-2xl font-bold mb-5">Редактирование урока</h1>

          <form
            onSubmit={(e) => {
              e.preventDefault();
              updateLessonMutation.mutate();
            }}
            className="space-y-5"
          >
            <div className="space-y-2">
              <Label htmlFor="lesson-title">Название урока</Label>
              <Input
                id="lesson-title"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                maxLength={100}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="lesson-content">Содержание</Label>
              <Textarea
                id="lesson-content"
                value={content}
                onChange={(e) => setContent(e.target.value)}
                rows={12}
                placeholder="Текст урока, который увидит студент"
              />
            </div>
            <Button type="submit" disabled={updateLessonMutation.isPending}>
              {updateLessonMutation.isPending ? "Сохранение..." : "Сохранить урок"}
            </Button>
            {updateLessonMutation.isError && (
              <p className="text-sm text-destructive">
                {extractErrorMessage(updateLessonMutation.error)}
              </p>
            )}
            {updateLessonMutation.isSuccess && (
              <p className="text-sm text-success">Сохранено</p>
            )}
          </form>
        </section>

        {/* Exercises */}
        <section className="rounded-3xl bg-white p-8 shadow-sm">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-semibold">Упражнения</h2>
            {!showAddEx && (
              <Button size="sm" onClick={() => setShowAddEx(true)}>
                <Plus className="mr-1 h-4 w-4" /> Добавить упражнение
              </Button>
            )}
          </div>

          <div className="mt-5 flex flex-col gap-3">
            {exercisesQuery.data?.map((ex, i) => (
              <div
                key={ex.id}
                className="group rounded-lg bg-muted/50 px-4 py-3"
              >
                <div className="flex items-start justify-between gap-3">
                  <div className="flex-1">
                    <p className="font-medium text-sm text-foreground/60">
                      Упражнение {i + 1} ·{" "}
                      {TYPES.find((t) => t.value === ex.type)?.label}
                    </p>
                    <p className="mt-1">{ex.question}</p>
                    {ex.options && ex.options.length > 0 && (
                      <ul className="mt-2 list-disc pl-6 text-sm text-foreground/70">
                        {ex.options.map((o, idx) => (
                          <li key={idx}>{o}</li>
                        ))}
                      </ul>
                    )}
                  </div>
                  <button
                    onClick={() => {
                      if (confirm("Удалить упражнение?")) {
                        deleteExerciseMutation.mutate(ex.id);
                      }
                    }}
                    className="text-muted-foreground opacity-0 group-hover:opacity-100 hover:text-destructive transition-opacity"
                  >
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>
              </div>
            ))}
            {(!exercisesQuery.data || exercisesQuery.data.length === 0) && !showAddEx && (
              <p className="text-sm text-muted-foreground">Пока нет упражнений.</p>
            )}
          </div>

          {showAddEx && (
            <form
              onSubmit={(e) => {
                e.preventDefault();
                createExerciseMutation.mutate();
              }}
              className="mt-5 rounded-2xl border border-border p-5 space-y-4"
            >
              <h3 className="font-semibold">Новое упражнение</h3>

              <div className="grid gap-3 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label>Тип</Label>
                  <NativeSelect
                    value={exType}
                    onChange={(e) => setExType(e.target.value as ExerciseType)}
                  >
                    {TYPES.map((t) => (
                      <option key={t.value} value={t.value}>
                        {t.label}
                      </option>
                    ))}
                  </NativeSelect>
                </div>
                <div className="space-y-2">
                  <Label>Правильный ответ</Label>
                  <Input
                    value={exAnswer}
                    onChange={(e) => setExAnswer(e.target.value)}
                    required
                    maxLength={1000}
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label>Вопрос</Label>
                <Textarea
                  value={exQuestion}
                  onChange={(e) => setExQuestion(e.target.value)}
                  required
                  maxLength={1000}
                  rows={2}
                />
              </div>

              {exType === "MULTIPLE_CHOICE" && (
                <div className="space-y-2">
                  <Label>Варианты ответа</Label>
                  {exOptions.map((opt, i) => (
                    <div key={i} className="flex gap-2">
                      <Input
                        value={opt}
                        onChange={(e) => {
                          const next = [...exOptions];
                          next[i] = e.target.value;
                          setExOptions(next);
                        }}
                        placeholder={`Вариант ${i + 1}`}
                      />
                      {exOptions.length > 2 && (
                        <Button
                          type="button"
                          variant="outline"
                          size="icon"
                          onClick={() =>
                            setExOptions(exOptions.filter((_, j) => j !== i))
                          }
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      )}
                    </div>
                  ))}
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={() => setExOptions([...exOptions, ""])}
                  >
                    <Plus className="mr-1 h-4 w-4" /> Ещё вариант
                  </Button>
                </div>
              )}

              <div className="space-y-2">
                <Label>Пояснение (опционально)</Label>
                <Textarea
                  value={exExplain}
                  onChange={(e) => setExExplain(e.target.value)}
                  rows={2}
                  maxLength={1000}
                />
              </div>

              {createExerciseMutation.isError && (
                <p className="text-sm text-destructive">
                  {extractErrorMessage(createExerciseMutation.error)}
                </p>
              )}

              <div className="flex gap-2">
                <Button type="submit" disabled={createExerciseMutation.isPending}>
                  {createExerciseMutation.isPending ? "Создание..." : "Создать"}
                </Button>
                <Button type="button" variant="outline" onClick={resetExerciseForm}>
                  Отмена
                </Button>
              </div>
            </form>
          )}
        </section>
      </main>
    </>
  );
}
