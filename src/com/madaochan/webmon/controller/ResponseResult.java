package com.madaochan.webmon.controller;

import com.madaochan.webmon.connection.Connection;

import javax.swing.*;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Vector;

public class ResponseResult implements OnResponseListener {

    private final String START_TEXT = "----------------------\r\n";
    private final String END_TEXT = "----------------------\r\n";
    private Vector<String> urlList;
    private Vector<String> resultList;
    private StringBuffer resultBuffer;
    private JTextArea textAreaInfo;
    private String oldText;
    private int count;

    public ResponseResult(List<String> urlList, JTextArea textAreaInfo) {
        this.urlList = new Vector<>(urlList);
        this.textAreaInfo = textAreaInfo;
        init();
    }

    private void init() {
        oldText = textAreaInfo.getText();
        count = urlList.size();
        resultList = new Vector<>();
        resultList.setSize(count);
    }

    public void doQuery() {
        for(int i=0; i<count; i++) {
            ConnectionRunnable connectionRunnable = new ConnectionRunnable(i, urlList.get(i), this);
            Thread thread = new Thread(connectionRunnable);
            thread.start();
        }
    }

    @Override
    public synchronized void onThreadStart(int position) {
        if (position >= 0 && position < count) {
            String start = urlList.get(position) + " 正在查询中…\r\n";
            resultList.set(position, start);
            putText(resultList);
        }
    }

    @Override
    public synchronized void onThreadFinish(int position, String resCode) {
        if (position >= 0 && position < count) {
            resultList.set(position, resCode + "\r\n");
            putText(resultList);
        }
    }

    private synchronized void putText(Vector<String> contentList) {
        textAreaInfo.setText("");
        textAreaInfo.setText(oldText);
        textAreaInfo.append(START_TEXT);
        for (String result : contentList) {
            textAreaInfo.append(result);
        }
        textAreaInfo.append(END_TEXT);
    }

    private static class ConnectionRunnable implements Runnable {

        private int position;
        private String url;
        private boolean isStop = false;
        private WeakReference<OnResponseListener> listenerRef;

        public ConnectionRunnable(int position, String url, OnResponseListener listener) {
            this.position = position;
            this.url = url;
            this.listenerRef = new WeakReference<>(listener);
        }

        public void setStop(boolean stop) {
            isStop = stop;
        }

        @Override
        public void run() {

            if (listenerRef != null) {
                OnResponseListener listener = listenerRef.get();
                if (listener != null) {
                    listener.onThreadStart(position);
                }
            }

            String resCode = new Connection().getResponseCode(url);
            if (!isStop && listenerRef != null) {
                OnResponseListener listener = listenerRef.get();
                if (listener != null) {
                    listener.onThreadFinish(position, resCode);
                }
            }
        }
    }
}
