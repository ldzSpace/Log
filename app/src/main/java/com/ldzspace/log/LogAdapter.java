package com.ldzspace.log;

import static android.R.attr.tag;
import static android.R.id.message;

/**
 * @作者 liudazhi
 * @创建日期 2017/1/2
 */

public interface  LogAdapter {

    void d(String tag,String message);

    void e(String tag,String message);

    void v(String tag,String message);

    void i(String tag,String message);

    void w(String tag,String message);

    void wtf(String tag,String message);
}
