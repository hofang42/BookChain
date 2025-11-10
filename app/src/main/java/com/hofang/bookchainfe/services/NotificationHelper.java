package com.hofang.bookchainfe.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.hofang.bookchainfe.R;
import com.hofang.bookchainfe.ui.message.MessageActivity;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final String CHANNEL_ID = "chat_messages_channel";
    private static final String CHANNEL_NAME = "Chat Messages";
    private static final String CHANNEL_DESCRIPTION = "Notifications for new chat messages";
    private static final int NOTIFICATION_ID = 1001;

    private Context context;
    private NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    /**
     * Create notification channel for Android O and above
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setShowBadge(true);
            
            // Set default notification sound
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            channel.setSound(soundUri, null);
            
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "Notification channel created");
        }
    }

    /**
     * Show notification for new message
     */
    public void showMessageNotification(String senderName, String messageContent, String conversationId) {
        try {
            // Create intent to open MessageActivity when notification is clicked
            Intent intent = new Intent(context, MessageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (conversationId != null) {
                intent.putExtra("conversation_id", conversationId);
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Build notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_message)
                    .setContentTitle(senderName != null ? senderName : "BookChain")
                    .setContentText(messageContent)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(messageContent))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setVibrate(new long[]{0, 250, 250, 250});

            // Show notification
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            Log.d(TAG, "Notification shown for message from: " + senderName);
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification", e);
        }
    }

    /**
     * Cancel all notifications
     */
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }

    /**
     * Cancel specific notification
     */
    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }
}

