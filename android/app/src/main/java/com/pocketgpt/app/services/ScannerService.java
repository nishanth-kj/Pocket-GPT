package com.pocketgpt.app.services;

import android.content.Context;

/**
 * Service for launching the device's document scanner.
 */
public interface ScannerService {

    /**
     * Starts the document scanning intent.
     *
     * @param context The application or activity context.
     */
    void startScan(Context context);
    
}
