package com.pocketgpt.app;

import android.app.Application;
import com.pocketgpt.app.utils.ThemeHelper;

public class PocketGptApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeHelper.applyTheme(this);
    }
}