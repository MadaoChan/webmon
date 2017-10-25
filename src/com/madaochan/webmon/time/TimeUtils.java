package com.madaochan.webmon.time;

import java.text.SimpleDateFormat;

/**
 * 时间类
 * @author MadaoChan
 * @since 2017/10/25
 */
public class TimeUtils {

    /**
     * 获取当前时间（格式yyyy-MM-dd HH:mm:ss）
     * @return 时间字符串
     */
    public static String getCurrentTime() {
        return getFutureTime(0);
    }

    /**
     * 获取未来的时间（格式yyyy-MM-dd HH:mm:ss）
     * @param futureTimeMs 从当前算起的间隔
     * @return 时间字符串
     */
    public static String getFutureTime(long futureTimeMs) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(System.currentTimeMillis() + futureTimeMs);
    }
}
