package com.pocketgpt.app.model;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "document_chunks", indices = {@Index("documentId")})
public class DocumentChunk {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int documentId;

    public String chunkText;

    public int chunkIndex;

    // A serialized representation of the float array for embedding (comma separated or JSON)
    // For large scale we'd use a dedicated Vector DB or SQLite VSS, but this works for basic usage.
    public String embeddingVector;
}
