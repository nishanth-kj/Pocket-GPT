package com.pocketgpt.app.utils;

public class NativeEngine {

    private static boolean isNativeLoaded = false;

    static {
        try {
            System.loadLibrary("pocketgpt_native");
            isNativeLoaded = true;
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
            isNativeLoaded = false;
        }
    }

    public static boolean isLoaded() {
        return isNativeLoaded;
    }

    /**
     * Test method returning status string from C++ JNI.
     */
    public native String stringFromJNI();

    /**
     * Ultra-fast SIMD/C++ Cosine Similarity between two float vectors.
     */
    public static native float nativeCosineSimilarity(float[] v1, float[] v2);

    /**
     * Fast C++ Feature Hashing & Vector Embedding generation.
     */
    public static native float[] nativeFastEmbedding(String text, int dimension);

    /**
     * Fast C++ Keyword Match Score calculation between query and chunk text.
     */
    public static native float nativeKeywordMatchScore(String query, String chunkText);

    /**
     * Fast C++ Document Chunker.
     */
    public static native String[] nativeChunkText(String text, int chunkSize, int overlap);

    /**
     * Wrapper for Cosine Similarity with automatic C++ fallback.
     */
    public static float computeCosineSimilarity(float[] v1, float[] v2) {
        if (isNativeLoaded) {
            try {
                return nativeCosineSimilarity(v1, v2);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        // Java fallback
        if (v1 == null || v2 == null || v1.length != v2.length) return 0.0f;
        float dot = 0.0f, norm1 = 0.0f, norm2 = 0.0f;
        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }
        if (norm1 <= 0 || norm2 <= 0) return 0.0f;
        float sim = (float) (dot / (Math.sqrt(norm1) * Math.sqrt(norm2)));
        return Math.max(0.0f, Math.min(1.0f, sim));
    }

    /**
     * Wrapper for Fast Vector Embedding generation with automatic C++ fallback.
     */
    public static float[] generateFastEmbedding(String text, int dimension) {
        if (isNativeLoaded) {
            try {
                return nativeFastEmbedding(text, dimension);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Wrapper for Fast Keyword Match Scoring with automatic C++ fallback.
     */
    public static float computeKeywordMatchScore(String query, String chunkText) {
        if (isNativeLoaded) {
            try {
                return nativeKeywordMatchScore(query, chunkText);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return -1.0f; // -1 indicates fallback needed
    }

    /**
     * Wrapper for Fast Document Chunking with automatic C++ fallback.
     */
    public static String[] chunkTextNative(String text, int chunkSize, int overlap) {
        if (isNativeLoaded) {
            try {
                return nativeChunkText(text, chunkSize, overlap);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return null;
    }
}
