package com.simats.digitallibrary;

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
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "digitallibrary_notifications";
    private static final String CHANNEL_NAME = "Digital Library";
    private static final String CHANNEL_DESC = "Notifications for book reservations and library updates";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleDataMessage(remoteMessage.getData());
        }

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            sendNotification(title, body, remoteMessage.getData());
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);

        // Send token to server
        SessionManager sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            int userId = sessionManager.getUserId();
            FirebaseTokenManager tokenManager = new FirebaseTokenManager(this);
            tokenManager.registerToken(userId);
        }
    }

    /**
     * Handle data message
     */
    private void handleDataMessage(Map<String, String> data) {
        String title = data.get("title");
        String message = data.get("message");
        String type = data.get("type");

        sendNotification(title, message, data);
    }

    /**
     * Create and show a notification
     */
    private void sendNotification(String title, String messageBody, Map<String, String> data) {
        Intent intent = getNotificationIntent(data);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVibrate(new long[] { 0, 500, 200, 500 })
                .setFullScreenIntent(pendingIntent, true) // This forces heads-up popup
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody));

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Use unique notification ID based on timestamp
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    /**
     * Get appropriate intent based on notification type
     */
    private Intent getNotificationIntent(Map<String, String> data) {
        String type = data.get("type");
        Intent intent;

        if (type != null) {
            switch (type) {
                case "new_book":
                case "general_announcement":
                    // Open notifications activity
                    intent = new Intent(this, NotificationsActivity.class);
                    break;

                case "reservation_confirmed":
                case "reservation_rejected":
                case "book_ready":
                case "return_reminder":
                case "overdue_fine":
                    // Open my bookings
                    intent = new Intent(this, MyBookingsActivity.class);
                    break;

                case "account_blocked":
                case "profile_updated":
                    // Open profile or notifications
                    intent = new Intent(this, NotificationsActivity.class);
                    break;

                default:
                    intent = new Intent(this, NotificationsActivity.class);
            }
        } else {
            intent = new Intent(this, NotificationsActivity.class);
        }

        // Add notification data for deep linking
        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }

        return intent;
    }

    /**
     * Get notification priority based on type
     */
    private int getNotificationPriority(String type) {
        // Always use HIGH priority for heads-up/pop-up notifications
        return NotificationCompat.PRIORITY_HIGH;
    }

    /**
     * Create notification channel for Android O and above
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH); // HIGH for heads-up pop-up notifications
            channel.setDescription(CHANNEL_DESC);
            channel.enableVibration(true);
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
            channel.enableLights(true);
            channel.setLightColor(android.graphics.Color.BLUE);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
