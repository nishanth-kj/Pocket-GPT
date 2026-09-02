package com.pocketgpt.app.services.implementation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.pocketgpt.app.services.OcrService;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class OcrServiceImpl implements OcrService {

    private static final long RECOGNITION_TIMEOUT_SECONDS = 30;

    @Override
    public String extractText(Bitmap image) {
        if (image == null) return "";
        try {
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            InputImage inputImage = InputImage.fromBitmap(image, 0);
            Text result = Tasks.await(recognizer.process(inputImage), RECOGNITION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            String recognizedText = result.getText();
            if (recognizedText == null || recognizedText.trim().isEmpty()) {
                return "Scanned Document Image [" + image.getWidth() + "x" + image.getHeight() + " px]\n\n" +
                       "No recognizable text was found in this image.";
            }
            return recognizedText;
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
            return "Scanned Document Image [" + image.getWidth() + "x" + image.getHeight() + " px]\n\n" +
                   "Text recognition failed for this image.";
        }
    }

    @Override
    public String extractTextFromUri(Context context, Uri imageUri) {
        if (context == null || imageUri == null) return "";
        try (InputStream is = context.getContentResolver().openInputStream(imageUri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            return extractText(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            return "Scanned Image Document: Text recognition failed to process this image.";
        }
    }
}

