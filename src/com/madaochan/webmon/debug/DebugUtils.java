package com.madaochan.webmon.debug;

import com.madaochan.webmon.file.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
//
//    public static void main(String[] args) {
//        System.out.println("Hello World!");
//
//        // 获得项目根目录的绝对路径
//        String path = System.getProperty("user.dir");
//        System.out.println(path);
//
//        FileUtils fileUtils = new FileUtils();
//        Properties prop = fileUtils.readProperties(path + "/config.txt");
//        System.out.println(prop.getProperty("username") + prop.getProperty("password") + prop.getProperty("interval"));
//
//        List<String> urlList = fileUtils.readFile(path + "/url.txt");
//        for (String url : urlList) {
//            System.out.println(url);
//        }
//    }

}
