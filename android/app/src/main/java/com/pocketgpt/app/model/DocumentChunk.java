package com.pocketgpt.app.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "document_chunks", indices = {@Index("documentId")})
public class DocumentChunk {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int documentId;

    public String documentTitle;

    public String chunkText;

    public int chunkIndex;

    // A comma-separated representation of the float array for embedding vector
    public String embeddingVector;

    public DocumentChunk() {
    }

    @Ignore
    public DocumentChunk(int documentId, String documentTitle, String chunkText, int chunkIndex, String embeddingVector) {
        this.documentId = documentId;
        this.documentTitle = documentTitle;
        this.chunkText = chunkText;
        this.chunkIndex = chunkIndex;
        this.embeddingVector = embeddingVector;
    }
}

