package com.hofang.bookchainfe.network;

import com.hofang.bookchainfe.model.ApiResponse;
import com.hofang.bookchainfe.model.ConversationResponse;
import com.hofang.bookchainfe.model.MessageResponse;
import com.hofang.bookchainfe.model.MessagesResponse;
import com.hofang.bookchainfe.model.SendMessageRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChatApiService {
    
    /**
     * Get user's conversation
     * GET /api/chat/conversation
     */
    @GET("api/chat/conversation")
    Call<ApiResponse<ConversationResponse>> getUserConversation();
    
    /**
     * Get messages in a conversation
     * GET /api/chat/conversation/:conversationId/messages
     */
    @GET("api/chat/conversation/{conversationId}/messages")
    Call<ApiResponse<MessagesResponse>> getMessages(@Path("conversationId") String conversationId);
    
    /**
     * Send a message in a conversation
     * POST /api/chat/conversation/:conversationId/messages
     */
    @POST("api/chat/conversation/{conversationId}/messages")
    Call<ApiResponse<MessageResponse>> sendMessage(@Path("conversationId") String conversationId, @Body SendMessageRequest request);
    
    /**
     * Close a conversation
     * POST /api/chat/conversation/:conversationId/close
     */
    @POST("api/chat/conversation/{conversationId}/close")
    Call<ApiResponse<ConversationResponse>> closeConversation(@Path("conversationId") String conversationId);
}

