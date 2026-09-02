package com.pocketgpt.app.model;

import java.io.File;

public class AiModel {
    private String id;
    private String name;
    private String publisher;
    private String sizeFormatted;
    private String description;
    private String downloadUrl;
    private String fileName;
    private String localFilePath;
    private long localFileSizeBytes = 0;
    private String downloadStatusMessage = "";
    private boolean isDownloaded;
    private boolean isDownloading;
    private int downloadProgress = 0;
    private boolean isActive;

    public AiModel(String id, String name, String publisher, String sizeFormatted, String description, String downloadUrl, String fileName) {
        this.id = id;
        this.name = name;
        this.publisher = publisher;
        this.sizeFormatted = sizeFormatted;
        this.description = description;
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
        this.isDownloaded = false;
        this.isDownloading = false;
        this.downloadProgress = 0;
        this.isActive = false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPublisher() {
        return publisher;
    }

    public String getSizeFormatted() {
        return sizeFormatted;
    }

    public String getDescription() {
        return description;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }

    public void setLocalFilePath(String localFilePath) {
        this.localFilePath = localFilePath;
    }

    public long getLocalFileSizeBytes() {
        return localFileSizeBytes;
    }

    public void setLocalFileSizeBytes(long localFileSizeBytes) {
        this.localFileSizeBytes = localFileSizeBytes;
    }

    public String getDownloadStatusMessage() {
        return downloadStatusMessage;
    }

    public void setDownloadStatusMessage(String downloadStatusMessage) {
        this.downloadStatusMessage = downloadStatusMessage;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    public void setDownloading(boolean downloading) {
        isDownloading = downloading;
    }

    public int getDownloadProgress() {
        return downloadProgress;
    }

    public void setDownloadProgress(int downloadProgress) {
        this.downloadProgress = downloadProgress;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean checkFileExists() {
        if (localFilePath != null) {
            File f = new File(localFilePath);
            return f.exists() && f.length() > 0;
        }
        return false;
    }
}


