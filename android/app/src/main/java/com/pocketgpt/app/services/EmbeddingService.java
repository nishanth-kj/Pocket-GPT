package com.pocketgpt.app.services;

import java.util.List;
import com.pocketgpt.app.services.implementation.EmbeddingServiceImpl;

public interface EmbeddingService {
    
    static EmbeddingService create() {
        return new EmbeddingServiceImpl();
    }

    /**
     * Generates an embedding vector for the given text.
     * @param text The text to embed.
     * @return A float array representing the embedding vector.
     */
    float[] generateEmbedding(String text);

    /**
     * Batch generates embeddings for a list of chunks.
     * @param chunks List of text chunks.
     * @return A list of embedding vectors.
     */
    List<float[]> generateEmbeddings(List<String> chunks);
}
