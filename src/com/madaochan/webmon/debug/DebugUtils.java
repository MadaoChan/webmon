package com.madaochan.webmon.debug;

import java.util.ArrayList;

/**
 * Created by MadaoChan on 2017/10/23.
 */
public class DebugUtils {

    /**
     * 建造Debug的Url列表
     * @return
     */
    public static ArrayList<String> buildTestUrlList() {
        ArrayList<String> urlList = new ArrayList<>();
        urlList.add("http://www.baidu.com");
        urlList.add("http://blog.csdn.net");
        urlList.add("http://www.sina.com");
        urlList.add("http://www.163.com");
        urlList.add("http://psnine.com");
        urlList.add("http://www.qq.com");
        return urlList;
    }

}
