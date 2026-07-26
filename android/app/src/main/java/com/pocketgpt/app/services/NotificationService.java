package com.pocketgpt.app.services;

/**
 * Service for displaying system notifications.
 */
public interface NotificationService {

    /**
     * Displays a notification to the user.
     *
     * @param title   The title of the notification.
     * @param message The body message of the notification.
     */
    void showNotification(String title, String message);
    
}
