package com.geekbrains.mycloud.common;

public class RefreshMessage extends AbstractMessage {

    private String refresh;

    public RefreshMessage(String refresh) {
        this.refresh = refresh;
    }

    public String getRefresh() {
        return refresh;
    }
}
