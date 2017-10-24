package com.madaochan.webmon.controller;

/**
 * 界面刷新监听器
 */
public interface RefreshListener {

    /**
     * 查询过程刷新
     * @param result 中间结果
     */
    void refresh(String result);

    /**
     * 查询完毕刷新
     * @param oldText 执行查询之前的旧数据
     * @param result 总结果
     */
    void allDoneRefresh(String oldText, String result);
}
