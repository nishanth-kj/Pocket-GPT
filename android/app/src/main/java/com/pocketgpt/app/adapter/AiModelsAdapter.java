package com.pocketgpt.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.pocketgpt.app.R;
import com.pocketgpt.app.model.AiModel;
import java.util.List;

public class AiModelsAdapter extends RecyclerView.Adapter<AiModelsAdapter.ViewHolder> {

    private final List<AiModel> models;

    public AiModelsAdapter(List<AiModel> models) {
        this.models = models;
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
        holder.textModelName.setText(model.getName() + " (" + model.getPublisher() + ")");
        holder.textModelDetails.setText("Size: " + model.getSizeFormatted() + " • " + model.getDescription());
        
        holder.btnDownload.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Starting download: " + model.getName() + "...", Toast.LENGTH_SHORT).show();
            // TODO: Implement actual background download logic
        });
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textModelName;
        public TextView textModelDetails;
        public MaterialButton btnDownload;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textModelName = itemView.findViewById(R.id.textModelName);
            textModelDetails = itemView.findViewById(R.id.textModelDetails);
            btnDownload = itemView.findViewById(R.id.btnDownload);
        }
    }
}
