package com.jetfiletransfer.mek.jetfiletransfer.models;

public class FileTransferModel {

    private String fileName;
    private long fileSize;
    private String folders;


    public FileTransferModel(String fileName, long fileSize, String folders) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.folders = folders;
    }

    public FileTransferModel(String fileName, long fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;

    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFolders() {
        return folders;
    }

    public void setFolders(String folders) {
        this.folders = folders;
    }

}
