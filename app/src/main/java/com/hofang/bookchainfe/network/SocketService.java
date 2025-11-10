package com.hofang.bookchainfe.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.hofang.bookchainfe.utils.TokenManager;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service to manage Socket.IO connection for real-time chat
 */
public class SocketService {
    private static final String TAG = "SocketService";
    private static SocketService instance;
    private Socket socket;
    private Context context;
    private boolean isConnected = false;
    private SocketListener listener;
    private List<SocketListener> globalListeners;

    public interface SocketListener {
        void onNewMessage(JSONObject messageData);
        void onMessageSent(JSONObject response);
        void onError(String error);
        void onConnected();
        void onDisconnected();
    }

    private SocketService(Context context) {
        this.context = context.getApplicationContext();
        this.globalListeners = new ArrayList<>();
    }

    public static synchronized SocketService getInstance(Context context) {
        if (instance == null) {
            instance = new SocketService(context);
        }
        return instance;
    }

    /**
     * Connect to Socket.IO server
     */
    public void connect() {
        if (socket != null && socket.connected()) {
            Log.d(TAG, "Socket already connected");
            return;
        }

        try {
            TokenManager tokenManager = new TokenManager(context);
            String token = tokenManager.getToken();

            if (token == null || token.isEmpty()) {
                Log.e(TAG, "No token available for Socket.IO connection");
                return;
            }

            // Build Socket.IO options
            Map<String, String> auth = new HashMap<>();
            auth.put("token", token);
            
            IO.Options options = IO.Options.builder()
                    .setAuth(auth)
                    .setTransports(new String[]{"websocket", "polling"})
                    .build();

            // Create socket connection
            socket = IO.socket(ApiConfig.BASE_URL, options);

            // Setup event listeners
            setupEventListeners();

            // Connect
            socket.connect();
            Log.d(TAG, "Connecting to Socket.IO server...");

        } catch (URISyntaxException e) {
            Log.e(TAG, "Error creating Socket.IO connection", e);
        }
    }

    /**
     * Force reconnect to Socket.IO server with new token
     * Use this when token changes (e.g., after login/logout)
     */
    public void reconnect() {
        Log.d(TAG, "Force reconnecting Socket.IO...");
        // Disconnect existing connection first
        disconnect();
        // Wait a bit for disconnect to complete, then connect with new token
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            connect();
        }, 200);
    }

    /**
     * Setup Socket.IO event listeners
     */
    private void setupEventListeners() {
        // Connection events
        socket.on(Socket.EVENT_CONNECT, args -> {
            Log.d(TAG, "Socket.IO connected");
            isConnected = true;
            if (listener != null) {
                listener.onConnected();
            }
        });

        socket.on(Socket.EVENT_DISCONNECT, args -> {
            Log.d(TAG, "Socket.IO disconnected");
            isConnected = false;
            if (listener != null) {
                listener.onDisconnected();
            }
        });

        socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
            Log.e(TAG, "Socket.IO connection error: " + (args.length > 0 ? args[0] : "Unknown"));
            if (listener != null && args.length > 0) {
                listener.onError(args[0].toString());
            }
        });

        // Chat events
        socket.on("new_message", args -> {
            if (args.length > 0 && args[0] instanceof JSONObject) {
                JSONObject messageData = (JSONObject) args[0];
                Log.d(TAG, "New message received: " + messageData.toString());
                if (listener != null) {
                    listener.onNewMessage(messageData);
                }
                // Notify all global listeners
                for (SocketListener globalListener : globalListeners) {
                    if (globalListener != null) {
                        globalListener.onNewMessage(messageData);
                    }
                }
            }
        });

        socket.on("message_sent", args -> {
            if (args.length > 0 && args[0] instanceof JSONObject) {
                JSONObject response = (JSONObject) args[0];
                Log.d(TAG, "Message sent: " + response.toString());
                if (listener != null) {
                    listener.onMessageSent(response);
                }
                // Notify all global listeners
                for (SocketListener globalListener : globalListeners) {
                    if (globalListener != null) {
                        globalListener.onMessageSent(response);
                    }
                }
            }
        });

        socket.on("error", args -> {
            String error = "Unknown error";
            if (args.length > 0) {
                if (args[0] instanceof JSONObject) {
                    try {
                        error = ((JSONObject) args[0]).getString("message");
                    } catch (JSONException e) {
                        error = args[0].toString();
                    }
                } else {
                    error = args[0].toString();
                }
            }
            Log.e(TAG, "Socket.IO error: " + error);
            if (listener != null) {
                listener.onError(error);
            }
        });
    }

    /**
     * Send a message via Socket.IO
     */
    public void sendMessage(String conversationId, String content) {
        if (socket == null || !socket.connected()) {
            Log.e(TAG, "Socket not connected, cannot send message");
            if (listener != null) {
                listener.onError("Not connected to server");
            }
            return;
        }

        try {
            JSONObject data = new JSONObject();
            if (conversationId != null && !conversationId.isEmpty()) {
                data.put("conversationId", conversationId);
            }
            data.put("content", content);
            data.put("messageType", "text");

            socket.emit("send_message", data);
            Log.d(TAG, "Sending message: " + data.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Error creating message data", e);
            if (listener != null) {
                listener.onError("Error creating message");
            }
        }
    }

    /**
     * Get or create conversation
     */
    public void getConversation() {
        if (socket == null || !socket.connected()) {
            Log.e(TAG, "Socket not connected, cannot get conversation");
            return;
        }

        socket.emit("get_conversation");
        Log.d(TAG, "Requesting conversation");
    }

    /**
     * Mark messages as read
     */
    public void markAsRead(String conversationId) {
        if (socket == null || !socket.connected()) {
            Log.e(TAG, "Socket not connected, cannot mark as read");
            return;
        }

        try {
            JSONObject data = new JSONObject();
            data.put("conversationId", conversationId);
            socket.emit("mark_as_read", data);
            Log.d(TAG, "Marking messages as read: " + conversationId);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating mark as read data", e);
        }
    }

    /**
     * Set listener for Socket.IO events
     */
    public void setListener(SocketListener listener) {
        this.listener = listener;
    }

    /**
     * Remove listener
     */
    public void removeListener() {
        this.listener = null;
    }

    /**
     * Add global listener (for notifications, etc.)
     */
    public void addGlobalListener(SocketListener listener) {
        if (listener != null && !globalListeners.contains(listener)) {
            globalListeners.add(listener);
        }
    }

    /**
     * Remove global listener
     */
    public void removeGlobalListener(SocketListener listener) {
        globalListeners.remove(listener);
    }

    /**
     * Disconnect from Socket.IO server
     */
    public void disconnect() {
        if (socket != null) {
            socket.disconnect();
            socket.off();
            socket = null;
            isConnected = false;
            Log.d(TAG, "Socket.IO disconnected");
        }
    }

    /**
     * Check if socket is connected
     */
    public boolean isConnected() {
        return socket != null && socket.connected() && isConnected;
    }
}

