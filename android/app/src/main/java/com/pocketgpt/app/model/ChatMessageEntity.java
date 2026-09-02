package com.pocketgpt.app.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_messages", indices = {@Index("sessionId")})
public class ChatMessageEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int sessionId;

    public int type; // 1 = User, 2 = Assistant

    public String content;

    public String modelName;

    public String timestamp;

    public long latencyMs;

    public String sourcesJson;

    public long createdAt;

    public ChatMessageEntity() {
        this.createdAt = System.currentTimeMillis();
    }

    @Ignore
    public ChatMessageEntity(int sessionId, int type, String content, String modelName, String timestamp, long latencyMs, String sourcesJson) {
        this.sessionId = sessionId;
        this.type = type;
        this.content = content;
        this.modelName = modelName;
        this.timestamp = timestamp;
        this.latencyMs = latencyMs;
        this.sourcesJson = sourcesJson;
        this.createdAt = System.currentTimeMillis();
    }
}