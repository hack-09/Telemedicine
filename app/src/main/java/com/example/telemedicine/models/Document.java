package com.example.telemedicine.models;

import com.google.firebase.Timestamp;

public class Document {
    private String id;
    private String fileName;
    private Timestamp uploadDate;
    private String documentUrl, fileUrl;


    public Document() {}

    public Document(String id, String fileName, Timestamp uploadDate, String documentUrl) {
        this.id = id;
        this.fileName = fileName;
        this.uploadDate = uploadDate;
        this.documentUrl = documentUrl;
    }

    public void setId(String id){
        this.id=id;
    }
    public String getId() { return id; }

    public String getFileName() { return fileName; }

    public Timestamp getUploadDate() {
        return uploadDate;
    }
    public void setUploadDate(Timestamp uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }

    public String getFileUrl() {
        return fileUrl;
    }
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}

