"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useMutation } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { authService } from "@/services/authService";
import { userService } from "@/services/userService";
import { useAuthStore } from "@/lib/auth-store";
import { setStoredToken, extractErrorMessage } from "@/lib/api";

export function LoginForm({ onSuccess }: { onSuccess?: () => void }) {
  const router = useRouter();
  const setAuth = useAuthStore((s) => s.setAuth);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: async () => {
      const { accessToken } = await authService.login({ email, password });
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
        mutation.mutate();
      }}
      className="flex flex-col gap-4"
    >
      {error && (
        <p className="text-sm text-destructive leading-snug">{error}</p>
      )}

      <Input
        type="email"
        placeholder="E-mail"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        required
        autoComplete="email"
      />
      <Input
        type="password"
        placeholder="Пароль"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        required
        autoComplete="current-password"
      />

      <Button
        type="submit"
        size="lg"
        className="w-full text-lg"
        disabled={mutation.isPending}
      >
        {mutation.isPending ? "Вход..." : "Войти"}
      </Button>

      <button
        type="button"
        className="self-start text-sm text-muted-foreground underline-offset-2 underline hover:text-foreground"
      >
        Забыли пароль?
      </button>
    </form>
  );
}
