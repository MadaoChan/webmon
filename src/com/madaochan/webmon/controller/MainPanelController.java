package com.madaochan.webmon.controller;

import com.madaochan.webmon.file.FileUtils;
import com.madaochan.webmon.time.TimeUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by MadaoChan on 2017/10/23.
 */
public class MainPanelController implements RefreshListener {

    private final static String CONFIG_FILE_NAME = "/config.txt";
    private final static String URL_LIST_FILE_NAME = "/urls.txt";
    private final static String LOG_FILE_NAME = "/log.txt";
    private final static int DEFAULT_INTERVAL_S = 60 * 60;  // 1小时

    private String username;
    private String password;
    private int interval;

    private List<String> urlList;
    private JTextArea textAreaInfo;
    private JButton buttonQuery;

    private ResponseController responseController;

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
        buttonQuery.setEnabled(false);
        responseController = new ResponseController(urlList, textAreaInfo.getText(), this);
        responseController.doQuery();
    }

    /**
     * 清屏
     */
    public void doClean() {
        textAreaInfo.setText("");
    }

    @Override
    public void refresh(String result) {
        textAreaInfo.setText("");
        textAreaInfo.setText(result);
        textAreaInfo.setCaretPosition(textAreaInfo.getDocument().getLength());
    }

    @Override
    public void allDoneRefresh(String oldText, String result) {

        if (responseController != null) {
            responseController.destroy();
            responseController = null;
        }

        textAreaInfo.setText("");
        textAreaInfo.append(oldText);
        textAreaInfo.append(result);

        // 写文件
        String writeLog = writeResultToFile(result);

        textAreaInfo.append(writeLog);
        textAreaInfo.setCaretPosition(textAreaInfo.getDocument().getLength());
        buttonQuery.setEnabled(true);

        //TODO 发邮件
    }

    private String writeResultToFile(String result) {
        FileUtils fileUtils = new FileUtils();
        String path = System.getProperty("user.dir");

        boolean isWriteSuccess = fileUtils.writeFile(path + LOG_FILE_NAME, result);
        String writeResult;
        if (isWriteSuccess) {
            writeResult = TimeUtils.getCurrentTime() + "\t查询记录写入" + LOG_FILE_NAME + "成功！\r\n";
        } else {
            writeResult = TimeUtils.getCurrentTime() + "\t查询记录写入失败！\r\n";
        }
        return writeResult;
    }

    public List<String> getUrlList() {
        return urlList;
    }

    public void destroy() {
        if (responseController != null) {
            responseController.destroy();
            responseController = null;
        }
        textAreaInfo = null;
        buttonQuery = null;
    }
}
