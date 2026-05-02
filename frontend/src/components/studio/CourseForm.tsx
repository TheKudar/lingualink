"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { NativeSelect } from "@/components/ui/native-select";
import {
  LANGUAGE_LABELS,
  type CourseCreateRequest,
  type CourseLanguage,
  type CourseLevel,
} from "@/types/api";

interface Props {
  defaultValues?: Partial<CourseCreateRequest>;
  submitLabel?: string;
  onSubmit: (data: CourseCreateRequest) => void;
  isPending?: boolean;
  error?: string | null;
}

const LEVELS: CourseLevel[] = ["A1", "A2", "B1", "B2", "C1", "C2"];
const LANGUAGES = Object.entries(LANGUAGE_LABELS) as [CourseLanguage, string][];

export function CourseForm({
  defaultValues,
  submitLabel = "Сохранить",
  onSubmit,
  isPending,
  error,
}: Props) {
  const [title, setTitle] = useState(defaultValues?.title ?? "");
  const [description, setDescription] = useState(defaultValues?.description ?? "");
  const [language, setLanguage] = useState<CourseLanguage>(
    defaultValues?.language ?? "ENGLISH"
  );
  const [level, setLevel] = useState<CourseLevel>(defaultValues?.level ?? "A1");
  const [price, setPrice] = useState<string>(
    defaultValues?.price != null ? String(defaultValues.price) : "0"
  );

  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        onSubmit({
          title,
          description,
          language,
          level,
          price: Number(price) || 0,
        });
      }}
      className="space-y-5"
    >
      {error && (
        <p className="rounded-lg bg-destructive/10 px-4 py-2 text-sm text-destructive">
          {error}
        </p>
      )}

      <div className="space-y-2">
        <Label htmlFor="title">Название курса</Label>
        <Input
          id="title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          maxLength={100}
          required
          placeholder="Например: Французский для начинающих"
        />
      </div>

      <div className="space-y-2">
        <Label htmlFor="description">Описание</Label>
        <Textarea
          id="description"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          maxLength={1000}
          required
          rows={5}
          placeholder="О чём этот курс, чему научится студент"
        />
      </div>

      <div className="grid gap-5 sm:grid-cols-2">
        <div className="space-y-2">
          <Label htmlFor="language">Язык курса</Label>
          <NativeSelect
            id="language"
            value={language}
            onChange={(e) => setLanguage(e.target.value as CourseLanguage)}
          >
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
            onChange={(e) => setLevel(e.target.value as CourseLevel)}
          >
            {LEVELS.map((l) => (
              <option key={l} value={l}>
                {l}
              </option>
            ))}
          </NativeSelect>
        </div>
      </div>

      <div className="space-y-2 max-w-xs">
        <Label htmlFor="price">Цена (₽)</Label>
        <Input
          id="price"
          type="number"
          min={0}
          step={100}
          value={price}
          onChange={(e) => setPrice(e.target.value)}
          placeholder="0 — бесплатно"
        />
      </div>

      <div className="pt-2">
        <Button type="submit" disabled={isPending}>
          {isPending ? "Сохранение..." : submitLabel}
        </Button>
      </div>
    </form>
  );
}
