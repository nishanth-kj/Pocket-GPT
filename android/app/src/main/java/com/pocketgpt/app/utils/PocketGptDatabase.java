package com.pocketgpt.app.utils;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.pocketgpt.app.repository.SearchDao;
import com.pocketgpt.app.model.DocumentFts;
import com.pocketgpt.app.model.AppDocument;

@Database(entities = {AppDocument.class, DocumentFts.class}, version = 1, exportSchema = false)
public abstract class PocketGptDatabase extends RoomDatabase {
    
    public abstract SearchDao searchDao();
    
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