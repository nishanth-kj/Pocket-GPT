package com.pocketgpt.app.ui.chat;

import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pocketgpt.app.R;
import com.pocketgpt.app.model.AiModel;
import com.pocketgpt.app.model.AppDocument;
import com.pocketgpt.app.model.ChatMessage;
import com.pocketgpt.app.model.ChatMessageEntity;
import com.pocketgpt.app.model.ChatSession;
import com.pocketgpt.app.model.DocumentChunk;
import com.pocketgpt.app.repository.ChatDao;
import com.pocketgpt.app.repository.SearchDao;
import com.pocketgpt.app.services.EmbeddingService;
import com.pocketgpt.app.services.OcrService;
import com.pocketgpt.app.services.PdfService;
import com.pocketgpt.app.utils.DocumentChunker;
import com.pocketgpt.app.utils.ModelManager;
import com.pocketgpt.app.utils.PocketGptDatabase;
import com.pocketgpt.app.utils.RagEngine;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatFragment extends Fragment {

    public static final String EXTRA_DOCUMENT_ID = "extra_document_id";
    public static final String EXTRA_DOCUMENT_TITLE = "extra_document_title";

    private Integer selectedDocId = null; // null = All Documents or General
    private String selectedDocTitle = "All Documents";
    private boolean isGeneralChatOnly = false;
    private Integer currentSessionId = null;

    private TextView ragContextText;
    private ProgressBar ragProgressBar;
    private EditText chatEditText;
    private FloatingActionButton sendButton;
    private ImageButton btnChatHistory;
    private ImageButton btnNewChat;
    private ImageButton btnAttachFile;
    private RecyclerView chatRecyclerView;
    private LinearLayout emptyStateLayout;
    private ChatAdapter chatAdapter;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final EmbeddingService embeddingService = EmbeddingService.create();
    private final PdfService pdfService = PdfService.create();
    private final OcrService ocrService = OcrService.create();

    private final ActivityResultLauncher<String[]> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    processSelectedFile(uri);
                }
            }
    );

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    processSelectedImage(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        chatEditText = view.findViewById(R.id.chatEditText);
        sendButton = view.findViewById(R.id.sendButton);
        btnAttachFile = view.findViewById(R.id.btnAttachFile);
        btnChatHistory = view.findViewById(R.id.btnChatHistory);
        btnNewChat = view.findViewById(R.id.btnNewChat);
        ragContextText = view.findViewById(R.id.ragContextText);
        ragProgressBar = view.findViewById(R.id.ragProgressBar);

        chatAdapter = new ChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        // Check if pre-selected document was passed
        if (getArguments() != null) {
            int docId = getArguments().getInt(EXTRA_DOCUMENT_ID, -1);
            String docTitle = getArguments().getString(EXTRA_DOCUMENT_TITLE, null);
            if (docId > 0 && docTitle != null) {
                selectedDocId = docId;
                selectedDocTitle = docTitle;
                isGeneralChatOnly = false;
            }
        }

        updateContextUI();

        // Attach buttons & context switchers
        view.findViewById(R.id.ragContextCard).setOnClickListener(v -> showContextSelectionDialog());
        btnAttachFile.setOnClickListener(v -> showAttachOptionsDialog());
        btnChatHistory.setOnClickListener(v -> showChatHistoryDialog());
        btnNewChat.setOnClickListener(v -> startNewChat());

        sendButton.setOnClickListener(v -> sendMessage());

        return view;
    }

    private void startNewChat() {
        currentSessionId = null;
        chatAdapter.clear();
        emptyStateLayout.setVisibility(View.VISIBLE);
        Toast.makeText(getContext(), "Started new chat session", Toast.LENGTH_SHORT).show();
    }

    private void showChatHistoryDialog() {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_chat_history, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewHistory);
        LinearLayout emptyHistoryContainer = dialogView.findViewById(R.id.emptyHistoryContainer);
        MaterialButton btnClearAll = dialogView.findViewById(R.id.btnClearAllHistory);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ChatHistoryAdapter historyAdapter = new ChatHistoryAdapter();
        recyclerView.setAdapter(historyAdapter);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setNegativeButton("Close", null)
                .create();

        Runnable loadSessions = () -> {
            executorService.execute(() -> {
                ChatDao chatDao = PocketGptDatabase.getDatabase(requireContext()).chatDao();
                List<ChatSession> sessions = chatDao.getAllSessions();
                mainHandler.post(() -> {
                    if (sessions.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyHistoryContainer.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyHistoryContainer.setVisibility(View.GONE);
                        historyAdapter.setSessions(sessions);
                    }
                });
            });
        };

        historyAdapter.setListener(new ChatHistoryAdapter.SessionClickListener() {
            @Override
            public void onSessionSelected(ChatSession session) {
                loadSession(session);
                dialog.dismiss();
            }

            @Override
            public void onSessionDeleted(ChatSession session) {
                executorService.execute(() -> {
                    ChatDao chatDao = PocketGptDatabase.getDatabase(requireContext()).chatDao();
                    chatDao.deleteMessagesForSession(session.id);
                    chatDao.deleteSessionById(session.id);
                    if (currentSessionId != null && currentSessionId == session.id) {
                        currentSessionId = null;
                        mainHandler.post(() -> {
                            chatAdapter.clear();
                            emptyStateLayout.setVisibility(View.VISIBLE);
                        });
                    }
                    loadSessions.run();
                });
            }
        });

        btnClearAll.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Clear All Chat History")
                    .setMessage("Are you sure you want to delete all saved conversations?")
                    .setPositiveButton("Clear All", (d, w) -> {
                        executorService.execute(() -> {
                            ChatDao chatDao = PocketGptDatabase.getDatabase(requireContext()).chatDao();
                            chatDao.clearAllMessages();
                            chatDao.clearAllSessions();
                            currentSessionId = null;
                            mainHandler.post(() -> {
                                chatAdapter.clear();
                                emptyStateLayout.setVisibility(View.VISIBLE);
                                loadSessions.run();
                            });
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        loadSessions.run();
        dialog.show();
    }

    private void loadSession(ChatSession session) {
        currentSessionId = session.id;
        if (session.targetDocId > 0) {
            selectedDocId = session.targetDocId;
            selectedDocTitle = session.targetDocTitle;
            isGeneralChatOnly = false;
        } else if (session.targetDocId == -1 && "None".equalsIgnoreCase(session.targetDocTitle)) {
            selectedDocId = null;
            selectedDocTitle = "None";
            isGeneralChatOnly = true;
        } else {
            selectedDocId = null;
            selectedDocTitle = "All Documents";
            isGeneralChatOnly = false;
        }
        updateContextUI();

        executorService.execute(() -> {
            ChatDao chatDao = PocketGptDatabase.getDatabase(requireContext()).chatDao();
            List<ChatMessageEntity> messageEntities = chatDao.getMessagesForSession(session.id);

            List<ChatMessage> chatMessages = new ArrayList<>();
            for (ChatMessageEntity entity : messageEntities) {
                ChatMessage cm;
                if (entity.type == ChatMessage.TYPE_USER) {
                    cm = new ChatMessage(ChatMessage.TYPE_USER, entity.content);
                } else {
                    cm = new ChatMessage(
                            ChatMessage.TYPE_ASSISTANT,
                            entity.content,
                            entity.modelName,
                            null,
                            entity.latencyMs
                    );
                }
                chatMessages.add(cm);
            }

            mainHandler.post(() -> {
                chatAdapter.setMessages(chatMessages);
                if (!chatMessages.isEmpty()) {
                    emptyStateLayout.setVisibility(View.GONE);
                    chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                } else {
                    emptyStateLayout.setVisibility(View.VISIBLE);
                }
                Toast.makeText(getContext(), "Loaded: " + session.title, Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void updateContextUI() {
        if (getContext() == null) return;
        AiModel activeModel = ModelManager.getInstance(getContext()).getActiveModel();
        String modelName = activeModel != null ? activeModel.getName() : "Gemma 2B";

        if (isGeneralChatOnly) {
            ragContextText.setText(modelName + " • General Chat (No Context)");
        } else if (selectedDocId != null) {
            ragContextText.setText(modelName + " • Context: " + selectedDocTitle);
        } else {
            ragContextText.setText(modelName + " • Context: All Indexed Documents");
        }
    }

    private void showAttachOptionsDialog() {
        if (getContext() == null) return;
        String[] options = {"Import PDF / Text Document", "Scan Document / Image (OCR)", "Change RAG Context"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Attach to Pocket GPT")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        filePickerLauncher.launch(new String[]{"application/pdf", "text/plain", "text/markdown"});
                    } else if (which == 1) {
                        imagePickerLauncher.launch("image/*");
                    } else {
                        showContextSelectionDialog();
                    }
                })
                .show();
    }

    private void showContextSelectionDialog() {
        if (getContext() == null) return;

        executorService.execute(() -> {
            SearchDao dao = PocketGptDatabase.getDatabase(requireContext()).searchDao();
            List<AppDocument> docs = dao.getAllDocuments();

            mainHandler.post(() -> {
                if (getContext() == null) return;

                List<String> items = new ArrayList<>();
                items.add("All Documents (Full Knowledge Base)");
                items.add("No Context (General Offline AI)");

                for (AppDocument doc : docs) {
                    items.add(doc.title + " (" + doc.chunkCount + " chunks)");
                }

                items.add("Upload New Document / PDF");

                new AlertDialog.Builder(requireContext())
                        .setTitle("Select RAG Context")
                        .setItems(items.toArray(new String[0]), (dialog, which) -> {
                            if (which == 0) {
                                selectedDocId = null;
                                selectedDocTitle = "All Documents";
                                isGeneralChatOnly = false;
                                updateContextUI();
                                Toast.makeText(getContext(), "Context set to All Documents", Toast.LENGTH_SHORT).show();
                            } else if (which == 1) {
                                selectedDocId = null;
                                selectedDocTitle = "None";
                                isGeneralChatOnly = true;
                                updateContextUI();
                                Toast.makeText(getContext(), "Context cleared (General AI)", Toast.LENGTH_SHORT).show();
                            } else if (which == items.size() - 1) {
                                filePickerLauncher.launch(new String[]{"application/pdf", "text/plain", "text/markdown"});
                            } else {
                                AppDocument picked = docs.get(which - 2);
                                selectedDocId = picked.id;
                                selectedDocTitle = picked.title;
                                isGeneralChatOnly = false;
                                updateContextUI();
                                Toast.makeText(getContext(), "Context set to " + picked.title, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        });
    }

    private void sendMessage() {
        String message = chatEditText.getText().toString().trim();
        if (message.isEmpty()) return;

        chatEditText.setText("");
        emptyStateLayout.setVisibility(View.GONE);

        // Add user message to UI
        ChatMessage userMsg = new ChatMessage(ChatMessage.TYPE_USER, message);
        chatAdapter.addMessage(userMsg);
        chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);

        // Loading state
        ragProgressBar.setVisibility(View.VISIBLE);
        sendButton.setEnabled(false);

        Integer targetDocId = isGeneralChatOnly ? -1 : selectedDocId;

        executorService.execute(() -> {
            if (getContext() == null) return;

            ChatDao chatDao = PocketGptDatabase.getDatabase(requireContext()).chatDao();

            // 1. Create or get session
            if (currentSessionId == null) {
                String sessionTitle = message.length() > 40 ? message.substring(0, 40) + "..." : message;
                int docId = targetDocId != null ? targetDocId : -1;
                ChatSession newSession = new ChatSession(sessionTitle, docId, selectedDocTitle);
                newSession.lastMessage = message;
                newSession.messageCount = 1;
                long sessId = chatDao.insertSession(newSession);
                currentSessionId = (int) sessId;
            }

            // Save User message entity
            ChatMessageEntity userEntity = new ChatMessageEntity(
                    currentSessionId,
                    ChatMessage.TYPE_USER,
                    userMsg.getContent(),
                    "User",
                    userMsg.getTimestamp(),
                    0,
                    null
            );
            chatDao.insertMessage(userEntity);

            // 2. Answer Query via RAG
            RagEngine.RagResult result = RagEngine.answerQuery(requireContext(), message, targetDocId);

            // Save Assistant message entity
            ChatMessageEntity assistantEntity = new ChatMessageEntity(
                    currentSessionId,
                    ChatMessage.TYPE_ASSISTANT,
                    result.answer,
                    result.modelName,
                    "Now",
                    result.processingTimeMs,
                    null
            );
            chatDao.insertMessage(assistantEntity);

            // Update session
            ChatSession session = chatDao.getSessionById(currentSessionId);
            if (session != null) {
                session.lastMessage = result.answer;
                session.messageCount = chatDao.getMessagesForSession(currentSessionId).size();
                session.updatedAt = System.currentTimeMillis();
                chatDao.updateSession(session);
            }

            mainHandler.post(() -> {
                ragProgressBar.setVisibility(View.GONE);
                sendButton.setEnabled(true);

                if (getContext() != null) {
                    ChatMessage assistantMsg = new ChatMessage(
                            ChatMessage.TYPE_ASSISTANT,
                            result.answer,
                            result.modelName,
                            result.sources,
                            result.processingTimeMs
                    );
                    chatAdapter.addMessage(assistantMsg);
                    chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                }
            });
        });
    }

    private void processSelectedFile(Uri uri) {
        String fileName = getFileName(uri);
        if (fileName == null) fileName = "Document_" + System.currentTimeMillis();
        final String docTitle = fileName;

        ragProgressBar.setVisibility(View.VISIBLE);
        ragContextText.setText("Processing & embedding " + docTitle + "...");

        executorService.execute(() -> {
            try {
                if (getContext() == null) return;

                // 1. Extract Text
                String extractedText;
                if (docTitle.toLowerCase().endsWith(".pdf")) {
                    extractedText = pdfService.extractTextFromUri(requireContext(), uri);
                } else {
                    extractedText = extractPlainTextFromUri(uri);
                }

                if (extractedText == null || extractedText.trim().isEmpty()) {
                    extractedText = "Content extracted from " + docTitle + " for offline search.";
                }

                // 2. Save Document to Room
                SearchDao dao = PocketGptDatabase.getDatabase(requireContext()).searchDao();
                String docType = docTitle.toLowerCase().endsWith(".pdf") ? "PDF" : "TEXT";
                AppDocument appDoc = new AppDocument(docTitle, extractedText, docType, "Imported");
                long insertedId = dao.insertDocument(appDoc);
                int docId = (int) insertedId;

                // 3. Chunk and Embed
                List<String> chunks = DocumentChunker.chunkText(extractedText, 450, 45);
                List<DocumentChunk> chunkEntities = new ArrayList<>();

                for (int i = 0; i < chunks.size(); i++) {
                    String chunkText = chunks.get(i);
                    float[] vector = embeddingService.generateEmbedding(chunkText);
                    String vecStr = embeddingService.serializeVector(vector);
                    chunkEntities.add(new DocumentChunk(docId, docTitle, chunkText, i, vecStr));
                }

                if (!chunkEntities.isEmpty()) {
                    dao.insertChunks(chunkEntities);
                    appDoc.id = docId;
                    appDoc.chunkCount = chunkEntities.size();
                    dao.updateDocument(appDoc);
                }

                mainHandler.post(() -> {
                    ragProgressBar.setVisibility(View.GONE);
                    selectedDocId = docId;
                    selectedDocTitle = docTitle;
                    isGeneralChatOnly = false;
                    updateContextUI();
                    emptyStateLayout.setVisibility(View.GONE);

                    ChatMessage welcomeMsg = new ChatMessage(
                            ChatMessage.TYPE_ASSISTANT,
                            "**" + docTitle + "** has been successfully ingested and indexed into **" + chunkEntities.size() + " vector chunks** on your device.\n\nYou can now ask any question regarding this document!"
                    );
                    chatAdapter.addMessage(welcomeMsg);
                    chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                    Toast.makeText(getContext(), "Document indexed locally!", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    ragProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to process document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void processSelectedImage(Uri imageUri) {
        String fileName = getFileName(imageUri);
        if (fileName == null) fileName = "Scanned_Image_" + System.currentTimeMillis();
        final String docTitle = fileName;

        ragProgressBar.setVisibility(View.VISIBLE);
        ragContextText.setText("Running OCR on " + docTitle + "...");

        executorService.execute(() -> {
            try {
                if (getContext() == null) return;

                String ocrText = ocrService.extractTextFromUri(requireContext(), imageUri);
                SearchDao dao = PocketGptDatabase.getDatabase(requireContext()).searchDao();
                AppDocument appDoc = new AppDocument(docTitle, ocrText, "OCR", "Scanned");
                long insertedId = dao.insertDocument(appDoc);
                int docId = (int) insertedId;

                List<String> chunks = DocumentChunker.chunkText(ocrText, 450, 45);
                List<DocumentChunk> chunkEntities = new ArrayList<>();
                for (int i = 0; i < chunks.size(); i++) {
                    String chunkText = chunks.get(i);
                    float[] vector = embeddingService.generateEmbedding(chunkText);
                    chunkEntities.add(new DocumentChunk(docId, docTitle, chunkText, i, embeddingService.serializeVector(vector)));
                }

                if (!chunkEntities.isEmpty()) {
                    dao.insertChunks(chunkEntities);
                    appDoc.id = docId;
                    appDoc.chunkCount = chunkEntities.size();
                    dao.updateDocument(appDoc);
                }

                mainHandler.post(() -> {
                    ragProgressBar.setVisibility(View.GONE);
                    selectedDocId = docId;
                    selectedDocTitle = docTitle;
                    isGeneralChatOnly = false;
                    updateContextUI();
                    emptyStateLayout.setVisibility(View.GONE);

                    ChatMessage welcomeMsg = new ChatMessage(
                            ChatMessage.TYPE_ASSISTANT,
                            "**" + docTitle + "** OCR completed and indexed into " + chunkEntities.size() + " chunks. Ready for questions!"
                    );
                    chatAdapter.addMessage(welcomeMsg);
                    chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                    Toast.makeText(getContext(), "OCR Document Indexed!", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    ragProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "OCR Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private String extractPlainTextFromUri(Uri uri) {
        if (getContext() == null) return "";
        try (InputStream is = getContext().getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme()) && getContext() != null) {
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
            int cut = result != null ? result.lastIndexOf('/') : -1;
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}