package com.dy.getresultapp;

import com.dy.fastframework.application.SuperBaseApp;

public class MyBaseApp extends SuperBaseApp {
    @Override
    protected String setBaseUrl() {
        return "http://api.fund.eastmoney.com/";
    }

    @Override
    public boolean isEnableDebugLog() {
        return true;
    }
}
