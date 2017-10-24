package com.madaochan.webmon.controller;

import com.madaochan.webmon.connection.Connection;
import com.madaochan.webmon.file.FileUtils;
import com.madaochan.webmon.ui.MainPanel;

import javax.swing.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by MadaoChan on 2017/10/23.
 */
public class MainPanelController {

    private final static String CONFIG_FILE_NAME = "/config.txt";
    private final static String URL_LIST_FILE_NAME = "/urls.txt";
    private final static int DEFAULT_INTERVAL_S = 60 * 60;  // 1小时

    private String username;
    private String password;
    private int interval;

    private List<String> urlList;
    private JTextArea textAreaInfo;
    private JButton buttonQuery;

    public MainPanelController(JTextArea textAreaInfo, JButton buttonQuery) {
        this.textAreaInfo = textAreaInfo;
        this.buttonQuery = buttonQuery;
        refreshConfig();
    }

    /**
     * 更新配置
     */
    private void refreshConfig() {
        FileUtils fileUtils = new FileUtils();
        String path = System.getProperty("user.dir");
        ensureProperties(fileUtils.readProperties(path + CONFIG_FILE_NAME));
        ensureUrls(fileUtils.readFile(path + URL_LIST_FILE_NAME));
    }

    /**
     * 读取配置
     * @param prop 配置
     */
    private void ensureProperties(Properties prop) {
        username = prop.getProperty("username");
        password = prop.getProperty("password");
        try {
            interval = Integer.valueOf(prop.getProperty("interval"));
            interval = (interval > 0) ? interval : DEFAULT_INTERVAL_S;
        } catch (NumberFormatException e) {
            interval = DEFAULT_INTERVAL_S;
        }
    }

    /**
     * 读取url列表
     * @param urls url列表
     */
    private void ensureUrls(List<String> urls) {
        if (urlList == null) {
            urlList = new ArrayList<>();
        }

        urlList.clear();
        for (String url : urls) {
            // 过滤注释
            if (url.substring(0, 2).equals("//") || url.substring(0, 2).equals("/*")) {
                continue;
            }
            urlList.add(url);
        }
    }

    /**
     * 查询状态
     */
    public void doQuery() {
//        buttonQuery.setEnabled(false);
//        textAreaInfo.append("----------\r\n");
//
//        for (String url : urlList) {
//            ConnectionRunnable runnable = new ConnectionRunnable(textAreaInfo, url);
//            Thread thread = new Thread(runnable);
//            thread.start();
//        }
//        buttonQuery.setEnabled(true);

        ResponseResult responseResult = new ResponseResult(urlList, textAreaInfo);
        responseResult.doQuery();
    }

    public List<String> getUrlList() {
        return urlList;
    }

    public void destroy() {
        textAreaInfo = null;
        buttonQuery = null;
    }

    private static class ConnectionRunnable implements Runnable {

        private WeakReference<JTextArea> textAreaRef;
        private String url;
        private boolean isStop = false;

        public ConnectionRunnable(JTextArea textArea, String url) {
            this.textAreaRef = new WeakReference<>(textArea);
            this.url = url;
        }

        public void setStop(boolean stop) {
            isStop = stop;
        }

        @Override
        public void run() {
            String response = new Connection().getResponseCode(url);
            JTextArea textArea = textAreaRef.get();
            if (!isStop && textArea != null) {
                textArea.append(response);
                textArea.invalidate();
            }
        }
    }
}
