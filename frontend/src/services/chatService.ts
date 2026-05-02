import { api } from "@/lib/api";
import type {
  ConversationResponse,
  MessageResponse,
  PageResponse,
} from "@/types/api";

export const chatService = {
  listConversations: async (): Promise<ConversationResponse[]> =>
    (await api.get<ConversationResponse[]>("/api/conversations")).data,

  createConversation: async (participantId: number): Promise<ConversationResponse> =>
    (await api.post<ConversationResponse>("/api/conversations", { participantId })).data,

  listMessages: async (
    conversationId: number,
    page = 0,
    size = 50
  ): Promise<PageResponse<MessageResponse>> =>
    (
      await api.get<PageResponse<MessageResponse>>(
        `/api/conversations/${conversationId}/messages`,
        { params: { page, size, sort: "sentAt,asc" } }
      )
    ).data,

  sendMessage: async (
    conversationId: number,
    content: string
  ): Promise<MessageResponse> =>
    (
      await api.post<MessageResponse>(
        `/api/conversations/${conversationId}/messages`,
        { content }
      )
    ).data,
};
