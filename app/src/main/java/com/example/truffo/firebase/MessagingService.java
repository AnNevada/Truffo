package com.example.truffo.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.truffo.R;
import com.example.truffo.activities.MainActivity;
import com.example.truffo.models.User;
import com.example.truffo.utils.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;
import java.util.Random;

public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        switch(Objects.requireNonNull(message.getData().get(Constants.MESSAGE_TYPE))) {
            // CREATE NOTIFICATION UPON RECEIVING MESSAGE
            case Constants.MESSAGE_TYPE_SEND_NOTIFICATION:
                createMessageNotification(message);
                break;
            // DISPLAY TYPING STATUS
            case Constants.MESSAGE_TYPE_TYPING_STATUS:
                updateTypingStatus(Integer.parseInt(Objects.requireNonNull(message.getData().get(Constants.KEY_IS_TYPING))));
                break;
            case Constants.MESSAGE_TYPE_GET_TYPING_STATUS:
                getTypingStatus();
                break;
        }
    }

    private void getTypingStatus() {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getBaseContext());
        Intent intent = new Intent(Constants.ACTION_GET_TYPING_STATUS);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void updateTypingStatus(int typingStatus) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getBaseContext());
        Intent intent = new Intent(Constants.ACTION_TYPING);
        intent.putExtra(Constants.KEY_IS_TYPING, typingStatus);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void createMessageNotification(RemoteMessage message) {
        User user = new User();
        user.id = message.getData().get(Constants.KEY_USER_ID);
        user.name = message.getData().get(Constants.KEY_NAME);
        user.token = message.getData().get(Constants.KEY_FCM_TOKEN);

        int notificationId = new Random().nextInt();
        String channelId = "chat_message";

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.KEY_USER, user);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(user.name)
                .setContentText(message.getData().get(Constants.KEY_MESSAGE))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message.getData().get(Constants.KEY_MESSAGE)))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Chat Message";
            String channelDescription = "This notification channel is used for chat message notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(notificationId, builder.build());
    }
}
