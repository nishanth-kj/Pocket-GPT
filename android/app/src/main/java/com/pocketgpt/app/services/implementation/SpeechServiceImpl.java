package com.pocketgpt.app.services.implementation;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import com.pocketgpt.app.services.SpeechService;

import java.util.Locale;

public class SpeechServiceImpl implements SpeechService {

    private static volatile SpeechServiceImpl INSTANCE;
    private TextToSpeech textToSpeech;
    private boolean isInitialized = false;

    private SpeechServiceImpl(Context context) {
        if (context != null) {
            textToSpeech = new TextToSpeech(context.getApplicationContext(), status -> {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(Locale.US);
                    isInitialized = true;
                }
            });
        }
    }

    public static SpeechServiceImpl getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SpeechServiceImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SpeechServiceImpl(context);
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void speak(String text) {
        if (textToSpeech != null && isInitialized && text != null) {
            // Strip markdown formatting for cleaner audio speech
            String cleanText = text.replaceAll("[#*_`\\[\\]()]", " ").trim();
            textToSpeech.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "POCKET_GPT_TTS");
        }
    }

    @Override
    public void stop() {
        if (textToSpeech != null && isInitialized) {
            textToSpeech.stop();
        }
    }

    @Override
    public boolean isSpeaking() {
        return textToSpeech != null && textToSpeech.isSpeaking();
    }

    @Override
    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            isInitialized = false;
        }
    }
}

