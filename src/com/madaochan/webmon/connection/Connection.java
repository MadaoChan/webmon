package com.madaochan.webmon.connection;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 链接类
 * @author MadaoChan
 * @since 2017/10/23.
 */
public class Connection {

    public String getResponseCodes(List<String> urlList) {
        int size = urlList.size();
        StringBuffer stringBuffer = new StringBuffer();
        for (int i=0; i<size; i++) {
            stringBuffer.append(getResponseCode(urlList.get(i)));
        }
        return stringBuffer.toString();
    }

    /**
     * 获取指定URL的响应码
     * @param urlStr url
     */
    public String getResponseCode(String urlStr) {
        // 默认5秒超时
        return getResponseCode(urlStr, 1000 * 5);
    }

    /**
     * 获取指定URL的响应码
     * @param urlStr url
     * @param timeOutMs 超时时间
     */
    public String getResponseCode(String urlStr, int timeOutMs) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String response;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection)url.openConnection();
            conn.setDoOutput(true);
            conn.setConnectTimeout(timeOutMs);
            conn.connect();
            response = format.format(System.currentTimeMillis()) + "\tRET:" + conn.getResponseCode() + "\t" + urlStr;
            conn.disconnect();
        } catch (SocketTimeoutException e) {
            response = format.format(System.currentTimeMillis()) + "\t连接超时" + "\t" + urlStr;
        } catch (IOException e) {
            e.printStackTrace();
            response = format.format(System.currentTimeMillis()) + "\t" + e.toString() + "\t" + urlStr;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return response;
    }
}