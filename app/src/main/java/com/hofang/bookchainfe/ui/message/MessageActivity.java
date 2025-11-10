package com.hofang.bookchainfe.ui.message;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hofang.bookchainfe.model.ApiResponse;
import com.hofang.bookchainfe.model.ConversationResponse;
import com.hofang.bookchainfe.network.ChatApiService;
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    private static final String TAG = "MessageActivity";
    private static final String ADMIN_NAME = "BookChain";
    
    private ChatApiService chatApiService;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tokenManager = new TokenManager(this);
        chatApiService = ApiConfig.getChatApiService();

        // Check if user is logged in
        if (!tokenManager.isLoggedIn()) {
            Toast.makeText(this, "Please login to view messages", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load conversation and navigate directly to chat detail
        loadConversationAndNavigate();
    }

    private void loadConversationAndNavigate() {
        // Call API to get user's conversation
        chatApiService.getUserConversation().enqueue(new Callback<ApiResponse<ConversationResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ConversationResponse>> call, Response<ApiResponse<ConversationResponse>> response) {
                String conversationId = null;
                
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ConversationResponse conversationResponse = response.body().getData();
                    conversationId = conversationResponse.getId();
                }
                
                // Navigate directly to ChatDetailActivity
                navigateToChatDetail(conversationId);
            }

            @Override
            public void onFailure(Call<ApiResponse<ConversationResponse>> call, Throwable t) {
                Log.e(TAG, "Error loading conversation", t);
                // Navigate to chat detail even if API fails - it will handle creating conversation
                navigateToChatDetail(null);
            }
        });
    }

    private void navigateToChatDetail(String conversationId) {
        Intent intent = new Intent(MessageActivity.this, ChatDetailActivity.class);
        if (conversationId != null) {
            intent.putExtra("conversation_id", conversationId);
        }
        intent.putExtra("conversation_name", ADMIN_NAME);
        startActivity(intent);
        finish(); // Finish MessageActivity so user can't go back to it
    }
}

