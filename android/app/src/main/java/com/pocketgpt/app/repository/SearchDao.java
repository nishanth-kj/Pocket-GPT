package com.pocketgpt.app.repository;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.pocketgpt.app.model.AppDocument;
import com.pocketgpt.app.model.DocumentChunk;

import java.util.List;

@Dao
public interface SearchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertDocument(AppDocument document);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertDocuments(List<AppDocument> documents);

    @Update
    void updateDocument(AppDocument document);

    @Delete
    void deleteDocument(AppDocument document);

    @Query("DELETE FROM app_documents WHERE id = :id")
    void deleteDocumentById(int id);

    @Query("SELECT * FROM app_documents ORDER BY createdAt DESC")
    List<AppDocument> getAllDocuments();

    @Query("SELECT * FROM app_documents ORDER BY createdAt DESC")
    LiveData<List<AppDocument>> getAllDocumentsLiveData();

    @Query("SELECT * FROM app_documents WHERE id = :id LIMIT 1")
    AppDocument getDocumentById(int id);

    @Query("SELECT * FROM app_documents WHERE title = :title LIMIT 1")
    AppDocument getDocumentByTitle(String title);

    @Transaction
    @Query("SELECT app_documents.* FROM app_documents " +
           "JOIN documents_fts ON app_documents.id = documents_fts.rowid " +
           "WHERE documents_fts MATCH :query")
    List<AppDocument> searchDocuments(String query);

    @Query("SELECT * FROM app_documents WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    List<AppDocument> searchDocumentsByText(String query);

    @Transaction
    @Query("SELECT app_documents.* FROM app_documents " +
           "JOIN documents_fts ON app_documents.id = documents_fts.rowid " +
           "WHERE documents_fts MATCH :query AND app_documents.documentType = :type")
    List<AppDocument> searchDocumentsByType(String query, String type);

    @Query("SELECT COUNT(*) FROM app_documents")
    int getDocumentCount();

    @Query("SELECT COUNT(*) FROM app_documents")
    LiveData<Integer> getDocumentCountLiveData();

    // --- Document Chunks ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChunk(DocumentChunk chunk);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertChunks(List<DocumentChunk> chunks);

    @Query("SELECT * FROM document_chunks WHERE documentId = :documentId ORDER BY chunkIndex ASC")
    List<DocumentChunk> getChunksForDocument(int documentId);

    @Query("SELECT * FROM document_chunks ORDER BY id ASC")
    List<DocumentChunk> getAllChunks();

    @Query("DELETE FROM document_chunks WHERE documentId = :documentId")
    void deleteChunksForDocument(int documentId);

    @Query("SELECT COUNT(*) FROM document_chunks")
    int getChunkCount();

    @Query("SELECT COUNT(*) FROM document_chunks")
    LiveData<Integer> getChunkCountLiveData();

    @Query("DELETE FROM app_documents")
    void clearAllDocuments();

    @Query("DELETE FROM document_chunks")
    void clearAllChunks();
}