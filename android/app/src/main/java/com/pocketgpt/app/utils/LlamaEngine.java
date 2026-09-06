package com.pocketgpt.app.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thin Java wrapper around the vendored llama.cpp native inference engine.
 * Loads one GGUF model at a time and streams generated text back token by
 * token so the UI can render responses in real time as they are produced.
 */
public class LlamaEngine {

    private static volatile LlamaEngine INSTANCE;

    public interface GenerationListener {
        void onToken(String piece);

        void onComplete(String fullText, long elapsedMs);

        void onError(String message);
    }

    /** Bridged to native code; return false from onToken to stop generation early. */
    public interface TokenSink {
        boolean onToken(String piece);
    }

    static {
        System.loadLibrary("pocketgpt_native");
    }

    private final ExecutorService inferenceExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean cancelRequested = new AtomicBoolean(false);

    private volatile long nativeHandle = 0L;
    private volatile String loadedModelPath = null;

    private LlamaEngine() {
    }

    public static LlamaEngine getInstance() {
        if (INSTANCE == null) {
            synchronized (LlamaEngine.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LlamaEngine();
                }
            }
        }
        return INSTANCE;
    }

    public boolean isModelLoaded() {
        return nativeHandle != 0L;
    }

    public String getLoadedModelPath() {
        return loadedModelPath;
    }

    /**
     * Loads the given GGUF model on a background thread if it isn't already
     * the active model. Safe to call before every generation request.
     */
    public void ensureModelLoaded(String modelPath, int contextSize, ModelLoadListener listener) {
        inferenceExecutor.execute(() -> {
            if (nativeHandle != 0L && modelPath.equals(loadedModelPath)) {
                if (listener != null) mainHandler.post(listener::onReady);
                return;
            }
            if (nativeHandle != 0L) {
                nativeUnload(nativeHandle);
                nativeHandle = 0L;
                loadedModelPath = null;
            }
            int threads = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);
            long handle = nativeLoadModel(modelPath, contextSize, threads);
            if (handle != 0L) {
                nativeHandle = handle;
                loadedModelPath = modelPath;
                if (listener != null) mainHandler.post(listener::onReady);
            } else {
                if (listener != null) {
                    mainHandler.post(() -> listener.onFailed("Failed to load model file"));
                }
            }
        });
    }

    public interface ModelLoadListener {
        void onReady();

        void onFailed(String message);
    }

    /**
     * Generates a response for the given prompt, streaming each decoded
     * piece back on the main thread via {@link GenerationListener#onToken}.
     * {@code systemContext} may be null/empty when there is no RAG context.
     */
    public void generateAsync(String systemContext, String userPrompt, int maxTokens, float temperature,
                               GenerationListener listener) {
        cancelRequested.set(false);
        inferenceExecutor.execute(() -> {
            if (nativeHandle == 0L) {
                mainHandler.post(() -> listener.onError("Model is not loaded"));
                return;
            }

            long start = System.currentTimeMillis();
            StringBuilder full = new StringBuilder();
            try {
                nativeGenerate(nativeHandle, systemContext, userPrompt, maxTokens, temperature, piece -> {
                    full.append(piece);
                    mainHandler.post(() -> listener.onToken(piece));
                    return !cancelRequested.get();
                });
            } catch (Throwable t) {
                mainHandler.post(() -> listener.onError(t.getMessage() != null ? t.getMessage() : "Generation failed"));
                return;
            }
            long elapsed = System.currentTimeMillis() - start;
            String result = full.toString();
            mainHandler.post(() -> listener.onComplete(result, elapsed));
        });
    }

    public void cancelGeneration() {
        cancelRequested.set(true);
    }

    private static native long nativeLoadModel(String modelPath, int contextSize, int threads);

    private static native void nativeGenerate(long handle, String systemPrompt, String userPrompt, int maxTokens,
                                               float temperature, TokenSink callback);

    private static native void nativeUnload(long handle);
}
