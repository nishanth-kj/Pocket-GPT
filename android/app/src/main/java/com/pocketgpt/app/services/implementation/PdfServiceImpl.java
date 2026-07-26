package com.pocketgpt.app.services.implementation;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import com.pocketgpt.app.services.PdfService;
public class PdfServiceImpl implements PdfService {
    @Override
    public List<String> extractPages(File pdfFile) { return new ArrayList<>(); }
}
