package com.hofang.bookchainfe.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.hofang.bookchainfe.MainActivity;
import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.network.SocketService;
import com.hofang.bookchainfe.utils.TokenManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Foreground Service to listen for new chat messages and show notifications
 * Runs in background even when app is closed
 * Similar to Messenger notifications
 */
public class ChatNotificationService extends Service {
    private static final String TAG = "ChatNotificationService";
    private static final String FOREGROUND_CHANNEL_ID = "chat_service_channel";
    private static final String FOREGROUND_CHANNEL_NAME = "Chat Service";
    private static final int FOREGROUND_NOTIFICATION_ID = 1002;
    
    private SocketService socketService;
    private NotificationHelper notificationHelper;
    private TokenManager tokenManager;
    private String currentUserId;
    private boolean isChatDetailActivityVisible = false;
    private SocketService.SocketListener notificationListener;
    private Handler reconnectHandler;
    private static final long RECONNECT_DELAY_MS = 5000; // 5 seconds

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "ChatNotificationService created");
        
        tokenManager = new TokenManager(this);
        notificationHelper = new NotificationHelper(this);
        socketService = SocketService.getInstance(this);
        currentUserId = tokenManager.getUserId();
        reconnectHandler = new Handler(Looper.getMainLooper());
        
        // Create notification channel for foreground service
        createForegroundNotificationChannel();
        
        setupSocketListener();
        
        // Connect to socket if user is logged in
        if (tokenManager.isLoggedIn()) {
            socketService.connect();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "ChatNotificationService started");
        
        // Check if ChatDetailActivity is visible
        if (intent != null) {
            String action = intent.getAction();
            if ("CHAT_DETAIL_VISIBLE".equals(action)) {
                isChatDetailActivityVisible = true;
            } else if ("CHAT_DETAIL_HIDDEN".equals(action)) {
                isChatDetailActivityVisible = false;
            }
        }
        
        // Start as foreground service to keep running even when app is closed
        if (tokenManager.isLoggedIn()) {
            startForeground(FOREGROUND_NOTIFICATION_ID, createForegroundNotification());
        }
        
        // Return START_STICKY to keep service running and restart if killed
        return START_STICKY;
    }

    /**
     * Setup Socket.IO listener for new messages
     */
    private void setupSocketListener() {
        notificationListener = new SocketService.SocketListener() {
            @Override
            public void onNewMessage(JSONObject messageData) {
                handleNewMessage(messageData);
            }

            @Override
            public void onMessageSent(JSONObject response) {
                // Don't show notification for messages sent by current user
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Socket error: " + error);
            }

            @Override
            public void onConnected() {
                Log.d(TAG, "Socket connected in notification service");
            }

            @Override
            public void onDisconnected() {
                Log.d(TAG, "Socket disconnected in notification service");
                // Auto-reconnect if user is still logged in
                if (tokenManager.isLoggedIn()) {
                    scheduleReconnect();
                }
            }
        };
        socketService.addGlobalListener(notificationListener);
    }

    /**
     * Handle new message received
     */
    private void handleNewMessage(JSONObject messageData) {
        try {
            // Extract sender information
            String senderId = null;
            String senderName = null;
            
            if (messageData.has("senderId")) {
                Object senderIdObj = messageData.get("senderId");
                if (senderIdObj instanceof JSONObject) {
                    JSONObject senderObj = (JSONObject) senderIdObj;
                    senderId = senderObj.has("_id") ? senderObj.getString("_id") : senderObj.getString("id");
                    
                    // Get sender name if available
                    if (senderObj.has("name")) {
                        senderName = senderObj.getString("name");
                    } else if (senderObj.has("username")) {
                        senderName = senderObj.getString("username");
                    } else if (senderObj.has("email")) {
                        senderName = senderObj.getString("email");
                    }
                } else if (senderIdObj instanceof String) {
                    senderId = (String) senderIdObj;
                }
            }
            
            // Don't show notification for messages sent by current user
            if (senderId != null && senderId.equals(currentUserId)) {
                return;
            }
            
            // Don't show notification if ChatDetailActivity is visible
            if (isChatDetailActivityVisible) {
                Log.d(TAG, "ChatDetailActivity is visible, skipping notification");
                return;
            }
            
            // Extract message content
            String messageContent = messageData.has("content") ? messageData.getString("content") : "New message";
            
            // Extract conversation ID
            String conversationId = null;
            if (messageData.has("conversationId")) {
                Object convIdObj = messageData.get("conversationId");
                if (convIdObj instanceof String) {
                    conversationId = (String) convIdObj;
                } else if (convIdObj instanceof JSONObject) {
                    JSONObject convObj = (JSONObject) convIdObj;
                    conversationId = convObj.has("_id") ? convObj.getString("_id") : convObj.getString("id");
                }
            }
            
            // Default sender name
            if (senderName == null || senderName.isEmpty()) {
                senderName = "BookChain";
            }
            
            // Show notification
            notificationHelper.showMessageNotification(senderName, messageContent, conversationId);
            Log.d(TAG, "Notification shown for message from: " + senderName);
            
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing message data for notification", e);
        }
    }

    /**
     * Create notification channel for foreground service (Android O+)
     */
    private void createForegroundNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    FOREGROUND_CHANNEL_ID,
                    FOREGROUND_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW // Low importance so it doesn't make sound
            );
            channel.setDescription("Service running in background to receive messages");
            channel.setShowBadge(false);
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Foreground notification channel created");
            }
        }
    }

    /**
     * Create persistent notification for foreground service
     */
    private Notification createForegroundNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID)
                .setContentTitle("BookChain")
                .setContentText("Đang lắng nghe tin nhắn mới...")
                .setSmallIcon(R.drawable.ic_message)
                .setContentIntent(pendingIntent)
                .setOngoing(true) // Cannot be dismissed by user
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
    }

    /**
     * Schedule automatic reconnection after delay
     */
    private void scheduleReconnect() {
        if (reconnectHandler != null) {
            reconnectHandler.postDelayed(() -> {
                if (tokenManager.isLoggedIn() && !socketService.isConnected()) {
                    Log.d(TAG, "Attempting to reconnect socket...");
                    socketService.connect();
                }
            }, RECONNECT_DELAY_MS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ChatNotificationService destroyed");
        
        // Cancel any pending reconnection attempts
        if (reconnectHandler != null) {
            reconnectHandler.removeCallbacksAndMessages(null);
        }
        
        // Remove socket listener
        if (socketService != null && notificationListener != null) {
            socketService.removeGlobalListener(notificationListener);
        }
        
        // Service will be restarted automatically due to START_STICKY
    }
}

