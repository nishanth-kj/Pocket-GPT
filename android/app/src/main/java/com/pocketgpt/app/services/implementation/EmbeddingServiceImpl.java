package com.pocketgpt.app.services.implementation;

import com.pocketgpt.app.services.EmbeddingService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A mock implementation of EmbeddingService.
 * In production, this should be replaced by an actual API call (e.g., OpenAI API)
 * or a local model (e.g., TFLite).
 */
public class EmbeddingServiceImpl implements EmbeddingService {

    private final Random random = new Random();
    private static final int EMBEDDING_DIMENSION = 1536; // e.g., OpenAI text-embedding-ada-002 size

    @Override
    public float[] generateEmbedding(String text) {
        // Simulate network/processing delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        float[] vector = new float[EMBEDDING_DIMENSION];
        for (int i = 0; i < EMBEDDING_DIMENSION; i++) {
            vector[i] = random.nextFloat();
        }
        return vector;
    }

    @Override
    public List<float[]> generateEmbeddings(List<String> chunks) {
        List<float[]> embeddings = new ArrayList<>();
        for (String chunk : chunks) {
            embeddings.add(generateEmbedding(chunk));
        }
        return embeddings;
    }
}
