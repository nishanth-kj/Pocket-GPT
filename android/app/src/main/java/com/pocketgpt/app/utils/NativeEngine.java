package com.pocketgpt.app.utils;

public class NativeEngine {

    // Load the native library on startup
    static {
        System.loadLibrary("pocketgpt_native");
    }

    /**
     * A native method that is implemented by the 'pocketgpt_native' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    
    // Future native methods (e.g. for Vector Search or ONNX inference) can be added here
}
