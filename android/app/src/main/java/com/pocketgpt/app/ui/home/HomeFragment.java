package com.pocketgpt.app.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.card.MaterialCardView;
import com.pocketgpt.app.R;

public class HomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        MaterialCardView cardSearch = view.findViewById(R.id.cardSearch);
        MaterialCardView cardBookmarks = view.findViewById(R.id.cardBookmarks);
        
        cardSearch.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Navigating to Search...", Toast.LENGTH_SHORT).show();
            // Navigate logic here if needed
        });
        
        cardBookmarks.setOnClickListener(v -> Toast.makeText(getContext(), "Quick Bookmarks Clicked", Toast.LENGTH_SHORT).show());
        
        return view;
    }
}