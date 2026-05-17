"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useQuery, useMutation } from "@tanstack/react-query";
import { Pencil, LogOut } from "lucide-react";
import { Navbar } from "@/components/layout/Navbar";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { NativeSelect } from "@/components/ui/native-select";
import { Progress } from "@/components/ui/progress";
import { CourseCard } from "@/components/courses/CourseCard";
import { userService } from "@/services/userService";
import { courseService } from "@/services/courseService";
import { useAuthStore } from "@/lib/auth-store";
import { resolveAssetUrl } from "@/lib/api";
import {
  LANGUAGE_LABELS,
  type CourseLanguage,
  type CourseLevel,
  type UserUpdateRequest,
} from "@/types/api";

const LEVELS: CourseLevel[] = ["A1", "A2", "B1", "B2", "C1", "C2"];
const LANGUAGES = Object.entries(LANGUAGE_LABELS) as [CourseLanguage, string][];

export default function ProfilePage() {
  const router = useRouter();
  const { user, isHydrated, logout, setUser } = useAuthStore();
  const [editingFirst, setEditingFirst] = useState(false);
  const [editingLast, setEditingLast] = useState(false);
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [nativeLanguage, setNativeLanguage] = useState<CourseLanguage | "">("");
  const [targetLanguage, setTargetLanguage] = useState<CourseLanguage | "">("");
  const [level, setLevel] = useState<CourseLevel | "">("");

  useEffect(() => {
    if (isHydrated && !user) {
      router.replace("/");
    }
  }, [isHydrated, user, router]);

  useEffect(() => {
    if (user) {
      setFirstName(user.firstName);
      setLastName(user.lastName);
      setNativeLanguage(user.nativeLanguage ?? "");
      setTargetLanguage(user.targetLanguage ?? "");
      setLevel(user.level ?? "");
    }
  }, [user]);

  const enrollmentsQuery = useQuery({
    queryKey: ["my-enrollments"],
    queryFn: () => courseService.myEnrollments({ page: 0, size: 6 }),
    enabled: !!user,
  });

  const updateMutation = useMutation({
    mutationFn: (data: UserUpdateRequest) => userService.updateMe(data),
    onSuccess: (updated) => setUser(updated),
  });

  const updateLanguagePreferences = () => {
    const payload: UserUpdateRequest = {};
    if (nativeLanguage) payload.nativeLanguage = nativeLanguage;
    if (targetLanguage) payload.targetLanguage = targetLanguage;
    if (level) payload.level = level;
    updateMutation.mutate(payload);
  };

  const overallProgress =
    enrollmentsQuery.data?.content.length
      ? Math.round(
          enrollmentsQuery.data.content.reduce(
            (sum, e) => sum + (e.progressPercentage ?? 0),
            0
          ) / enrollmentsQuery.data.content.length
        )
      : 0;

  if (!user) return <Navbar />;

  const isAdmin = user.role === "ADMIN";

  return (
    <>
      <Navbar />
      <main className="mx-auto max-w-7xl px-6 py-6 space-y-6">
        <section className="rounded-3xl bg-white p-8 shadow-sm">
          <h1 className="text-3xl font-bold">Личный кабинет</h1>

          <div className="mt-6 flex items-start gap-6">
            <Avatar className="h-28 w-28">
              <AvatarImage src={resolveAssetUrl(user.avatarUrl) ?? undefined} />
              <AvatarFallback className="text-2xl">
                {user.firstName[0]}
                {user.lastName[0]}
              </AvatarFallback>
            </Avatar>

            <div className="flex-1 space-y-3 max-w-xl">
              <NameRow
                value={firstName}
                editing={editingFirst}
                onChange={setFirstName}
                onToggle={() => {
                  if (editingFirst && firstName !== user.firstName) {
                    updateMutation.mutate({ firstName });
                  }
                  setEditingFirst((v) => !v);
                }}
              />
              <NameRow
                value={lastName}
                editing={editingLast}
                onChange={setLastName}
                onToggle={() => {
                  if (editingLast && lastName !== user.lastName) {
                    updateMutation.mutate({ lastName });
                  }
                  setEditingLast((v) => !v);
                }}
              />
            </div>

            <Button variant="outline" size="sm" onClick={() => { logout(); router.push("/"); }}>
              <LogOut className="mr-2 h-4 w-4" /> Выйти
            </Button>
          </div>

          {isAdmin ? (
            <div className="mt-8">
              <span className="inline-flex items-center rounded-lg bg-muted px-5 py-2.5 text-base font-semibold">
                Администратор
              </span>
            </div>
          ) : (
            <>
              <div className="mt-8">
                <h2 className="text-2xl font-bold">Мои языки:</h2>
                <div className="mt-3 grid gap-4 md:grid-cols-[1fr_1fr_160px_auto] md:items-end">
                  <div className="space-y-2">
                    <Label htmlFor="nativeLanguage">Родной язык</Label>
                    <NativeSelect
                      id="nativeLanguage"
                      value={nativeLanguage}
                      onChange={(e) => setNativeLanguage(e.target.value as CourseLanguage | "")}
                    >
                      <option value="">Не выбран</option>
                      {LANGUAGES.map(([code, label]) => (
                        <option key={code} value={code}>
                          {label}
                        </option>
                      ))}
                    </NativeSelect>
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="targetLanguage">Изучаемый язык</Label>
                    <NativeSelect
                      id="targetLanguage"
                      value={targetLanguage}
                      onChange={(e) => setTargetLanguage(e.target.value as CourseLanguage | "")}
                    >
                      <option value="">Не выбран</option>
                      {LANGUAGES.map(([code, label]) => (
                        <option key={code} value={code}>
                          {label}
                        </option>
                      ))}
                    </NativeSelect>
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="level">Уровень</Label>
                    <NativeSelect
                      id="level"
                      value={level}
                      onChange={(e) => setLevel(e.target.value as CourseLevel | "")}
                    >
                      <option value="">Не выбран</option>
                      {LEVELS.map((item) => (
                        <option key={item} value={item}>
                          {item}
                        </option>
                      ))}
                    </NativeSelect>
                  </div>

                  <Button
                    type="button"
                    onClick={updateLanguagePreferences}
                    disabled={updateMutation.isPending}
                  >
                    {updateMutation.isPending ? "Сохранение..." : "Сохранить"}
                  </Button>
                </div>
              </div>

              {/* Progress */}
              <div className="mt-8 rounded-2xl ring-1 ring-border p-6">
                <h3 className="text-xl font-bold">Ваш прогресс по курсам</h3>
                <div className="mt-4 flex items-center gap-4">
                  <div className="flex-1 relative">
                    <Progress value={overallProgress} className="h-12" />
                    <span className="absolute inset-0 flex items-center justify-end pr-4 font-semibold">
                      {overallProgress}%
                    </span>
                  </div>
                </div>
              </div>
            </>
          )}
        </section>

        {!isAdmin && <section>
          <div className="flex items-end justify-between">
            <h2 className="text-3xl font-bold">Мои курсы</h2>
            <Link href="/courses" className="text-primary hover:underline">
              Все курсы
            </Link>
          </div>

          <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {enrollmentsQuery.isLoading &&
              Array.from({ length: 3 }).map((_, i) => (
                <div key={i} className="h-44 animate-pulse rounded-2xl bg-muted" />
              ))}
            {enrollmentsQuery.data?.content.map((e) => (
              <CourseCard
                key={e.enrollmentId}
                course={{
                  id: e.courseId,
                  title: e.title,
                  language: e.language,
                  level: e.level,
                  price: e.price,
                  rating: e.rating,
                  reviewsCount: e.reviewsCount,
                  totalStudents: e.totalStudents,
                  coverImageUrl: e.coverImageUrl,
                  createdAt: e.enrolledAt,
                }}
              />
            ))}
            {enrollmentsQuery.data?.content.length === 0 && (
              <p className="col-span-full text-sm text-muted-foreground">
                У вас пока нет курсов. <Link href="/courses" className="text-primary underline">Найти курс</Link>
              </p>
            )}
          </div>
        </section>}
      </main>
    </>
  );
}

function NameRow({
  value,
  editing,
  onChange,
  onToggle,
}: {
  value: string;
  editing: boolean;
  onChange: (v: string) => void;
  onToggle: () => void;
}) {
  return (
    <div className="flex items-center gap-3">
      <Input
        value={value}
        onChange={(e) => onChange(e.target.value)}
        readOnly={!editing}
        className="bg-white ring-1 ring-border"
      />
      <button
        onClick={onToggle}
        className="text-primary hover:underline text-sm flex items-center gap-1"
      >
        <Pencil className="h-3.5 w-3.5" /> {editing ? "Сохранить" : "Редактировать"}
      </button>
    </div>
  );
}
