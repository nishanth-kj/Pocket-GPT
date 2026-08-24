package com.pocketgpt.app.ui.documents;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.pocketgpt.app.R;
import com.pocketgpt.app.constants.ResponseStatus;

public class DocumentsFragment extends Fragment {

    private DocumentsViewModel viewModel;
    private DocumentAdapter adapter;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_documents, container, false);
        
        TextInputEditText searchEditText = view.findViewById(R.id.searchEditText);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView emptyStateText = view.findViewById(R.id.emptyStateText);
        
        adapter = new DocumentAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        
        viewModel = new ViewModelProvider(requireActivity()).get(DocumentsViewModel.class);
        
        viewModel.getSearchResult().observe(getViewLifecycleOwner(), response -> {
            progressBar.setVisibility(View.GONE);
            if (response.status == ResponseStatus.SUCCESS) {
                if (response.data != null && !response.data.isEmpty()) {
                    adapter.setDocuments(response.data);
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyStateText.setVisibility(View.GONE);
                } else {
                    adapter.setDocuments(null);
                    recyclerView.setVisibility(View.GONE);
                    emptyStateText.setVisibility(View.VISIBLE);
                    emptyStateText.setText("No documents found.");
                }
            } else if (response.status == ResponseStatus.ERROR) {
                recyclerView.setVisibility(View.GONE);
                emptyStateText.setVisibility(View.VISIBLE);
                String errorMsg = (response.error != null && response.error.errorMessage != null) 
                        ? response.error.errorMessage : "An unknown error occurred.";
                emptyStateText.setText("Error: " + errorMsg);
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                String query = s.toString();
                if (!query.isEmpty()) progressBar.setVisibility(View.VISIBLE);
                viewModel.search(query);
            }
        });
        
        return view;
    }
}