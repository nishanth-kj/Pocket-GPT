package com.pocketgpt.app.ui.viewer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.pocketgpt.app.R;
import com.pocketgpt.app.model.AppDocument;
import com.pocketgpt.app.repository.SearchDao;
import com.pocketgpt.app.ui.activities.ChatActivity;
import com.pocketgpt.app.ui.chat.ChatFragment;
import com.pocketgpt.app.utils.PocketGptDatabase;

public class PdfViewerFragment extends Fragment {

    public static final String ARG_DOCUMENT_ID = "arg_document_id";

    private TextView textViewerTitle;
    private TextView textViewerMeta;
    private TextView textViewerContent;
    private ExtendedFloatingActionButton fabViewerChat;
    private AppDocument currentDoc;

    public static PdfViewerFragment newInstance(int documentId) {
        PdfViewerFragment fragment = new PdfViewerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DOCUMENT_ID, documentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pdf_viewer, container, false);

        textViewerTitle = view.findViewById(R.id.textViewerTitle);
        textViewerMeta = view.findViewById(R.id.textViewerMeta);
        textViewerContent = view.findViewById(R.id.textViewerContent);
        fabViewerChat = view.findViewById(R.id.fabViewerChat);

        int docId = getArguments() != null ? getArguments().getInt(ARG_DOCUMENT_ID, -1) : -1;
        if (docId > 0) {
            android.content.Context appContext = requireContext().getApplicationContext();
            new Thread(() -> {
                SearchDao dao = PocketGptDatabase.getDatabase(appContext).searchDao();
                AppDocument doc = dao.getDocumentById(docId);
                if (doc != null && isAdded() && getActivity() != null) {
                    currentDoc = doc;
                    getActivity().runOnUiThread(() -> {
                        if (!isAdded()) return;
                        textViewerTitle.setText(currentDoc.title);
                        textViewerMeta.setText("Type: " + currentDoc.documentType + " • " + currentDoc.chunkCount + " Vector Chunks");
                        textViewerContent.setText(currentDoc.content);
                    });
                }
            }).start();
        }

        fabViewerChat.setOnClickListener(v -> {
            if (currentDoc != null) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(ChatFragment.EXTRA_DOCUMENT_ID, currentDoc.id);
                intent.putExtra(ChatFragment.EXTRA_DOCUMENT_TITLE, currentDoc.title);
                startActivity(intent);
            }
        });

        return view;
    }
}