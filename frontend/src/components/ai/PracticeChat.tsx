"use client";

import { useEffect, useRef, useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { LanguageSelect } from "./LanguageSelect";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { extractErrorMessage } from "@/lib/api";
import { languageToolsService, type PracticeTurn } from "@/services/languageToolsService";
import type { CourseLanguage } from "@/types/api";

export function PracticeChat({ initialLanguage }: { initialLanguage: CourseLanguage }) {
  const [language, setLanguage] = useState(initialLanguage);
  const [turns, setTurns] = useState<PracticeTurn[]>([]);
  const [draft, setDraft] = useState("");
  const log = useRef<HTMLDivElement>(null);
  const chat = useMutation({
    mutationFn: (message: string) => languageToolsService.chat(language, message, turns),
    onSuccess: (assistant, user) => {
      setTurns(previous => [...previous, { user, assistant }]);
      setDraft("");
    },
  });

  useEffect(() => {
    if (log.current) log.current.scrollTop = log.current.scrollHeight;
  }, [turns, chat.isPending]);

  return (
    <details className="mb-6 rounded-2xl bg-white p-5 shadow-sm">
      <summary className="cursor-pointer font-semibold text-primary">Языковая практика с ИИ</summary>
      <div className="mt-4 space-y-4">
        <LanguageSelect label="Язык практики" value={language} disabled={chat.isPending}
          onChange={next => { setLanguage(next); setTurns([]); chat.reset(); }} />
        {(turns.length > 0 || chat.isPending) && (
          <div ref={log} role="log" aria-label="Диалог с ИИ"
            className="max-h-80 space-y-3 overflow-y-auto rounded-xl border p-4">
            {turns.map((turn, index) =>
              <div key={index} className="space-y-2 whitespace-pre-wrap break-words">
                <p><strong>Вы</strong> · {turn.user}</p>
                <p><strong>Собеседник</strong> · {turn.assistant}</p>
              </div>
            )}
            {chat.isPending && <p className="text-muted-foreground">Собеседник отвечает…</p>}
          </div>
        )}
        <form className="space-y-3" onSubmit={event => {
          event.preventDefault();
          if (draft.trim() && !chat.isPending) chat.mutate(draft.trim());
        }}>
          <Textarea aria-label="Сообщение для языковой практики" placeholder="Начните разговор на выбранном языке…"
            maxLength={2000} value={draft} disabled={chat.isPending} onChange={e => setDraft(e.target.value)} />
          {chat.isError && <p role="alert" className="text-sm text-destructive">{extractErrorMessage(chat.error)}</p>}
          <Button type="submit" disabled={!draft.trim() || chat.isPending}>Отправить</Button>
        </form>
      </div>
    </details>
  );
}
