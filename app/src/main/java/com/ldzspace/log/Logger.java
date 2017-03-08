package com.ldzspace.log;

import static android.R.attr.priority;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 * 格式化的log日志
 * Created by liudazhi on 2016/12/18.
 */

public final class Logger {
    public static final int DEBUG   = 3;
    public static final int ERROR   = 6;
    public static final int ASSERT  = 7;
    public static final int INFO    = 4;
    public static final int VERBOSE = 2;
    public static final int WARN    = 5;

    private static final String TAG = "DZLOG";
    /**
     * 防止用户没有调用初始化方法,直接创建好LoggerPrinter构造方法中也已经调用了
     * 初始化
     */
    private static LoggerPrinter printer = new LoggerPrinter();

    private Logger(){

    }

    /**
     * 使用的时候如果不初始化,使用的是默认的settings,如果使用初始化则使用新设置的settings
     * settings 在输出前会使用
     * @return
     */
    public static Settings init() {
        return init(TAG);
    }

    public static Settings init(String tag){
        printer = new LoggerPrinter();
        return printer.init(tag);
    }

    public Printer t(String tag){
        return printer.t(tag,printer.getSettings().getMethodCount());
    }

    public Printer t(int methodCount){
        return printer.t(null,methodCount);
    }

    /**
     * 设置tag和method
     * @param tag
     * @param methodCount
     * @return
     */
    public Printer t(String tag , int methodCount){
        return  printer.t(tag,methodCount);
    }
    /**
     * 提供对外暴露的log,用户自己选择输出日志的级别
     * @param priority
     * @param tag
     * @param message
     * @param throwable
     */
    public static void log(int priority,String tag,String message,Throwable throwable){
        printer.log(priority,tag,message,throwable);
    }

    public static void d(Object object){
        printer.d(object);
    }

    public static void d(String message,Object object){
        printer.d(message,object);
    }

    public static void e(String message,Object object){
        printer.e(message,object);
    }

    public static void e(Throwable throwable ,String message, Object object){
        printer.e(throwable, message, object);
    }

    public static void i(String message,Object object){
        printer.i(message, object);
    }

    public static void v(String message,Object object){
        printer.v(message,object);
    }

    public static void w(String message,Object object){
        printer.w(message, object);
    }

    public static void wtf(String message,Object object){
        printer.wtf(message, object);
    }

    /**
     * 格式化json字符串
     * @param json
     */
    public static void json(String json){
        printer.json(json);
    }

    /**
     * 格式化xml字符串
     * @param xml
     */
    public static void xml(String xml){
        printer.xml(xml);
    }
}
