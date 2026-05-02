import { cn } from "@/lib/utils";
import type { CourseStatus } from "@/types/api";

const STYLES: Record<CourseStatus, { label: string; className: string }> = {
  DRAFT: { label: "Черновик", className: "bg-muted text-foreground/70" },
  PENDING_REVIEW: {
    label: "На модерации",
    className: "bg-amber-100 text-amber-700",
  },
  PUBLISHED: { label: "Опубликован", className: "bg-success/15 text-success" },
  REJECTED: {
    label: "Отклонён",
    className: "bg-destructive/15 text-destructive",
  },
  ARCHIVED: { label: "В архиве", className: "bg-muted text-muted-foreground" },
};

export function StatusBadge({ status }: { status: CourseStatus }) {
  const s = STYLES[status];
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-full px-3 py-1 text-xs font-medium",
        s.className
      )}
    >
      {s.label}
    </span>
  );
}
