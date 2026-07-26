package com.pocketgpt.app.utils;

import java.util.ArrayList;
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
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return chunks;
        }

        int length = text.length();
        int start = 0;

        while (start < length) {
            int end = Math.min(start + chunkSize, length);
            
            // Try to not break words in half if possible
            if (end < length) {
                int lastSpace = text.lastIndexOf(' ', end);
                if (lastSpace > start + overlap) {
                    end = lastSpace;
                }
            }

            chunks.add(text.substring(start, end).trim());
            
            // Move start forward, accounting for overlap
            start = end - overlap;
            
            // Prevent infinite loop if overlap is too large
            if (start <= end - chunkSize) {
                start = end;
            }
        }
        return chunks;
    }
}
