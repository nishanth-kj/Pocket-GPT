package com.pocketgpt.app.ui.documents;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.pocketgpt.app.model.AppDocument;
import com.pocketgpt.app.model.DocumentChunk;
import com.pocketgpt.app.repository.SearchDao;
import com.pocketgpt.app.services.EmbeddingService;
import com.pocketgpt.app.utils.DocumentChunker;
import com.pocketgpt.app.utils.PocketGptDatabase;
import com.pocketgpt.app.utils.SampleDataLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DocumentsViewModel extends AndroidViewModel {

    private final SearchDao searchDao;
    private final EmbeddingService embeddingService = EmbeddingService.create();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<List<AppDocument>> documentsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private String currentQuery = "";

    public DocumentsViewModel(@NonNull Application application) {
        super(application);
        searchDao = PocketGptDatabase.getDatabase(application).searchDao();
        loadDocuments();
    }

    public LiveData<List<AppDocument>> getDocumentsLiveData() {
        return documentsLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void loadDocuments() {
        search(currentQuery);
    }

    public void search(String query) {
        currentQuery = query != null ? query.trim() : "";
        isLoading.postValue(true);
        executor.execute(() -> {
            List<AppDocument> list;
            if (currentQuery.isEmpty()) {
                list = searchDao.getAllDocuments();
            } else {
                list = searchDao.searchDocumentsByText(currentQuery);
                if (list.isEmpty()) {
                    try {
                        list = searchDao.searchDocuments("*" + currentQuery + "*");
                    } catch (Exception ignored) {
                    }
                }
            }
            documentsLiveData.postValue(list);
            isLoading.postValue(false);
        });
    }

    public void deleteDocument(AppDocument document) {
        if (document == null) return;
        executor.execute(() -> {
            searchDao.deleteChunksForDocument(document.id);
            searchDao.deleteDocument(document);
            loadDocuments();
        });
    }

    public void ingestDocument(String title, String content, String type, Runnable onComplete) {
        isLoading.postValue(true);
        executor.execute(() -> {
            AppDocument doc = new AppDocument(title, content, type, "Imported");
            long id = searchDao.insertDocument(doc);
            int docId = (int) id;

            List<String> chunkTexts = DocumentChunker.chunkText(content, 450, 45);
            List<DocumentChunk> chunks = new ArrayList<>();
            for (int i = 0; i < chunkTexts.size(); i++) {
                String c = chunkTexts.get(i);
                float[] vec = embeddingService.generateEmbedding(c);
                chunks.add(new DocumentChunk(docId, title, c, i, embeddingService.serializeVector(vec)));
            }

            if (!chunks.isEmpty()) {
                searchDao.insertChunks(chunks);
                doc.id = docId;
                doc.chunkCount = chunks.size();
                searchDao.updateDocument(doc);
            }

            loadDocuments();
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public void loadSampleDocuments(Runnable onComplete) {
        isLoading.postValue(true);
        SampleDataLoader.loadSampleDocuments(getApplication(), (docCount, chunkCount) -> {
            loadDocuments();
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    @Override
    protected void onCleared() {
        executor.shutdown();
        super.onCleared();
    }
}