package com.pocketgpt.app.ui.documents;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.pocketgpt.app.R;
import com.pocketgpt.app.model.AppDocument;
import com.pocketgpt.app.services.OcrService;
import com.pocketgpt.app.services.PdfService;
import com.pocketgpt.app.ui.activities.ChatActivity;
import com.pocketgpt.app.ui.chat.ChatFragment;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DocumentsFragment extends Fragment implements DocumentAdapter.DocumentActionListener {

    private DocumentsViewModel viewModel;
    private DocumentAdapter adapter;
    private final PdfService pdfService = PdfService.create();
    private final OcrService ocrService = OcrService.create();

    private final ActivityResultLauncher<String[]> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    processImportFile(uri);
                }
            }
    );

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    processImportImage(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_documents, container, false);

        TextInputEditText searchEditText = view.findViewById(R.id.searchEditText);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        LinearLayout emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        ExtendedFloatingActionButton fabAddDocument = view.findViewById(R.id.fabAddDocument);
        MaterialButton btnLoadSamples = view.findViewById(R.id.btnLoadSamples);
        MaterialButton btnUploadFirstDoc = view.findViewById(R.id.btnUploadFirstDoc);

        adapter = new DocumentAdapter();
        adapter.setListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(DocumentsViewModel.class);

        viewModel.getDocumentsLiveData().observe(getViewLifecycleOwner(), documents -> {
            if (documents != null && !documents.isEmpty()) {
                adapter.setDocuments(documents);
                recyclerView.setVisibility(View.VISIBLE);
                emptyStateContainer.setVisibility(View.GONE);
            } else {
                adapter.setDocuments(null);
                recyclerView.setVisibility(View.GONE);
                emptyStateContainer.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                viewModel.search(s.toString());
            }
        });

        fabAddDocument.setOnClickListener(v -> showAddDocumentOptionsDialog());
        btnLoadSamples.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Loading sample legal & privacy knowledge base...", Toast.LENGTH_SHORT).show();
            viewModel.loadSampleDocuments(() -> {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Sample Knowledge Base Loaded Successfully!", Toast.LENGTH_SHORT).show()
                    );
                }
            });
        });
        btnUploadFirstDoc.setOnClickListener(v -> showAddDocumentOptionsDialog());

        return view;
    }

    private void showAddDocumentOptionsDialog() {
        if (getContext() == null) return;
        String[] options = {
                "Import PDF / Text File",
                "Scan Document Image (OCR)",
                "Write Custom Note / Document",
                "Load Sample Legal Knowledge Pack"
        };

        new AlertDialog.Builder(requireContext())
                .setTitle("Add to Knowledge Base")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        filePickerLauncher.launch(new String[]{"application/pdf", "text/plain", "text/markdown"});
                    } else if (which == 1) {
                        imagePickerLauncher.launch("image/*");
                    } else if (which == 2) {
                        showCustomNoteDialog();
                    } else if (which == 3) {
                        viewModel.loadSampleDocuments(() -> {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() ->
                                        Toast.makeText(getContext(), "Sample Documents Loaded!", Toast.LENGTH_SHORT).show()
                                );
                            }
                        });
                    }
                })
                .show();
    }

    private void showCustomNoteDialog() {
        if (getContext() == null) return;
        EditText inputTitle = new EditText(getContext());
        inputTitle.setHint("Document Title");
        EditText inputContent = new EditText(getContext());
        inputContent.setHint("Paste or type document text...");
        inputContent.setMinLines(5);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);
        layout.addView(inputTitle);
        layout.addView(inputContent);

        new AlertDialog.Builder(requireContext())
                .setTitle("New Note / Document")
                .setView(layout)
                .setPositiveButton("Save & Index", (dialog, which) -> {
                    String title = inputTitle.getText().toString().trim();
                    String content = inputContent.getText().toString().trim();
                    if (!title.isEmpty() && !content.isEmpty()) {
                        viewModel.ingestDocument(title, content, "NOTE", () -> {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() ->
                                        Toast.makeText(getContext(), "Document indexed locally!", Toast.LENGTH_SHORT).show()
                                );
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void processImportFile(Uri uri) {
        Context context = getContext();
        if (context == null) return;
        final Context appContext = context.getApplicationContext();
        String fileName = getFileName(uri);
        if (fileName == null) fileName = "Document_" + System.currentTimeMillis();
        final String docTitle = fileName;

        Toast.makeText(getContext(), "Processing & chunking " + docTitle + "...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                String extractedText;
                if (docTitle.toLowerCase().endsWith(".pdf")) {
                    extractedText = pdfService.extractTextFromUri(appContext, uri);
                } else {
                    extractedText = extractPlainTextFromUri(appContext, uri);
                }

                if (extractedText == null || extractedText.trim().isEmpty()) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(appContext, "No text could be extracted from " + docTitle, Toast.LENGTH_SHORT).show()
                        );
                    }
                    return;
                }

                String docType = docTitle.toLowerCase().endsWith(".pdf") ? "PDF" : "TEXT";
                viewModel.ingestDocument(docTitle, extractedText, docType, () -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Ingested " + docTitle + " successfully!", Toast.LENGTH_SHORT).show()
                        );
                    }
                });
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error importing: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        }).start();
    }

    private void processImportImage(Uri imageUri) {
        Context context = getContext();
        if (context == null) return;
        final Context appContext = context.getApplicationContext();
        String fileName = getFileName(imageUri);
        if (fileName == null) fileName = "Scanned_Doc_" + System.currentTimeMillis();
        final String docTitle = fileName;

        Toast.makeText(getContext(), "Running OCR on " + docTitle + "...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                String ocrText = ocrService.extractTextFromUri(appContext, imageUri);
                viewModel.ingestDocument(docTitle, ocrText, "OCR", () -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "OCR Document Indexed!", Toast.LENGTH_SHORT).show()
                        );
                    }
                });
            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "OCR Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        }).start();
    }

    private String extractPlainTextFromUri(Context context, Uri uri) {
        try (InputStream is = context.getContentResolver().openInputStream(uri);
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

    @Override
    public void onChatClicked(AppDocument document) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(ChatFragment.EXTRA_DOCUMENT_ID, document.id);
        intent.putExtra(ChatFragment.EXTRA_DOCUMENT_TITLE, document.title);
        startActivity(intent);
    }

    @Override
    public void onDeleteClicked(AppDocument document) {
        if (getContext() == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Document")
                .setMessage("Are you sure you want to remove '" + document.title + "' and all its vector chunks from device storage?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteDocument(document);
                    Toast.makeText(getContext(), "Document deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onItemClicked(AppDocument document) {
        if (getContext() == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle(document.title)
                .setMessage("Type: " + document.documentType + "\nChunks: " + document.chunkCount + "\n\n" + document.content)
                .setPositiveButton("Chat with Document", (dialog, which) -> onChatClicked(document))
                .setNegativeButton("Close", null)
                .show();
    }
}