package com.pocketgpt.app.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import com.pocketgpt.app.R;
import com.pocketgpt.app.ui.MainActivity;

public class SplashActivity extends Activity {

    private TextView typingTextView;
    private final String fullText = "POCKET GPT";
    private int index = 0;
    private final long delay = 150; // Delay between characters in ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        
        // Hide navigation and status bar for a clean splash
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                      | View.SYSTEM_UI_FLAG_FULLSCREEN
                      | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_splash);

        typingTextView = findViewById(R.id.typingTextView);
        
        startTypingAnimation();
    }

    private void startTypingAnimation() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (index <= fullText.length()) {
                    typingTextView.setText(fullText.substring(0, index));
                    index++;
                    handler.postDelayed(this, delay);
                } else {
                    // Animation finished, wait a bit then move to MainActivity
                    handler.postDelayed(() -> {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                    }, 500);
                }
            }
        }, delay);
    }
}