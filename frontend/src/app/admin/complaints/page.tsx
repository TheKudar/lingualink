"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { ChevronDown, ChevronUp } from "lucide-react";
import { Navbar } from "@/components/layout/Navbar";
import { useAuthStore } from "@/lib/auth-store";
import { cn } from "@/lib/utils";

// Backend doesn't support complaints yet — using placeholder data.
// Replace with real `complaintsService` call when backend adds /api/complaints.
const COMPLAINTS_STUB = [
  { id: 1, title: "Жалоба №1", body: "Пример содержимого жалобы." },
];

export default function AdminComplaintsPage() {
  const router = useRouter();
  const { user, isHydrated } = useAuthStore();
  const [openId, setOpenId] = useState<number | null>(null);

  useEffect(() => {
    if (isHydrated && (!user || user.role !== "ADMIN")) router.replace("/");
  }, [isHydrated, user, router]);

  if (!user || user.role !== "ADMIN") return <Navbar />;

  return (
    <>
      <Navbar />
      <main className="mx-auto max-w-7xl px-6 py-6">
        <div className="rounded-3xl bg-white p-8 shadow-sm min-h-[600px]">
          <h1 className="text-3xl font-bold mb-6">Жалобы</h1>

          <div className="flex flex-col gap-3">
            {COMPLAINTS_STUB.map((c) => {
              const open = openId === c.id;
              return (
                <div key={c.id} className="rounded-lg bg-muted overflow-hidden">
                  <button
                    onClick={() => setOpenId(open ? null : c.id)}
                    className="flex w-full items-center justify-between px-5 py-3 text-left font-semibold hover:bg-muted/70 transition-colors"
                  >
                    <span>{c.title}</span>
                    {open ? (
                      <ChevronUp className="h-4 w-4" />
                    ) : (
                      <ChevronDown className="h-4 w-4" />
                    )}
                  </button>
                  <div
                    className={cn(
                      "px-5 overflow-hidden transition-all duration-200",
                      open ? "py-3 max-h-96" : "py-0 max-h-0"
                    )}
                  >
                    <p className="text-foreground/80">{c.body}</p>
                  </div>
                </div>
              );
            })}
          </div>

          {COMPLAINTS_STUB.length === 0 && (
            <p className="text-foreground/60">Жалоб нет.</p>
          )}

          <p className="mt-8 text-xs text-muted-foreground">
            * Раздел в разработке. Бэкенд для жалоб появится позже.
          </p>
        </div>
      </main>
    </>
  );
}
