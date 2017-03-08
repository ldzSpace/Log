package com.ldzspace.log;

import static android.R.attr.priority;

/**
 * log日志的打印的接口
 * Created by liudazhi on 2016/12/18.
 */
public interface Printer {
    Settings init(String tag);
    Settings getSettings();
    Printer t(String tag , int methodCount);
    void log(int priority ,String tag, String message,Throwable throwable);
    void d(String message,Object... args);
    void d(Object object);
    void e(String message,Object... args);
    void e(Throwable throwable,String message,Object object);
    void i(String message,Object... args);
    void v(String message,Object... args);
    void w(String message,Object... args);
    void wtf(String message,Object... args);
    void json(String json);
    void xml(String xml);
}
