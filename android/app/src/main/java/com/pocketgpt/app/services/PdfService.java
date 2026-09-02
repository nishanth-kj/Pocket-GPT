package com.pocketgpt.app.services;

import android.content.Context;
import android.net.Uri;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import com.pocketgpt.app.services.implementation.PdfServiceImpl;

/**
 * Service for extracting and parsing text from PDF documents on Android.
 */
public interface PdfService {

    static PdfService create() {
        return new PdfServiceImpl();
    }

    /**
     * Extracts all text pages from the given PDF file.
     */
    List<String> extractPages(File pdfFile);

    /**
     * Extracts full concatenated text from a PDF input stream.
     */
    String extractTextFromStream(InputStream inputStream);

    /**
     * Extracts full concatenated text from a content URI.
     */
    String extractTextFromUri(Context context, Uri uri);
}

