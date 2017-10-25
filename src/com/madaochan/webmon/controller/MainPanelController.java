package com.madaochan.webmon.controller;

import com.madaochan.webmon.connection.Connection;
import com.madaochan.webmon.file.FileUtils;
import com.madaochan.webmon.time.TimeUtils;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 主界面控制器
 * @author MadaoChan
 * @since 2017/10/23
 */
public class MainPanelController implements RefreshListener {

    private final static String CONFIG_FILE_NAME = "/config.txt";
    private final static String URL_LIST_FILE_NAME = "/urls.txt";
    private final static String LOG_FILE_NAME = "/log.txt";

    private final static String POLL_INTERVAL_PROP_KEY = "poll_interval";

    private final static String URL_TAG_SPLITTER = ",";

    // 默认轮询时间15分钟
    private final static int DEFAULT_INTERVAL_M = 15;

    private String username;
    private String password;

    private List<String> urlList;
    private List<String> tagList;
    private JButton buttonQuery;
    private JTextPane textPaneMain;
    private JFormattedTextField textFieldInterval;
    private JLabel labelState;

    private QueryPerformer queryPerformer;
    private ScheduledExecutorService scheduledExecutorService;

    public MainPanelController(JTextPane textPaneMain, JButton buttonQuery,
                               JFormattedTextField textFieldInterval, JLabel labelState) {
        this.textPaneMain = textPaneMain;
        this.buttonQuery = buttonQuery;
        this.textFieldInterval = textFieldInterval;
        this.labelState = labelState;
        initTextPane();
        refreshConfig();
    }

    private void initTextPane() {
        Style def = textPaneMain.getStyledDocument().addStyle(null, null);
        StyleConstants.setFontFamily(def, "verdana");
        StyleConstants.setFontSize(def, 14);

        Style normal = textPaneMain.addStyle("normal", def);

        Style s = textPaneMain.addStyle("red", normal);
        StyleConstants.setForeground(s, Color.RED);
        textPaneMain.setParagraphAttributes(normal, true);
    }

    /**
     * 更新配置
     */
    private void refreshConfig() {
        FileUtils fileUtils = new FileUtils();
        ensureProperties(fileUtils.readProperties(fileUtils.getRootDir() + CONFIG_FILE_NAME));
        ensureUrls(fileUtils.readFile(fileUtils.getRootDir() + URL_LIST_FILE_NAME));
    }

    /**
     * 读取配置
     * @param prop 配置
     */
    private void ensureProperties(Properties prop) {
        username = prop.getProperty("username");
        password = prop.getProperty("password");
        int interval = DEFAULT_INTERVAL_M;
        try {
            interval = Integer.valueOf(prop.getProperty(POLL_INTERVAL_PROP_KEY));
            interval = (interval > 0) ? interval : DEFAULT_INTERVAL_M;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        textFieldInterval.setText(String.valueOf(interval));
    }

    /**
     * 读取url列表
     * @param urls url列表
     */
    private void ensureUrls(List<String> urls) {
        if (urlList == null) {
            urlList = new ArrayList<>();
        }

        if (tagList == null) {
            tagList = new ArrayList<>();
        }

        urlList.clear();
        tagList.clear();

        for (String url : urls) {

            // 过滤注释和空行
            if (url.trim().length() == 0 || url.substring(0, 2).equals("//") || url.substring(0, 2).equals("/*")) {
                continue;
            }

            // 分离网页标签和网页URL，例：百度,http://www.baidu.com
            String[] tagUrl = url.split(URL_TAG_SPLITTER, 2);

            if (tagUrl.length == 2) {

                if (tagUrl[1].contains("http://") || tagUrl[1].contains("https://")) {
                    tagList.add(tagUrl[0]);
                    urlList.add(tagUrl[1]);
                }

            } else if (tagUrl.length == 1) {
                // 如果没有逗号分隔，则认为只有网址，检查是否网址然后再加入
                if (tagUrl[0].contains("http://") || tagUrl[0].contains("https://")) {
                    tagList.add("");
                    urlList.add(tagUrl[0]);
                }
            }
        }
    }

    /**
     * 查询状态
     */
    public void doQuery() {
        buttonQuery.setEnabled(false);
        refreshConfig();
        queryPerformer = new QueryPerformer(tagList, urlList, textPaneMain.getDocument().getLength(), this);
        queryPerformer.performQuery();
    }

    /**
     * 清屏
     */
    public void doClean() {
        textPaneMain.setText("");
    }

    /**
     * 开始轮询
     */
    public void doPoll() {

        // 停止还没完毕的定时任务
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
            scheduledExecutorService = null;
        }

        // 获取自定义轮询时间
        int interval = DEFAULT_INTERVAL_M;
        try {
            interval = Integer.valueOf(textFieldInterval.getText());
            interval = (interval > 0) ? interval : DEFAULT_INTERVAL_M;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        // 更新界面
        textFieldInterval.setText(String.valueOf(interval));
        textFieldInterval.setEditable(false);

        // 时间写入配置文件
        writeProperties(POLL_INTERVAL_PROP_KEY, String.valueOf(interval));

        final long intervalMs = interval * 60 * 1000;

        // 开启定时任务
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            System.out.println(TimeUtils.getCurrentTime() + "轮询开始");
            doQuery();
            labelState.setText("轮询已开始，下次轮询时间为：" + TimeUtils.getFutureTime(intervalMs));
        },  interval, interval, TimeUnit.MINUTES);

        labelState.setText("轮询已开始，下次轮询时间为：" + TimeUtils.getFutureTime(intervalMs));
    }

    /**
     * 停止轮询
     */
    public void stopPoll() {
        System.out.println(TimeUtils.getCurrentTime() + "轮询停止");
        textFieldInterval.setEditable(true);
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
            scheduledExecutorService = null;
        }
        labelState.setText("轮询未开始");
    }

    /**
     * 写配置文件
     * @param key 配置Key
     * @param value 配置value
     */
    private void writeProperties(String key, String value) {
        FileUtils fileUtils = new FileUtils();
        fileUtils.writeProperties(fileUtils.getRootDir() + CONFIG_FILE_NAME, key, value, "");
    }

    @Override
    public void refresh(int offset, Vector<String> result, boolean isAllDone) {

        int count = 0;
        for (int position = result.size()-1; position >= 0; position--) {
            count += insertTextToTextPane(result.get(position), offset);
        }

        removeTempText(offset, count);

        if (isAllDone) {
            if (queryPerformer != null) {
                queryPerformer.destroy();
                queryPerformer = null;
            }

            StringBuilder totalResult = new StringBuilder();
            for (String text : result) {
                totalResult.append(text);
            }
            String writeLog = writeResultToFile(totalResult.toString());
            insertTextToTextPane(writeLog, textPaneMain.getDocument().getLength());
            buttonQuery.setEnabled(true);
        }
    }

    /**
     * 删除中间结果
     * @param offset 开始
     * @param count 总大小
     */
    private void removeTempText(int offset, int count) {
        try {
            if (textPaneMain.getDocument().getLength() > offset + count) {
                textPaneMain.getDocument().remove(offset + count,
                        textPaneMain.getDocument().getLength() - offset - count);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * 追加结果字符串
     * @param text 结果
     * @param offset 插入偏移位
     * @return 插入字符串长度
     */
    private int insertTextToTextPane(String text, int offset) {

        if (text == null) {
            return 0;
        }

        boolean isAbnormal = text.contains(Connection.ABNORMAL_STATE);
        try {
            textPaneMain.getDocument().insertString(
                    offset,
                    text, textPaneMain.getStyle(isAbnormal ? "red" : "normal"));
            textPaneMain.setCaretPosition(textPaneMain.getStyledDocument().getLength());
            return text.length();
        } catch (BadLocationException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 查询结果写日志
     * @param result 查询结果
     * @return 写入结果字符串
     */
    private String writeResultToFile(String result) {
        FileUtils fileUtils = new FileUtils();

        boolean isWriteSuccess = fileUtils.writeFile(fileUtils.getRootDir() + LOG_FILE_NAME, result);
        String writeResult;
        if (isWriteSuccess) {
            writeResult = TimeUtils.getCurrentTime() + "\t查询记录写入" + LOG_FILE_NAME + "成功！\r\n";
        } else {
            writeResult = TimeUtils.getCurrentTime() + "\t查询记录写入失败！\r\n";
        }
        return writeResult;
    }

    public void destroy() {
        if (queryPerformer != null) {
            queryPerformer.destroy();
            queryPerformer = null;
        }
        buttonQuery = null;
        textPaneMain = null;
        textFieldInterval  = null;
        labelState = null;
    }
}
