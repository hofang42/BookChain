package com.hofang.bookchainfe.ui.message;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.model.ApiResponse;
import com.hofang.bookchainfe.model.MessageResponse;
import com.hofang.bookchainfe.model.MessagesResponse;
import com.hofang.bookchainfe.model.SendMessageRequest;
import com.hofang.bookchainfe.network.ChatApiService;
import com.hofang.bookchainfe.network.ApiConfig;
import com.hofang.bookchainfe.network.SocketService;
import com.hofang.bookchainfe.services.ChatNotificationService;
import com.hofang.bookchainfe.utils.TokenManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatDetailActivity extends AppCompatActivity {

    private static final String TAG = "ChatDetailActivity";

    private String conversationId;
    private String conversationName;
    private String currentUserId;

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyState;
    private TextView tvTitle;

    private MessageAdapter adapter;
    private ArrayList<MessageItem> messageList;
    private ChatApiService chatApiService;
    private TokenManager tokenManager;
    private SocketService socketService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);
        
        // Notify notification service that ChatDetailActivity is visible
        notifyServiceChatVisible(true);

        // Get conversation info from intent
        conversationId = getIntent().getStringExtra("conversation_id");
        conversationName = getIntent().getStringExtra("conversation_name");
        if (conversationName == null) {
            conversationName = "BookChain";
        }

        tokenManager = new TokenManager(this);
        currentUserId = tokenManager.getUserId();
        chatApiService = ApiConfig.getChatApiService();
        socketService = SocketService.getInstance(this);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSocketService();
        loadMessages();
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rv_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        progressBar = findViewById(R.id.progress_bar);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        tvTitle = findViewById(R.id.tv_title);

        if (tvTitle != null) {
            tvTitle.setText(conversationName);
        }

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void setupToolbar() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList, currentUserId);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);
    }

    private void setupSocketService() {
        // Set listener for Socket.IO events
        socketService.setListener(new SocketService.SocketListener() {
            @Override
            public void onNewMessage(JSONObject messageData) {
                // Handle new message received via Socket.IO
                runOnUiThread(() -> {
                    try {
                        String msgConversationId = null;
                        if (messageData.has("conversationId")) {
                            Object convIdObj = messageData.get("conversationId");
                            if (convIdObj instanceof String) {
                                msgConversationId = (String) convIdObj;
                            } else if (convIdObj instanceof JSONObject) {
                                JSONObject convObj = (JSONObject) convIdObj;
                                msgConversationId = convObj.has("_id") ? convObj.getString("_id") : convObj.getString("id");
                            }
                        }
                        
                        // Only add message if it belongs to current conversation
                        if (conversationId != null && msgConversationId != null && conversationId.equals(msgConversationId)) {
                            MessageItem messageItem = convertToMessageItemFromJson(messageData);
                            messageList.add(messageItem);
                            adapter.notifyItemInserted(messageList.size() - 1);
                            scrollToBottom();
                            
                            // Hide empty state if showing
                            if (layoutEmptyState.getVisibility() == View.VISIBLE) {
                                layoutEmptyState.setVisibility(View.GONE);
                                rvMessages.setVisibility(View.VISIBLE);
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing new message", e);
                    }
                });
            }

            @Override
            public void onMessageSent(JSONObject response) {
                // Message sent successfully via Socket.IO
                runOnUiThread(() -> {
                    btnSend.setEnabled(true);
                    // Message will be added via new_message event
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnSend.setEnabled(true);
                    Toast.makeText(ChatDetailActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onConnected() {
                Log.d(TAG, "Socket.IO connected");
                // Mark messages as read when connected
                if (conversationId != null && !conversationId.equals("admin")) {
                    socketService.markAsRead(conversationId);
                }
            }

            @Override
            public void onDisconnected() {
                Log.d(TAG, "Socket.IO disconnected");
            }
        });

        // Reconnect Socket.IO to ensure using latest token (important after login/logout)
        socketService.reconnect();
    }

    private void loadMessages() {
        if (conversationId == null || conversationId.equals("admin")) {
            // If no conversation ID, try to get user's conversation first
            loadUserConversation();
            return;
        }

        // Validate conversationId belongs to current user before loading
        Log.d(TAG, "Loading messages - ConversationId: " + conversationId + ", CurrentUserId: " + currentUserId);

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        rvMessages.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);

        chatApiService.getMessages(conversationId).enqueue(new Callback<ApiResponse<MessagesResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<MessagesResponse>> call, Response<ApiResponse<MessagesResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    MessagesResponse messagesResponse = response.body().getData();
                    List<MessageResponse> messages = messagesResponse.getMessages();

                    messageList.clear();
                    for (MessageResponse msg : messages) {
                        messageList.add(convertToMessageItem(msg));
                    }

                    adapter.notifyDataSetChanged();
                    scrollToBottom();

                    if (messageList.isEmpty()) {
                        rvMessages.setVisibility(View.GONE);
                        layoutEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        rvMessages.setVisibility(View.VISIBLE);
                        layoutEmptyState.setVisibility(View.GONE);
                    }
                    
                    // Mark as read via Socket.IO if connected
                    if (socketService.isConnected()) {
                        socketService.markAsRead(conversationId);
                    }
                } else {
                    String errorMsg = response.body() != null && response.body().getError() != null 
                        ? response.body().getError() 
                        : "Failed to load messages";
                    Log.e(TAG, "Failed to load messages - Error: " + errorMsg);
                    
                    // If permission denied, reload user's own conversation
                    if (errorMsg.contains("Permission denied") || errorMsg.contains("403")) {
                        Log.w(TAG, "Permission denied for conversation " + conversationId + ", loading user's own conversation");
                        conversationId = null;
                        loadUserConversation();
                    } else {
                        Toast.makeText(ChatDetailActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MessagesResponse>> call, Throwable t) {
                Log.e(TAG, "Error loading messages", t);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ChatDetailActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserConversation() {
        Log.d(TAG, "Loading user conversation - CurrentUserId: " + currentUserId);
        chatApiService.getUserConversation().enqueue(new Callback<ApiResponse<com.hofang.bookchainfe.model.ConversationResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<com.hofang.bookchainfe.model.ConversationResponse>> call, Response<ApiResponse<com.hofang.bookchainfe.model.ConversationResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    com.hofang.bookchainfe.model.ConversationResponse conversation = response.body().getData();
                    conversationId = conversation.getId();
                    Log.d(TAG, "User conversation loaded - ConversationId: " + conversationId);
                    loadMessages();
                    
                    // Mark as read via Socket.IO if connected
                    if (socketService.isConnected()) {
                        socketService.markAsRead(conversationId);
                    }
                } else {
                    Log.e(TAG, "Failed to get user conversation - Response: " + (response.body() != null ? response.body().getError() : "null"));
                    Toast.makeText(ChatDetailActivity.this, "Failed to get conversation", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<com.hofang.bookchainfe.model.ConversationResponse>> call, Throwable t) {
                Log.e(TAG, "Error loading conversation", t);
                Toast.makeText(ChatDetailActivity.this, "Error loading conversation", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        if (conversationId == null || conversationId.equals("admin")) {
            // If no conversation ID, get user's conversation first
            Log.d(TAG, "No conversation ID, loading user conversation first");
            loadUserConversation();
            // Store message to send after conversation is loaded
            etMessage.setText(messageText);
            return;
        }

        // Log conversation info for debugging
        Log.d(TAG, "Sending message - ConversationId: " + conversationId + ", CurrentUserId: " + currentUserId);

        // Disable send button
        btnSend.setEnabled(false);

        // Clear input field immediately for better UX
        etMessage.setText("");

        // Send message via Socket.IO for real-time
        if (socketService.isConnected()) {
            Log.d(TAG, "Sending message via Socket.IO");
            socketService.sendMessage(conversationId, messageText);
        } else {
            // Fallback to REST API if Socket.IO not connected
            SendMessageRequest request = new SendMessageRequest(messageText);
            chatApiService.sendMessage(conversationId, request).enqueue(new Callback<ApiResponse<MessageResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<MessageResponse>> call, Response<ApiResponse<MessageResponse>> response) {
                    btnSend.setEnabled(true);

                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        MessageResponse messageResponse = response.body().getData();
                        messageList.add(convertToMessageItem(messageResponse));
                        adapter.notifyItemInserted(messageList.size() - 1);
                        scrollToBottom();
                    } else {
                        Toast.makeText(ChatDetailActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<MessageResponse>> call, Throwable t) {
                    Log.e(TAG, "Error sending message", t);
                    btnSend.setEnabled(true);
                    Toast.makeText(ChatDetailActivity.this, "Error sending message", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private MessageItem convertToMessageItem(MessageResponse response) {
        boolean isSent = response.getSenderId() != null && 
                        response.getSenderId().getId() != null &&
                        response.getSenderId().getId().equals(currentUserId);
        
        String content = response.getContent();
        String timestamp = formatTimestamp(response.getCreatedAt());
        
        return new MessageItem(content, timestamp, isSent);
    }

    private MessageItem convertToMessageItemFromJson(JSONObject messageData) throws JSONException {
        String senderId = null;
        if (messageData.has("senderId")) {
            Object senderIdObj = messageData.get("senderId");
            if (senderIdObj instanceof JSONObject) {
                JSONObject senderObj = (JSONObject) senderIdObj;
                senderId = senderObj.has("_id") ? senderObj.getString("_id") : senderObj.getString("id");
            } else if (senderIdObj instanceof String) {
                senderId = (String) senderIdObj;
            }
        }
        
        boolean isSent = senderId != null && senderId.equals(currentUserId);
        String content = messageData.getString("content");
        String timestamp = formatTimestamp(messageData.getString("createdAt"));
        
        return new MessageItem(content, timestamp, isSent);
    }

    private String formatTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return "";
        }
        
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(timestamp);
            
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing timestamp", e);
        }
        
        return timestamp;
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            rvMessages.post(() -> rvMessages.smoothScrollToPosition(adapter.getItemCount() - 1));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Notify notification service that ChatDetailActivity is hidden
        notifyServiceChatVisible(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Notify notification service that ChatDetailActivity is visible
        notifyServiceChatVisible(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Notify notification service that ChatDetailActivity is hidden
        notifyServiceChatVisible(false);
        
        // Remove Socket.IO listener when activity is destroyed
        if (socketService != null) {
            socketService.removeListener();
        }
    }

    /**
     * Notify notification service about ChatDetailActivity visibility
     */
    private void notifyServiceChatVisible(boolean visible) {
        Intent serviceIntent = new Intent(this, ChatNotificationService.class);
        serviceIntent.setAction(visible ? "CHAT_DETAIL_VISIBLE" : "CHAT_DETAIL_HIDDEN");
        startService(serviceIntent);
    }
}

