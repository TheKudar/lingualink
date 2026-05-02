"use client";

import { useEffect } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { Plus } from "lucide-react";
import { Navbar } from "@/components/layout/Navbar";
import { Button } from "@/components/ui/button";
import { courseService } from "@/services/courseService";
import { useAuthStore } from "@/lib/auth-store";
import { resolveAssetUrl } from "@/lib/api";
import { LANGUAGE_LABELS } from "@/types/api";

export default function StudioPage() {
  const router = useRouter();
  const { user, isHydrated } = useAuthStore();

  useEffect(() => {
    if (isHydrated && !user) router.replace("/");
  }, [isHydrated, user, router]);

  const myQuery = useQuery({
    queryKey: ["my-courses"],
    queryFn: () => courseService.myCourses(0, 20),
    enabled: !!user,
  });

  if (!user) return <Navbar />;

  return (
    <>
      <Navbar />
      <main className="mx-auto max-w-7xl px-6 py-6">
        <div className="rounded-3xl bg-white p-8 shadow-sm">
          <div className="flex items-end justify-between gap-4">
            <div>
              <h1 className="text-3xl font-bold">Мои курсы</h1>
              <p className="mt-1 text-foreground/70">
                Создавайте курсы, добавляйте модули и публикуйте после модерации
              </p>
            </div>
            <Link href="/studio/courses/new">
              <Button>
                <Plus className="mr-2 h-5 w-5" /> Создать курс
              </Button>
            </Link>
          </div>

          <div className="mt-8 flex flex-col gap-3">
            {myQuery.isLoading &&
              Array.from({ length: 3 }).map((_, i) => (
                <div key={i} className="h-20 animate-pulse rounded-2xl bg-muted" />
              ))}
            {myQuery.data?.content.length === 0 && (
              <div className="rounded-2xl border-2 border-dashed border-border p-10 text-center">
                <p className="text-foreground/70">У вас пока нет курсов</p>
                <Link href="/studio/courses/new" className="mt-3 inline-block">
                  <Button variant="outline" size="sm">
                    Создать первый курс
                  </Button>
                </Link>
              </div>
            )}
            {myQuery.data?.content.map((c) => (
              <Link
                key={c.id}
                href={`/studio/courses/${c.id}`}
                className="flex items-center gap-4 rounded-2xl bg-muted/40 p-4 ring-1 ring-transparent hover:ring-primary/40 transition-all"
              >
                {c.coverImageUrl ? (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img
                    src={resolveAssetUrl(c.coverImageUrl) ?? ""}
                    alt={c.title}
                    className="h-16 w-16 rounded-lg object-cover"
                  />
                ) : (
                  <div className="h-16 w-16 rounded-lg bg-muted" />
                )}
                <div className="flex-1 min-w-0">
                  <p className="font-semibold text-base truncate">{c.title}</p>
                  <p className="text-sm text-foreground/60 mt-0.5">
                    {LANGUAGE_LABELS[c.language]} · {c.level}
                  </p>
                </div>
              </Link>
            ))}
          </div>
        </div>
      </main>
    </>
  );
}
