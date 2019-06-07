package com.geekbrains.mycloud.common;

import java.util.TreeMap;

public class RefreshSrvFileListMessage extends AbstractMessage {

    private TreeMap<String, Long> findFiles;
    private String file;
    private Long size;


    public RefreshSrvFileListMessage(TreeMap<String, Long> map) {
        this.findFiles = map;
//        getFindFiles();
//        this.file=getFile();
//        this.size=getSize();
    }


    public TreeMap<String, Long> getFindFiles() {
        return findFiles;
    }

    public String getFile() {
        return file;
    }

    public Long getSize() {
        return size;
    }
}
