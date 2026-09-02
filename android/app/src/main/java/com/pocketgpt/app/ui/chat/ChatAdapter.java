package com.pocketgpt.app.ui.chat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.pocketgpt.app.R;
import com.pocketgpt.app.model.ChatMessage;
import com.pocketgpt.app.services.SpeechService;
import com.pocketgpt.app.utils.RagEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> messages = new ArrayList<>();

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void setMessages(List<ChatMessage> list) {
        messages.clear();
        if (list != null) {
            messages.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        messages.clear();
        notifyDataSetChanged();
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == ChatMessage.TYPE_USER) {
            View view = inflater.inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_chat_assistant, parent, false);
            return new AssistantViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).bind(message);
        } else if (holder instanceof AssistantViewHolder) {
            ((AssistantViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView textUserMessage;
        private final TextView textUserTime;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textUserMessage = itemView.findViewById(R.id.textUserMessage);
            textUserTime = itemView.findViewById(R.id.textUserTime);
        }

        public void bind(ChatMessage message) {
            textUserMessage.setText(message.getContent());
            textUserTime.setText(message.getTimestamp());
        }
    }

    static class AssistantViewHolder extends RecyclerView.ViewHolder {
        private final TextView textAssistantStats;
        private final TextView textAssistantMessage;
        private final LinearLayout layoutCitations;
        private final TextView textAssistantCitations;
        private final ImageButton btnAssistantSpeak;
        private final ImageButton btnAssistantCopy;

        public AssistantViewHolder(@NonNull View itemView) {
            super(itemView);
            textAssistantStats = itemView.findViewById(R.id.textAssistantStats);
            textAssistantMessage = itemView.findViewById(R.id.textAssistantMessage);
            layoutCitations = itemView.findViewById(R.id.layoutCitations);
            textAssistantCitations = itemView.findViewById(R.id.textAssistantCitations);
            btnAssistantSpeak = itemView.findViewById(R.id.btnAssistantSpeak);
            btnAssistantCopy = itemView.findViewById(R.id.btnAssistantCopy);
        }

        public void bind(ChatMessage message) {
            Context context = itemView.getContext();
            String model = message.getModelName() != null ? message.getModelName() : "Pocket GPT";
            if (message.getLatencyMs() > 0) {
                model += " • " + message.getLatencyMs() + "ms • Offline";
            } else {
                model += " • Offline";
            }
            textAssistantStats.setText(model);

            // Render formatted markdown/bold text cleanly
            String formattedHtml = message.getContent()
                    .replace("\n", "<br/>")
                    .replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>")
                    .replaceAll("\\*(.*?)\\*", "<i>$1</i>");
            textAssistantMessage.setText(Html.fromHtml(formattedHtml, Html.FROM_HTML_MODE_COMPACT));

            // Render Sources
            List<RagEngine.RetrievedChunk> sources = message.getSources();
            if (sources != null && !sources.isEmpty() && sources.get(0).combinedScore >= 0.20f) {
                layoutCitations.setVisibility(View.VISIBLE);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < sources.size(); i++) {
                    RagEngine.RetrievedChunk rc = sources.get(i);
                    String docTitle = rc.chunk.documentTitle != null ? rc.chunk.documentTitle : "Document #" + rc.chunk.documentId;
                    sb.append("• ").append(docTitle)
                            .append(" (Chunk #").append(rc.chunk.chunkIndex + 1)
                            .append(" - ").append(String.format(Locale.US, "%.0f%% match", rc.combinedScore * 100))
                            .append(")\n");
                }
                textAssistantCitations.setText(sb.toString().trim());
            } else {
                layoutCitations.setVisibility(View.GONE);
            }

            // Speak TTS
            btnAssistantSpeak.setOnClickListener(v -> {
                SpeechService speechService = SpeechService.getInstance(context);
                if (speechService.isSpeaking()) {
                    speechService.stop();
                } else {
                    speechService.speak(message.getContent());
                    Toast.makeText(context, "Speaking response...", Toast.LENGTH_SHORT).show();
                }
            });

            // Copy to Clipboard
            btnCopyClick(context, message);
        }

        private void btnCopyClick(Context context, ChatMessage message) {
            btnAssistantCopy.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData clip = ClipData.newPlainText("Pocket GPT Response", message.getContent());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}


