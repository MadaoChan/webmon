package com.madaochan.webmon.file;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 文件操作类
 * @author MadaoChan
 * @since 2017/10/24
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

        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            prop.load(in);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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

    /**
     * 写文件
     * @param location 路径
     * @param content 内容
     * @return 是否成功
     */
    public boolean writeFile(String location, String content) {

        FileOutputStream fop = null;
        File file;

        try {
            file = new File(location);
            boolean isCreated = true;

            if (!file.exists()) {
                isCreated = file.createNewFile();
            }
            if (!isCreated) {
                return false;
            }
            fop = new FileOutputStream(file, true);
            fop.write(content.getBytes());
            fop.flush();
            fop.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fop != null) {
                try {
                    fop.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
