package com.geekbrains.mycloud.common;

public class FileDeleteMessage extends AbstractMessage {

    private String fileDelName;

    public FileDeleteMessage(String fileDelName) throws Exception {
        this.fileDelName = fileDelName;
    }

    public String getFileName() {
        return fileDelName;
    }

}