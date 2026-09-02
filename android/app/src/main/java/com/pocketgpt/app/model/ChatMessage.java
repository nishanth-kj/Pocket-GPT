package com.pocketgpt.app.model;

import com.pocketgpt.app.utils.RagEngine;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessage {

    public static final int TYPE_USER = 1;
    public static final int TYPE_ASSISTANT = 2;

    private int type;
    private String content;
    private String modelName;
    private String timestamp;
    private List<RagEngine.RetrievedChunk> sources;
    private long latencyMs;

    public ChatMessage(int type, String content) {
        this(type, content, null, null, 0);
    }

    public ChatMessage(int type, String content, String modelName, List<RagEngine.RetrievedChunk> sources, long latencyMs) {
        this.type = type;
        this.content = content;
        this.modelName = modelName;
        this.sources = sources;
        this.latencyMs = latencyMs;
        this.timestamp = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
    }

    public int getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getModelName() {
        return modelName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public List<RagEngine.RetrievedChunk> getSources() {
        return sources;
    }

    public long getLatencyMs() {
        return latencyMs;
    }
}

