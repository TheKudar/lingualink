"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { BookOpen, Check, Edit2, Plus, Trash2, X } from "lucide-react";
import { Navbar } from "@/components/layout/Navbar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { dictionaryService } from "@/services/dictionaryService";
import { useAuthStore } from "@/lib/auth-store";
import { cn } from "@/lib/utils";
import type { DictionaryEntryResponse } from "@/types/api";

export default function ResourcesPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { user, isHydrated } = useAuthStore();
  const [activeId, setActiveId] = useState<number | null>(null);
  const [newDictionaryName, setNewDictionaryName] = useState("");
  const [renameValue, setRenameValue] = useState("");
  const [sourceWord, setSourceWord] = useState("");
  const [targetWord, setTargetWord] = useState("");
  const [editingEntryId, setEditingEntryId] = useState<number | null>(null);
  const [editSourceWord, setEditSourceWord] = useState("");
  const [editTargetWord, setEditTargetWord] = useState("");

  useEffect(() => {
    if (isHydrated && !user) router.replace("/");
  }, [isHydrated, user, router]);

  const dictionariesQuery = useQuery({
    queryKey: ["dictionaries"],
    queryFn: () => dictionaryService.list(),
    enabled: !!user,
  });

  const dictionaries = useMemo(() => dictionariesQuery.data ?? [], [dictionariesQuery.data]);
  const activeDictionary = useMemo(
    () => dictionaries.find((dictionary) => dictionary.id === activeId) ?? dictionaries[0] ?? null,
    [dictionaries, activeId]
  );

  useEffect(() => {
    if (!activeId && dictionaries.length > 0) {
      setActiveId(dictionaries[0].id);
    }
  }, [activeId, dictionaries]);

  useEffect(() => {
    setRenameValue(activeDictionary?.name ?? "");
    setEditingEntryId(null);
  }, [activeDictionary?.id, activeDictionary?.name]);

  const refreshDictionaries = () => {
    queryClient.invalidateQueries({ queryKey: ["dictionaries"] });
  };

  const createDictionaryMutation = useMutation({
    mutationFn: (name: string) => dictionaryService.create({ name }),
    onSuccess: (dictionary) => {
      setNewDictionaryName("");
      setActiveId(dictionary.id);
      refreshDictionaries();
    },
  });

  const renameDictionaryMutation = useMutation({
    mutationFn: ({ id, name }: { id: number; name: string }) =>
      dictionaryService.update(id, { name }),
    onSuccess: refreshDictionaries,
  });

  const deleteDictionaryMutation = useMutation({
    mutationFn: (id: number) => dictionaryService.remove(id),
    onSuccess: () => {
      setActiveId(null);
      refreshDictionaries();
    },
  });

  const addEntryMutation = useMutation({
    mutationFn: ({
      dictionaryId,
      sourceWord,
      targetWord,
    }: {
      dictionaryId: number;
      sourceWord: string;
      targetWord: string;
    }) => dictionaryService.addEntry(dictionaryId, { sourceWord, targetWord }),
    onSuccess: () => {
      setSourceWord("");
      setTargetWord("");
      refreshDictionaries();
    },
  });

  const updateEntryMutation = useMutation({
    mutationFn: ({
      dictionaryId,
      entryId,
      sourceWord,
      targetWord,
    }: {
      dictionaryId: number;
      entryId: number;
      sourceWord: string;
      targetWord: string;
    }) => dictionaryService.updateEntry(dictionaryId, entryId, { sourceWord, targetWord }),
    onSuccess: () => {
      setEditingEntryId(null);
      refreshDictionaries();
    },
  });

  const deleteEntryMutation = useMutation({
    mutationFn: ({ dictionaryId, entryId }: { dictionaryId: number; entryId: number }) =>
      dictionaryService.removeEntry(dictionaryId, entryId),
    onSuccess: refreshDictionaries,
  });

  const createDictionary = (event: React.FormEvent) => {
    event.preventDefault();
    const name = newDictionaryName.trim();
    if (!name) return;
    createDictionaryMutation.mutate(name);
  };

  const renameDictionary = (event: React.FormEvent) => {
    event.preventDefault();
    if (!activeDictionary) return;
    const name = renameValue.trim();
    if (!name || name === activeDictionary.name) return;
    renameDictionaryMutation.mutate({ id: activeDictionary.id, name });
  };

  const addEntry = (event: React.FormEvent) => {
    event.preventDefault();
    if (!activeDictionary || !sourceWord.trim() || !targetWord.trim()) return;
    addEntryMutation.mutate({
      dictionaryId: activeDictionary.id,
      sourceWord: sourceWord.trim(),
      targetWord: targetWord.trim(),
    });
  };

  const startEditingEntry = (entry: DictionaryEntryResponse) => {
    setEditingEntryId(entry.id);
    setEditSourceWord(entry.sourceWord);
    setEditTargetWord(entry.targetWord);
  };

  const saveEntry = (entryId: number) => {
    if (!activeDictionary || !editSourceWord.trim() || !editTargetWord.trim()) return;
    updateEntryMutation.mutate({
      dictionaryId: activeDictionary.id,
      entryId,
      sourceWord: editSourceWord.trim(),
      targetWord: editTargetWord.trim(),
    });
  };

  if (!user) return <Navbar />;

  return (
    <>
      <Navbar />
      <main className="mx-auto max-w-7xl px-6 py-6">
        <div className="grid gap-6 lg:grid-cols-[320px_1fr]">
          <aside className="space-y-4 rounded-2xl bg-white p-4 shadow-sm">
            <form onSubmit={createDictionary} className="space-y-3">
              <label className="text-sm font-medium" htmlFor="dictionary-name">
                New dictionary
              </label>
              <div className="flex gap-2">
                <Input
                  id="dictionary-name"
                  value={newDictionaryName}
                  onChange={(event) => setNewDictionaryName(event.target.value)}
                  placeholder="Dictionary name"
                />
                <Button
                  type="submit"
                  size="icon"
                  disabled={!newDictionaryName.trim() || createDictionaryMutation.isPending}
                  aria-label="Create dictionary"
                >
                  <Plus className="h-4 w-4" />
                </Button>
              </div>
            </form>

            <div className="space-y-2">
              {dictionariesQuery.isLoading && (
                <p className="text-sm text-muted-foreground">Loading dictionaries...</p>
              )}
              {!dictionariesQuery.isLoading && dictionaries.length === 0 && (
                <p className="text-sm text-muted-foreground">
                  Create a dictionary to start saving word pairs.
                </p>
              )}
              {dictionaries.map((dictionary) => (
                <button
                  key={dictionary.id}
                  type="button"
                  onClick={() => setActiveId(dictionary.id)}
                  className={cn(
                    "flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left transition-colors",
                    activeDictionary?.id === dictionary.id
                      ? "bg-primary text-primary-foreground"
                      : "bg-input-soft hover:bg-muted"
                  )}
                >
                  <BookOpen className="h-4 w-4 shrink-0" />
                  <span className="min-w-0 flex-1 truncate font-medium">{dictionary.name}</span>
                  <span className="text-xs opacity-80">{dictionary.entries.length}</span>
                </button>
              ))}
            </div>
          </aside>

          <section className="min-h-[560px] rounded-2xl bg-white p-5 shadow-sm">
            {activeDictionary ? (
              <div className="space-y-6">
                <div className="flex flex-col gap-3 border-b border-border pb-5 md:flex-row md:items-center md:justify-between">
                  <form onSubmit={renameDictionary} className="flex flex-1 gap-2">
                    <Input
                      value={renameValue}
                      onChange={(event) => setRenameValue(event.target.value)}
                      aria-label="Dictionary name"
                      className="max-w-md text-lg font-semibold"
                    />
                    <Button
                      type="submit"
                      size="icon"
                      variant="outline"
                      disabled={
                        !renameValue.trim() ||
                        renameValue.trim() === activeDictionary.name ||
                        renameDictionaryMutation.isPending
                      }
                      aria-label="Save dictionary name"
                    >
                      <Check className="h-4 w-4" />
                    </Button>
                  </form>
                  <Button
                    type="button"
                    variant="outline"
                    onClick={() => deleteDictionaryMutation.mutate(activeDictionary.id)}
                    disabled={deleteDictionaryMutation.isPending}
                  >
                    <Trash2 className="mr-2 h-4 w-4" />
                    Delete
                  </Button>
                </div>

                <form onSubmit={addEntry} className="grid gap-3 md:grid-cols-[1fr_1fr_auto]">
                  <Input
                    value={sourceWord}
                    onChange={(event) => setSourceWord(event.target.value)}
                    placeholder="Source word"
                    aria-label="Source word"
                  />
                  <Input
                    value={targetWord}
                    onChange={(event) => setTargetWord(event.target.value)}
                    placeholder="Target word"
                    aria-label="Target word"
                  />
                  <Button
                    type="submit"
                    disabled={!sourceWord.trim() || !targetWord.trim() || addEntryMutation.isPending}
                  >
                    <Plus className="mr-2 h-4 w-4" />
                    Add
                  </Button>
                </form>

                <div className="overflow-hidden rounded-xl border border-border">
                  <div className="grid grid-cols-[1fr_1fr_96px] bg-input-soft px-4 py-2 text-sm font-medium text-muted-foreground">
                    <span>Source</span>
                    <span>Target</span>
                    <span className="text-right">Actions</span>
                  </div>
                  {activeDictionary.entries.length === 0 ? (
                    <p className="px-4 py-6 text-sm text-muted-foreground">
                      No words yet. Add the first pair above.
                    </p>
                  ) : (
                    <ul className="divide-y divide-border">
                      {activeDictionary.entries.map((entry) => {
                        const isEditing = editingEntryId === entry.id;
                        return (
                          <li
                            key={entry.id}
                            className="grid items-center gap-3 px-4 py-3 md:grid-cols-[1fr_1fr_96px]"
                          >
                            {isEditing ? (
                              <>
                                <Input
                                  value={editSourceWord}
                                  onChange={(event) => setEditSourceWord(event.target.value)}
                                  aria-label="Edit source word"
                                />
                                <Input
                                  value={editTargetWord}
                                  onChange={(event) => setEditTargetWord(event.target.value)}
                                  aria-label="Edit target word"
                                />
                                <div className="flex justify-end gap-2">
                                  <Button
                                    type="button"
                                    size="icon"
                                    onClick={() => saveEntry(entry.id)}
                                    disabled={
                                      !editSourceWord.trim() ||
                                      !editTargetWord.trim() ||
                                      updateEntryMutation.isPending
                                    }
                                    aria-label="Save word pair"
                                  >
                                    <Check className="h-4 w-4" />
                                  </Button>
                                  <Button
                                    type="button"
                                    size="icon"
                                    variant="outline"
                                    onClick={() => setEditingEntryId(null)}
                                    aria-label="Cancel editing"
                                  >
                                    <X className="h-4 w-4" />
                                  </Button>
                                </div>
                              </>
                            ) : (
                              <>
                                <span className="break-words font-medium">{entry.sourceWord}</span>
                                <span className="break-words">{entry.targetWord}</span>
                                <div className="flex justify-end gap-2">
                                  <Button
                                    type="button"
                                    size="icon"
                                    variant="outline"
                                    onClick={() => startEditingEntry(entry)}
                                    aria-label="Edit word pair"
                                  >
                                    <Edit2 className="h-4 w-4" />
                                  </Button>
                                  <Button
                                    type="button"
                                    size="icon"
                                    variant="outline"
                                    onClick={() =>
                                      deleteEntryMutation.mutate({
                                        dictionaryId: activeDictionary.id,
                                        entryId: entry.id,
                                      })
                                    }
                                    disabled={deleteEntryMutation.isPending}
                                    aria-label="Delete word pair"
                                  >
                                    <Trash2 className="h-4 w-4" />
                                  </Button>
                                </div>
                              </>
                            )}
                          </li>
                        );
                      })}
                    </ul>
                  )}
                </div>
              </div>
            ) : (
              <div className="flex min-h-[520px] items-center justify-center text-center text-muted-foreground">
                Create a dictionary to manage your own words.
              </div>
            )}
          </section>
        </div>
      </main>
    </>
  );
}
