import { api } from "@/lib/api";
import type {
  DictionaryCreateRequest,
  DictionaryEntryRequest,
  DictionaryResponse,
} from "@/types/api";

export const dictionaryService = {
  list: async (): Promise<DictionaryResponse[]> =>
    (await api.get<DictionaryResponse[]>("/api/dictionaries")).data,

  create: async (request: DictionaryCreateRequest): Promise<DictionaryResponse> =>
    (await api.post<DictionaryResponse>("/api/dictionaries", request)).data,

  update: async (
    dictionaryId: number,
    request: DictionaryCreateRequest
  ): Promise<DictionaryResponse> =>
    (await api.put<DictionaryResponse>(`/api/dictionaries/${dictionaryId}`, request)).data,

  remove: async (dictionaryId: number): Promise<void> => {
    await api.delete(`/api/dictionaries/${dictionaryId}`);
  },

  addEntry: async (
    dictionaryId: number,
    request: DictionaryEntryRequest
  ): Promise<DictionaryResponse> =>
    (
      await api.post<DictionaryResponse>(
        `/api/dictionaries/${dictionaryId}/entries`,
        request
      )
    ).data,

  updateEntry: async (
    dictionaryId: number,
    entryId: number,
    request: DictionaryEntryRequest
  ): Promise<DictionaryResponse> =>
    (
      await api.put<DictionaryResponse>(
        `/api/dictionaries/${dictionaryId}/entries/${entryId}`,
        request
      )
    ).data,

  removeEntry: async (dictionaryId: number, entryId: number): Promise<void> => {
    await api.delete(`/api/dictionaries/${dictionaryId}/entries/${entryId}`);
  },
};
