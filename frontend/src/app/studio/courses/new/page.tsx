"use client";

import { useEffect } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useMutation } from "@tanstack/react-query";
import { ArrowLeft } from "lucide-react";
import { Navbar } from "@/components/layout/Navbar";
import { CourseForm } from "@/components/studio/CourseForm";
import { courseService } from "@/services/courseService";
import { useAuthStore } from "@/lib/auth-store";
import { extractErrorMessage } from "@/lib/api";

export default function NewCoursePage() {
  const router = useRouter();
  const { user, isHydrated } = useAuthStore();

  useEffect(() => {
    if (isHydrated && !user) router.replace("/");
  }, [isHydrated, user, router]);

  const createMutation = useMutation({
    mutationFn: courseService.create,
    onSuccess: (created) => {
      router.push(`/studio/courses/${created.id}`);
    },
  });

  return (
    <>
      <Navbar />
      <main className="mx-auto max-w-3xl px-6 py-6">
        <Link
          href="/studio"
          className="inline-flex items-center gap-2 text-sm text-foreground/70 hover:text-foreground"
        >
          <ArrowLeft className="h-4 w-4" /> К моим курсам
        </Link>

        <div className="mt-4 rounded-3xl bg-white p-8 shadow-sm">
          <h1 className="text-3xl font-bold mb-6">Новый курс</h1>
          <CourseForm
            submitLabel="Создать курс"
            isPending={createMutation.isPending}
            error={createMutation.isError ? extractErrorMessage(createMutation.error) : null}
            onSubmit={(data) => createMutation.mutate(data)}
          />
        </div>
      </main>
    </>
  );
}
