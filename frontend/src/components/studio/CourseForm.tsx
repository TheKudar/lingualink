"use client";

import { useState } from "react";
import { RichTextEditor } from "@/components/editor/RichTextEditor";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { NativeSelect } from "@/components/ui/native-select";
import {
  isRichTextEmpty,
  normalizeRichTextValue,
  parseRichTextDocument,
  serializeRichTextDocument,
} from "@/lib/rich-text";
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
  const [description, setDescription] = useState(
    normalizeRichTextValue(defaultValues?.description)
  );
  const [localError, setLocalError] = useState<string | null>(null);
  const [language, setLanguage] = useState<CourseLanguage>(
    defaultValues?.language ?? "ENGLISH"
  );
  const [level, setLevel] = useState<CourseLevel>(defaultValues?.level ?? "A1");
  const [price, setPrice] = useState<string>(
    defaultValues?.price != null ? String(defaultValues.price) : "0"
  );

  return (
    <form
      onSubmit={(event) => {
        event.preventDefault();
        const descriptionDocument = parseRichTextDocument(description);

        if (isRichTextEmpty(descriptionDocument)) {
          setLocalError("Добавьте описание курса.");
          return;
        }

        setLocalError(null);
        onSubmit({
          title,
          description: serializeRichTextDocument(descriptionDocument),
          language,
          level,
          price: Number(price) || 0,
        });
      }}
      className="space-y-5"
    >
      {(error || localError) && (
        <p className="rounded-lg bg-destructive/10 px-4 py-2 text-sm text-destructive">
          {error || localError}
        </p>
      )}

      <div className="space-y-2">
        <Label htmlFor="title">Название курса</Label>
        <Input
          id="title"
          value={title}
          onChange={(event) => setTitle(event.target.value)}
          maxLength={100}
          required
          placeholder="Например: Французский для начинающих"
        />
      </div>

      <div className="space-y-2">
        <Label htmlFor="description">Описание</Label>
        <RichTextEditor
          id="description"
          value={description}
          onChange={(nextValue) => {
            setDescription(nextValue);
            setLocalError(null);
          }}
          placeholder="О чем этот курс, чему научится студент"
          minHeightClassName="min-h-40"
        />
      </div>

      <div className="grid gap-5 sm:grid-cols-2">
        <div className="space-y-2">
          <Label htmlFor="language">Язык курса</Label>
          <NativeSelect
            id="language"
            value={language}
            onChange={(event) => setLanguage(event.target.value as CourseLanguage)}
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
            onChange={(event) => setLevel(event.target.value as CourseLevel)}
          >
            {LEVELS.map((level) => (
              <option key={level} value={level}>
                {level}
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
          onChange={(event) => setPrice(event.target.value)}
          placeholder="0 - бесплатно"
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
