package com.pocketgpt.app.repository;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.pocketgpt.app.model.AppDocument;

import java.util.List;

@Dao
public interface SearchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDocument(AppDocument document);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDocuments(List<AppDocument> documents);

    @Transaction
    @Query("SELECT app_documents.* FROM app_documents " +
           "JOIN documents_fts ON app_documents.id = documents_fts.rowid " +
           "WHERE documents_fts MATCH :query")
    List<AppDocument> searchDocuments(String query);

    @Transaction
    @Query("SELECT app_documents.* FROM app_documents " +
           "JOIN documents_fts ON app_documents.id = documents_fts.rowid " +
           "WHERE documents_fts MATCH :query AND app_documents.documentType = :type")
    List<AppDocument> searchDocumentsByType(String query, String type);

    @Query("SELECT * FROM app_documents WHERE title = :title LIMIT 1")
    AppDocument getDocumentByTitle(String title);
}