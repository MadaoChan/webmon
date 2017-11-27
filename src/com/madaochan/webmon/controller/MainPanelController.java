package com.madaochan.webmon.controller;

import com.madaochan.webmon.connection.Connection;
import com.madaochan.webmon.constants.Constants;
import com.madaochan.webmon.file.FileUtils;
import com.madaochan.webmon.mail.SendMailRunnable;
import com.madaochan.webmon.time.TimeUtils;

import javax.mail.Address;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.InternetAddress;
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
public class MainPanelController implements RefreshListener, TransportListener {

    private String username;
    private String password;

    // 网址URL列表
    private List<String> urlList;

    // 网址标签列表
    private List<String> tagList;

    // 查询执行器
    private QueryPerformer queryPerformer;

    //
    private ScheduledExecutorService scheduledExecutorService;

    private JButton buttonQuery;
    private JTextPane textPaneMain;
    private JFormattedTextField textFieldInterval;
    private JLabel labelState;

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
        ensureProperties(fileUtils.readProperties(fileUtils.getRootDir() + Constants.CONFIG_FILENAME));
        ensureUrls(fileUtils.readFile(fileUtils.getRootDir() + Constants.URL_LIST_FILENAME));
    }

    /**
     * 读取配置
     * @param prop 配置
     */
    private void ensureProperties(Properties prop) {
        username = prop.getProperty("username");
        password = prop.getProperty("password");
        int interval = Constants.DEFAULT_INTERVAL_M;
        try {
            interval = Integer.valueOf(prop.getProperty(Constants.POLL_INTERVAL_PROP_KEY));
            interval = (interval > 0) ? interval : Constants.DEFAULT_INTERVAL_M;
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
            String[] tagUrl = url.split(Constants.FILE_SPLITTER, 2);

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
        int interval = Constants.DEFAULT_INTERVAL_M;
        try {
            interval = Integer.valueOf(textFieldInterval.getText());
            interval = (interval > 0) ? interval : Constants.DEFAULT_INTERVAL_M;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        // 更新界面
        textFieldInterval.setText(String.valueOf(interval));
        textFieldInterval.setEditable(false);

        // 时间写入配置文件
        writeProperties(Constants.POLL_INTERVAL_PROP_KEY, String.valueOf(interval));

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
        fileUtils.writeProperties(fileUtils.getRootDir() + Constants.CONFIG_FILENAME, key, value, "");
    }

    /**
     * 压缩网址部分为最长30个字符串，中间用...省略
     * @param text 原始字符串
     * @return 处理后的字符串
     */
    private String processDisplayText(String text) {
        if (text == null) {
            return "";
        }

        String[] splitText = text.split("http");
        if (splitText.length == 2) {
            splitText[1] = "http" + splitText[1];
            return splitText[0] + ellipsizeMiddle(splitText[1], 30);
        } else if (splitText.length > 2){
            return text;
        } else {
            return splitText[0];
        }
    }

    /**
     * 省略中间字符串（中间省略用...表示）
     * @param text 原始字符串
     * @param totalLength 处理后的总长度
     * @return 处理后的字符串
     */
    private String ellipsizeMiddle(String text, int totalLength) {
        if (text == null) {
            return "";
        }

        int textLength = text.length();
        if (textLength + 3 < totalLength || totalLength < 1) {
            return text;
        }

        String headPart = text.substring(0, totalLength / 2);
        String tailPart = text.substring(textLength - totalLength / 2,textLength);
        return headPart + "..." + tailPart;
    }

    @Override
    public void refresh(int offset, Vector<String> result, boolean isAllDone) {

        int count = 0;
        for (int position = result.size()-1; position >= 0; position--) {
            String displayLine = result.get(position);
            count += insertTextToTextPane(processDisplayText(displayLine), offset);
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

            sendEmail(totalResult.toString());

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

        boolean isWriteSuccess = fileUtils.writeFile(fileUtils.getRootDir() + Constants.LOG_FILENAME, result);
        String writeResult;
        if (isWriteSuccess) {
            writeResult = TimeUtils.getCurrentTime() + "\t查询记录写入" + Constants.LOG_FILENAME + "成功！\r\n";
        } else {
            writeResult = TimeUtils.getCurrentTime() + "\t查询记录写入失败！\r\n";
        }
        return writeResult;
    }

    /**
     * 查询结果发送邮件
     * @param result 结果
     */
    private void sendEmail(String result) {
        Thread thread = new Thread(new SendMailRunnable(result, this));
        thread.start();
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

    @Override
    public void messageDelivered(TransportEvent transportEvent) {
        System.out.print("邮件全部发完");
        logDeliverResult(transportEvent);
    }

    @Override
    public void messageNotDelivered(TransportEvent transportEvent) {
        System.out.print("邮件无法发送成功");
        logDeliverResult(transportEvent);
    }

    @Override
    public void messagePartiallyDelivered(TransportEvent transportEvent) {
        System.out.print("邮件部分发完");
        logDeliverResult(transportEvent);
    }

    private void logDeliverResult(TransportEvent transportEvent) {
        // 没有发出的列表
        Address[] unsentList = transportEvent.getValidUnsentAddresses();
        int unsentCount = unsentList == null ? 0 : unsentList.length;

        // 无效的列表
        Address[] invalidList = transportEvent.getInvalidAddresses();
        int invalidCount = invalidList == null ? 0 : invalidList.length;

        // 成功的列表
        Address[] sentList = transportEvent.getValidSentAddresses();
        int sentCount = sentList == null ? 0 : sentList.length;

        int total = unsentCount + invalidCount + sentCount;

        if (total == sentCount) {
            String allDone = TimeUtils.getCurrentTime() + "\t邮件全部发送成功 " + sentCount + "/" + total + "\r\n";
            insertTextToTextPane(allDone, textPaneMain.getDocument().getLength());

        } else {
            StringBuilder stringBuilder = new StringBuilder();
            String start = TimeUtils.getCurrentTime() + "\t【异常】邮件部分发送成功 " + sentCount + "/" + total + "\r\n";
            stringBuilder.append(start);

            // 无效的邮件地址在发送之前已经被过滤掉，所以这里不一定会有内容
            if (invalidCount > 0) {
                stringBuilder.append("无效邮件地址：");
                logSent(stringBuilder, invalidList);
            }

            // 若使用QQ邮箱代发，就算邮件地址不存在，SMTP也会接受地址
            // 只要地址被接受，javamail就当作发送成功
            // 而SMTP服务器正在不断地重试发送邮件到不存在的邮箱里
            // 换言之，unsentCount不一定>0
            if (unsentCount > 0) {
                stringBuilder.append("发送失败的邮件地址：");
                logSent(stringBuilder, sentList);
            }

            insertTextToTextPane(stringBuilder.toString(), textPaneMain.getDocument().getLength());
        }
    }

    /**
     * 处理发送完毕回调的显示字符串
     * @param builder StringBuilder
     * @param list 列表
     */
    private void logSent(StringBuilder builder, Address[] list) {

        for (Address unsent : list) {
            if (unsent instanceof InternetAddress) {
                builder.append(((InternetAddress) unsent).getAddress())
                        .append("\t");
            }
            builder.append("\r\n");
        }
    }
}
