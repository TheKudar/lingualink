"use client";

import { Navbar } from "@/components/layout/Navbar";

const RESOURCES = [
  "Книги",
  "Переводчик",
  "Мой словарь",
  "Англо-русский словарь",
  "Франко-русский словарь",
  "Немецко-русский словарь",
  "Итальяно-русский словарь",
  "Испано-русский словарь",
];

export default function ResourcesPage() {
  return (
    <>
      <Navbar />
      <main className="mx-auto max-w-7xl px-6 py-6">
        <div className="rounded-3xl bg-white p-8 shadow-sm">
          <ul className="flex flex-col gap-3">
            {RESOURCES.map((item) => (
              <li
                key={item}
                className="rounded-lg bg-muted px-5 py-3 text-base font-medium hover:bg-muted/70 cursor-pointer transition-colors"
              >
                {item}
              </li>
            ))}
          </ul>
        </div>
      </main>
    </>
  );
}
