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
     * 获取程序目录
     * @return 目录
     */
    public String getRootDir() {
        return System.getProperty("user.dir");
    }

    /**
     * 写入配置文件
     * @param location 路径
     * @param key 配置key
     * @param value 配置value
     * @param comment 注释
     */
    public synchronized void writeProperties(String location, String key, String value, String comment) {

        FileOutputStream fop = null;
        File file;

        try {
            file = new File(location);
            boolean isCreated = true;

            if (!file.exists()) {
                isCreated = file.createNewFile();
            }
            if (!isCreated) {
                return;
            }
            Properties prop = readProperties(location);
            fop = new FileOutputStream(file);
            prop.setProperty(key, value);
            prop.store(fop, comment);
            fop.close();
        } catch (IOException e) {
            e.printStackTrace();
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

    /**
     * 读取配置文件
     * @param location 配置文件路径
     * @return 配置文件
     */
    public synchronized Properties readProperties(String location) {
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
            FileInputStream fileInputStream = new FileInputStream(location);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            BufferedReader br = new BufferedReader(inputStreamReader);

            String str;
            while((str = br.readLine()) != null) {
                lineList.add(str);
            }
            br.close();
            inputStreamReader.close();
            fileInputStream.close();
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
