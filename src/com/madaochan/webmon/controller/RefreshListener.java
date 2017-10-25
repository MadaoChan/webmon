package com.madaochan.webmon.controller;

import java.util.Vector;

/**
 * 界面刷新监听器
 */
public interface RefreshListener {

    /**
     * 查询完毕刷新
     * @param offset 插入偏移量
     * @param result 总结果
     * @param isAllDone 是否已经全部查询完毕
     */
    void refresh(int offset, Vector<String> result, boolean isAllDone);
}
