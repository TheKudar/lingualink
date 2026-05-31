import { generateHTML, type JSONContent } from "@tiptap/core";
import Link from "@tiptap/extension-link";
import Placeholder from "@tiptap/extension-placeholder";
import TextAlign from "@tiptap/extension-text-align";
import { Color, FontSize, TextStyle } from "@tiptap/extension-text-style";
import Underline from "@tiptap/extension-underline";
import StarterKit from "@tiptap/starter-kit";

export const RICH_TEXT_FORMAT = "tiptap-json";

const ALLOWED_NODES = new Set([
  "doc",
  "paragraph",
  "text",
  "heading",
  "bulletList",
  "orderedList",
  "listItem",
  "blockquote",
  "codeBlock",
  "hardBreak",
]);

const ALLOWED_MARKS = new Set(["bold", "italic", "underline", "textStyle", "link", "code"]);
const ALLOWED_ALIGNMENTS = new Set(["left", "center", "right", "justify"]);
const FONT_SIZES = ["14px", "16px", "18px", "20px", "24px", "30px"];
const FONT_SIZE_SET = new Set(FONT_SIZES);
type RichTextMark = NonNullable<JSONContent["marks"]>[number];

export const richTextFontSizes = FONT_SIZES;

export const emptyRichTextDocument: JSONContent = {
  type: "doc",
  content: [{ type: "paragraph" }],
};

export const getRichTextExtensions = (placeholder?: string) => [
  StarterKit.configure({
    heading: {
      levels: [1, 2, 3],
    },
  }),
  Underline,
  TextStyle,
  Color,
  FontSize,
  TextAlign.configure({
    types: ["heading", "paragraph"],
    alignments: ["left", "center", "right", "justify"],
    defaultAlignment: "left",
  }),
  Link.configure({
    autolink: true,
    linkOnPaste: true,
    openOnClick: false,
    defaultProtocol: "https",
    protocols: ["http", "https", "mailto", "tel"],
    HTMLAttributes: {
      rel: "noopener noreferrer nofollow",
      target: "_blank",
    },
  }),
  ...(placeholder
    ? [
        Placeholder.configure({
          placeholder,
        }),
      ]
    : []),
];

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === "object" && value !== null && !Array.isArray(value);

const cleanText = (value: unknown): string | undefined => {
  if (typeof value !== "string") return undefined;
  return value.replace(/\u0000/g, "");
};

const cleanColor = (value: unknown): string | undefined => {
  if (typeof value !== "string") return undefined;
  const trimmed = value.trim();
  if (/^#[0-9a-fA-F]{3}([0-9a-fA-F]{3})?$/.test(trimmed)) return trimmed;
  if (/^rgb\(\s*(\d{1,3}\s*,\s*){2}\d{1,3}\s*\)$/.test(trimmed)) return trimmed;
  return undefined;
};

const cleanHref = (value: unknown): string | undefined => {
  if (typeof value !== "string") return undefined;
  const trimmed = value.trim();
  if (!trimmed) return undefined;
  if (trimmed.startsWith("/") && !trimmed.startsWith("//")) return trimmed;
  try {
    const url = new URL(trimmed);
    return ["http:", "https:", "mailto:", "tel:"].includes(url.protocol) ? trimmed : undefined;
  } catch {
    return undefined;
  }
};

const cleanNodeAttrs = (type: string, attrs: unknown): Record<string, unknown> | undefined => {
  if (!isRecord(attrs)) return undefined;

  const cleaned: Record<string, unknown> = {};

  if (type === "heading") {
    const level = Number(attrs.level);
    cleaned.level = [1, 2, 3].includes(level) ? level : 2;
  }

  if ((type === "heading" || type === "paragraph") && typeof attrs.textAlign === "string") {
    if (ALLOWED_ALIGNMENTS.has(attrs.textAlign)) cleaned.textAlign = attrs.textAlign;
  }

  return Object.keys(cleaned).length ? cleaned : undefined;
};

const cleanMarkAttrs = (type: string, attrs: unknown): Record<string, unknown> | undefined => {
  if (!isRecord(attrs)) return undefined;

  const cleaned: Record<string, unknown> = {};

  if (type === "textStyle") {
    const color = cleanColor(attrs.color);
    if (color) cleaned.color = color;

    if (typeof attrs.fontSize === "string" && FONT_SIZE_SET.has(attrs.fontSize)) {
      cleaned.fontSize = attrs.fontSize;
    }
  }

  if (type === "link") {
    const href = cleanHref(attrs.href);
    if (!href) return undefined;
    cleaned.href = href;
    cleaned.target = "_blank";
    cleaned.rel = "noopener noreferrer nofollow";
  }

  return Object.keys(cleaned).length ? cleaned : undefined;
};

const sanitizeMark = (mark: unknown): RichTextMark | null => {
  if (!isRecord(mark) || typeof mark.type !== "string" || !ALLOWED_MARKS.has(mark.type)) {
    return null;
  }

  const cleaned: RichTextMark = { type: mark.type };
  const attrs = cleanMarkAttrs(mark.type, mark.attrs);
  if (attrs) cleaned.attrs = attrs;
  return cleaned;
};

const sanitizeNode = (node: unknown): JSONContent | null => {
  if (!isRecord(node) || typeof node.type !== "string" || !ALLOWED_NODES.has(node.type)) {
    return null;
  }

  const cleaned: JSONContent = { type: node.type };
  const attrs = cleanNodeAttrs(node.type, node.attrs);
  if (attrs) cleaned.attrs = attrs;

  const text = cleanText(node.text);
  if (node.type === "text") {
    if (!text) return null;
    cleaned.text = text;
  }

  if (Array.isArray(node.marks)) {
    const marks = node.marks
      .map(sanitizeMark)
      .filter((mark): mark is RichTextMark => mark !== null);
    if (marks.length) cleaned.marks = marks;
  }

  if (Array.isArray(node.content)) {
    const content = node.content
      .map(sanitizeNode)
      .filter((child): child is JSONContent => child !== null);
    if (content.length) cleaned.content = content;
  }

  return cleaned;
};

export const sanitizeRichTextDocument = (doc: unknown): JSONContent => {
  const sanitized = sanitizeNode(doc);
  if (!sanitized || sanitized.type !== "doc") return emptyRichTextDocument;
  return sanitized.content?.length ? sanitized : emptyRichTextDocument;
};

export const plainTextToRichTextDocument = (text: string): JSONContent => {
  const lines = text.replace(/\r\n/g, "\n").split("\n");
  return {
    type: "doc",
    content: lines.map((line) => ({
      type: "paragraph",
      content: line ? [{ type: "text", text: line }] : undefined,
    })),
  };
};

export const parseRichTextDocument = (value: string | null | undefined): JSONContent => {
  if (!value?.trim()) return emptyRichTextDocument;

  try {
    const parsed = JSON.parse(value);
    if (isRecord(parsed) && parsed.type === "doc") {
      return sanitizeRichTextDocument(parsed);
    }
  } catch {
    return sanitizeRichTextDocument(plainTextToRichTextDocument(value));
  }

  return sanitizeRichTextDocument(plainTextToRichTextDocument(value));
};

export const normalizeRichTextValue = (value: string | null | undefined): string => {
  if (!value?.trim()) return "";
  return JSON.stringify(parseRichTextDocument(value));
};

export const serializeRichTextDocument = (doc: JSONContent): string => {
  const sanitized = sanitizeRichTextDocument(doc);
  return isRichTextEmpty(sanitized) ? "" : JSON.stringify(sanitized);
};

export const isRichTextEmpty = (doc: JSONContent): boolean => {
  if (!doc.content?.length) return true;

  const hasText = (node: JSONContent): boolean => {
    if (typeof node.text === "string" && node.text.trim()) return true;
    return node.content?.some(hasText) ?? false;
  };

  return !doc.content.some(hasText);
};

export const richTextToHtml = (value: string | null | undefined): string => {
  const doc = parseRichTextDocument(value);
  if (isRichTextEmpty(doc)) return "";
  return generateHTML(doc, getRichTextExtensions());
};
