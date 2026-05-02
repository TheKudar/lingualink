"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useMutation } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { authService } from "@/services/authService";
import { userService } from "@/services/userService";
import { useAuthStore } from "@/lib/auth-store";
import { setStoredToken, extractErrorMessage } from "@/lib/api";

export function RegisterForm({ onSuccess }: { onSuccess?: () => void }) {
  const router = useRouter();
  const setAuth = useAuthStore((s) => s.setAuth);
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [wantTeach, setWantTeach] = useState(false);
  const [agreed, setAgreed] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: async () => {
      const [firstName, ...rest] = fullName.trim().split(/\s+/);
      const lastName = rest.join(" ") || firstName;
      // Username: derive from email local part + random tail, alphanumeric only
      const localPart = email.split("@")[0]?.replace(/[^a-zA-Z0-9._-]/g, "") || "user";
      const username = `${localPart}_${Math.random().toString(36).slice(2, 6)}`;

      const { accessToken } = await authService.register({
        email,
        username,
        firstName,
        lastName,
        password,
        role: wantTeach ? "CREATOR" : "STUDENT",
      });
      setStoredToken(accessToken);
      const me = await userService.getMe();
      return { accessToken, me };
    },
    onSuccess: ({ accessToken, me }) => {
      setAuth(accessToken, me);
      setError(null);
      onSuccess?.();
      router.push("/");
      router.refresh();
    },
    onError: (err) => {
      setError(extractErrorMessage(err));
    },
  });

  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        if (!agreed) {
          setError("Необходимо согласиться с условиями использования");
          return;
        }
        mutation.mutate();
      }}
      className="flex flex-col gap-1"
    >
      {error && (
        <p className="mb-2 text-sm text-destructive leading-snug">{error}</p>
      )}

      {/* Underlined inputs (different style from login) per Figma */}
      <div className="rounded-xl border border-muted-foreground/30 divide-y divide-muted-foreground/30">
        <input
          type="text"
          placeholder="Имя и фамилия"
          value={fullName}
          onChange={(e) => setFullName(e.target.value)}
          required
          autoComplete="name"
          className="block w-full bg-transparent px-4 py-3.5 text-base placeholder:text-muted-foreground focus:outline-none focus:ring-0"
        />
        <input
          type="email"
          placeholder="E-mail"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
          autoComplete="email"
          className="block w-full bg-transparent px-4 py-3.5 text-base placeholder:text-muted-foreground focus:outline-none focus:ring-0"
        />
        <input
          type="password"
          placeholder="Пароль"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          minLength={6}
          autoComplete="new-password"
          className="block w-full bg-transparent px-4 py-3.5 text-base placeholder:text-muted-foreground focus:outline-none focus:ring-0"
        />
      </div>

      <label className="mt-5 flex items-center gap-2 text-sm text-foreground/85 cursor-pointer">
        <Checkbox
          checked={wantTeach}
          onCheckedChange={(v) => setWantTeach(v === true)}
        />
        <span>Я хочу преподавать (создавать курсы)</span>
      </label>

      <label className="mt-3 flex items-start gap-2 text-sm text-foreground/80 leading-snug cursor-pointer">
        <Checkbox
          checked={agreed}
          onCheckedChange={(v) => setAgreed(v === true)}
          className="mt-0.5"
        />
        <span>
          Я соглашаюсь с{" "}
          <a className="text-primary hover:underline" href="#">
            условиями использования
          </a>
          ,{" "}
          <a className="text-primary hover:underline" href="#">
            политикой конфиденциальности
          </a>{" "}
          и разрешаю обрабатывать мои персональные данные
        </span>
      </label>

      <Button
        type="submit"
        variant="success"
        size="lg"
        className="mt-5 w-full text-lg"
        disabled={mutation.isPending}
      >
        {mutation.isPending ? "Регистрация..." : "Зарегистрироваться"}
      </Button>
    </form>
  );
}
