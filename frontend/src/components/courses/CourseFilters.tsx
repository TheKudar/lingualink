"use client";

import { useState } from "react";
import { Dialog, DialogContent, DialogTitle } from "@/components/ui/dialog";
import { Checkbox } from "@/components/ui/checkbox";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import {
  LANGUAGE_LABELS,
  type CourseFilters as Filters,
  type CourseLanguage,
  type CourseLevel,
} from "@/types/api";

const LEVELS: CourseLevel[] = ["A1", "A2", "B1", "B2", "C1", "C2"];
const LANGUAGES: CourseLanguage[] = [
  "ENGLISH",
  "FRENCH",
  "GERMAN",
  "CHINESE",
  "SPANISH",
  "JAPANESE",
];

type Tab = "language" | "level" | "rating";

const TABS: { key: Tab; label: string }[] = [
  { key: "language", label: "Язык" },
  { key: "level", label: "Уровень" },
  { key: "rating", label: "Рейтинг" },
];

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  initial: Filters;
  onApply: (filters: Filters) => void;
}

export function CourseFiltersDialog({ open, onOpenChange, initial, onApply }: Props) {
  const [tab, setTab] = useState<Tab>("language");
  const [draft, setDraft] = useState<Filters>(initial);

  const reset = () => setDraft({});
  const apply = () => {
    onApply(draft);
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl p-6">
        <DialogTitle className="sr-only">Фильтры курсов</DialogTitle>

        <div className="grid grid-cols-[160px_1fr] gap-6">
          <nav className="flex flex-col gap-1">
            {TABS.map((t) => (
              <button
                key={t.key}
                type="button"
                onClick={() => setTab(t.key)}
                className={cn(
                  "rounded-lg px-3 py-2 text-left text-xl font-semibold transition-colors",
                  tab === t.key ? "text-primary" : "text-foreground/80 hover:text-primary"
                )}
              >
                {t.label}
              </button>
            ))}
          </nav>

          <div>
            {tab === "language" && (
              <div>
                <h3 className="text-xl font-semibold text-primary mb-4">Язык курса</h3>
                <div className="flex flex-col gap-3">
                  {LANGUAGES.map((lang) => (
                    <label key={lang} className="flex cursor-pointer items-center gap-3 text-base">
                      <Checkbox
                        checked={draft.language === lang}
                        onCheckedChange={(v) =>
                          setDraft((d) => ({ ...d, language: v === true ? lang : undefined }))
                        }
                      />
                      {LANGUAGE_LABELS[lang]}
                    </label>
                  ))}
                </div>
              </div>
            )}

            {tab === "level" && (
              <div>
                <h3 className="text-xl font-semibold text-primary mb-4">Уровень владения</h3>
                <div className="flex flex-col gap-3">
                  {LEVELS.map((lvl) => (
                    <label key={lvl} className="flex cursor-pointer items-center gap-3 text-base">
                      <Checkbox
                        checked={draft.level === lvl}
                        onCheckedChange={(v) =>
                          setDraft((d) => ({ ...d, level: v === true ? lvl : undefined }))
                        }
                      />
                      {lvl}
                    </label>
                  ))}
                </div>
              </div>
            )}

            {tab === "rating" && (
              <div>
                <h3 className="text-xl font-semibold text-primary mb-4">Минимальный рейтинг</h3>
                <div className="flex flex-col gap-3">
                  {[5, 4, 3].map((r) => (
                    <label key={r} className="flex cursor-pointer items-center gap-3 text-base">
                      <Checkbox
                        checked={draft.minRating === r}
                        onCheckedChange={(v) =>
                          setDraft((d) => ({ ...d, minRating: v === true ? r : undefined }))
                        }
                      />
                      от {r} ★
                    </label>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>

        <div className="mt-6 flex gap-3">
          <Button onClick={apply}>Применить</Button>
          <Button variant="outline" onClick={reset}>
            Сбросить
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
