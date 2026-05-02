"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Navbar } from "@/components/layout/Navbar";
import { Button } from "@/components/ui/button";
import { AuthDialog } from "@/components/auth/AuthDialog";
import { CourseCard } from "@/components/courses/CourseCard";
import { courseService } from "@/services/courseService";
import { useAuthStore } from "@/lib/auth-store";

export default function HomePage() {
  const [authOpen, setAuthOpen] = useState(false);
  const [authTab, setAuthTab] = useState<"login" | "register">("register");
  const user = useAuthStore((s) => s.user);

  const popularQuery = useQuery({
    queryKey: ["courses", "popular"],
    queryFn: () =>
      courseService.listPublished(undefined, { page: 0, size: 3, sort: "totalStudents,desc" }),
  });

  const openAuth = (tab: "login" | "register") => {
    setAuthTab(tab);
    setAuthOpen(true);
  };

  return (
    <>
      <Navbar />

      <main className="mx-auto max-w-7xl px-6 py-6">
        {/* Hero */}
        <section
          className="relative overflow-hidden rounded-3xl bg-cover bg-center text-white"
          style={{
            backgroundImage:
              "linear-gradient(rgba(40,50,70,0.55), rgba(40,50,70,0.55)), url('https://images.unsplash.com/photo-1488646953014-85cb44e25828?auto=format&fit=crop&w=1600&q=80')",
          }}
        >
          <div className="px-10 py-16 md:px-16 md:py-24 max-w-2xl">
            <h1 className="text-4xl md:text-5xl font-bold leading-tight">
              Изучай языки легко<br />и увлекательно!
            </h1>
            <p className="mt-6 text-lg md:text-xl opacity-95">
              Выбирай курсы, проходи уроки и общайся с единомышленниками
            </p>
            {!user && (
              <div className="mt-8 flex flex-wrap gap-4">
                <Button size="lg" onClick={() => openAuth("register")}>
                  Регистрация
                </Button>
                <Button size="lg" variant="white" onClick={() => openAuth("login")}>
                  Войти
                </Button>
              </div>
            )}
          </div>
        </section>

        {/* Popular courses */}
        <section className="mt-8 rounded-3xl bg-white p-8 shadow-sm">
          <h2 className="text-3xl font-bold">Популярные курсы</h2>

          <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {popularQuery.isLoading &&
              Array.from({ length: 3 }).map((_, i) => (
                <div key={i} className="h-44 animate-pulse rounded-2xl bg-muted" />
              ))}
            {popularQuery.data?.content.map((course) => (
              <CourseCard key={course.id} course={course} />
            ))}
            {popularQuery.isError && (
              <p className="col-span-full text-sm text-muted-foreground">
                Не удалось загрузить курсы. Убедитесь, что backend запущен на http://localhost:8080
              </p>
            )}
            {popularQuery.data && popularQuery.data.content.length === 0 && (
              <p className="col-span-full text-sm text-muted-foreground">Пока нет курсов.</p>
            )}
          </div>
        </section>
      </main>

      <AuthDialog open={authOpen} onOpenChange={setAuthOpen} defaultTab={authTab} />
    </>
  );
}
