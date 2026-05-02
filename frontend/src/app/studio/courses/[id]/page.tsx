"use client";

import { useEffect, useRef, useState } from "react";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { ArrowLeft, Image as ImageIcon, Plus, Send, Trash2, Archive } from "lucide-react";
import { Navbar } from "@/components/layout/Navbar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { CourseForm } from "@/components/studio/CourseForm";
import { StatusBadge } from "@/components/studio/StatusBadge";
import { courseService } from "@/services/courseService";
import { useAuthStore } from "@/lib/auth-store";
import { resolveAssetUrl, extractErrorMessage } from "@/lib/api";

export default function StudioCoursePage() {
  const router = useRouter();
  const params = useParams<{ id: string }>();
  const courseId = Number(params.id);
  const queryClient = useQueryClient();
  const { user, isHydrated } = useAuthStore();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [newModuleTitle, setNewModuleTitle] = useState("");

  useEffect(() => {
    if (isHydrated && !user) router.replace("/");
  }, [isHydrated, user, router]);

  const courseQuery = useQuery({
    queryKey: ["course", courseId],
    queryFn: () => courseService.getById(courseId),
    enabled: !!user && Number.isFinite(courseId),
  });

  const modulesQuery = useQuery({
    queryKey: ["course", courseId, "modules"],
    queryFn: () => courseService.listModules(courseId),
    enabled: !!user && Number.isFinite(courseId),
  });

  const updateMutation = useMutation({
    mutationFn: (data: Parameters<typeof courseService.update>[1]) =>
      courseService.update(courseId, data),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["course", courseId] }),
  });

  const uploadCoverMutation = useMutation({
    mutationFn: (file: File) => courseService.uploadCover(courseId, file),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["course", courseId] }),
  });

  const submitReviewMutation = useMutation({
    mutationFn: () => courseService.submitForReview(courseId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["course", courseId] }),
  });

  const archiveMutation = useMutation({
    mutationFn: () => courseService.archive(courseId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["course", courseId] }),
  });

  const deleteMutation = useMutation({
    mutationFn: () => courseService.remove(courseId),
    onSuccess: () => router.push("/studio"),
  });

  const createModuleMutation = useMutation({
    mutationFn: () =>
      courseService.createModule(courseId, {
        title: newModuleTitle,
        orderIndex: modulesQuery.data?.length ?? 0,
      }),
    onSuccess: () => {
      setNewModuleTitle("");
      queryClient.invalidateQueries({ queryKey: ["course", courseId, "modules"] });
    },
  });

  const deleteModuleMutation = useMutation({
    mutationFn: (moduleId: number) => courseService.removeModule(courseId, moduleId),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: ["course", courseId, "modules"] }),
  });

  if (!user || !courseQuery.data) return <Navbar />;

  const course = courseQuery.data;
  const cover = resolveAssetUrl(course.coverImageUrl);
  const isDraft = course.status === "DRAFT" || course.status === "REJECTED";
  const isPublished = course.status === "PUBLISHED";

  return (
    <>
      <Navbar />
      <main className="mx-auto max-w-5xl px-6 py-6 space-y-6">
        <Link
          href="/studio"
          className="inline-flex items-center gap-2 text-sm text-foreground/70 hover:text-foreground"
        >
          <ArrowLeft className="h-4 w-4" /> К моим курсам
        </Link>

        {/* Header */}
        <section className="rounded-3xl bg-white p-8 shadow-sm">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div className="flex items-start gap-5">
              <div className="relative">
                {cover ? (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img
                    src={cover}
                    alt={course.title}
                    className="h-24 w-24 rounded-xl object-cover"
                  />
                ) : (
                  <div className="flex h-24 w-24 items-center justify-center rounded-xl bg-muted text-muted-foreground">
                    <ImageIcon className="h-7 w-7" />
                  </div>
                )}
                <button
                  onClick={() => fileInputRef.current?.click()}
                  className="absolute -bottom-1 -right-1 rounded-full bg-primary p-1.5 text-white shadow hover:bg-primary/90"
                  title="Загрузить обложку"
                >
                  <ImageIcon className="h-3.5 w-3.5" />
                </button>
                <input
                  ref={fileInputRef}
                  type="file"
                  accept="image/*"
                  className="hidden"
                  onChange={(e) => {
                    const f = e.target.files?.[0];
                    if (f) uploadCoverMutation.mutate(f);
                    e.target.value = "";
                  }}
                />
              </div>

              <div>
                <h1 className="text-2xl font-bold leading-tight">{course.title}</h1>
                <div className="mt-2 flex items-center gap-3">
                  <StatusBadge status={course.status} />
                  {course.rejectionReason && (
                    <span className="text-sm text-destructive">
                      Причина: {course.rejectionReason}
                    </span>
                  )}
                </div>
              </div>
            </div>

            <div className="flex flex-wrap gap-2">
              {isDraft && (
                <Button
                  variant="success"
                  onClick={() => submitReviewMutation.mutate()}
                  disabled={submitReviewMutation.isPending}
                >
                  <Send className="mr-2 h-4 w-4" />
                  {submitReviewMutation.isPending ? "Отправка..." : "На модерацию"}
                </Button>
              )}
              {isPublished && (
                <Button
                  variant="outline"
                  onClick={() => archiveMutation.mutate()}
                  disabled={archiveMutation.isPending}
                >
                  <Archive className="mr-2 h-4 w-4" /> В архив
                </Button>
              )}
              {isDraft && (
                <Button
                  variant="outline"
                  onClick={() => {
                    if (confirm("Удалить курс? Это действие необратимо.")) {
                      deleteMutation.mutate();
                    }
                  }}
                  className="text-destructive hover:text-destructive"
                >
                  <Trash2 className="mr-2 h-4 w-4" /> Удалить
                </Button>
              )}
            </div>
          </div>

          {(submitReviewMutation.isError || uploadCoverMutation.isError) && (
            <p className="mt-3 text-sm text-destructive">
              {extractErrorMessage(
                submitReviewMutation.error ?? uploadCoverMutation.error
              )}
            </p>
          )}
        </section>

        {/* Edit info */}
        <section className="rounded-3xl bg-white p-8 shadow-sm">
          <h2 className="text-xl font-semibold mb-4">Информация о курсе</h2>
          <CourseForm
            defaultValues={{
              title: course.title,
              description: course.description,
              language: course.language,
              level: course.level,
              price: course.price,
            }}
            submitLabel="Сохранить изменения"
            isPending={updateMutation.isPending}
            error={updateMutation.isError ? extractErrorMessage(updateMutation.error) : null}
            onSubmit={(data) => updateMutation.mutate(data)}
          />
        </section>

        {/* Modules */}
        <section className="rounded-3xl bg-white p-8 shadow-sm">
          <h2 className="text-xl font-semibold">Модули</h2>

          <div className="mt-5 flex flex-col gap-2">
            {modulesQuery.data?.map((m, i) => (
              <div
                key={m.id}
                className="group flex items-center gap-3 rounded-lg bg-muted/60 px-4 py-3"
              >
                <Link
                  href={`/studio/courses/${courseId}/modules/${m.id}`}
                  className="flex-1 font-medium hover:text-primary transition-colors"
                >
                  Модуль {i + 1}: {m.title}
                </Link>
                <button
                  onClick={() => {
                    if (confirm("Удалить модуль и все его уроки?")) {
                      deleteModuleMutation.mutate(m.id);
                    }
                  }}
                  className="text-muted-foreground opacity-0 group-hover:opacity-100 hover:text-destructive transition-opacity"
                >
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>
            ))}
            {modulesQuery.data?.length === 0 && (
              <p className="text-sm text-muted-foreground">
                Пока нет модулей. Добавьте первый.
              </p>
            )}
          </div>

          <form
            className="mt-4 flex gap-2"
            onSubmit={(e) => {
              e.preventDefault();
              if (newModuleTitle.trim()) createModuleMutation.mutate();
            }}
          >
            <Input
              value={newModuleTitle}
              onChange={(e) => setNewModuleTitle(e.target.value)}
              placeholder="Название нового модуля"
              maxLength={100}
            />
            <Button type="submit" disabled={!newModuleTitle.trim() || createModuleMutation.isPending}>
              <Plus className="mr-1 h-4 w-4" /> Добавить
            </Button>
          </form>
        </section>
      </main>
    </>
  );
}
