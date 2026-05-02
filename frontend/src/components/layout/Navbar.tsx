"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { AuthDialog } from "@/components/auth/AuthDialog";
import { Logo } from "./Logo";
import { useAuthStore } from "@/lib/auth-store";
import { resolveAssetUrl } from "@/lib/api";
import { cn as clsx } from "@/lib/utils";

const NAV_DEFAULT = [
  { href: "/courses", label: "Курсы" },
  { href: "/resources", label: "Ресурсы" },
  { href: "/chat", label: "Чат" },
  { href: "/about", label: "О нас" },
];

const NAV_ADMIN = [
  { href: "/admin/moderation", label: "Курсы" },
  { href: "/admin/complaints", label: "Жалобы" },
];

export function Navbar() {
  const pathname = usePathname();
  const user = useAuthStore((s) => s.user);
  const isHydrated = useAuthStore((s) => s.isHydrated);
  const [authOpen, setAuthOpen] = useState(false);
  const [authTab, setAuthTab] = useState<"login" | "register">("login");

  const openAuth = (tab: "login" | "register") => {
    setAuthTab(tab);
    setAuthOpen(true);
  };

  return (
    <>
      <header className="sticky top-0 z-40 bg-white shadow-sm">
        <div className="mx-auto flex h-20 max-w-7xl items-center gap-8 px-6">
          <Link href="/" className="flex items-center gap-3">
            <Logo />
            <span className="text-2xl font-bold tracking-tight">LinguaLink</span>
          </Link>

          <nav className="hidden md:flex items-center gap-8 text-base">
            {(isHydrated && user?.role === "ADMIN" ? NAV_ADMIN : NAV_DEFAULT).map((item) => {
              const active = pathname === item.href || pathname.startsWith(item.href + "/");
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={clsx(
                    "transition-colors",
                    active ? "text-primary font-medium" : "text-foreground hover:text-primary"
                  )}
                >
                  {item.label}
                </Link>
              );
            })}
            {isHydrated && user?.role === "CREATOR" && (
              <Link
                href="/studio"
                className={clsx(
                  "transition-colors",
                  pathname.startsWith("/studio")
                    ? "text-primary font-medium"
                    : "text-foreground hover:text-primary"
                )}
              >
                Студия
              </Link>
            )}
            {isHydrated && user && (
              <Link
                href="/profile"
                className={clsx(
                  "transition-colors",
                  pathname === "/profile"
                    ? "text-primary font-medium"
                    : "text-foreground hover:text-primary"
                )}
              >
                Личный кабинет
              </Link>
            )}
          </nav>

          <div className="ml-auto flex items-center gap-3">
            {isHydrated && user ? (
              <Link href="/profile">
                <Avatar className="h-11 w-11 ring-2 ring-transparent hover:ring-primary/30">
                  <AvatarImage src={resolveAssetUrl(user.avatarUrl) ?? undefined} />
                  <AvatarFallback>
                    {(user.firstName?.[0] ?? "") + (user.lastName?.[0] ?? "")}
                  </AvatarFallback>
                </Avatar>
              </Link>
            ) : isHydrated ? (
              <>
                <Button onClick={() => openAuth("login")} size="sm">
                  Войти
                </Button>
                <Button onClick={() => openAuth("register")} size="sm">
                  Регистрация
                </Button>
              </>
            ) : null}
          </div>
        </div>
      </header>

      <AuthDialog open={authOpen} onOpenChange={setAuthOpen} defaultTab={authTab} />
    </>
  );
}
