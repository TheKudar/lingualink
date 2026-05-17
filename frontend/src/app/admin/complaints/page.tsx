"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Ban, Check, ChevronDown, ChevronUp } from "lucide-react";
import { Navbar } from "@/components/layout/Navbar";
import { Button } from "@/components/ui/button";
import { useAuthStore } from "@/lib/auth-store";
import { extractErrorMessage } from "@/lib/api";
import { cn } from "@/lib/utils";
import { reportService } from "@/services/reportService";

export default function AdminComplaintsPage() {
  const router = useRouter();
  const { user, isHydrated } = useAuthStore();
  const queryClient = useQueryClient();
  const [openId, setOpenId] = useState<number | null>(null);

  const reportsQuery = useQuery({
    queryKey: ["reports"],
    queryFn: reportService.listAll,
    enabled: user?.role === "ADMIN",
  });

  const banMutation = useMutation({
    mutationFn: reportService.banCourse,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["reports"] }),
  });

  const keepMutation = useMutation({
    mutationFn: reportService.keepCourse,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["reports"] }),
  });

  useEffect(() => {
    if (isHydrated && (!user || user.role !== "ADMIN")) router.replace("/");
  }, [isHydrated, user, router]);

  if (!user || user.role !== "ADMIN") return <Navbar />;

  return (
    <>
      <Navbar />
      <main className="mx-auto max-w-7xl px-6 py-6">
        <div className="rounded-3xl bg-white p-8 shadow-sm min-h-[600px]">
          <h1 className="text-3xl font-bold mb-6">Жалобы</h1>

          <div className="flex flex-col gap-3">
            {reportsQuery.isLoading &&
              Array.from({ length: 3 }).map((_, index) => (
                <div key={index} className="h-16 animate-pulse rounded-lg bg-muted" />
              ))}

            {reportsQuery.data?.map((report) => {
              const open = openId === report.id;
              return (
                <div key={report.id} className="rounded-lg bg-muted overflow-hidden">
                  <button
                    onClick={() => setOpenId(open ? null : report.id)}
                    className="flex w-full items-center justify-between gap-4 px-5 py-3 text-left font-semibold hover:bg-muted/70 transition-colors"
                  >
                    <span>
                      Жалоба на курс #{report.courseId} от пользователя #{report.userId}
                    </span>
                    {open ? (
                      <ChevronUp className="h-4 w-4 shrink-0" />
                    ) : (
                      <ChevronDown className="h-4 w-4 shrink-0" />
                    )}
                  </button>
                  <div
                    className={cn(
                      "px-5 overflow-hidden transition-all duration-200",
                      open ? "py-3 max-h-96" : "py-0 max-h-0"
                    )}
                  >
                    <div className="space-y-2 text-sm text-foreground/80">
                      <p>
                        <span className="font-semibold">ID курса:</span>{" "}
                        {report.courseId}
                      </p>
                      <p>
                        <span className="font-semibold">ID пользователя:</span>{" "}
                        {report.userId}
                      </p>
                      <p>
                        <span className="font-semibold">Дата создания:</span>{" "}
                        {new Date(report.createdAt).toLocaleString()}
                      </p>
                      <p className="whitespace-pre-wrap">
                        <span className="font-semibold">Сообщение:</span>{" "}
                        {report.message}
                      </p>
                      <div className="flex flex-wrap gap-2 pt-2">
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => keepMutation.mutate(report.id)}
                          disabled={keepMutation.isPending || banMutation.isPending}
                        >
                          <Check className="mr-1 h-4 w-4" />
                          Оставить курс
                        </Button>
                        <Button
                          size="sm"
                          variant="outline"
                          className="text-destructive hover:text-destructive"
                          onClick={() => banMutation.mutate(report.id)}
                          disabled={keepMutation.isPending || banMutation.isPending}
                        >
                          <Ban className="mr-1 h-4 w-4" />
                          Забанить курс
                        </Button>
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>

          {reportsQuery.isError && (
            <p className="mt-4 text-sm text-destructive">
              {extractErrorMessage(reportsQuery.error)}
            </p>
          )}

          {(banMutation.isError || keepMutation.isError) && (
            <p className="mt-4 text-sm text-destructive">
              {extractErrorMessage(banMutation.error ?? keepMutation.error)}
            </p>
          )}

          {reportsQuery.data?.length === 0 && (
            <p className="text-foreground/60">Жалоб пока нет.</p>
          )}
        </div>
      </main>
    </>
  );
}
