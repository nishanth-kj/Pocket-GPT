package com.pocketgpt.app.ui.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.pocketgpt.app.R;
import com.pocketgpt.app.ui.chat.ChatFragment;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ChatFragment())
                    .commit();
        }
    }
}