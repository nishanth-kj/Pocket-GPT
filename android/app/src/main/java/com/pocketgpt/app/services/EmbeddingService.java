package com.pocketgpt.app.services;

import java.util.List;
import com.pocketgpt.app.services.implementation.EmbeddingServiceImpl;

public interface EmbeddingService {
    
    int VECTOR_DIM = 256;

    static EmbeddingService create() {
        return new EmbeddingServiceImpl();
    }

    /**
     * Generates a normalized embedding vector for the given text.
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

    /**
     * Calculates cosine similarity between two unit-normalized vectors.
     */
    float cosineSimilarity(float[] v1, float[] v2);

    /**
     * Serializes vector to compact string for SQLite storage.
     */
    String serializeVector(float[] vector);

    /**
     * Deserializes string from SQLite to float array.
     */
    float[] deserializeVector(String vectorStr);
}

