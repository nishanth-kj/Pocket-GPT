package com.pocketgpt.app.services.implementation;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;

import com.pocketgpt.app.R;
import com.pocketgpt.app.model.AiModel;
import com.pocketgpt.app.utils.ModelManager;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Keeps model downloads alive at foreground priority so the OS won't kill
 * them while the app is backgrounded, and surfaces a persistent progress
 * notification. The actual HTTP download logic still lives in
 * {@link ModelManager}; this service only watches its state and reflects it.
 */
public class ModelDownloadService extends Service {

    public static final String EXTRA_MODEL_ID = "extra_model_id";
    private static final String CHANNEL_ID = "pocketgpt_download_channel";
    private static final int NOTIFICATION_ID = 9001;
    private static final long POLL_INTERVAL_MS = 400;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Set<String> watchedModelIds = new LinkedHashSet<>();
    private final Runnable pollTask = this::pollProgress;

    private ModelManager modelManager;
    private boolean pollingStarted = false;

    @Override
    public void onCreate() {
        super.onCreate();
        modelManager = ModelManager.getInstance(this);
        createChannel();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        String modelId = intent != null ? intent.getStringExtra(EXTRA_MODEL_ID) : null;
        if (modelId != null) {
            watchedModelIds.add(modelId);
        }

        Notification initial = buildNotification("Downloading model…", "Connecting to server…", 0, true);
        ServiceCompat.startForeground(this, NOTIFICATION_ID, initial, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);

        if (!pollingStarted) {
            pollingStarted = true;
            handler.post(pollTask);
        }
        return START_NOT_STICKY;
    }

    private void pollProgress() {
        AiModel active = null;
        int activeCount = 0;

        for (String id : new LinkedHashSet<>(watchedModelIds)) {
            AiModel model = modelManager.getModel(id);
            if (model == null || !model.isDownloading()) {
                watchedModelIds.remove(id);
                continue;
            }
            activeCount++;
            active = model;
        }

        if (activeCount == 0 || active == null) {
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf();
            pollingStarted = false;
            return;
        }

        String title = activeCount > 1
                ? "Downloading " + activeCount + " models…"
                : "Downloading " + active.getName();
        String text = active.getDownloadStatusMessage() != null ? active.getDownloadStatusMessage() : "";
        int progress = active.getDownloadProgress();

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, buildNotification(title, text, progress, progress <= 0));
        }

        handler.postDelayed(pollTask, POLL_INTERVAL_MS);
    }

    private Notification buildNotification(String title, String text, int progress, boolean indeterminate) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_ai)
                .setContentTitle(title)
                .setContentText(text)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(100, Math.max(0, Math.min(100, progress)), indeterminate)
                .build();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Model Downloads", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Shows progress while AI models download in the background");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(pollTask);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
