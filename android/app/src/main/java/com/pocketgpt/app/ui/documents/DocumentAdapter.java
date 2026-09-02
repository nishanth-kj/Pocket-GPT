package com.pocketgpt.app.ui.documents;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.pocketgpt.app.R;
import com.pocketgpt.app.model.AppDocument;

import java.util.ArrayList;
import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {

    public interface DocumentActionListener {
        void onChatClicked(AppDocument document);
        void onDeleteClicked(AppDocument document);
        void onItemClicked(AppDocument document);
    }

    private List<AppDocument> documents = new ArrayList<>();
    private DocumentActionListener listener;

    public void setListener(DocumentActionListener listener) {
        this.listener = listener;
    }

    public void setDocuments(List<AppDocument> documents) {
        this.documents = documents == null ? new ArrayList<>() : documents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_legal_document, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppDocument doc = documents.get(position);
        holder.bind(doc, listener);
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textTitle;
        private final Chip textType;
        private final TextView textContentSnippet;
        private final TextView textChunkBadge;
        private final MaterialButton btnDeleteDoc;
        private final MaterialButton btnChatDoc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textType = itemView.findViewById(R.id.textType);
            textContentSnippet = itemView.findViewById(R.id.textContentSnippet);
            textChunkBadge = itemView.findViewById(R.id.textChunkBadge);
            btnDeleteDoc = itemView.findViewById(R.id.btnDeleteDoc);
            btnChatDoc = itemView.findViewById(R.id.btnChatDoc);
        }

        public void bind(AppDocument doc, DocumentActionListener listener) {
            textTitle.setText(doc.title);
            textType.setText(doc.documentType != null ? doc.documentType : "DOC");

            String snippet = doc.content != null ? doc.content.replace("\n", " ").trim() : "";
            if (snippet.length() > 160) {
                snippet = snippet.substring(0, 160) + "...";
            }
            textContentSnippet.setText(snippet);

            int chunkCount = doc.chunkCount > 0 ? doc.chunkCount : 1;
            textChunkBadge.setText(chunkCount + " Vector Chunks");

            btnChatDoc.setOnClickListener(v -> {
                if (listener != null) listener.onChatClicked(doc);
            });

            btnDeleteDoc.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClicked(doc);
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClicked(doc);
            });
        }
    }
}