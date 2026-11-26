"use client";

import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { LanguageSelect } from "./LanguageSelect";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { extractErrorMessage } from "@/lib/api";
import { languageToolsService } from "@/services/languageToolsService";
import type { CourseLanguage } from "@/types/api";

export function Translator({ initialLanguage }: { initialLanguage: CourseLanguage }) {
  const [target, setTarget] = useState(initialLanguage);
  const [text, setText] = useState("");
  const translation = useMutation({
    mutationFn: () => languageToolsService.translate(target, text.trim()),
  });

  return (
    <details className="mb-6 rounded-2xl bg-white p-5 shadow-sm">
      <summary className="cursor-pointer font-semibold text-primary">Переводчик</summary>
      <form className="mt-4 space-y-3" onSubmit={event => {
        event.preventDefault();
        if (text.trim() && !translation.isPending) translation.mutate();
      }}>
        <LanguageSelect label="Язык перевода" value={target} disabled={translation.isPending}
          onChange={language => { setTarget(language); translation.reset(); }} />
        <Textarea aria-label="Исходный текст" placeholder="Введите текст для перевода…"
          maxLength={4000} value={text} disabled={translation.isPending}
          onChange={event => { setText(event.target.value); translation.reset(); }} />
        <Button type="submit" disabled={!text.trim() || translation.isPending}>
          {translation.isPending ? "Переводим…" : "Перевести"}
        </Button>
        {translation.isError && <p role="alert" className="text-sm text-destructive">{extractErrorMessage(translation.error)}</p>}
        {translation.data && (
          <div role="status" className="rounded-xl bg-input-soft p-4">
            <h2 className="mb-1 font-medium">Перевод</h2>
            <p className="whitespace-pre-wrap break-words">{translation.data}</p>
          </div>
        )}
      </form>
    </details>
  );
}
