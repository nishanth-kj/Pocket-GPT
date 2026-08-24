package com.pocketgpt.app.ui.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.pocketgpt.app.R;
import com.pocketgpt.app.ui.settings.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SettingsFragment())
                    .commit();
        }
    }
}