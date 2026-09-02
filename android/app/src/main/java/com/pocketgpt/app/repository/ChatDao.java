package com.pocketgpt.app.repository;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.pocketgpt.app.model.ChatMessageEntity;
import com.pocketgpt.app.model.ChatSession;

import java.util.List;

@Dao
public interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSession(ChatSession session);

    @Update
    void updateSession(ChatSession session);

    @Delete
    void deleteSession(ChatSession session);

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    void deleteSessionById(int sessionId);

    @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC")
    List<ChatSession> getAllSessions();

    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId LIMIT 1")
    ChatSession getSessionById(int sessionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertMessage(ChatMessageEntity message);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessages(List<ChatMessageEntity> messages);

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    List<ChatMessageEntity> getMessagesForSession(int sessionId);

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    void deleteMessagesForSession(int sessionId);

    @Query("DELETE FROM chat_sessions")
    void clearAllSessions();

    @Query("DELETE FROM chat_messages")
    void clearAllMessages();
}