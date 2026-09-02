package com.pocketgpt.app.ui.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.pocketgpt.app.R;
import com.pocketgpt.app.model.AiModel;
import com.pocketgpt.app.utils.ModelManager;

import java.io.File;
import java.util.List;

public class AiModelsAdapter extends RecyclerView.Adapter<AiModelsAdapter.ViewHolder> {

    public interface ModelActionListener {
        void onModelChanged();
    }

    private final List<AiModel> models;
    private final ModelActionListener listener;

    public AiModelsAdapter(List<AiModel> models, ModelActionListener listener) {
        this.models = models;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ai_model, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AiModel model = models.get(position);
        holder.bind(model, listener, this);
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    private static void safeNotifyItemChanged(AiModelsAdapter adapter, RecyclerView.ViewHolder holder) {
        int position = holder.getAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            adapter.notifyItemChanged(position);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textModelName;
        private final TextView textModelDetails;
        private final Chip chipModelStatus;
        private final ProgressBar progressBarModelDownload;
        private final MaterialButton btnDownload;
        private final MaterialButton btnSetActive;
        private final MaterialButton btnDeleteModel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textModelName = itemView.findViewById(R.id.textModelName);
            textModelDetails = itemView.findViewById(R.id.textModelDetails);
            chipModelStatus = itemView.findViewById(R.id.chipModelStatus);
            progressBarModelDownload = itemView.findViewById(R.id.progressBarModelDownload);
            btnDownload = itemView.findViewById(R.id.btnDownload);
            btnSetActive = itemView.findViewById(R.id.btnSetActive);
            btnDeleteModel = itemView.findViewById(R.id.btnDeleteModel);
        }

        public void bind(AiModel model, ModelActionListener listener, AiModelsAdapter adapter) {
            Context context = itemView.getContext();
            ModelManager manager = ModelManager.getInstance(context);

            textModelName.setText(model.getName() + " (" + model.getPublisher() + ")");
            
            String details = "Size: " + model.getSizeFormatted() + " • " + model.getDescription();
            if (model.isDownloading() && !model.getDownloadStatusMessage().isEmpty()) {
                details += "\n" + model.getDownloadStatusMessage();
            } else if (model.isDownloaded() && model.getLocalFilePath() != null) {
                details += "\nSaved: " + model.getFileName();
            }
            textModelDetails.setText(details);

            if (model.isActive() && model.isDownloaded()) {
                chipModelStatus.setVisibility(View.VISIBLE);
                chipModelStatus.setText("ACTIVE");
                btnDownload.setVisibility(View.GONE);
                btnSetActive.setVisibility(View.GONE);
                btnDeleteModel.setVisibility(View.VISIBLE);
                progressBarModelDownload.setVisibility(View.GONE);
            } else if (model.isDownloading()) {
                chipModelStatus.setVisibility(View.VISIBLE);
                chipModelStatus.setText("DOWNLOADING " + model.getDownloadProgress() + "%");
                btnDownload.setVisibility(View.VISIBLE);
                btnDownload.setText("Cancel");
                btnSetActive.setVisibility(View.GONE);
                btnDeleteModel.setVisibility(View.GONE);
                progressBarModelDownload.setVisibility(View.VISIBLE);
                progressBarModelDownload.setIndeterminate(model.getDownloadProgress() <= 0);
                if (model.getDownloadProgress() > 0) {
                    progressBarModelDownload.setProgress(model.getDownloadProgress());
                }
            } else if (model.isDownloaded()) {
                chipModelStatus.setVisibility(View.VISIBLE);
                chipModelStatus.setText("DOWNLOADED");
                btnDownload.setVisibility(View.GONE);
                btnSetActive.setVisibility(View.VISIBLE);
                btnDeleteModel.setVisibility(View.VISIBLE);
                progressBarModelDownload.setVisibility(View.GONE);
            } else {
                chipModelStatus.setVisibility(View.GONE);
                btnDownload.setVisibility(View.VISIBLE);
                btnDownload.setText("Download");
                btnSetActive.setVisibility(View.GONE);
                btnDeleteModel.setVisibility(View.GONE);
                progressBarModelDownload.setVisibility(View.GONE);
            }

            btnDownload.setOnClickListener(v -> {
                if (model.isDownloading()) {
                    manager.cancelDownload(model.getId());
                    Toast.makeText(context, "Download cancelled", Toast.LENGTH_SHORT).show();
                    safeNotifyItemChanged(adapter, this);
                    return;
                }

                Toast.makeText(context, "Connecting to Hugging Face CDN for " + model.getName() + "...", Toast.LENGTH_SHORT).show();
                manager.downloadModel(model.getId(), new ModelManager.ModelDownloadListener() {
                    @Override
                    public void onProgress(String modelId, int progressPercent, String statusMessage) {
                        safeNotifyItemChanged(adapter, ViewHolder.this);
                    }

                    @Override
                    public void onComplete(String modelId, File modelFile) {
                        Toast.makeText(context, model.getName() + " downloaded (" + (modelFile.length() / (1024 * 1024)) + " MB)!", Toast.LENGTH_SHORT).show();
                        if (listener != null) listener.onModelChanged();
                    }

                    @Override
                    public void onError(String modelId, String errorMessage) {
                        Toast.makeText(context, "Download failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        safeNotifyItemChanged(adapter, ViewHolder.this);
                    }
                });
                safeNotifyItemChanged(adapter, this);
            });

            btnSetActive.setOnClickListener(v -> {
                manager.setActiveModel(model.getId());
                Toast.makeText(context, "Switched active AI Model to " + model.getName(), Toast.LENGTH_SHORT).show();
                if (listener != null) listener.onModelChanged();
            });

            btnDeleteModel.setOnClickListener(v -> {
                manager.deleteModel(model.getId());
                Toast.makeText(context, "Deleted " + model.getName() + " from device storage", Toast.LENGTH_SHORT).show();
                if (listener != null) listener.onModelChanged();
            });
        }
    }
}
