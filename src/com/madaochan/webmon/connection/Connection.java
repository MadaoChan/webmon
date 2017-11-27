package com.madaochan.webmon.connection;

import com.madaochan.webmon.time.TimeUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * 链接类
 * @author MadaoChan
 * @since 2017/10/23.
 */
public class Connection {

    public static String NORMAL_STATE = "正常";
    public static String ABNORMAL_STATE = "异常";
    public static String QUERYING_STATE = "查询中…";

    // 默认30秒超时
    private final int DEFAULT_TIME_OUT_MS = 1000 * 30;

    // 默认3次重试
    private final int DEFAULT_RETRY_TIMES = 3;

    private int timeOutMs;
    private int retryTimes;

    public Connection() {
        timeOutMs = DEFAULT_TIME_OUT_MS;
        retryTimes = DEFAULT_RETRY_TIMES;
    }

    /**
     * 获取指定URL的响应码（默认5s超时时间）
     * @param tag 网址标签
     * @param urlStr 网址url
     * @return 查询结果
     */
    public String getResponseCode(String tag, String urlStr) {
        return getResponseCode(tag, urlStr, timeOutMs, retryTimes);
    }

    /**
     * 获取指定URL的响应码
     * @param tag 网址标签
     * @param urlStr 网址url
     * @param timeOutMs 超时时间
     * @param retryTimes 重试次数
     * @return 查询结果
     */
    public String getResponseCode(String tag, String urlStr, int timeOutMs, int retryTimes) {
        String response = null;
        HttpURLConnection conn = null;

        boolean isSuccess = false;

        for (int i = 0; i < retryTimes && !isSuccess; i++) {
            if (i > 0) {
                System.out.println(TimeUtils.getCurrentTime() + " " + tag + " 重试" + i);
            }
            try {
                URL url = new URL(urlStr);
                long startTime = System.currentTimeMillis();
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setConnectTimeout(timeOutMs);
                conn.connect();
                long resTime = System.currentTimeMillis() - startTime;
                    response = TimeUtils.getCurrentTime() + "\t" + translateResCode(conn.getResponseCode()) +
                            "\t响应时间:" + resTime + "ms\t\t" + tag + "\t\t" + urlStr;
                    isSuccess = true;
                    conn.disconnect();
                } catch (SocketTimeoutException e) {
                    response = TimeUtils.getCurrentTime() + "\t【异常】连接超时" + "\t\t\t" + tag + "\t\t" + urlStr;
                } catch (IOException e) {
                    e.printStackTrace();
                    response = TimeUtils.getCurrentTime() + "\t【异常】无法建立连接" + "\t\t\t" + tag + "\t\t" + urlStr;
                } finally {
                    if (conn != null) {
                    conn.disconnect();
                }
            }
        }
        return response;
    }

    /**
     * 解析返回码
     * @param resCode 返回码
     * @return 解析
     */
    private String translateResCode(int resCode) {
        String translation;
        switch(resCode) {
            case HttpURLConnection.HTTP_OK:
                translation = "正常";
                break;

            case HttpURLConnection.HTTP_FORBIDDEN:
                translation = "【异常】403禁用";
                break;

            case HttpURLConnection.HTTP_NOT_FOUND:
                translation = "【异常】404找不到页面";
                break;

            case HttpURLConnection.HTTP_INTERNAL_ERROR:
                translation = "【异常】500内部错误";
                break;

            case HttpURLConnection.HTTP_UNAVAILABLE:
                translation = "【异常】503不可用";
                break;

            default:
                translation = resCode + "页面异常";
                break;
        }
        return translation;
    }
}