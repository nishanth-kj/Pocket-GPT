package com.pocketgpt.app.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.pocketgpt.app.R;
import com.pocketgpt.app.model.AiModel;
import com.pocketgpt.app.model.AppDocument;
import com.pocketgpt.app.repository.SearchDao;
import com.pocketgpt.app.ui.activities.ChatActivity;
import com.pocketgpt.app.ui.chat.ChatFragment;
import com.pocketgpt.app.ui.documents.DocumentAdapter;
import com.pocketgpt.app.ui.documents.DocumentsFragment;
import com.pocketgpt.app.ui.models.AiModelsFragment;
import com.pocketgpt.app.utils.ModelManager;
import com.pocketgpt.app.utils.PocketGptDatabase;
import com.pocketgpt.app.utils.SampleDataLoader;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment implements DocumentAdapter.DocumentActionListener {

    private TextView textHomeDocCount;
    private TextView textHomeChunkCount;
    private TextView textHomeActiveModel;
    private RecyclerView recyclerViewHomeRecent;
    private DocumentAdapter documentAdapter;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        textHomeDocCount = view.findViewById(R.id.textHomeDocCount);
        textHomeChunkCount = view.findViewById(R.id.textHomeChunkCount);
        textHomeActiveModel = view.findViewById(R.id.textHomeActiveModel);
        recyclerViewHomeRecent = view.findViewById(R.id.recyclerViewHomeRecent);

        MaterialCardView cardAiChat = view.findViewById(R.id.cardAiChat);
        MaterialCardView cardSearch = view.findViewById(R.id.cardSearch);
        MaterialCardView cardModels = view.findViewById(R.id.cardModels);
        MaterialCardView cardLoadSampleData = view.findViewById(R.id.cardLoadSampleData);

        documentAdapter = new DocumentAdapter();
        documentAdapter.setListener(this);
        recyclerViewHomeRecent.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewHomeRecent.setAdapter(documentAdapter);

        cardAiChat.setOnClickListener(v -> startActivity(new Intent(getActivity(), ChatActivity.class)));

        cardSearch.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new DocumentsFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        cardModels.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AiModelsFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        cardLoadSampleData.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Loading sample legal & privacy knowledge pack...", Toast.LENGTH_SHORT).show();
            SampleDataLoader.loadSampleDocuments(requireContext(), (docCount, chunkCount) -> {
                mainHandler.post(() -> {
                    Toast.makeText(getContext(), "Loaded " + docCount + " documents & " + chunkCount + " vector chunks!", Toast.LENGTH_SHORT).show();
                    loadDashboardData();
                });
            });
        });

        loadDashboardData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void loadDashboardData() {
        if (getContext() == null) return;

        AiModel activeModel = ModelManager.getInstance(getContext()).getActiveModel();
        if (activeModel != null) {
            textHomeActiveModel.setText(activeModel.getName());
        }

        executor.execute(() -> {
            SearchDao dao = PocketGptDatabase.getDatabase(requireContext()).searchDao();
            int docCount = dao.getDocumentCount();
            int chunkCount = dao.getChunkCount();
            List<AppDocument> docs = dao.getAllDocuments();

            mainHandler.post(() -> {
                if (getContext() == null) return;
                textHomeDocCount.setText(String.valueOf(docCount));
                textHomeChunkCount.setText(String.valueOf(chunkCount));
                documentAdapter.setDocuments(docs);
            });
        });
    }

    @Override
    public void onChatClicked(AppDocument document) {
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(ChatFragment.EXTRA_DOCUMENT_ID, document.id);
        intent.putExtra(ChatFragment.EXTRA_DOCUMENT_TITLE, document.title);
        startActivity(intent);
    }

    @Override
    public void onDeleteClicked(AppDocument document) {
        executor.execute(() -> {
            SearchDao dao = PocketGptDatabase.getDatabase(requireContext()).searchDao();
            dao.deleteChunksForDocument(document.id);
            dao.deleteDocument(document);
            mainHandler.post(this::loadDashboardData);
        });
    }

    @Override
    public void onItemClicked(AppDocument document) {
        onChatClicked(document);
    }
}