package com.madaochan.webmon.controller;

import com.madaochan.webmon.connection.Connection;
import com.madaochan.webmon.time.TimeUtils;

import java.util.List;
import java.util.Vector;

/**
 * 网页状态查询执行器
 * @author MadaoChan
 * @since 2017/10/24
 */
public class QueryPerformer implements OnResponseListener {

    private final String START_TEXT = "----------------------\r\n";
    private final String END_TEXT = "----------------------\r\n";
    private Vector<String> urlList;
    private Vector<String> resultList;
    private Vector<Boolean> doneList;
    private Vector<ConnectionRunnable> threadList;
    private String oldText;
    private int count;
    private RefreshListener refreshListener;

    public QueryPerformer(List<String> urlList, String oldText, RefreshListener refreshListener) {
        this.urlList = new Vector<>(urlList);
        this.oldText = oldText;
        this.refreshListener = refreshListener;
        init();
    }

    private void init() {
        count = urlList.size();

        resultList = new Vector<>();
        resultList.setSize(count);

        doneList = new Vector<>();
        doneList.setSize(count);

        threadList = new Vector<>();
        threadList.setSize(count);
    }

    /**
     * 执行查询
     */
    public void performQuery() {

        if (count == 0) {
            passContent("urls.txt无内容，请添加网页列表后重启程序。");
        }

        for(int i=0; i<count; i++) {
            ConnectionRunnable connectionRunnable = new ConnectionRunnable(i, urlList.get(i), this);
            threadList.add(connectionRunnable);
            Thread thread = new Thread(connectionRunnable);
            thread.start();
        }
    }

    @Override
    public synchronized void onQueryThreadStart(int position) {
        System.out.println("ThreadStart " + position);
        if (position >= 0 && position < count) {
            String start = TimeUtils.getCurrentTime() + "\t查询中…\t" + urlList.get(position) + "\r\n";
            resultList.set(position, start);
            passResult(resultList, false);
        }
    }

    @Override
    public synchronized void onQueryThreadFinish(int position, String resCode) {
        System.out.println("ThreadFinish " + position + " " + resCode);
        if (position >= 0 && position < count) {
            doneList.set(position, true);
            resultList.set(position, resCode + "\r\n");
            passResult(resultList, false);
            parseDoneList();
        }
    }

    /**
     * 检查完成情况，全部完成则发告知RefreshListener已经全部完成查询
     */
    private void parseDoneList() {
        if (doneList == null) {
            return;
        }

        boolean isAllDone = true;
        for (Boolean isDone : doneList) {
            if (isDone == null) {
                isAllDone = false;
                break;
            }
            isAllDone &= isDone;
        }

        if (isAllDone) {
            passResult(resultList, true);
        }
    }

    public void destroy() {
        refreshListener = null;
        if (threadList != null) {
            for (ConnectionRunnable runnable :threadList) {
                if (runnable != null) {
                    runnable.setStop();
                }
            }
        }
    }

    private void passContent(String content) {
        if (refreshListener != null) {
            refreshListener.refresh(content);
        }
    }

    private synchronized void passResult(Vector<String> contentList, boolean isAllDone) {

        if (refreshListener == null) {
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();

        if (isAllDone) {
            stringBuilder.append(START_TEXT);
            for (String result : contentList) {
                stringBuilder.append(result);
            }
            stringBuilder.append(END_TEXT);
            refreshListener.allDoneRefresh(oldText, stringBuilder.toString());
        } else {
            stringBuilder.append(oldText);
            stringBuilder.append(START_TEXT);
            for (String result : contentList) {
                stringBuilder.append(result);
            }
            stringBuilder.append(END_TEXT);
            refreshListener.refresh(stringBuilder.toString());
        }
    }

    /**
     * 获取单个网址的状态Runnable
     */
    private static class ConnectionRunnable implements Runnable {

        private int position;
        private String url;
        private boolean isStop = false;
        private OnResponseListener listener;

        public ConnectionRunnable(int position, String url, OnResponseListener listener) {
            this.position = position;
            this.url = url;
            this.listener = listener;
        }

        public void setStop() {
            isStop = true;
            listener = null;
        }

        @Override
        public void run() {

            if (listener != null) {
                listener.onQueryThreadStart(position);
            } else {
                System.out.println("ThreadStart listenerRef null " + position);
            }

            String resCode = new Connection().getResponseCode(url);
            if (!isStop && listener != null) {
                listener.onQueryThreadFinish(position, resCode);
            } else {
                System.out.println("ThreadFinish listenerRef null " + position);
            }
            listener = null;
        }
    }
}
