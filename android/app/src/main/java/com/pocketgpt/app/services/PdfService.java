package com.pocketgpt.app.services;

import java.io.File;
import java.util.List;

/**
 * Service for handling PDF documents.
 */
public interface PdfService {

    /**
     * Extracts all text pages from the given PDF file.
     *
     * @param pdfFile The PDF file to read.
     * @return A list of strings where each string represents a page of text.
     */
    List<String> extractPages(File pdfFile);
    
}
