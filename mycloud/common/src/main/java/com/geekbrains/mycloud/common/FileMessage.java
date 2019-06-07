package com.geekbrains.mycloud.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMessage extends AbstractMessage {  // сообщение с данными в виде байтового массива
    private String filename;
    private byte[] data;


    public FileMessage(Path path) throws IOException {
        filename = path.getFileName().toString();
        data = Files.readAllBytes(path);
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getData() {
        return data;
    }

    public long getCount() {
        return getData().length;
    }
}

