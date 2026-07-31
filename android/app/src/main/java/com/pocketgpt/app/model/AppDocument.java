package com.pocketgpt.app.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.pocketgpt.app.constants.AppConstants;
import com.pocketgpt.app.constants.AppConstants.Status;

@Entity(tableName = "app_documents")
public class AppDocument {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    
    public String content;
    
    public String documentType; // e.g., "BARE_ACT", "JUDGMENT", "TEMPLATE"
    
    public String date; // e.g., judgment date or enactment date
    
    public int status = Status.ACTIVE.getCode();
    
    public long createdAt = System.currentTimeMillis();
    
    public long updatedAt = System.currentTimeMillis();

    // Optional: store embeddings later
    // public byte[] embedding;
}
