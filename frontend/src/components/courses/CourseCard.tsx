"use client";

import Link from "next/link";
import { useState } from "react";
import { Heart, Star, User as UserIcon, Clock } from "lucide-react";
import { cn } from "@/lib/utils";
import { formatPrice, formatStudentCount } from "@/lib/utils";
import { resolveAssetUrl } from "@/lib/api";
import type { CourseSummaryResponse } from "@/types/api";

interface Props {
  course: CourseSummaryResponse;
  durationHours?: number;
  authorName?: string;
}

export function CourseCard({ course, durationHours, authorName }: Props) {
  const [favorite, setFavorite] = useState(false);
  const cover = resolveAssetUrl(course.coverImageUrl);

  return (
    <Link
      href={`/courses/${course.id}`}
      className="group flex flex-col rounded-2xl bg-white p-4 shadow-sm transition-shadow hover:shadow-md ring-1 ring-transparent hover:ring-primary/20"
    >
      <div className="flex items-start gap-3">
        <div className="flex-1">
          <h3 className="text-base font-semibold leading-snug line-clamp-3">{course.title}</h3>
          {authorName && (
            <p className="mt-2 text-sm text-foreground/70">{authorName}</p>
          )}
        </div>

        <div className="relative shrink-0">
          {cover ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img
              src={cover}
              alt={course.title}
              className="h-16 w-16 rounded-md object-cover"
            />
          ) : (
            <div className="h-16 w-16 rounded-md bg-muted" />
          )}
          <button
            type="button"
            aria-label={favorite ? "Убрать из избранного" : "В избранное"}
            onClick={(e) => {
              e.preventDefault();
              setFavorite((v) => !v);
            }}
            className="absolute -top-1.5 -right-1.5"
          >
            <Heart
              className={cn(
                "h-5 w-5 stroke-[2.5] transition-colors",
                favorite ? "fill-red-500 text-red-500" : "fill-transparent text-muted-foreground/40"
              )}
            />
          </button>
        </div>
      </div>

      <div className="mt-4 flex items-center gap-4 text-sm text-foreground/70">
        <span className="flex items-center gap-1">
          <Star className="h-4 w-4 fill-foreground text-foreground" />
          {course.rating.toFixed(1)}
        </span>
        <span className="flex items-center gap-1">
          <UserIcon className="h-4 w-4" />
          {formatStudentCount(course.totalStudents)}
        </span>
        {durationHours != null && (
          <span className="flex items-center gap-1">
            <Clock className="h-4 w-4" />
            {durationHours} ч.
          </span>
        )}
      </div>

      <div className="mt-2 text-base font-semibold text-primary">
        {formatPrice(course.price)}
      </div>
    </Link>
  );
}
