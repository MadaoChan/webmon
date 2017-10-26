package com.madaochan.webmon.constants;

/**
 * 常量
 * @author MadaoChan
 * @since 2017/10/25.
 */
public class Constants {

    // Debug开关
    public static boolean DEBUG = true;

    public final static String CONFIG_FILENAME = "config.txt";
    public final static String MAIL_CONFIG_FILENAME = "mail_conf.txt";
    public final static String URL_LIST_FILENAME = "urls.txt";
    public final static String LOG_FILENAME = "log.txt";

    public final static String POLL_INTERVAL_PROP_KEY = "poll_interval";

    // 文件分隔符
    public final static String FILE_SPLITTER = ",";

    // 邮件分隔符
    public final static String MAIL_ADDR_SPLITTER = " ";

    // 默认轮询时间15分钟
    public final static int DEFAULT_INTERVAL_M = 15;
}
