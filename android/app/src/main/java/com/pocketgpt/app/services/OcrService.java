package com.pocketgpt.app.services;

import android.graphics.Bitmap;

/**
 * Service for Optical Character Recognition (OCR).
 */
public interface OcrService {

    /**
     * Extracts text from the provided image bitmap.
     *
     * @param image The image to process.
     * @return The extracted text as a String.
     */
    String extractText(Bitmap image);
    
}
