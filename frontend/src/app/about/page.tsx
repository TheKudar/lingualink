"use client";

import { Navbar } from "@/components/layout/Navbar";

export default function AboutPage() {
  return (
    <>
      <Navbar />
      <main className="mx-auto max-w-7xl px-6 py-6">
        <div className="rounded-3xl bg-white p-12 shadow-sm min-h-[600px] flex items-center justify-center">
          <div className="text-center max-w-2xl">
            <h1 className="text-4xl font-bold">О нас</h1>
            <p className="mt-6 text-lg text-foreground/70">
              LinguaLink — платформа для изучения иностранных языков, объединяющая курсы,
              практику и общение. Раздел в разработке.
            </p>
          </div>
        </div>
      </main>
    </>
  );
}
