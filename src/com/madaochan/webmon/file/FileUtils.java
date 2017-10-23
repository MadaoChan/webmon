package com.madaochan.webmon.file;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by MadaoChan on 2017/10/23.
 */
public class FileUtils {

    /**
     * 读取配置文件
     * @param location 配置文件路径
     * @return 配置文件
     */
    public Properties readProperties(String location) {
        Properties prop = new Properties();
        File file = new File(location);
        if (!file.exists()) {
            return prop;
        }

        try {
            FileInputStream in = new FileInputStream(file);
            prop.load(in);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prop;
    }

    /**
     * 按行读取文件
     * @param location 路径
     * @return 内容行列表
     */
    public List<String> readFile(String location) {
        List<String> lineList = new ArrayList<>();
        File file = new File(location);
        if (!file.exists()) {
            return lineList;
        }
        try {
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);

            String str;
            while((str = br.readLine()) != null) {
                lineList.add(str);
            }
            br.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lineList;
    }



}
