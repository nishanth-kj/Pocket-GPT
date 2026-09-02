package com.pocketgpt.app.utils;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

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
    
    public static PocketGptDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (PocketGptDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            PocketGptDatabase.class, "PocketGpt_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
