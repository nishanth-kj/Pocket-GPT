package com.pocketgpt.app.services.implementation;

import com.pocketgpt.app.services.EmbeddingService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * High-performance on-device feature hashing & TF-IDF embedding service.
 * Produces deterministic, unit-normalized vector embeddings for local RAG retrieval.
 */
public class EmbeddingServiceImpl implements EmbeddingService {

    private static final int DIMENSION = EmbeddingService.VECTOR_DIM;

    // Common stop words to reduce noise in embeddings
    private static final Set<String> STOP_WORDS = new HashSet<>();
    static {
        String[] words = {"the", "is", "at", "which", "on", "a", "an", "and", "or", "in", "to", "for", "of", "with", "as", "by"};
        for (String w : words) {
            STOP_WORDS.add(w);
        }
    }

    @Override
    public float[] generateEmbedding(String text) {
        float[] vector = new float[DIMENSION];
        if (text == null || text.trim().isEmpty()) {
            return vector;
        }

        String normalized = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        String[] tokens = normalized.split("\\s+");

        int validTokens = 0;
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].trim();
            if (token.isEmpty() || STOP_WORDS.contains(token)) {
                continue;
            }
            validTokens++;

            // 1. Single word hash projection
            int h1 = Math.abs(hashString(token, 0x9747b28c)) % DIMENSION;
            int sign1 = ((token.hashCode() & 1) == 0) ? 1 : -1;
            vector[h1] += sign1 * 1.5f;

            // 2. Bigram context hash projection
            if (i < tokens.length - 1 && !tokens[i + 1].trim().isEmpty()) {
                String bigram = token + "_" + tokens[i + 1].trim();
                int h2 = Math.abs(hashString(bigram, 0x5bd1e995)) % DIMENSION;
                int sign2 = ((bigram.hashCode() & 1) == 0) ? 1 : -1;
                vector[h2] += sign2 * 2.0f;
            }

            // 3. Substring 3-gram character features
            if (token.length() >= 3) {
                for (int c = 0; c <= token.length() - 3; c++) {
                    String sub = token.substring(c, c + 3);
                    int h3 = Math.abs(hashString(sub, 0x1b873593)) % DIMENSION;
                    vector[h3] += 0.5f;
                }
            }
        }

        // L2 Unit Normalization
        float norm = 0.0f;
        for (float v : vector) {
            norm += v * v;
        }
        if (norm > 0) {
            float invNorm = (float) (1.0 / Math.sqrt(norm));
            for (int i = 0; i < DIMENSION; i++) {
                vector[i] *= invNorm;
            }
        }

        return vector;
    }

    @Override
    public List<float[]> generateEmbeddings(List<String> chunks) {
        List<float[]> embeddings = new ArrayList<>();
        if (chunks == null) return embeddings;
        for (String chunk : chunks) {
            embeddings.add(generateEmbedding(chunk));
        }
        return embeddings;
    }

    @Override
    public float cosineSimilarity(float[] v1, float[] v2) {
        if (v1 == null || v2 == null || v1.length != v2.length) {
            return 0.0f;
        }
        float dot = 0.0f;
        for (int i = 0; i < v1.length; i++) {
            dot += v1[i] * v2[i];
        }
        return Math.max(0.0f, Math.min(1.0f, dot));
    }

    @Override
    public String serializeVector(float[] vector) {
        if (vector == null || vector.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vector.length; i++) {
            sb.append(String.format(java.util.Locale.US, "%.5f", vector[i]));
            if (i < vector.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    @Override
    public float[] deserializeVector(String vectorStr) {
        if (vectorStr == null || vectorStr.trim().isEmpty()) {
            return new float[DIMENSION];
        }
        String[] parts = vectorStr.split(",");
        float[] vec = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                vec[i] = Float.parseFloat(parts[i]);
            } catch (NumberFormatException e) {
                vec[i] = 0.0f;
            }
        }
        return vec;
    }

    private int hashString(String str, int seed) {
        int hash = seed;
        for (int i = 0; i < str.length(); i++) {
            hash = (hash * 31) ^ str.charAt(i);
        }
        return hash;
    }
}

