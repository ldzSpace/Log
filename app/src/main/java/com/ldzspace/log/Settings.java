package com.ldzspace.log;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

/**
 * log日志的控制类 主要控制以下的变量:
 *                   methodCount 打印的方法数
 *                   methodOffset 省略打印的方法数
 *                   showThreadInfo 是否打印线程信息
 *                   logAdapter     是否使用adapter
 * Created by liudazhi on 2016/12/18.
 */

public class Settings {
    private int methodCount  = 2;                // 初始化方法数量
    private int methodOffset = 0;                // 方法偏移量
    private boolean showThreadInfo = true;       // 是否打印线程信息
    private LogAdapter logAdapter;               // log的适配器,用于使用Android的log类.当然也可以使用其他的日志类

    private LogLevel logLevel = LogLevel.FULL;   // 全打印log

    /**
     * 是否隐藏线程信息
     * @return
     */
    public Settings hideThreadInfo(){
        showThreadInfo = false;
        return this;
    }

    /**
     * 打印的方法数量
     * @param methodCount
     * @return
     */
    public Settings methodCount(int methodCount){
        if(methodCount < 0)
            methodCount=0;
        this.methodCount=methodCount;
        return this;
    }

    /**
     * 省略的方法数量
     * @param methodOffset
     * @return
     */
    public Settings methodOffset(int methodOffset){
        this.methodOffset = methodOffset;
        return  this;
    }

    public int getMethodCount() {
        return methodCount;
    }

    public int getMethodOffset() {
        return methodOffset;
    }

    public void setLogLevel(LogLevel logLevel){
        this.logLevel = logLevel;
    }

    public LogLevel getLogLevel(){
        return this.logLevel;
    }

    public LogAdapter getLogAdapter() {
        if(logAdapter == null){
            logAdapter = new AndroidLogAdapter();
        }
        return logAdapter;
    }

    public void setLogAdapter(LogAdapter logAdapter) {
        this.logAdapter = logAdapter;
    }

    public boolean isShowThreadInfo() {
        return showThreadInfo;
    }

}
