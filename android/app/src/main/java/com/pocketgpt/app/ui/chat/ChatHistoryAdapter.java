package com.pocketgpt.app.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.pocketgpt.app.R;
import com.pocketgpt.app.model.ChatSession;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.ViewHolder> {

    public interface SessionClickListener {
        void onSessionSelected(ChatSession session);
        void onSessionDeleted(ChatSession session);
    }

    private List<ChatSession> sessions = new ArrayList<>();
    private SessionClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());

    public void setListener(SessionClickListener listener) {
        this.listener = listener;
    }

    public void setSessions(List<ChatSession> list) {
        this.sessions = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_session, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatSession session = sessions.get(position);
        holder.bind(session, listener, dateFormat);
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textSessionTitle;
        private final Chip chipSessionContext;
        private final TextView textSessionSnippet;
        private final TextView textSessionMeta;
        private final MaterialButton btnDeleteSession;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textSessionTitle = itemView.findViewById(R.id.textSessionTitle);
            chipSessionContext = itemView.findViewById(R.id.chipSessionContext);
            textSessionSnippet = itemView.findViewById(R.id.textSessionSnippet);
            textSessionMeta = itemView.findViewById(R.id.textSessionMeta);
            btnDeleteSession = itemView.findViewById(R.id.btnDeleteSession);
        }

        public void bind(ChatSession session, SessionClickListener listener, SimpleDateFormat dateFormat) {
            textSessionTitle.setText(session.title != null ? session.title : "Untitled Conversation");
            
            String contextLabel = session.targetDocTitle != null && !session.targetDocTitle.isEmpty()
                    ? session.targetDocTitle : "General";
            chipSessionContext.setText(contextLabel);

            String snippet = session.lastMessage != null ? session.lastMessage.replace("\n", " ").trim() : "Empty session";
            if (snippet.length() > 110) {
                snippet = snippet.substring(0, 110) + "...";
            }
            textSessionSnippet.setText(snippet);

            String dateFormatted = dateFormat.format(new Date(session.updatedAt > 0 ? session.updatedAt : session.createdAt));
            textSessionMeta.setText(session.messageCount + " messages • " + dateFormatted);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onSessionSelected(session);
            });

            btnDeleteSession.setOnClickListener(v -> {
                if (listener != null) listener.onSessionDeleted(session);
            });
        }
    }
}