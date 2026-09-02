package com.pocketgpt.app.services.implementation;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.pocketgpt.app.R;
import com.pocketgpt.app.services.NotificationService;

public class NotificationServiceImpl implements NotificationService {

    private static final String CHANNEL_ID = "pocketgpt_general_channel";
    private final Context context;

    public NotificationServiceImpl(Context context) {
        this.context = context != null ? context.getApplicationContext() : null;
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && context != null) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Pocket GPT Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for document processing and model downloads");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void showNotification(String title, String message) {
        if (context == null) return;
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_ai)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.notify((int) System.currentTimeMillis(), builder.build());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

