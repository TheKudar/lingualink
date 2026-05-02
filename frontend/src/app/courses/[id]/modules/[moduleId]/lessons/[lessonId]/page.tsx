"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useQuery, useMutation } from "@tanstack/react-query";
import { Navbar } from "@/components/layout/Navbar";
import { Button } from "@/components/ui/button";
import { courseService } from "@/services/courseService";

export default function LessonPage() {
  const params = useParams<{ id: string; moduleId: string; lessonId: string }>();
  const courseId = Number(params.id);
  const moduleId = Number(params.moduleId);
  const lessonId = Number(params.lessonId);

  const courseQuery = useQuery({
    queryKey: ["course", courseId],
    queryFn: () => courseService.getById(courseId),
  });
  const lessonQuery = useQuery({
    queryKey: ["lesson", lessonId],
    queryFn: () => courseService.getLesson(courseId, moduleId, lessonId),
  });
  const exercisesQuery = useQuery({
    queryKey: ["lesson", lessonId, "exercises"],
    queryFn: () => courseService.listExercises(courseId, moduleId, lessonId),
  });

  const completeMutation = useMutation({
    mutationFn: () => courseService.completeLesson(courseId, moduleId, lessonId),
  });

  return (
    <>
      <Navbar />
      <main className="mx-auto max-w-5xl px-6 py-6">
        <article className="rounded-3xl bg-white p-10 shadow-sm">
          <h1 className="text-2xl font-semibold leading-tight">
            {courseQuery.data?.title}
          </h1>
          <h2 className="mt-6 text-lg font-medium">{lessonQuery.data?.title}</h2>

          <div className="mt-6 whitespace-pre-wrap text-base leading-relaxed text-foreground/85">
            {lessonQuery.isLoading ? "Загрузка..." : lessonQuery.data?.content}
          </div>

          {exercisesQuery.data && exercisesQuery.data.length > 0 && (
            <div className="mt-10 flex flex-col gap-2">
              {exercisesQuery.data.map((ex, idx) => (
                <Link
                  key={ex.id}
                  href={`/courses/${courseId}/modules/${moduleId}/lessons/${lessonId}/exercises/${ex.id}`}
                  className="text-primary text-lg hover:underline"
                >
                  Упражнение {idx + 1}
                </Link>
              ))}
            </div>
          )}

          <div className="mt-10">
            <Button
              variant="success"
              onClick={() => completeMutation.mutate()}
              disabled={completeMutation.isPending || completeMutation.isSuccess}
            >
              {completeMutation.isSuccess
                ? "Урок отмечен пройденным"
                : completeMutation.isPending
                ? "Сохранение..."
                : "Отметить как пройденный"}
            </Button>
          </div>
        </article>
      </main>
    </>
  );
}
