package com.pocketgpt.app.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_sessions")
public class ChatSession {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;

    public long createdAt;

    public long updatedAt;

    public int targetDocId; // -1 for all / none

    public String targetDocTitle;

    public String lastMessage;

    public int messageCount;

    public ChatSession() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    @Ignore
    public ChatSession(String title, int targetDocId, String targetDocTitle) {
        this.title = title;
        this.targetDocId = targetDocId;
        this.targetDocTitle = targetDocTitle;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.messageCount = 0;
    }
}