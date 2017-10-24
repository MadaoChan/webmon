package com.madaochan.webmon.controller;

public interface OnResponseListener {
    void onThreadStart(int position);
    void onThreadFinish(int position, String resCode);
}
