package com.pocketgpt.app.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.pocketgpt.app.model.AppDocument;
import com.pocketgpt.app.model.ChatMessageEntity;
import com.pocketgpt.app.model.ChatSession;
import com.pocketgpt.app.model.DocumentChunk;
import com.pocketgpt.app.model.DocumentFts;
import com.pocketgpt.app.repository.ChatDao;
import com.pocketgpt.app.repository.SearchDao;

@Database(entities = {
        AppDocument.class,
        DocumentFts.class,
        DocumentChunk.class,
        ChatSession.class,
        ChatMessageEntity.class
}, version = 3, exportSchema = false)
public abstract class PocketGptDatabase extends RoomDatabase {
    
    public abstract SearchDao searchDao();
    public abstract ChatDao chatDao();
    
    private static volatile PocketGptDatabase INSTANCE;

    // v1 (AppDocument + DocumentFts) -> v3 (adds chunkCount column plus
    // document_chunks/chat_sessions/chat_messages tables). Without this migration,
    // existing installations would hit fallbackToDestructiveMigration() and lose
    // every previously indexed document.
    static final Migration MIGRATION_1_3 = new Migration(1, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE app_documents ADD COLUMN chunkCount INTEGER NOT NULL DEFAULT 0");

            database.execSQL("CREATE TABLE IF NOT EXISTS `document_chunks` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`documentId` INTEGER NOT NULL, " +
                    "`documentTitle` TEXT, " +
                    "`chunkText` TEXT, " +
                    "`chunkIndex` INTEGER NOT NULL, " +
                    "`embeddingVector` TEXT)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_document_chunks_documentId` ON `document_chunks` (`documentId`)");

            database.execSQL("CREATE TABLE IF NOT EXISTS `chat_sessions` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`title` TEXT, " +
                    "`createdAt` INTEGER NOT NULL, " +
                    "`updatedAt` INTEGER NOT NULL, " +
                    "`targetDocId` INTEGER NOT NULL, " +
                    "`targetDocTitle` TEXT, " +
                    "`lastMessage` TEXT, " +
                    "`messageCount` INTEGER NOT NULL)");

            database.execSQL("CREATE TABLE IF NOT EXISTS `chat_messages` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`sessionId` INTEGER NOT NULL, " +
                    "`type` INTEGER NOT NULL, " +
                    "`content` TEXT, " +
                    "`modelName` TEXT, " +
                    "`timestamp` TEXT, " +
                    "`latencyMs` INTEGER NOT NULL, " +
                    "`sourcesJson` TEXT, " +
                    "`createdAt` INTEGER NOT NULL)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_messages_sessionId` ON `chat_messages` (`sessionId`)");
        }
    };
    
    public static PocketGptDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (PocketGptDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            PocketGptDatabase.class, "PocketGpt_database")
                            .addMigrations(MIGRATION_1_3)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
