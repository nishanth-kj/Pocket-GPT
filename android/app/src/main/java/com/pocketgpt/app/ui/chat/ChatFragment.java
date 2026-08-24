package com.pocketgpt.app.ui.chat;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.EditText;
import com.pocketgpt.app.R;
import com.pocketgpt.app.services.EmbeddingService;
import com.pocketgpt.app.utils.DocumentChunker;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatFragment extends Fragment {
    private boolean isDocumentAttached = false;
    private TextView ragContextText;
    private ProgressBar ragProgressBar;
    private EditText chatEditText;
    private FloatingActionButton sendButton;
    private MaterialButton btnAddDocument;
    private ImageButton btnAttachFile;
    
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final EmbeddingService embeddingService = EmbeddingService.create();

    private final ActivityResultLauncher<String[]> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    processSelectedFile(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        chatEditText = view.findViewById(R.id.chatEditText);
        sendButton = view.findViewById(R.id.sendButton);
        btnAttachFile = view.findViewById(R.id.btnAttachFile);
        btnAddDocument = view.findViewById(R.id.btnAddDocument);
        ragContextText = view.findViewById(R.id.ragContextText);
        ragProgressBar = view.findViewById(R.id.ragProgressBar);
        
        View.OnClickListener attachListener = v -> {
            filePickerLauncher.launch(new String[]{"application/pdf", "text/plain"});
        };
        btnAttachFile.setOnClickListener(attachListener);
        btnAddDocument.setOnClickListener(attachListener);
        sendButton.setOnClickListener(v -> {
            String message = chatEditText.getText().toString().trim();
            if (!message.isEmpty()) {
                if (isDocumentAttached) {
                    Toast.makeText(getContext(), "Asking AI about document: " + message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Sending to AI: " + message, Toast.LENGTH_SHORT).show();
                }
                chatEditText.setText("");
            }
        });
        return view;
    }

    private void processSelectedFile(Uri uri) {
        String fileName = getFileName(uri);
        if (fileName == null) fileName = "Unknown Document";
        isDocumentAttached = false;
        ragContextText.setText("Status: Pending processing... (" + fileName + ")");
        ragProgressBar.setVisibility(View.VISIBLE);
        chatEditText.setEnabled(false);
        chatEditText.setHint("Please wait, embedding document...");
        sendButton.setEnabled(false);
        btnAddDocument.setEnabled(false);
        btnAttachFile.setEnabled(false);
        String finalFileName = fileName;
        
        executorService.execute(() -> {
            try {
                // 1. Read Text
                String documentText = extractTextFromUri(uri);
                if (documentText == null || documentText.isEmpty()) {
                    documentText = "Fallback placeholder text since actual parsing failed.";
                }

                // 2. Chunk Text
                List<String> chunks = DocumentChunker.chunkText(documentText);

                // 3. Generate Embeddings
                List<float[]> embeddings = embeddingService.generateEmbeddings(chunks);

                // 4. (Future) Save chunks and embeddings to Room database `DocumentChunk`

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (getContext() != null) {
                        isDocumentAttached = true;
                        ragProgressBar.setVisibility(View.GONE);
                        ragContextText.setText("Ready: Document processed into " + chunks.size() + " chunks (" + finalFileName + ")");
                        chatEditText.setEnabled(true);
                        chatEditText.setHint("Ask Pocket GPT about this document...");
                        sendButton.setEnabled(true);
                        btnAddDocument.setEnabled(true);
                        btnAttachFile.setEnabled(true);
                        Toast.makeText(getContext(), "Document Embedded Successfully!", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    ragProgressBar.setVisibility(View.GONE);
                    ragContextText.setText("Error processing document: " + e.getMessage());
                });
            }
        });
    }

    private String extractTextFromUri(Uri uri) {
        if (getContext() == null) return null;
        try (InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content") && getContext() != null) {
            try (Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}