package com.jetfiletransfer.mek.jetfiletransfer.models;

public class FileStatusChanged {
    FileItemModelEnum fileItemModelEnum;

    public FileItemModelEnum getFileItemModelEnum() {
        return fileItemModelEnum;
    }

    public void setFileItemModelEnum(FileItemModelEnum fileItemModelEnum) {
        this.fileItemModelEnum = fileItemModelEnum;
    }

    public FileStatusChanged(FileItemModelEnum fileItemModelEnum) {
        this.fileItemModelEnum = fileItemModelEnum;
    }
}
