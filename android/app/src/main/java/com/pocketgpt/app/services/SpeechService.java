package com.pocketgpt.app.services;

import android.content.Context;
import com.pocketgpt.app.services.implementation.SpeechServiceImpl;

/**
 * Service for Text-To-Speech (TTS) capabilities.
 */
public interface SpeechService {

    static SpeechService getInstance(Context context) {
        return SpeechServiceImpl.getInstance(context);
    }

    /**
     * Speaks the given text aloud using the device's TTS engine.
     */
    void speak(String text);

    /**
     * Stops any currently playing speech.
     */
    void stop();

    /**
     * Checks if speech is currently active.
     */
    boolean isSpeaking();

    /**
     * Releases TTS resources.
     */
    void shutdown();
}

