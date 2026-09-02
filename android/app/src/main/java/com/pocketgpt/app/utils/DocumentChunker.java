package com.pocketgpt.app.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DocumentChunker {

    private static final int DEFAULT_CHUNK_SIZE = 500;
    private static final int DEFAULT_OVERLAP = 50;

    /**
     * Splits a text into smaller chunks with overlap to retain context.
     *
     * @param text The full document text.
     * @return A list of chunked strings.
     */
    public static List<String> chunkText(String text) {
        return chunkText(text, DEFAULT_CHUNK_SIZE, DEFAULT_OVERLAP);
    }

    public static List<String> chunkText(String text, int chunkSize, int overlap) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }

        // Try fast native C++ chunking
        String[] nativeChunks = NativeEngine.chunkTextNative(text, chunkSize, overlap);
        if (nativeChunks != null && nativeChunks.length > 0) {
            return Arrays.asList(nativeChunks);
        }

        // Fallback Java chunker
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        int start = 0;

        while (start < length) {
            int end = Math.min(start + chunkSize, length);

            if (end < length) {
                int lastSpace = text.lastIndexOf(' ', end);
                if (lastSpace > start + overlap) {
                    end = lastSpace;
                }
            }

            chunks.add(text.substring(start, end).trim());

            start = end - overlap;

            if (start <= end - chunkSize) {
                start = end;
            }
        }
        return chunks;
    }
}
