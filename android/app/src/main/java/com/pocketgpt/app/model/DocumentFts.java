package com.pocketgpt.app.model;

import androidx.room.Entity;
import androidx.room.Fts4;
import com.pocketgpt.app.constants.AppConstants;
import com.pocketgpt.app.constants.AppConstants.Status;

@Fts4(contentEntity = AppDocument.class)
@Entity(tableName = "documents_fts")
public class DocumentFts {
    public String title;
    
    public String content;
    
    public String documentType;
    
    public int status = Status.ACTIVE.getCode();
    
    public long createdAt = System.currentTimeMillis();
    
    public long updatedAt = System.currentTimeMillis();
}
