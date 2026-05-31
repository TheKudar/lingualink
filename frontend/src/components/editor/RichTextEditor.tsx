"use client";

import { useEffect } from "react";
import { EditorContent, useEditor } from "@tiptap/react";
import type { Editor } from "@tiptap/core";
import type { ReactNode } from "react";
import {
  AlignCenter,
  AlignJustify,
  AlignLeft,
  AlignRight,
  Bold,
  Code2,
  Italic,
  Link2,
  List,
  ListOrdered,
  Quote,
  Redo2,
  Underline as UnderlineIcon,
  Undo2,
  X,
} from "lucide-react";
import { cn } from "@/lib/utils";
import {
  getRichTextExtensions,
  isRichTextEmpty,
  parseRichTextDocument,
  richTextFontSizes,
  serializeRichTextDocument,
} from "@/lib/rich-text";

interface RichTextEditorProps {
  id?: string;
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  minHeightClassName?: string;
}

const COLORS = [
  "#111827",
  "#dc2626",
  "#ea580c",
  "#16a34a",
  "#2563eb",
  "#7c3aed",
];

const FONT_SIZE_LABELS: Record<string, string> = {
  "14px": "Мелкий",
  "16px": "Обычный",
  "18px": "Средний",
  "20px": "Крупный",
  "24px": "Большой",
  "30px": "Очень большой",
};

function ToolbarButton({
  label,
  active,
  disabled,
  onClick,
  children,
}: {
  label: string;
  active?: boolean;
  disabled?: boolean;
  onClick: () => void;
  children: ReactNode;
}) {
  return (
    <button
      type="button"
      title={label}
      aria-label={label}
      aria-pressed={active}
      disabled={disabled}
      onClick={onClick}
      className={cn(
        "inline-flex h-9 w-9 items-center justify-center rounded-md border border-transparent text-foreground/75 transition-colors hover:bg-muted hover:text-foreground disabled:pointer-events-none disabled:opacity-40",
        active && "border-primary/30 bg-accent text-primary"
      )}
    >
      {children}
    </button>
  );
}

function MenuSection({ children }: { children: ReactNode }) {
  return <div className="flex items-center gap-1 border-r border-border pr-2 last:border-r-0 last:pr-0">{children}</div>;
}

function setLink(editor: Editor) {
  const previousUrl = editor.getAttributes("link").href as string | undefined;
  const url = window.prompt("Введите ссылку", previousUrl ?? "https://");

  if (url === null) return;

  if (url.trim() === "") {
    editor.chain().focus().extendMarkRange("link").unsetLink().run();
    return;
  }

  editor.chain().focus().extendMarkRange("link").setLink({ href: url.trim() }).run();
}

export function RichTextEditor({
  id,
  value,
  onChange,
  placeholder = "Введите текст",
  minHeightClassName = "min-h-48",
}: RichTextEditorProps) {
  const editor = useEditor(
    {
      extensions: getRichTextExtensions(placeholder),
      content: parseRichTextDocument(value),
      immediatelyRender: false,
      editorProps: {
        attributes: {
          class: cn(
            "rich-text-editor-content prose max-w-none px-4 py-3 text-base leading-relaxed outline-none",
            minHeightClassName
          ),
        },
      },
      onUpdate: ({ editor }) => {
        onChange(isRichTextEmpty(editor.getJSON()) ? "" : serializeRichTextDocument(editor.getJSON()));
      },
    },
    []
  );

  useEffect(() => {
    if (!editor) return;
    const nextDocument = parseRichTextDocument(value);
    if (JSON.stringify(editor.getJSON()) !== JSON.stringify(nextDocument)) {
      editor.commands.setContent(nextDocument, { emitUpdate: false });
    }
  }, [editor, value]);

  if (!editor) {
    return (
      <div className="rounded-xl border border-input bg-white">
        <div className="border-b border-border px-3 py-2 text-sm text-muted-foreground">
          Загрузка редактора...
        </div>
        <div className={cn("px-4 py-3", minHeightClassName)} />
      </div>
    );
  }

  const currentFontSize = (editor.getAttributes("textStyle").fontSize as string | undefined) ?? "16px";

  return (
    <div id={id} className="rounded-xl border border-input bg-white focus-within:ring-2 focus-within:ring-ring">
      <div className="flex flex-wrap items-center gap-2 border-b border-border px-3 py-2">
        <MenuSection>
          <ToolbarButton label="Полужирный" active={editor.isActive("bold")} onClick={() => editor.chain().focus().toggleBold().run()}>
            <Bold className="h-4 w-4" />
          </ToolbarButton>
          <ToolbarButton label="Курсив" active={editor.isActive("italic")} onClick={() => editor.chain().focus().toggleItalic().run()}>
            <Italic className="h-4 w-4" />
          </ToolbarButton>
          <ToolbarButton label="Подчеркнутый" active={editor.isActive("underline")} onClick={() => editor.chain().focus().toggleUnderline().run()}>
            <UnderlineIcon className="h-4 w-4" />
          </ToolbarButton>
        </MenuSection>

        <MenuSection>
          <select
            title="Заголовок"
            aria-label="Заголовок"
            className="h-9 rounded-md border border-input bg-white px-2 text-sm"
            value={
              editor.isActive("heading", { level: 1 })
                ? "h1"
                : editor.isActive("heading", { level: 2 })
                ? "h2"
                : editor.isActive("heading", { level: 3 })
                ? "h3"
                : "paragraph"
            }
            onChange={(event) => {
              const value = event.target.value;
              if (value === "paragraph") editor.chain().focus().setParagraph().run();
              if (value === "h1") editor.chain().focus().toggleHeading({ level: 1 }).run();
              if (value === "h2") editor.chain().focus().toggleHeading({ level: 2 }).run();
              if (value === "h3") editor.chain().focus().toggleHeading({ level: 3 }).run();
            }}
          >
            <option value="paragraph">Обычный текст</option>
            <option value="h1">Заголовок 1</option>
            <option value="h2">Заголовок 2</option>
            <option value="h3">Заголовок 3</option>
          </select>

          <select
            title="Размер текста"
            aria-label="Размер текста"
            className="h-9 rounded-md border border-input bg-white px-2 text-sm"
            value={richTextFontSizes.includes(currentFontSize) ? currentFontSize : "16px"}
            onChange={(event) => editor.chain().focus().setFontSize(event.target.value).run()}
          >
            {richTextFontSizes.map((size) => (
              <option key={size} value={size}>
                {FONT_SIZE_LABELS[size]}
              </option>
            ))}
          </select>
        </MenuSection>

        <MenuSection>
          <div className="flex items-center gap-1" role="group" aria-label="Цвет текста">
            {COLORS.map((color) => (
              <button
                key={color}
                type="button"
                title={`Цвет текста ${color}`}
                aria-label={`Цвет текста ${color}`}
                onClick={() => editor.chain().focus().setColor(color).run()}
                className={cn(
                  "h-7 w-7 rounded-full border border-border",
                  editor.isActive("textStyle", { color }) && "ring-2 ring-primary ring-offset-2"
                )}
                style={{ backgroundColor: color }}
              />
            ))}
            <ToolbarButton label="Сбросить цвет" onClick={() => editor.chain().focus().unsetColor().run()}>
              <X className="h-4 w-4" />
            </ToolbarButton>
          </div>
        </MenuSection>

        <MenuSection>
          <ToolbarButton label="Выровнять по левому краю" active={editor.isActive({ textAlign: "left" })} onClick={() => editor.chain().focus().setTextAlign("left").run()}>
            <AlignLeft className="h-4 w-4" />
          </ToolbarButton>
          <ToolbarButton label="Выровнять по центру" active={editor.isActive({ textAlign: "center" })} onClick={() => editor.chain().focus().setTextAlign("center").run()}>
            <AlignCenter className="h-4 w-4" />
          </ToolbarButton>
          <ToolbarButton label="Выровнять по правому краю" active={editor.isActive({ textAlign: "right" })} onClick={() => editor.chain().focus().setTextAlign("right").run()}>
            <AlignRight className="h-4 w-4" />
          </ToolbarButton>
          <ToolbarButton label="Выровнять по ширине" active={editor.isActive({ textAlign: "justify" })} onClick={() => editor.chain().focus().setTextAlign("justify").run()}>
            <AlignJustify className="h-4 w-4" />
          </ToolbarButton>
        </MenuSection>

        <MenuSection>
          <ToolbarButton label="Маркированный список" active={editor.isActive("bulletList")} onClick={() => editor.chain().focus().toggleBulletList().run()}>
            <List className="h-4 w-4" />
          </ToolbarButton>
          <ToolbarButton label="Нумерованный список" active={editor.isActive("orderedList")} onClick={() => editor.chain().focus().toggleOrderedList().run()}>
            <ListOrdered className="h-4 w-4" />
          </ToolbarButton>
          <ToolbarButton label="Цитата" active={editor.isActive("blockquote")} onClick={() => editor.chain().focus().toggleBlockquote().run()}>
            <Quote className="h-4 w-4" />
          </ToolbarButton>
          <ToolbarButton label="Блок кода" active={editor.isActive("codeBlock")} onClick={() => editor.chain().focus().toggleCodeBlock().run()}>
            <Code2 className="h-4 w-4" />
          </ToolbarButton>
          <ToolbarButton label="Ссылка" active={editor.isActive("link")} onClick={() => setLink(editor)}>
            <Link2 className="h-4 w-4" />
          </ToolbarButton>
        </MenuSection>

        <MenuSection>
          <ToolbarButton label="Отменить" disabled={!editor.can().undo()} onClick={() => editor.chain().focus().undo().run()}>
            <Undo2 className="h-4 w-4" />
          </ToolbarButton>
          <ToolbarButton label="Повторить" disabled={!editor.can().redo()} onClick={() => editor.chain().focus().redo().run()}>
            <Redo2 className="h-4 w-4" />
          </ToolbarButton>
        </MenuSection>
      </div>

      <EditorContent editor={editor} />
    </div>
  );
}
