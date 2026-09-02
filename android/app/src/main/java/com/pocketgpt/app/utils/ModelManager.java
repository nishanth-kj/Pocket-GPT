package com.pocketgpt.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import com.pocketgpt.app.model.AiModel;
import com.pocketgpt.app.services.NotificationService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ModelManager {

    private static final String PREFS_NAME = "pocketgpt_models_prefs";
    private static final String KEY_ACTIVE_MODEL = "active_model_id";
    private static final String KEY_CUSTOM_DIR = "custom_models_directory";

    private static volatile ModelManager INSTANCE;
    private final Context context;
    private final SharedPreferences prefs;
    private final Map<String, AiModel> modelCatalog = new LinkedHashMap<>();
    private final Map<String, HttpURLConnection> activeDownloads = new ConcurrentHashMap<>();
    private final java.util.Set<String> cancelledDownloads = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private final ExecutorService downloadExecutor = Executors.newFixedThreadPool(2);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private File modelsDir;

    public interface ModelDownloadListener {
        void onProgress(String modelId, int progressPercent, String statusMessage);

        void onComplete(String modelId, File modelFile);

        void onError(String modelId, String errorMessage);
    }

    private ModelManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.modelsDir = resolveModelsDirectory();
        initializeCatalog();
    }

    private File resolveModelsDirectory() {
        String customPath = prefs.getString(KEY_CUSTOM_DIR, null);
        if (customPath != null && !customPath.trim().isEmpty()) {
            File customDir = new File(customPath.trim());
            if (!customDir.exists()) {
                customDir.mkdirs();
            }
            if (customDir.exists() && customDir.canWrite()) {
                return customDir;
            }
        }

        File dir = this.context.getExternalFilesDir("models");
        if (dir == null) {
            dir = new File(this.context.getFilesDir(), "models");
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public void setCustomModelsDirectory(String newPath) {
        if (newPath == null || newPath.trim().isEmpty()) {
            prefs.edit().remove(KEY_CUSTOM_DIR).apply();
        } else {
            File target = new File(newPath.trim());
            if (!target.exists()) {
                target.mkdirs();
            }
            prefs.edit().putString(KEY_CUSTOM_DIR, target.getAbsolutePath()).apply();
        }
        this.modelsDir = resolveModelsDirectory();
        refreshModelState();
    }

    public long getTotalStorageUsedBytes() {
        if (modelsDir == null || !modelsDir.exists())
            return 0;
        File[] files = modelsDir.listFiles();
        if (files == null)
            return 0;
        long total = 0;
        for (File f : files) {
            if (f.isFile()) {
                total += f.length();
            }
        }
        return total;
    }

    public String getTotalStorageUsedFormatted() {
        long bytes = getTotalStorageUsedBytes();
        if (bytes <= 0)
            return "0 MB";
        double mb = bytes / (1024.0 * 1024.0);
        if (mb >= 1024.0) {
            return String.format(Locale.US, "%.2f GB", mb / 1024.0);
        }
        return String.format(Locale.US, "%.1f MB", mb);
    }

    public void deleteAllDownloadedModels() {
        if (modelsDir != null && modelsDir.exists()) {
            File[] files = modelsDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile()) {
                        f.delete();
                    }
                }
            }
        }
        refreshModelState();
    }

    public static ModelManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ModelManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ModelManager(context);
                }
            }
        }
        return INSTANCE;
    }

    private void initializeCatalog() {
        // Real Hugging Face & Open Source GGUF/ONNX Quantized SLM endpoints
        modelCatalog.put("smollm-135m", new AiModel(
                "smollm-135m",
                "SmolLM 135M",
                "Hugging Face (TensorBlock)",
                "85 MB",
                "Ultra-fast mobile SLM for instant on-device queries",
                "https://huggingface.co/tensorblock/SmolLM-135M-Instruct-GGUF/resolve/main/SmolLM-135M-Instruct-Q4_K_M.gguf",
                "SmolLM-135M-Instruct-Q4_K_M.gguf"));

        modelCatalog.put("qwen-0.5b", new AiModel(
                "qwen-0.5b",
                "Qwen 2.5 0.5B",
                "Alibaba Qwen",
                "390 MB",
                "Compact multilingual reasoning & instruction model",
                "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf",
                "qwen2.5-0.5b-instruct-q4_k_m.gguf"));

        modelCatalog.put("tiny-llama", new AiModel(
                "tiny-llama",
                "TinyLlama 1.1B",
                "TheBloke (Open Source)",
                "480 MB",
                "Fast, lightweight 1.1B model optimized for mobile CPUs",
                "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q2_K.gguf",
                "tinyllama-1.1b-chat-v1.0.Q2_K.gguf"));

        modelCatalog.put("gemma-2b", new AiModel(
                "gemma-2b",
                "Gemma 2 2B",
                "Google (bartowski)",
                "1.4 GB",
                "Google on-device reasoning engine for deep legal & RAG synthesis",
                "https://huggingface.co/bartowski/gemma-2-2b-it-GGUF/resolve/main/gemma-2-2b-it-Q4_K_M.gguf",
                "gemma-2-2b-it-Q4_K_M.gguf"));

        modelCatalog.put("gemma-4b", new AiModel(
                "gemma-4b",
                "Gemma 4B (Gemma 3 4B IT)",
                "Google (bartowski)",
                "2.49 GB",
                "Google next-generation 4B multimodal & instruction model with 128k context",
                "https://huggingface.co/bartowski/google_gemma-3-4b-it-GGUF/resolve/main/google_gemma-3-4b-it-Q4_K_M.gguf",
                "google_gemma-3-4b-it-Q4_K_M.gguf"));

        modelCatalog.put("phi-3-mini", new AiModel(
                "phi-3-mini",
                "Phi-3 Mini 3.8B",
                "Microsoft",
                "2.3 GB",
                "State-of-the-art compact reasoning & logical synthesis",
                "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-gguf/resolve/main/Phi-3-mini-4k-instruct-q4.gguf",
                "Phi-3-mini-4k-instruct-q4.gguf"));

        refreshModelState();
    }

    public void refreshModelState() {
        String activeId = getActiveModelId();
        for (AiModel model : modelCatalog.values()) {
            File file = new File(modelsDir, model.getFileName());
            boolean exists = file.exists() && file.length() > 0;
            model.setLocalFilePath(exists ? file.getAbsolutePath() : null);
            model.setLocalFileSizeBytes(exists ? file.length() : 0);
            model.setDownloaded(exists);
            model.setActive(model.getId().equals(activeId));
        }
    }

    public List<AiModel> getAllModels() {
        refreshModelState();
        return new ArrayList<>(modelCatalog.values());
    }

    public AiModel getModel(String modelId) {
        refreshModelState();
        return modelCatalog.get(modelId);
    }

    public String getActiveModelId() {
        return prefs.getString(KEY_ACTIVE_MODEL, "smollm-135m");
    }

    public AiModel getActiveModel() {
        AiModel model = getModel(getActiveModelId());
        if (model == null) {
            model = modelCatalog.get("smollm-135m");
        }
        return model;
    }

    public void setActiveModel(String modelId) {
        if (modelCatalog.containsKey(modelId)) {
            prefs.edit().putString(KEY_ACTIVE_MODEL, modelId).apply();
            refreshModelState();
        }
    }

    public boolean deleteModel(String modelId) {
        AiModel model = modelCatalog.get(modelId);
        if (model != null) {
            File file = new File(modelsDir, model.getFileName());
            if (file.exists()) {
                file.delete();
            }
            File partFile = new File(modelsDir, model.getFileName() + ".part");
            if (partFile.exists()) {
                partFile.delete();
            }

            model.setDownloaded(false);
            model.setLocalFilePath(null);
            model.setLocalFileSizeBytes(0);

            if (modelId.equals(getActiveModelId())) {
                // Find first downloaded model or fallback
                for (AiModel m : modelCatalog.values()) {
                    if (m.isDownloaded()) {
                        setActiveModel(m.getId());
                        break;
                    }
                }
            }
            refreshModelState();
            return true;
        }
        return false;
    }

    public void cancelDownload(String modelId) {
        cancelledDownloads.add(modelId);
        HttpURLConnection conn = activeDownloads.remove(modelId);
        if (conn != null) {
            try {
                conn.disconnect();
            } catch (Exception ignored) {
            }
        }
        AiModel model = modelCatalog.get(modelId);
        if (model != null) {
            model.setDownloading(false);
            model.setDownloadProgress(0);
            model.setDownloadStatusMessage("Cancelled");
        }
    }

    public void downloadModel(String modelId, ModelDownloadListener listener) {
        AiModel model = modelCatalog.get(modelId);
        if (model == null) {
            if (listener != null)
                listener.onError(modelId, "Model not found in catalog");
            return;
        }

        if (model.isDownloaded()) {
            if (listener != null) {
                listener.onComplete(modelId, new File(model.getLocalFilePath()));
            }
            return;
        }

        model.setDownloading(true);
        model.setDownloadProgress(0);
        model.setDownloadStatusMessage("Connecting to server...");
        cancelledDownloads.remove(modelId);

        downloadExecutor.execute(() -> {
            File partFile = new File(modelsDir, model.getFileName() + ".part");
            File finalFile = new File(modelsDir, model.getFileName());

            HttpURLConnection conn = null;
            InputStream is = null;
            FileOutputStream fos = null;

            try {
                String downloadUrl = model.getDownloadUrl();
                URL url = new URL(downloadUrl);

                // Handle HTTP redirects (Hugging Face CDN redirects)
                int redirects = 0;
                while (redirects < 6) {
                    if (cancelledDownloads.contains(modelId)) {
                        throw new java.io.InterruptedIOException("Download cancelled");
                    }
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(30000);
                    conn.setReadTimeout(60000);
                    conn.setInstanceFollowRedirects(true);
                    conn.setRequestProperty("User-Agent", "Pocket-GPT-Mobile/1.0");

                    // Register the connection before the blocking call so cancelDownload()
                    // can disconnect it even while still waiting on the server response.
                    activeDownloads.put(modelId, conn);

                    int responseCode = conn.getResponseCode();
                    if (cancelledDownloads.contains(modelId)) {
                        throw new java.io.InterruptedIOException("Download cancelled");
                    }
                    if (responseCode == HttpURLConnection.HTTP_MOVED_PERM
                            || responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                            || responseCode == 307
                            || responseCode == 308) {
                        String newLocation = conn.getHeaderField("Location");
                        conn.disconnect();
                        activeDownloads.remove(modelId);
                        url = new URL(newLocation);
                        redirects++;
                    } else {
                        break;
                    }
                }

                if (conn == null || conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new Exception(
                            "HTTP error code: " + (conn != null ? conn.getResponseCode() : "No Connection"));
                }

                activeDownloads.put(modelId, conn);

                long contentLength = conn.getContentLengthLong();
                is = conn.getInputStream();
                fos = new FileOutputStream(partFile);

                byte[] buffer = new byte[32768];
                long totalBytesRead = 0;
                int bytesRead;
                long lastProgressUpdateTime = 0;

                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;

                    long now = System.currentTimeMillis();
                    if (now - lastProgressUpdateTime > 150) {
                        lastProgressUpdateTime = now;
                        int progress = contentLength > 0 ? (int) ((totalBytesRead * 100) / contentLength) : -1;
                        if (progress > 100)
                            progress = 100;
                        if (progress < 0)
                            progress = 0;

                        double downloadedMb = totalBytesRead / (1024.0 * 1024.0);
                        double totalMb = contentLength > 0 ? contentLength / (1024.0 * 1024.0) : 0;

                        String statusText;
                        if (totalMb > 0) {
                            statusText = String.format(Locale.US, "%.1f MB / %.1f MB (%d%%)", downloadedMb, totalMb,
                                    progress);
                        } else {
                            statusText = String.format(Locale.US, "%.1f MB downloaded", downloadedMb);
                        }

                        model.setDownloadProgress(progress);
                        model.setDownloadStatusMessage(statusText);

                        int finalProgress = progress;
                        if (listener != null) {
                            mainHandler.post(() -> listener.onProgress(modelId, finalProgress, statusText));
                        }
                    }
                }

                fos.flush();
                fos.close();
                fos = null;

                is.close();
                is = null;

                // Rename .part to target .gguf file
                if (finalFile.exists()) {
                    finalFile.delete();
                }
                if (!partFile.renameTo(finalFile)) {
                    throw new Exception("Failed to finalize downloaded model file.");
                }

                model.setDownloading(false);
                model.setDownloaded(true);
                model.setDownloadProgress(100);
                model.setLocalFilePath(finalFile.getAbsolutePath());
                model.setLocalFileSizeBytes(finalFile.length());
                model.setDownloadStatusMessage("Downloaded");

                activeDownloads.remove(modelId);
                setActiveModel(modelId);

                mainHandler.post(() -> {
                    NotificationService.getInstance(context).showNotification(
                            "Model Ready",
                            model.getName() + " has been downloaded to device storage ("
                                    + String.format(Locale.US, "%.1f MB", finalFile.length() / (1024.0 * 1024.0))
                                    + ").");
                    if (listener != null) {
                        listener.onComplete(modelId, finalFile);
                    }
                });

            } catch (Exception e) {
                if (partFile.exists()) {
                    partFile.delete();
                }
                activeDownloads.remove(modelId);
                boolean wasCancelled = cancelledDownloads.remove(modelId);
                model.setDownloading(false);
                model.setDownloadProgress(0);

                if (wasCancelled) {
                    model.setDownloadStatusMessage("Cancelled");
                } else {
                    model.setDownloadStatusMessage("Download error");
                    if (listener != null) {
                        mainHandler.post(() -> listener.onError(modelId,
                                e.getMessage() != null ? e.getMessage() : "Download failed"));
                    }
                }
            } finally {
                cancelledDownloads.remove(modelId);
                try {
                    if (fos != null)
                        fos.close();
                    if (is != null)
                        is.close();
                    if (conn != null)
                        conn.disconnect();
                } catch (Exception ignored) {
                }
            }
        });
    }

    public File getModelsDirectory() {
        return modelsDir;
    }
}
