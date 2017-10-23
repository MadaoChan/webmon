package com.madaochan.webmon.controller;

import com.madaochan.webmon.file.FileUtils;

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

    public MainPanelController() {
        refreshConfig();
    }

    public void refreshConfig() {
        FileUtils fileUtils = new FileUtils();
        String path = System.getProperty("user.dir");
        ensureProperties(fileUtils.readProperties(path + CONFIG_FILE_NAME));
        ensureUrls(fileUtils.readFile(path + URL_LIST_FILE_NAME));
    }

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

    private void ensureUrls(List<String> urls) {
        if (urlList == null) {
            urlList = new ArrayList<>();
        }

        for (String url : urls) {
            // 过滤注释
            if (url.substring(0, 2).equals("//") || url.substring(0, 2).equals("/*")) {
                continue;
            }
            urlList.add(url);
        }
    }

    public List<String> getUrlList() {
        return urlList;
    }
}
