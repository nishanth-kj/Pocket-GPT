package com.pocketgpt.app.ui.models;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.pocketgpt.app.R;
import com.pocketgpt.app.model.AiModel;
import com.pocketgpt.app.utils.ModelManager;

import java.util.List;

public class AiModelsFragment extends Fragment implements AiModelsAdapter.ModelActionListener {

    private RecyclerView recyclerView;
    private AiModelsAdapter adapter;
    private TextView textActiveModelBannerTitle;
    private TextView textActiveModelBannerSubtitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_models, container, false);

        textActiveModelBannerTitle = view.findViewById(R.id.textActiveModelBannerTitle);
        textActiveModelBannerSubtitle = view.findViewById(R.id.textActiveModelBannerSubtitle);
        recyclerView = view.findViewById(R.id.recyclerViewModels);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadModels();
        return view;
    }

    private void loadModels() {
        if (getContext() == null) return;
        ModelManager manager = ModelManager.getInstance(getContext());
        List<AiModel> list = manager.getAllModels();
        AiModel activeModel = manager.getActiveModel();

        if (activeModel != null) {
            textActiveModelBannerTitle.setText("Active Engine: " + activeModel.getName() + " (" + activeModel.getPublisher() + ")");
            textActiveModelBannerSubtitle.setText("100% On-Device Offline Inference Ready • " + activeModel.getSizeFormatted());
        }

        adapter = new AiModelsAdapter(list, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onModelChanged() {
        loadModels();
    }
}