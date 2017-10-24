package com.madaochan.webmon.controller;

public interface RefreshListener {
    void refresh(String result);
    void allDoneRefresh(String oldText, String result);
}
