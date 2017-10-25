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
    private Vector<String> tagList;
    private Vector<String> urlList;
    private Vector<String> resultList;
    private Vector<Boolean> doneList;
    private Vector<ConnectionRunnable> threadList;
    private int count;
    private RefreshListener refreshListener;

    private int offset;

    public QueryPerformer(List<String> tagList, List<String> urlList, int offset, RefreshListener refreshListener) {
        this.tagList = new Vector<>(tagList);
        this.urlList = new Vector<>(urlList);
        this.offset = offset;
        this.refreshListener = refreshListener;
        init();
    }

    private void init() {
        count = Math.min(urlList.size(), tagList.size());

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
            ConnectionRunnable connectionRunnable = new ConnectionRunnable(i, tagList.get(i), urlList.get(i), this);
            threadList.add(connectionRunnable);
            Thread thread = new Thread(connectionRunnable);
            thread.start();
        }
    }

    @Override
    public synchronized void onQueryThreadStart(int position) {
        System.out.println("ThreadStart " + position);
        if (position >= 0 && position < count) {
            String start = TimeUtils.getCurrentTime()
                    + "\t" + Connection.QUERYING_STATE + "\t\t\t" + tagList.get(position) + "\t\t"
                    + urlList.get(position) + "\r\n";
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

    /**
     * 传递普通信息到RefreshListener
     * @param content 普通信息
     */
    private synchronized void passContent(String content) {
        if (refreshListener != null) {
            Vector<String> contentList = new Vector<>();
            contentList.add(content);
            refreshListener.refresh(offset, contentList, false);
        }
    }

    /**
     * 传递查询结果到RefreshListener
     * @param contentList 结果列表
     * @param isAllDone 是否已经全部完成查询
     */
    private synchronized void passResult(Vector<String> contentList, boolean isAllDone) {

        if (refreshListener == null || contentList == null) {
            return;
        }

        Vector<String> resultList = new Vector<>(contentList);

        resultList.insertElementAt(START_TEXT, 0);
        resultList.add(END_TEXT);
        refreshListener.refresh(offset, resultList, isAllDone);
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

    /**
     * 获取单个网址的状态Runnable
     */
    private static class ConnectionRunnable implements Runnable {

        private int position;
        private String tag;
        private String url;
        private boolean isStop = false;
        private OnResponseListener listener;

        public ConnectionRunnable(int position, String tag, String url, OnResponseListener listener) {
            this.position = position;
            this.tag = tag;
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

            String resCode = new Connection().getResponseCode(tag, url);
            if (!isStop && listener != null) {
                listener.onQueryThreadFinish(position, resCode);
            } else {
                System.out.println("ThreadFinish listenerRef null " + position);
            }
            listener = null;
        }
    }
}
