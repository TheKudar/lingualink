"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { ArrowLeft, Plus, Trash2 } from "lucide-react";
import { Navbar } from "@/components/layout/Navbar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { courseService } from "@/services/courseService";
import { useAuthStore } from "@/lib/auth-store";
import { extractErrorMessage } from "@/lib/api";

export default function ModuleEditorPage() {
  const router = useRouter();
  const params = useParams<{ id: string; mid: string }>();
  const courseId = Number(params.id);
  const moduleId = Number(params.mid);
  const queryClient = useQueryClient();
  const { user, isHydrated } = useAuthStore();
  const [newLesson, setNewLesson] = useState("");

  useEffect(() => {
    if (isHydrated && !user) router.replace("/");
  }, [isHydrated, user, router]);

  const modulesQuery = useQuery({
    queryKey: ["course", courseId, "modules"],
    queryFn: () => courseService.listModules(courseId),
    enabled: !!user,
  });

  const courseModule = modulesQuery.data?.find((m) => m.id === moduleId);

  const createLessonMutation = useMutation({
    mutationFn: () =>
      courseService.createLesson(courseId, moduleId, {
        title: newLesson,
        orderIndex: courseModule?.lessons?.length ?? 0,
      }),
    onSuccess: () => {
      setNewLesson("");
      queryClient.invalidateQueries({ queryKey: ["course", courseId, "modules"] });
    },
  });

  const deleteLessonMutation = useMutation({
    mutationFn: (lessonId: number) =>
      courseService.removeLesson(courseId, moduleId, lessonId),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: ["course", courseId, "modules"] }),
  });

  if (!user || !modulesQuery.data) return <Navbar />;
  if (!courseModule) {
    return (
      <>
        <Navbar />
        <main className="mx-auto max-w-3xl px-6 py-6">
          <p>Модуль не найден.</p>
        </main>
      </>
    );
  }

  return (
    <>
      <Navbar />
      <main className="mx-auto max-w-4xl px-6 py-6 space-y-6">
        <Link
          href={`/studio/courses/${courseId}`}
          className="inline-flex items-center gap-2 text-sm text-foreground/70 hover:text-foreground"
        >
          <ArrowLeft className="h-4 w-4" /> К курсу
        </Link>

        <section className="rounded-3xl bg-white p-8 shadow-sm">
          <h1 className="text-2xl font-bold">{courseModule.title}</h1>
          {courseModule.description && (
            <p className="mt-2 text-foreground/70">{courseModule.description}</p>
          )}

          <h2 className="mt-8 text-xl font-semibold">Уроки</h2>

          <div className="mt-4 flex flex-col gap-2">
            {courseModule.lessons?.map((l, i) => (
              <div
                key={l.id}
                className="group flex items-center gap-3 rounded-lg bg-muted/60 px-4 py-3"
              >
                <Link
                  href={`/studio/courses/${courseId}/modules/${moduleId}/lessons/${l.id}`}
                  className="flex-1 font-medium hover:text-primary transition-colors"
                >
                  Урок {i + 1}: {l.title}
                </Link>
                <button
                  onClick={() => {
                    if (confirm("Удалить урок и все его упражнения?")) {
                      deleteLessonMutation.mutate(l.id);
                    }
                  }}
                  className="text-muted-foreground opacity-0 group-hover:opacity-100 hover:text-destructive transition-opacity"
                >
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>
            ))}
            {(!courseModule.lessons || courseModule.lessons.length === 0) && (
              <p className="text-sm text-muted-foreground">Пока нет уроков.</p>
            )}
          </div>

          <form
            className="mt-4 flex gap-2"
            onSubmit={(e) => {
              e.preventDefault();
              if (newLesson.trim()) createLessonMutation.mutate();
            }}
          >
            <Input
              value={newLesson}
              onChange={(e) => setNewLesson(e.target.value)}
              placeholder="Название нового урока"
              maxLength={100}
            />
            <Button type="submit" disabled={!newLesson.trim() || createLessonMutation.isPending}>
              <Plus className="mr-1 h-4 w-4" /> Добавить
            </Button>
          </form>

          {createLessonMutation.isError && (
            <p className="mt-2 text-sm text-destructive">
              {extractErrorMessage(createLessonMutation.error)}
            </p>
          )}
        </section>
      </main>
    </>
  );
}
