package com.geekbrains.mycloud.common;

public class FileRequest extends AbstractMessage {  // запрос файла с именем getFilename()
    private String filename;

    public FileRequest(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }
}
