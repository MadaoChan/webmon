package com.madaochan.webmon.time;

import java.text.SimpleDateFormat;

public class TimeUtils {

    /**
     * 获取当前时间（格式yyyy-MM-dd HH:mm:ss）
     * @return 时间字符串
     */
    public static String getCurrentTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(System.currentTimeMillis());
    }
}
