"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Send } from "lucide-react";
import { Navbar } from "@/components/layout/Navbar";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { chatService } from "@/services/chatService";
import { useAuthStore } from "@/lib/auth-store";
import { resolveAssetUrl } from "@/lib/api";
import { cn } from "@/lib/utils";

export default function ChatPage() {
  const router = useRouter();
  const { user, isHydrated } = useAuthStore();
  const queryClient = useQueryClient();
  const [activeId, setActiveId] = useState<number | null>(null);
  const [input, setInput] = useState("");
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (isHydrated && !user) router.replace("/");
  }, [isHydrated, user, router]);

  const conversationsQuery = useQuery({
    queryKey: ["conversations"],
    queryFn: () => chatService.listConversations(),
    enabled: !!user,
  });

  const messagesQuery = useQuery({
    queryKey: ["messages", activeId],
    queryFn: () => chatService.listMessages(activeId!),
    enabled: activeId != null,
    refetchInterval: 5000,
  });

  const sendMutation = useMutation({
    mutationFn: ({ conversationId, content }: { conversationId: number; content: string }) =>
      chatService.sendMessage(conversationId, content),
    onSuccess: () => {
      setInput("");
      queryClient.invalidateQueries({ queryKey: ["messages", activeId] });
    },
  });

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messagesQuery.data]);

  const activeConv = useMemo(
    () => conversationsQuery.data?.find((c) => c.id === activeId) ?? null,
    [conversationsQuery.data, activeId]
  );

  const send = (e: React.FormEvent) => {
    e.preventDefault();
    if (!activeId || !input.trim()) return;
    sendMutation.mutate({ conversationId: activeId, content: input.trim() });
  };

  if (!user) return <Navbar />;

  return (
    <>
      <Navbar />
      <main className="mx-auto max-w-7xl px-6 py-6">
        <div className="grid grid-cols-[260px_1fr] gap-6">
          {/* Sidebar */}
          <aside className="flex flex-col gap-4">
            <div className="rounded-2xl bg-white shadow-sm overflow-hidden">
              <div className="bg-primary text-white px-4 py-2 text-center font-medium">
                Чаты
              </div>
              <ul className="divide-y divide-border">
                {conversationsQuery.data?.length === 0 && (
                  <li className="px-4 py-3 text-sm text-muted-foreground">
                    Пока нет диалогов
                  </li>
                )}
                {conversationsQuery.data?.map((c) => (
                  <li key={c.id}>
                    <button
                      onClick={() => setActiveId(c.id)}
                      className={cn(
                        "flex w-full items-center gap-3 px-3 py-2.5 text-left transition-colors",
                        activeId === c.id ? "bg-input-soft" : "hover:bg-muted/50"
                      )}
                    >
                      <Avatar className="h-9 w-9">
                        <AvatarImage
                          src={resolveAssetUrl(c.participantAvatarUrl) ?? undefined}
                        />
                        <AvatarFallback>
                          {c.participantFirstName[0] ?? "?"}
                        </AvatarFallback>
                      </Avatar>
                      <span className="font-medium">{c.participantFirstName}</span>
                    </button>
                  </li>
                ))}
              </ul>
            </div>

            <div className="rounded-2xl bg-white shadow-sm overflow-hidden">
              <div className="bg-primary text-white px-4 py-2 text-center font-medium">
                Фильтр
              </div>
              <button className="flex w-full items-center justify-between px-4 py-2.5 hover:bg-muted/50">
                <span>Язык</span> <span>→</span>
              </button>
              <button className="flex w-full items-center justify-between px-4 py-2.5 hover:bg-muted/50 border-t border-border">
                <span>Уровень языка</span> <span>→</span>
              </button>
              <Button className="w-full rounded-none">Применить фильтры</Button>
            </div>
          </aside>

          {/* Conversation */}
          <section className="rounded-2xl bg-white shadow-sm flex flex-col min-h-[600px]">
            {activeConv ? (
              <>
                <header className="flex items-center gap-3 border-b border-border px-5 py-3">
                  <Avatar className="h-12 w-12">
                    <AvatarImage
                      src={resolveAssetUrl(activeConv.participantAvatarUrl) ?? undefined}
                    />
                    <AvatarFallback>{activeConv.participantFirstName[0]}</AvatarFallback>
                  </Avatar>
                  <div>
                    <p className="text-lg font-semibold leading-tight">
                      {activeConv.participantFirstName}
                    </p>
                    <p className="text-sm text-success">в сети</p>
                  </div>
                </header>

                <div className="flex-1 overflow-y-auto bg-input-soft/40 p-6 space-y-3">
                  {messagesQuery.data?.content.map((m) => {
                    const mine = m.senderId === user.id;
                    return (
                      <div
                        key={m.id}
                        className={cn(
                          "flex",
                          mine ? "justify-end" : "justify-start"
                        )}
                      >
                        <div
                          className={cn(
                            "max-w-[70%] rounded-2xl px-4 py-2.5 text-base",
                            mine
                              ? "bg-primary text-primary-foreground"
                              : "bg-input-soft text-foreground"
                          )}
                        >
                          {m.content}
                        </div>
                      </div>
                    );
                  })}
                  <div ref={messagesEndRef} />
                </div>

                <form onSubmit={send} className="flex items-center gap-2 border-t border-border p-4">
                  <input
                    type="text"
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    placeholder="Введите сообщение..."
                    className="h-12 flex-1 rounded-xl bg-white ring-1 ring-border px-4 text-base placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                  <Button type="submit" size="icon" disabled={!input.trim()}>
                    <Send className="h-5 w-5" />
                  </Button>
                </form>
              </>
            ) : (
              <div className="flex-1 flex items-center justify-center text-2xl text-muted-foreground">
                Начните чат!
              </div>
            )}
          </section>
        </div>
      </main>
    </>
  );
}
