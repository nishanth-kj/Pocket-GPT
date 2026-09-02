package com.pocketgpt.app.services;

import android.content.Context;
import com.pocketgpt.app.services.implementation.NotificationServiceImpl;

/**
 * Service for displaying system notifications.
 */
public interface NotificationService {

    static NotificationService getInstance(Context context) {
        return new NotificationServiceImpl(context);
    }

    /**
     * Displays a notification to the user.
     *
     * @param title   The title of the notification.
     * @param message The body message of the notification.
     */
    void showNotification(String title, String message);
}

