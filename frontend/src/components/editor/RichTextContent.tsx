"use client";

import DOMPurify from "dompurify";
import { cn } from "@/lib/utils";
import { richTextToHtml } from "@/lib/rich-text";

interface RichTextContentProps {
  value: string | null | undefined;
  className?: string;
  emptyText?: string;
}

export function RichTextContent({ value, className, emptyText }: RichTextContentProps) {
  const html = richTextToHtml(value);

  if (!html) {
    return emptyText ? <p className={className}>{emptyText}</p> : null;
  }

  return (
    <div
      className={cn("rich-text-content prose max-w-none", className)}
      dangerouslySetInnerHTML={{
        __html: DOMPurify.sanitize(html, {
          USE_PROFILES: { html: true },
          ALLOWED_URI_REGEXP:
            /^(?:(?:(?:f|ht)tps?|mailto|tel):|[^a-z]|[a-z+.-]+(?:[^a-z+.-:]|$))/i,
        }),
      }}
    />
  );
}
