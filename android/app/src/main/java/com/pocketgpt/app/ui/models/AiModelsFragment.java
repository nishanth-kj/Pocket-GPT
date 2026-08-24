package com.pocketgpt.app.ui.models;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.pocketgpt.app.R;
import com.pocketgpt.app.model.AiModel;
import java.util.ArrayList;
import java.util.List;

public class AiModelsFragment extends Fragment {
    
    private RecyclerView recyclerView;
    private AiModelsAdapter adapter;
    private List<AiModel> modelsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_models, container, false);
        
        recyclerView = view.findViewById(R.id.recyclerViewModels);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Initialize dynamic list
        modelsList = new ArrayList<>();
        modelsList.add(new AiModel("gemma-2b", "Gemma 2B", "Google", "1.4 GB", "Fast & Efficient"));
        modelsList.add(new AiModel("llama-3-8b", "Llama-3 8B", "Meta", "4.2 GB", "Highly Capable"));
        modelsList.add(new AiModel("mistral-v0.2", "Mistral v0.2", "Mistral AI", "3.8 GB", "Balanced Performance"));
        
        adapter = new AiModelsAdapter(modelsList);
        recyclerView.setAdapter(adapter);
        
        return view;
    }
}