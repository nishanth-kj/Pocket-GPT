package com.pocketgpt.app.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.pocketgpt.app.constants.AppConstants;
import com.pocketgpt.app.constants.AppConstants.Status;

import androidx.room.Ignore;

@Entity(tableName = "app_documents")
public class AppDocument {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    
    public String content;
    
    public String documentType; // e.g., "PDF", "TEXT", "OCR", "BARE_ACT", "NOTE"
    
    public String date; // e.g., date formatted
    
    public int chunkCount = 0;

    public int status = Status.ACTIVE.getCode();
    
    public long createdAt = System.currentTimeMillis();
    
    public long updatedAt = System.currentTimeMillis();

    public AppDocument() {
    }

    @Ignore
    public AppDocument(String title, String content, String documentType, String date) {
        this.title = title;
        this.content = content;
        this.documentType = documentType;
        this.date = date;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
}

