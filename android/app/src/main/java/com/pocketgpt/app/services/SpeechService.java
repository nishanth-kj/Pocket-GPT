package com.pocketgpt.app.services;

/**
 * Service for Text-To-Speech (TTS) capabilities.
 */
public interface SpeechService {

    /**
     * Speaks the given text aloud using the device's TTS engine.
     *
     * @param text The text to speak.
     */
    void speak(String text);
    
}
