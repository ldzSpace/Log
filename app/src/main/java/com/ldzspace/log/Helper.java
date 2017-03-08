package com.ldzspace.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 * @作者 liudazhi
 * @创建日期 2016/12/28
 */

public class Helper {
    private Helper(){};
    /**
     * 判断一个字符串是否为空
     * @param str
     * @return
     */
    static boolean isEmpty(CharSequence str){
        return str == null || str.length() == 0;
    }

    /**
     * 判断是否两个字符串是否相同
     * @param a
     * @param b
     * @return
     */
    static boolean equals(CharSequence a , CharSequence b){
        // == 判断类型和长度相同则认为相同
        if(a == b) return true;
        int length = a.length();
        if(length == b.length()){
            if(a instanceof String && b instanceof String){
                return a.equals(b);
            }else{
                for(int i = 0 ; i < length ;i++){
                    if(a.charAt(i) != b.charAt(i)) return false;
                }
               return true;
            }
        }
        return false;
    }

    /**
     * 获取异常的堆栈信息
     * @return
     */
    static String getStackTrace(Throwable throwable){
        if(throwable == null){
            return "";
        }
        Throwable t = throwable;
        while (t != null){
            if(t instanceof UnknownHostException){
                return "";
            }
            // 获取抛出异常的原因
            t = t.getCause();
        }
        // 写入字符串到sw中
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
}
