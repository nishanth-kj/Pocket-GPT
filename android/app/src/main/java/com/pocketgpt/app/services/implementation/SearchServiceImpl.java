package com.pocketgpt.app.services.implementation;

import com.pocketgpt.app.model.AppDocument;
import com.pocketgpt.app.repository.SearchDao;
import com.pocketgpt.app.services.SearchService;

import java.util.ArrayList;
import java.util.List;

public class SearchServiceImpl implements SearchService {

    private final SearchDao searchDao;

    public SearchServiceImpl(SearchDao searchDao) {
        this.searchDao = searchDao;
    }

    @Override
    public List<AppDocument> searchAiKnowledge(String query) {
        // Preprocess query here (e.g. removing stop words, stemming) if needed
        // before passing it to the database repository
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        String ftsQuery = "*" + query.trim() + "*";
        return searchDao.searchDocuments(ftsQuery);
    }

    @Override
    public List<AppDocument> searchDomainSpecific(String query, String domainType) {
        if (query == null || query.trim().isEmpty() || domainType == null) {
            return List.of();
        }
        String ftsQuery = "*" + query.trim() + "*";
        return searchDao.searchDocumentsByType(ftsQuery, domainType);
    }

    @Override
    public void indexDocuments(List<AppDocument> documents) {
        if (documents != null && !documents.isEmpty()) {
            // We use a background thread to prevent Room from throwing a MainThreadException
            // when checking for duplicates.
            new Thread(() -> {
                List<AppDocument> toInsert = new ArrayList<>();
                for (AppDocument doc : documents) {
                    AppDocument existing = searchDao.getDocumentByTitle(doc.title);
                    if (existing == null) {
                        toInsert.add(doc);
                    }
                }
                if (!toInsert.isEmpty()) {
                    searchDao.insertDocuments(toInsert);
                }
            }).start();
        }
    }
}
