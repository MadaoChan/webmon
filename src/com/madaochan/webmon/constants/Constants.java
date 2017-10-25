package com.madaochan.webmon.constants;

/**
 * 常量
 * @author MadaoChan
 * @since 2017/10/25.
 */
public class Constants {

    public final static String CONFIG_FILE_NAME = "/config.txt";
    public final static String URL_LIST_FILE_NAME = "/urls.txt";
    public final static String LOG_FILE_NAME = "/log.txt";

    public final static String POLL_INTERVAL_PROP_KEY = "poll_interval";

    // 标签/URL分隔符
    public final static String URL_TAG_SPLITTER = ",";

    // 默认轮询时间15分钟
    public final static int DEFAULT_INTERVAL_M = 15;
}
