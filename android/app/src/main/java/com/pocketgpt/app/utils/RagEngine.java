package com.pocketgpt.app.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.pocketgpt.app.model.AiModel;
import com.pocketgpt.app.model.DocumentChunk;
import com.pocketgpt.app.repository.SearchDao;
import com.pocketgpt.app.services.EmbeddingService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Retrieval-Augmented Generation pipeline: retrieves the most relevant
 * locally indexed document chunks for a query, then streams a real answer
 * from the on-device GGUF model (via {@link LlamaEngine}) grounded in that
 * retrieved context.
 */
public class RagEngine {

    private static final float RELEVANCE_THRESHOLD = 0.20f;
    private static final int CONTEXT_SIZE = 2048;
    private static final int MAX_NEW_TOKENS = 384;
    private static final float TEMPERATURE = 0.7f;

    // Retrieval (Room DB + embedding) work must never run on the caller's
    // thread, since answerQueryStreaming is called directly from the main
    // thread by ChatFragment.
    private static final ExecutorService RETRIEVAL_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    public static class RetrievedChunk {
        public final DocumentChunk chunk;
        public final float combinedScore;
        public final float cosineScore;
        public final float keywordScore;

        public RetrievedChunk(DocumentChunk chunk, float combinedScore, float cosineScore, float keywordScore) {
            this.chunk = chunk;
            this.combinedScore = combinedScore;
            this.cosineScore = cosineScore;
            this.keywordScore = keywordScore;
        }
    }

    public static class RagResult {
        public final String answer;
        public final List<RetrievedChunk> sources;
        public final String modelName;
        public final long processingTimeMs;

        public RagResult(String answer, List<RetrievedChunk> sources, String modelName, long processingTimeMs) {
            this.answer = answer;
            this.sources = sources;
            this.modelName = modelName;
            this.processingTimeMs = processingTimeMs;
        }
    }

    public interface StreamListener {
        void onToken(String piece);

        void onComplete(RagResult result);

        void onError(String message);
    }

    /**
     * Retrieves context and streams a real, on-device generated answer.
     * All callbacks are delivered on the main thread.
     */
    public static void answerQueryStreaming(Context context, String query, Integer specificDocId, StreamListener listener) {
        RETRIEVAL_EXECUTOR.execute(() -> {
            long startTime = System.currentTimeMillis();
            SearchDao dao = PocketGptDatabase.getDatabase(context).searchDao();
            EmbeddingService embeddingService = EmbeddingService.create();
            AiModel activeModel = ModelManager.getInstance(context).getActiveModel();
            String modelName = activeModel != null ? activeModel.getName() : "Pocket GPT";

            if (query == null || query.trim().isEmpty()) {
                MAIN_HANDLER.post(() -> listener.onComplete(
                        new RagResult("Please enter a question or topic to search.", List.of(), modelName, 0)));
                return;
            }

            List<DocumentChunk> candidates;
            if (specificDocId != null && specificDocId == -1) {
                candidates = Collections.emptyList();
            } else if (specificDocId != null && specificDocId > 0) {
                candidates = dao.getChunksForDocument(specificDocId);
            } else {
                candidates = dao.getAllChunks();
            }

            List<RetrievedChunk> topChunks = rankChunks(query, candidates, embeddingService, 3);
            boolean hasContext = !topChunks.isEmpty() && topChunks.get(0).combinedScore >= RELEVANCE_THRESHOLD;

            if (activeModel == null || !activeModel.isDownloaded() || activeModel.getLocalFilePath() == null) {
                String msg = "No offline AI model is downloaded yet.\n\nGo to the **Models** tab and download one (e.g. SmolLM 135M is a good fast starting point) to start chatting.";
                long elapsed = System.currentTimeMillis() - startTime;
                MAIN_HANDLER.post(() -> listener.onComplete(new RagResult(msg, topChunks, modelName, elapsed)));
                return;
            }

            String systemPrompt = buildSystemPrompt(topChunks, hasContext);
            String modelPath = activeModel.getLocalFilePath();

            LlamaEngine engine = LlamaEngine.getInstance();
            engine.ensureModelLoaded(modelPath, CONTEXT_SIZE, new LlamaEngine.ModelLoadListener() {
                @Override
                public void onReady() {
                    engine.generateAsync(systemPrompt, query, MAX_NEW_TOKENS, TEMPERATURE, new LlamaEngine.GenerationListener() {
                        @Override
                        public void onToken(String piece) {
                            listener.onToken(piece);
                        }

                        @Override
                        public void onComplete(String fullText, long elapsedMs) {
                            String answer = fullText.trim();
                            if (answer.isEmpty()) {
                                answer = "(The model returned an empty response. Try rephrasing your question.)";
                            }
                            listener.onComplete(new RagResult(answer, topChunks, modelName, elapsedMs));
                        }

                        @Override
                        public void onError(String message) {
                            listener.onError(message);
                        }
                    });
                }

                @Override
                public void onFailed(String message) {
                    listener.onError("Failed to load " + modelName + ": " + message);
                }
            });
        });
    }

    private static String buildSystemPrompt(List<RetrievedChunk> topChunks, boolean hasContext) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are Pocket GPT, a private, 100% on-device AI assistant. Answer the user's question clearly and concisely.");

        if (hasContext) {
            sb.append(" Use the following context retrieved from the user's own documents to answer. ")
              .append("If the context does not actually help answer the question, ignore it and answer from general knowledge instead.\n\nContext:\n");
            for (RetrievedChunk rc : topChunks) {
                if (rc.combinedScore < RELEVANCE_THRESHOLD) continue;
                sb.append("---\n").append(rc.chunk.chunkText.trim()).append("\n");
            }
        }
        return sb.toString();
    }

    public static List<RetrievedChunk> rankChunks(String query, List<DocumentChunk> candidates, EmbeddingService embeddingService, int topK) {
        if (candidates == null || candidates.isEmpty()) {
            return new ArrayList<>();
        }

        float[] queryVector = embeddingService.generateEmbedding(query);
        Set<String> queryTokens = tokenize(query);

        List<RetrievedChunk> scored = new ArrayList<>();
        for (DocumentChunk chunk : candidates) {
            float[] chunkVector = embeddingService.deserializeVector(chunk.embeddingVector);
            float cosine = embeddingService.cosineSimilarity(queryVector, chunkVector);

            float keywordMatch = NativeEngine.computeKeywordMatchScore(query, chunk.chunkText);
            if (keywordMatch < 0.0f) {
                // Java fallback
                keywordMatch = computeKeywordMatchScore(queryTokens, chunk.chunkText);
            }

            float combined = (cosine * 0.65f) + (keywordMatch * 0.35f);
            scored.add(new RetrievedChunk(chunk, combined, cosine, keywordMatch));
        }

        Collections.sort(scored, (a, b) -> Float.compare(b.combinedScore, a.combinedScore));

        List<RetrievedChunk> result = new ArrayList<>();
        for (int i = 0; i < Math.min(topK, scored.size()); i++) {
            result.add(scored.get(i));
        }
        return result;
    }

    private static Set<String> tokenize(String text) {
        Set<String> tokens = new HashSet<>();
        if (text == null) return tokens;
        String[] split = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").split("\\s+");
        for (String s : split) {
            String t = s.trim();
            if (t.length() > 2) {
                tokens.add(t);
            }
        }
        return tokens;
    }

    private static float computeKeywordMatchScore(Set<String> queryTokens, String chunkText) {
        if (queryTokens.isEmpty() || chunkText == null) return 0.0f;
        String lowerChunk = chunkText.toLowerCase();
        int matched = 0;
        for (String q : queryTokens) {
            if (lowerChunk.contains(q)) {
                matched++;
            }
        }
        return (float) matched / (float) queryTokens.size();
    }
}
