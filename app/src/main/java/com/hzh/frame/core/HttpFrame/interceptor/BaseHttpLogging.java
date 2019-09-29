package com.hzh.frame.core.HttpFrame.interceptor;

import android.util.Log;

import okhttp3.logging.HttpLoggingInterceptor;

/**
 * @author
 * @version 1.0
 * @date 2018/5/13
 */

public class BaseHttpLogging implements HttpLoggingInterceptor.Logger{
    @Override
    public void log(String message) {
        Log.d("HttpLogInfo", message);
    }
}
