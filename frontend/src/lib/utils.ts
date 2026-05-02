import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function formatPrice(price: number | null | undefined): string {
  if (price == null || price === 0) return "Бесплатно";
  return `${price.toLocaleString("ru-RU")} ₽`;
}

export function formatStudentCount(n: number): string {
  if (n >= 1000) return `${(n / 1000).toFixed(1).replace(".0", "")}K`;
  return String(n);
}
