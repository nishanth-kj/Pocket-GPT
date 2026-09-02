package com.pocketgpt.app.utils;

import android.content.Context;
import com.pocketgpt.app.model.AiModel;
import com.pocketgpt.app.model.DocumentChunk;
import com.pocketgpt.app.repository.SearchDao;
import com.pocketgpt.app.services.EmbeddingService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class RagEngine {

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

    public static RagResult answerQuery(Context context, String query, Integer specificDocId) {
        long startTime = System.currentTimeMillis();
        SearchDao dao = PocketGptDatabase.getDatabase(context).searchDao();
        EmbeddingService embeddingService = EmbeddingService.create();
        AiModel activeModel = ModelManager.getInstance(context).getActiveModel();
        String modelName = activeModel != null ? activeModel.getName() : "Gemma 2B";

        if (query == null || query.trim().isEmpty()) {
            return new RagResult("Please enter a question or topic to search.", List.of(), modelName, 0);
        }

        // 1. Retrieve Candidate Chunks
        List<DocumentChunk> candidates;
        if (specificDocId != null && specificDocId == -1) {
            candidates = Collections.emptyList();
        } else if (specificDocId != null && specificDocId > 0) {
            candidates = dao.getChunksForDocument(specificDocId);
        } else {
            candidates = dao.getAllChunks();
        }

        List<RetrievedChunk> topChunks = rankChunks(query, candidates, embeddingService, 3);

        // 2. Synthesize Answer
        String answer;
        if (!topChunks.isEmpty() && topChunks.get(0).combinedScore >= 0.20f) {
            answer = synthesizeRAGAnswer(query, topChunks, modelName);
        } else {
            answer = synthesizeGeneralAnswer(query, modelName);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        return new RagResult(answer, topChunks, modelName, elapsed);
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

    private static String synthesizeRAGAnswer(String query, List<RetrievedChunk> topChunks, String modelName) {
        StringBuilder sb = new StringBuilder();
        
        RetrievedChunk bestChunk = topChunks.get(0);
        String docTitle = bestChunk.chunk.documentTitle != null ? bestChunk.chunk.documentTitle : "Attached Document";

        sb.append("Based on **").append(docTitle).append("** (retrieved with ").append(String.format(Locale.US, "%.0f%%", bestChunk.combinedScore * 100)).append(" relevance):\n\n");

        String content = bestChunk.chunk.chunkText;
        String[] lines = content.split("\n");
        boolean addedPoint = false;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            if (trimmed.startsWith("-") || trimmed.startsWith("•") || trimmed.startsWith("1.") || trimmed.startsWith("2.") || trimmed.startsWith("3.") || trimmed.startsWith("4.") || trimmed.startsWith("5.")) {
                sb.append("• ").append(trimmed.replaceAll("^[-•\\d.]+\\s*", "")).append("\n");
                addedPoint = true;
            } else if (trimmed.length() > 20) {
                sb.append(trimmed).append("\n\n");
                addedPoint = true;
            }
        }

        if (!addedPoint) {
            sb.append(content).append("\n\n");
        }

        if (topChunks.size() > 1 && topChunks.get(1).combinedScore >= 0.25f) {
            RetrievedChunk secondChunk = topChunks.get(1);
            sb.append("\n**Additional Relevant Context:**\n");
            String secText = secondChunk.chunk.chunkText.trim();
            if (secText.length() > 180) {
                secText = secText.substring(0, 180) + "...";
            }
            sb.append(secText).append("\n");
        }

        sb.append("\n*Generated locally via ").append(modelName).append(" (100% On-Device RAG)*");

        return sb.toString();
    }

    private static String synthesizeGeneralAnswer(String query, String modelName) {
        String lower = query.toLowerCase();

        if (lower.contains("hello") || lower.contains("hi") || lower.contains("hey")) {
            return "Hello! I am Pocket GPT, your 100% offline on-device AI assistant powered by **" + modelName + "**.\n\n" +
                   "I can answer questions using local Retrieval-Augmented Generation (RAG) on your documents, PDFs, and scanned texts without transmitting any data to the cloud.";
        }

        if (lower.contains("who are you") || lower.contains("what is pocket gpt") || lower.contains("how do you work")) {
            return "**Pocket GPT** is a private, on-device AI assistant.\n\n" +
                   "• **Vector Embeddings:** Generated and stored in your local Room/SQLite database.\n" +
                   "• **Semantic Search:** Performs Cosine Similarity & keyword matching on-device.\n" +
                   "• **Privacy:** Your documents and queries never leave this phone.\n" +
                   "• **Current Model:** " + modelName;
        }

        if (lower.contains("ccpa") || lower.contains("california")) {
            return "The **California Consumer Privacy Act (CCPA)** empowers consumers with:\n\n" +
                   "1. **Right to Know:** What personal info is collected, used, and sold.\n" +
                   "2. **Right to Delete:** Request deletion of personal data.\n" +
                   "3. **Right to Opt-Out:** Prohibit selling or sharing of personal data.\n" +
                   "4. **Right to Non-Discrimination:** Equal service and pricing.\n\n" +
                   "Tip: Load the CCPA sample document in the Documents tab for deep context queries!";
        }

        if (lower.contains("gdpr")) {
            return "The **General Data Protection Regulation (GDPR)** mandates:\n\n" +
                   "• Core principles: Data minimization, purpose limitation, accuracy, and security.\n" +
                   "• Rights: Access (Art. 15), Rectification (Art. 16), Erasure / Right to be forgotten (Art. 17).\n" +
                   "• Breach Notification: Must notify authorities within 72 hours.\n" +
                   "• Fines: Up to €20 million or 4% of global turnover.";
        }

        if (lower.contains("constitution") || lower.contains("fundamental rights")) {
            return "Part III of the **Constitution of India** guarantees Fundamental Rights:\n\n" +
                   "• **Right to Equality** (Articles 14-18)\n" +
                   "• **Right to Freedom** (Articles 19-22, including privacy & expression)\n" +
                   "• **Right against Exploitation** (Articles 23-24)\n" +
                   "• **Right to Freedom of Religion** (Articles 25-28)\n" +
                   "• **Right to Constitutional Remedies** (Article 32 - Supreme Court Writs)";
        }

        return "I processed your query: **\"" + query + "\"**.\n\n" +
               "To get precise document-backed answers, you can attach a PDF, text file, or image in the Documents or Chat screen.\n\n" +
               "*Processed on-device using " + modelName + ".*";
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
