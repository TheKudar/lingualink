"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Search, Send, UserPlus, X } from "lucide-react";
import { Navbar } from "@/components/layout/Navbar";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { NativeSelect } from "@/components/ui/native-select";
import { chatService } from "@/services/chatService";
import { userService } from "@/services/userService";
import { useAuthStore } from "@/lib/auth-store";
import { resolveAssetUrl } from "@/lib/api";
import { cn } from "@/lib/utils";
import {
  LANGUAGE_LABELS,
  type ChatUserSearchResponse,
  type CourseLanguage,
  type CourseLevel,
} from "@/types/api";

const LEVELS: CourseLevel[] = ["A1", "A2", "B1", "B2", "C1", "C2"];
const LANGUAGES = Object.entries(LANGUAGE_LABELS) as [CourseLanguage, string][];

export default function ChatPage() {
  const router = useRouter();
  const { user, isHydrated } = useAuthStore();
  const queryClient = useQueryClient();
  const [activeId, setActiveId] = useState<number | null>(null);
  const [input, setInput] = useState("");
  const [search, setSearch] = useState("");
  const [language, setLanguage] = useState<CourseLanguage | "">("");
  const [level, setLevel] = useState<CourseLevel | "">("");
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (isHydrated && !user) router.replace("/");
  }, [isHydrated, user, router]);

  const conversationsQuery = useQuery({
    queryKey: ["conversations"],
    queryFn: () => chatService.listConversations(),
    enabled: !!user,
  });

  const usersQuery = useQuery({
    queryKey: ["chat-users", { search, language, level }],
    queryFn: () =>
      userService.search({
        query: search.trim() || undefined,
        language: language || undefined,
        level: level || undefined,
      }),
    enabled: !!user,
  });

  const messagesQuery = useQuery({
    queryKey: ["messages", activeId],
    queryFn: () => chatService.listMessages(activeId!),
    enabled: activeId != null,
    refetchInterval: 5000,
  });

  const createConversationMutation = useMutation({
    mutationFn: (participantId: number) => chatService.createConversation(participantId),
    onSuccess: (conversation) => {
      setActiveId(conversation.id);
      queryClient.invalidateQueries({ queryKey: ["conversations"] });
    },
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

  const activeConv = useMemo(() => {
    const existing = conversationsQuery.data?.find((c) => c.id === activeId) ?? null;
    if (existing) return existing;
    const created = createConversationMutation.data;
    return created?.id === activeId ? created : null;
  }, [conversationsQuery.data, createConversationMutation.data, activeId]);

  const conversationByUserId = useMemo(() => {
    return new Map(
      conversationsQuery.data?.map((conversation) => [
        conversation.participantId,
        conversation.id,
      ]) ?? []
    );
  }, [conversationsQuery.data]);

  const startConversation = (participant: ChatUserSearchResponse) => {
    const existingConversationId = conversationByUserId.get(participant.id);
    if (existingConversationId) {
      setActiveId(existingConversationId);
      return;
    }
    createConversationMutation.mutate(participant.id);
  };

  const resetFilters = () => {
    setSearch("");
    setLanguage("");
    setLevel("");
  };

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
        <div className="grid gap-6 lg:grid-cols-[320px_1fr]">
          <aside className="flex flex-col gap-4">
            <div className="overflow-hidden rounded-2xl bg-white shadow-sm">
              <div className="bg-primary px-4 py-2 text-center font-medium text-white">
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
                      type="button"
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
                      <div className="min-w-0">
                        <p className="truncate font-medium">{c.participantFirstName}</p>
                        <p className="truncate text-xs text-muted-foreground">
                          @{c.participantUsername}
                        </p>
                      </div>
                    </button>
                  </li>
                ))}
              </ul>
            </div>

            <div className="overflow-hidden rounded-2xl bg-white shadow-sm">
              <div className="bg-primary px-4 py-2 text-center font-medium text-white">
                Фильтр
              </div>
              <div className="space-y-3 p-4">
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                  <input
                    type="text"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    placeholder="Имя или логин"
                    className="h-11 w-full rounded-xl bg-input-soft pl-9 pr-3 text-sm placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                </div>

                <NativeSelect
                  aria-label="Язык"
                  value={language}
                  onChange={(e) => setLanguage(e.target.value as CourseLanguage | "")}
                  className="h-11 text-sm"
                >
                  <option value="">Любой язык</option>
                  {LANGUAGES.map(([code, label]) => (
                    <option key={code} value={code}>
                      {label}
                    </option>
                  ))}
                </NativeSelect>

                <NativeSelect
                  aria-label="Уровень языка"
                  value={level}
                  onChange={(e) => setLevel(e.target.value as CourseLevel | "")}
                  className="h-11 text-sm"
                >
                  <option value="">Любой уровень</option>
                  {LEVELS.map((item) => (
                    <option key={item} value={item}>
                      {item}
                    </option>
                  ))}
                </NativeSelect>

                <div className="flex gap-2">
                  <Button
                    type="button"
                    size="sm"
                    className="flex-1"
                    onClick={() => usersQuery.refetch()}
                  >
                    Применить
                  </Button>
                  <Button
                    type="button"
                    variant="outline"
                    size="icon"
                    onClick={resetFilters}
                    aria-label="Сбросить фильтры"
                  >
                    <X className="h-4 w-4" />
                  </Button>
                </div>
              </div>

              <ul className="max-h-80 divide-y divide-border overflow-y-auto border-t border-border">
                {usersQuery.isLoading && (
                  <li className="px-4 py-3 text-sm text-muted-foreground">Загрузка...</li>
                )}
                {usersQuery.data?.length === 0 && (
                  <li className="px-4 py-3 text-sm text-muted-foreground">
                    Пользователи не найдены
                  </li>
                )}
                {usersQuery.data?.map((person) => {
                  const existingConversationId = conversationByUserId.get(person.id);
                  return (
                    <li key={person.id}>
                      <button
                        type="button"
                        onClick={() => startConversation(person)}
                        disabled={createConversationMutation.isPending}
                        className="flex w-full items-center gap-3 px-3 py-2.5 text-left transition-colors hover:bg-muted/50 disabled:cursor-not-allowed disabled:opacity-60"
                      >
                        <Avatar className="h-9 w-9">
                          <AvatarImage src={resolveAssetUrl(person.avatarUrl) ?? undefined} />
                          <AvatarFallback>{person.firstName[0] ?? "?"}</AvatarFallback>
                        </Avatar>
                        <div className="min-w-0 flex-1">
                          <p className="truncate font-medium">
                            {person.firstName} {person.lastName}
                          </p>
                          <p className="truncate text-xs text-muted-foreground">
                            @{person.username}
                          </p>
                        </div>
                        {existingConversationId ? (
                          <span className="text-xs font-medium text-primary">Открыть</span>
                        ) : (
                          <UserPlus className="h-4 w-4 text-primary" />
                        )}
                      </button>
                    </li>
                  );
                })}
              </ul>
            </div>
          </aside>

          <section className="flex min-h-[600px] flex-col rounded-2xl bg-white shadow-sm">
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
                      {activeConv.participantFirstName} {activeConv.participantLastName}
                    </p>
                    <p className="text-sm text-success">@{activeConv.participantUsername}</p>
                  </div>
                </header>

                <div className="flex-1 space-y-3 overflow-y-auto bg-input-soft/40 p-6">
                  {messagesQuery.data?.content.map((m) => {
                    const mine = m.senderId === user.id;
                    return (
                      <div
                        key={m.id}
                        className={cn("flex", mine ? "justify-end" : "justify-start")}
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

                <form
                  onSubmit={send}
                  className="flex items-center gap-2 border-t border-border p-4"
                >
                  <input
                    type="text"
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    placeholder="Введите сообщение..."
                    className="h-12 flex-1 rounded-xl bg-white px-4 text-base ring-1 ring-border placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary"
                  />
                  <Button type="submit" size="icon" disabled={!input.trim() || sendMutation.isPending}>
                    <Send className="h-5 w-5" />
                  </Button>
                </form>
              </>
            ) : (
              <div className="flex flex-1 items-center justify-center px-6 text-center text-2xl text-muted-foreground">
                Выберите диалог или найдите собеседника через фильтры
              </div>
            )}
          </section>
        </div>
      </main>
    </>
  );
}
