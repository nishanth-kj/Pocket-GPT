package com.pocketgpt.app.services;

import com.pocketgpt.app.model.AppDocument;

import java.util.List;

public interface SearchService {
    
    /**
     * Search documents using natural language or keywords.
     * The service may perform additional preprocessing on the query before hitting the repository.
     */
    List<AppDocument> searchAiKnowledge(String query);

    /**
     * Specifically search within a particular legal domain (e.g. Bare Acts).
     */
    List<AppDocument> searchDomainSpecific(String query, String domainType);

    /**
     * Index new documents imported by the user or OCR.
     */
    void indexDocuments(List<AppDocument> documents);
}
