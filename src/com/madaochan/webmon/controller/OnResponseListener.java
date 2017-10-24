package com.madaochan.webmon.controller;

/**
 * 单个网页状态监听器
 */
public interface OnResponseListener {

    /**
     * 查询线程开始
     * @param position 索引
     */
    void onQueryThreadStart(int position);

    /**
     * 查询线程结束
     * @param position 索引
     * @param resCode 查询结果
     */
    void onQueryThreadFinish(int position, String resCode);
}
