package com.pocketgpt.app.services.implementation;

import android.content.Context;
import android.net.Uri;
import com.pocketgpt.app.services.PdfService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Inflater;

public class PdfServiceImpl implements PdfService {

    private static final Pattern TEXT_BLOCK_PATTERN = Pattern.compile("BT(.*?)ET", Pattern.DOTALL);
    private static final Pattern TJ_PATTERN = Pattern.compile("\\((.*?)\\)\\s*Tj", Pattern.DOTALL);
    private static final Pattern TJ_ARRAY_PATTERN = Pattern.compile("\\[(.*?)\\]\\s*TJ", Pattern.DOTALL);
    private static final Pattern STREAM_PATTERN = Pattern.compile("stream[\\r\\n]+(.*?)endstream", Pattern.DOTALL);

    @Override
    public List<String> extractPages(File pdfFile) {
        List<String> pages = new ArrayList<>();
        if (pdfFile == null || !pdfFile.exists()) {
            return pages;
        }

        try (FileInputStream fis = new FileInputStream(pdfFile)) {
            String fullText = extractTextFromStream(fis);
            if (!fullText.isEmpty()) {
                String[] split = fullText.split("--- Page \\d+ ---");
                for (String p : split) {
                    String trimmed = p.trim();
                    if (!trimmed.isEmpty()) {
                        pages.add(trimmed);
                    }
                }
                if (pages.isEmpty()) {
                    pages.add(fullText);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pages;
    }

    @Override
    public String extractTextFromStream(InputStream inputStream) {
        if (inputStream == null) return "";
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }
            byte[] rawBytes = baos.toByteArray();
            return parsePdfBytes(rawBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public String extractTextFromUri(Context context, Uri uri) {
        if (context == null || uri == null) return "";
        try (InputStream is = context.getContentResolver().openInputStream(uri)) {
            return extractTextFromStream(is);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String parsePdfBytes(byte[] bytes) {
        StringBuilder extracted = new StringBuilder();
        String rawContent = new String(bytes, StandardCharsets.ISO_8859_1);

        // 1. Try extracting text streams (decompressed streams first)
        Matcher streamMatcher = STREAM_PATTERN.matcher(rawContent);
        int pageIndex = 1;

        while (streamMatcher.find()) {
            String streamData = streamMatcher.group(1);
            byte[] streamBytes = streamData.getBytes(StandardCharsets.ISO_8859_1);
            
            // Try inflating zlib stream
            String decompressed = tryDecompress(streamBytes);
            String textSource = (decompressed != null && !decompressed.isEmpty()) ? decompressed : streamData;

            String pageText = extractTextFromPdfStream(textSource);
            if (!pageText.trim().isEmpty()) {
                if (extracted.length() > 0) {
                    extracted.append("\n\n");
                }
                extracted.append("--- Page ").append(pageIndex++).append(" ---\n");
                extracted.append(pageText.trim());
            }
        }

        // Fallback: If no standard BT/ET text streams found, extract readable ASCII sequences
        if (extracted.length() == 0) {
            StringBuilder fallback = new StringBuilder();
            Matcher tjFallback = Pattern.compile("\\(([^)]{3,})\\)").matcher(rawContent);
            while (tjFallback.find()) {
                String candidate = cleanPdfString(tjFallback.group(1));
                if (isReadableSentence(candidate)) {
                    fallback.append(candidate).append(" ");
                }
            }
            if (fallback.length() > 0) {
                extracted.append(fallback.toString().trim());
            }
        }

        return cleanWhitespace(extracted.toString());
    }

    private String tryDecompress(byte[] data) {
        try {
            Inflater inflater = new Inflater(false);
            inflater.setInput(data);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
            byte[] buffer = new byte[4096];
            while (!inflater.needsInput() && !inflater.finished()) {
                int count = inflater.inflate(buffer);
                if (count == 0) break;
                outputStream.write(buffer, 0, count);
            }
            inflater.end();
            byte[] decompressed = outputStream.toByteArray();
            if (decompressed.length > 0) {
                return new String(decompressed, StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String extractTextFromPdfStream(String streamText) {
        StringBuilder sb = new StringBuilder();
        Matcher btMatcher = TEXT_BLOCK_PATTERN.matcher(streamText);

        while (btMatcher.find()) {
            String block = btMatcher.group(1);

            // Match Tj
            Matcher tj = TJ_PATTERN.matcher(block);
            while (tj.find()) {
                sb.append(cleanPdfString(tj.group(1))).append(" ");
            }

            // Match TJ arrays
            Matcher tjArray = TJ_ARRAY_PATTERN.matcher(block);
            while (tjArray.find()) {
                String arrayContent = tjArray.group(1);
                Matcher innerStr = Pattern.compile("\\((.*?)\\)").matcher(arrayContent);
                while (innerStr.find()) {
                    sb.append(cleanPdfString(innerStr.group(1)));
                }
                sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String cleanPdfString(String str) {
        if (str == null) return "";
        return str.replace("\\n", "\n")
                  .replace("\\r", "\r")
                  .replace("\\t", "\t")
                  .replace("\\(", "(")
                  .replace("\\)", ")")
                  .replace("\\\\", "\\");
    }

    private boolean isReadableSentence(String s) {
        if (s.length() < 4) return false;
        int letters = 0;
        for (char c : s.toCharArray()) {
            if (Character.isLetter(c)) letters++;
        }
        return ((double) letters / s.length()) >= 0.6;
    }

    private String cleanWhitespace(String text) {
        return text.replaceAll("[ \\t]+", " ").replaceAll("\\n{3,}", "\n\n").trim();
    }
}

