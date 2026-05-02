"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Check, X } from "lucide-react";
import { Navbar } from "@/components/layout/Navbar";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import {
  Dialog,
  DialogContent,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/dialog";
import { StatusBadge } from "@/components/studio/StatusBadge";
import { courseService } from "@/services/courseService";
import { useAuthStore } from "@/lib/auth-store";
import { resolveAssetUrl, extractErrorMessage } from "@/lib/api";
import { LANGUAGE_LABELS, type CourseResponse } from "@/types/api";

export default function AdminModerationPage() {
  const router = useRouter();
  const { user, isHydrated } = useAuthStore();
  const queryClient = useQueryClient();
  const [rejectTarget, setRejectTarget] = useState<CourseResponse | null>(null);
  const [rejectReason, setRejectReason] = useState("");

  useEffect(() => {
    if (isHydrated && (!user || user.role !== "ADMIN")) router.replace("/");
  }, [isHydrated, user, router]);

  const pendingQuery = useQuery({
    queryKey: ["admin", "pending"],
    queryFn: () => courseService.pendingForModeration(0, 30),
    enabled: !!user && user.role === "ADMIN",
  });

  const approveMutation = useMutation({
    mutationFn: (id: number) => courseService.approve(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin", "pending"] }),
  });

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: number; reason: string }) =>
      courseService.reject(id, reason),
    onSuccess: () => {
      setRejectTarget(null);
      setRejectReason("");
      queryClient.invalidateQueries({ queryKey: ["admin", "pending"] });
    },
  });

  if (!user || user.role !== "ADMIN") return <Navbar />;

  return (
    <>
      <Navbar />
      <main className="mx-auto max-w-7xl px-6 py-6">
        <div className="rounded-3xl bg-white p-8 shadow-sm">
          <h1 className="text-3xl font-bold">Модерация курсов</h1>
          <p className="mt-1 text-foreground/70">
            Курсы, ожидающие проверки перед публикацией
          </p>

          <div className="mt-8 flex flex-col gap-3">
            {pendingQuery.isLoading &&
              Array.from({ length: 3 }).map((_, i) => (
                <div key={i} className="h-24 animate-pulse rounded-2xl bg-muted" />
              ))}

            {pendingQuery.data?.content.length === 0 && (
              <div className="rounded-2xl border-2 border-dashed border-border p-10 text-center text-foreground/60">
                Нет курсов на модерации
              </div>
            )}

            {pendingQuery.data?.content.map((c) => (
              <div
                key={c.id}
                className="flex flex-col gap-4 rounded-2xl bg-muted/40 p-5 sm:flex-row sm:items-start"
              >
                {c.coverImageUrl ? (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img
                    src={resolveAssetUrl(c.coverImageUrl) ?? ""}
                    alt={c.title}
                    className="h-20 w-20 shrink-0 rounded-lg object-cover"
                  />
                ) : (
                  <div className="h-20 w-20 shrink-0 rounded-lg bg-muted" />
                )}

                <div className="flex-1 min-w-0">
                  <div className="flex flex-wrap items-center gap-2">
                    <h3 className="font-semibold text-base">{c.title}</h3>
                    <StatusBadge status={c.status} />
                  </div>
                  <p className="mt-1 text-sm text-foreground/60">
                    {c.creatorName} · {LANGUAGE_LABELS[c.language]} · {c.level}
                  </p>
                  <p className="mt-2 text-sm text-foreground/80 line-clamp-3">
                    {c.description}
                  </p>
                </div>

                <div className="flex gap-2 sm:flex-col">
                  <Button
                    size="sm"
                    variant="success"
                    onClick={() => approveMutation.mutate(c.id)}
                    disabled={approveMutation.isPending}
                  >
                    <Check className="mr-1 h-4 w-4" /> Одобрить
                  </Button>
                  <Button
                    size="sm"
                    variant="outline"
                    className="text-destructive hover:text-destructive"
                    onClick={() => setRejectTarget(c)}
                  >
                    <X className="mr-1 h-4 w-4" /> Отклонить
                  </Button>
                </div>
              </div>
            ))}

            {(approveMutation.isError || rejectMutation.isError) && (
              <p className="text-sm text-destructive">
                {extractErrorMessage(approveMutation.error ?? rejectMutation.error)}
              </p>
            )}
          </div>
        </div>
      </main>

      <Dialog
        open={!!rejectTarget}
        onOpenChange={(open) => {
          if (!open) {
            setRejectTarget(null);
            setRejectReason("");
          }
        }}
      >
        <DialogContent className="p-6">
          <DialogTitle>Отклонить курс</DialogTitle>
          <DialogDescription>
            Укажите причину — автор увидит её и сможет исправить курс.
          </DialogDescription>
          <Textarea
            value={rejectReason}
            onChange={(e) => setRejectReason(e.target.value)}
            rows={5}
            placeholder="Причина отклонения..."
            className="mt-4"
          />
          <div className="mt-4 flex justify-end gap-2">
            <Button variant="outline" onClick={() => setRejectTarget(null)}>
              Отмена
            </Button>
            <Button
              variant="success"
              onClick={() => {
                if (rejectTarget && rejectReason.trim()) {
                  rejectMutation.mutate({ id: rejectTarget.id, reason: rejectReason.trim() });
                }
              }}
              disabled={!rejectReason.trim() || rejectMutation.isPending}
            >
              Отклонить
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </>
  );
}
