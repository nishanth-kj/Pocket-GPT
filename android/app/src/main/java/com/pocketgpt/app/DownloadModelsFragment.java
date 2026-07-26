package com.pocketgpt.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;

public class DownloadModelsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download_models, container, false);
        MaterialButton btnDownloadGemma = view.findViewById(R.id.btnDownloadGemma);
        MaterialButton btnDownloadLlama = view.findViewById(R.id.btnDownloadLlama);
        MaterialButton btnDownloadMistral = view.findViewById(R.id.btnDownloadMistral);
        btnDownloadGemma.setOnClickListener(v -> Toast.makeText(getContext(), "Starting download: Gemma 2B...", Toast.LENGTH_SHORT).show());
        btnDownloadLlama.setOnClickListener(v -> Toast.makeText(getContext(), "Starting download: Llama-3 8B...", Toast.LENGTH_SHORT).show());
        btnDownloadMistral.setOnClickListener(v -> Toast.makeText(getContext(), "Starting download: Mistral v0.2...", Toast.LENGTH_SHORT).show());
        return view;
    }
}