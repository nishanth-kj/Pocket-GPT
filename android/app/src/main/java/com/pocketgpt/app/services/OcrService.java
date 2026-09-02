package com.pocketgpt.app.services;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import com.pocketgpt.app.services.implementation.OcrServiceImpl;

/**
 * Service for Optical Character Recognition (OCR) and Image text extraction.
 */
public interface OcrService {

    static OcrService create() {
        return new OcrServiceImpl();
    }

    /**
     * Extracts text from the provided image bitmap.
     */
    String extractText(Bitmap image);

    /**
     * Extracts text from an image at a content URI.
     */
    String extractTextFromUri(Context context, Uri imageUri);
}

