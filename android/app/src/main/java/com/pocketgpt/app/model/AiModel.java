package com.pocketgpt.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

@Data
@NoArgsConstructor
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
    private boolean downloaded;
    private boolean downloading;
    private int downloadProgress = 0;
    private boolean active;

    public AiModel(String id, String name, String publisher, String sizeFormatted, String description, String downloadUrl, String fileName) {
        this.id = id;
        this.name = name;
        this.publisher = publisher;
        this.sizeFormatted = sizeFormatted;
        this.description = description;
        this.downloadUrl = downloadUrl;
        this.fileName = fileName;
        this.downloaded = false;
        this.downloading = false;
        this.downloadProgress = 0;
        this.active = false;
    }

    public boolean checkFileExists() {
        if (localFilePath != null) {
            File f = new File(localFilePath);
            return f.exists() && f.length() > 0;
        }
        return false;
    }
}
