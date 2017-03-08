package com.ldzspace.log;

import android.graphics.RegionIterator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Objects;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * log日志的打印输出类
 * Created by liudazhi on 2016/12/18.
 */

public final class LoggerPrinter implements Printer {
    private static final String DEFAULT_TAG = "DZLOG";

    private static final int DEBUG   = 3;
    private static final int ERROR   = 6;
    private static final int ASSERT  = 7;
    private static final int INFO    = 4;
    private static final int VERBOSE = 2;
    private static final int WARN    = 5;

    /**
     * 安卓最大的日志输出实体限制是小于4076 bytes,合4M
     * 每块的大小限制为4000 byte
     */
    private static final int CHUNK_SIZE = 4000;
    /**
     * json是否使用漂亮的打印
     */
    private static final int JSON_INDENT = 2;
    /**
     * 最小的栈指针
     */
    private static final int MIN_STACK_OFFSET = 3;
    /**
     * 输出框
     */
    private static final char TOP_LEFT_CORNER = '╔';
    private static final char BOTTOM_LEFT_CORNER = '╚';
    private static final char MIDDLE_CORNER = '╟';
    private static final char HORIZONTAL_DOUBLE_LINE = '║';
    private static final String DOUBLE_DIVIDER = "════════════════════════════════════════════";
    private static final String SINGLE_DIVIDER = "────────────────────────────────────────────";
    private static final String TOP_BORDER = TOP_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String BOTTOM_BORDER = BOTTOM_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String MIDDLE_BORDER = MIDDLE_CORNER + SINGLE_DIVIDER + SINGLE_DIVIDER;

    /**
     * 线程本地变量:ThreadLocal为每个使用该变量的线程提供独立的变量副本，
     * 所以每一个线程都可以独立地改变自己的副本，而不会影响其它线程所对应的副本。
     */
    private final ThreadLocal<String> localTag = new ThreadLocal<>();
    private final ThreadLocal<Integer> localMethodCount = new ThreadLocal<>();

    /**
     * 通常是用来定义log的配置 如:方法的数量,和可见的线程信息
     */
    private final Settings settings = new Settings();

    private String tag;

    public LoggerPrinter(){
        init(DEFAULT_TAG);
    }
    
    @Override
    public Settings init(String tag){
        if(tag == null){
            throw new IllegalArgumentException("tag is null");
        }

        if(tag.trim().length() == 0){
            throw new IllegalStateException("tag may be not empty");
        }

        this.tag = tag;
        return settings ;
    }

    @Override
    public Settings getSettings() {
        return settings;
    }

    @Override
    public Printer t(String tag, int methodCount) {
        localMethodCount.set(methodCount);
        if(tag != null){
            localTag.set(tag);
        }
        return this;
    }

    @Override
    public void d(String message, Object... args) {
        log(DEBUG,null,message,args);
    }

    /**
     * 将传入的对象变成字符串传入的message里面
     * @param object
     */
    @Override
    public void d(Object object){
        String message;
        // 检查是否传入的是否是array类型
        if(object.getClass().isArray()){
            message = Arrays.deepToString((Object[])object);
        }else{
            message = object.toString();
        }
        // 最后的参数不一定要传
        log(DEBUG,null,message);
    }

    @Override
    public void e(String message, Object... args) {
        e(null,message,args);
    }

    @Override
    public void e(Throwable throwable,String message,Object object){
        log(ERROR,throwable,message,object);
    }

    @Override
    public void i(String message, Object... args) {
        log(INFO,null,message,args);
    }

    @Override
    public void v(String message, Object... args) {
        log(VERBOSE,null,message,args);
    }

    @Override
    public void w(String message, Object... args) {
        log(WARN,null,message,args);
    }

    @Override
    public void wtf(String message, Object... args) {
        log(ASSERT,null,message,args);
    }

    @Override
    public void json(String json) {
        if(Helper.isEmpty(json)){
            d("Empty/Null json content");
            return;
        }
        try {
            json = json.trim();
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                // 加上JSON_INDENT 使json更易读
                String message = jsonObject.toString(JSON_INDENT);
                d(message);
                return;
            }

            if (json.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                String message = jsonArray.toString(JSON_INDENT);
                d(message);
                return;
            }
            e("invalid message");
        }catch(Exception e){
            e("invalid message");
        }
    }

    @Override
    public void xml(String xml) {
        if(Helper.isEmpty(xml)){
            d("Empty/Null xml content");
            return;
        }
        try {
            Source xmlSource = new StreamSource(new StringReader(xml));
            StreamResult xmlResult = new StreamResult(new StringWriter());
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            // 输出的时候是否可以使用空白格
            transformer.setOutputProperty(OutputKeys.INDENT,"yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");
            transformer.transform(xmlSource,xmlResult);
            d(xmlResult.getWriter().toString().replaceFirst(">", ">\n"));
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 控制和整理输出信息(所有6大级别log日志方法和2个特殊的方法)
     * @param priority  log级别
     * @param throwable 抛出的栈信息
     * @param message   日志信息
     * @param agrs     可变参数  只是输出消息的补充
     */
    public void log(int priority, Throwable throwable, String message, Object... agrs){
        if(settings.getLogLevel() == LogLevel.NONE){
            return;
        }
        String tag = getTag();
        // 将message和agrs 合并,使用了String.format,
        String messageInfo = createMessage(message, agrs);
        log(priority,tag,messageInfo,throwable);
    }

    /**
     * 创建消息 如果后面的参数为空,我们就直接输出 message
     * @param message 消息
     * @param args 参数
     * @return
     */
    private String  createMessage(String message , Object... args) {
        return args == null || args.length == 0 ? message : String.format(message,args);
    }

    /**
     * log日志拼装方法
     * @param priority      级别
     * @param tag           tag
     * @param message       输出信息
     * @param throwable     抛出的信息
     */
    @Override
    public void log(int priority, String tag, String message, Throwable throwable) {
        if(settings.getLogLevel() == LogLevel.NONE){
            return;
        }
        if(throwable != null && message != null){
            message += ": " + Helper.getStackTrace(throwable);
        }
        if(throwable != null && message == null){
            message = Helper.getStackTrace(throwable);
        }
        if(message == null){
            message = "No message/exception is set";
        }
        int methodCount = getMethodCount();
        if(Helper.isEmpty(message)){
            message = "Empty/null log message";
        }
        logTopBorder(priority,tag);
        logHeaderContent(priority,tag,methodCount);

        byte[] bytes = message.getBytes();
        int length = bytes.length;
        // 如果输出的信息小于chunksize,我们认为这是一次能输出的大小
        if(length <= CHUNK_SIZE){
            if(methodCount > 0){
                logDivdier(priority,tag);
            }
            logContent(priority, tag,message);
            logBottomBorder(priority,tag);
            return;
        }
        if(methodCount > 0){
            logDivdier(priority,tag);
        }

        for(int i = 0; i < length; i+= CHUNK_SIZE){
            int count = Math.min(length - i, CHUNK_SIZE);
            logContent(priority,tag,new String(bytes,i,count));
        }
        logBottomBorder(priority,tag);
    }

    public int getMethodCount(){
        Integer count = localMethodCount.get();
        int result = settings.getMethodCount();
        if(count != null){
            localMethodCount.remove();
            result = count;
        }
        if(result < 0){
            // 方法数量无法预期
            throw new IllegalArgumentException("methodCount cannot be negative");
        }
        return result;
    }

    /**
     * 获取当前类在栈中的指针
     * @param trace
     * @return
     */
    public int getStackOffset(StackTraceElement[] trace){
        for(int i = MIN_STACK_OFFSET;i <trace.length;i++){
            StackTraceElement stackTraceElement = trace[i];
            String className = stackTraceElement.getClassName();
            // 去除我们自己的方法输出,剩余的方法数量就是便宜量
            if(!className.equals(LoggerPrinter.class.getName()) && !className.equals(Logger.class.getName())){
                return --i;
            }
        }
        return -1;
    }

    /**
     * 绘制打印框的上边界
     * @param logType
     * @param tag
     */
    private void logTopBorder(int logType , String tag) {
        logChunk(logType,tag,TOP_BORDER);
    }

    /**
     * 绘制头部内容
     * @param logType
     * @param tag
     * @param methodCount
     */
    private void logHeaderContent(int logType, String tag, int methodCount) {
        // 获取当前线程的栈信息
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // 查看控制器/设置器 是否允许展示线程栈信息
        if(settings.isShowThreadInfo()){
            // 样式 : ||  Thread :  线程名
            logChunk(logType,tag,HORIZONTAL_DOUBLE_LINE + " Thread: " + Thread.currentThread().getName());
            // 绘制分割线
            logDivdier(logType,tag);
        }
        String level = "";
        int offset = getStackOffset(stackTrace) + settings.getMethodOffset();
        // 当前的堆栈对应的方法数信息可能超过了追踪堆栈的的方法数量.需要去除
        if(methodCount + offset > stackTrace.length){
            methodCount = stackTrace.length - offset - 1;
        }

        for(int i = methodCount; i > 0; i--){
            // 找到每个自己方法的堆栈中的位置
            int stackIndex = i + offset;
            if (stackIndex >= stackTrace.length) {
                continue;
            }
            // 拼接自有方法的堆栈信息
            StringBuilder builder = new StringBuilder();
            builder.append("║ ")
                    .append(level)
                    .append(getSimpleClassName(stackTrace[stackIndex].getClassName()))
                    .append(".")
                    .append(stackTrace[stackIndex].getMethodName())
                    .append(" ")
                    .append(" (")
                    .append(stackTrace[stackIndex].getFileName())
                    .append(":")
                    .append(stackTrace[stackIndex].getLineNumber())
                    .append(")");
            level += "   ";
            logChunk(logType, tag, builder.toString());
        }

    }

    /**
     * 绘制分割线
     * @param logType
     * @param tag
     */
    private void logDivdier(int logType, String tag) {
        logChunk(logType,tag,MIDDLE_BORDER);
    }

    /**
     * 打印输出内容
     * @param priority
     * @param tag
     * @param message
     */
    private void logContent(int priority, String tag, String message) {
        String[] lines = message.split(System.getProperty("line.separator"));
        for (String line : lines){
            logChunk(priority,tag,line);
        }
    }

    /**
     * 绘制下边界
     * @param priority
     * @param tag
     */
    private void logBottomBorder(int priority, String tag) {
        logChunk(priority,tag,BOTTOM_BORDER);
    }

    /**
     * 使用android的log日志工具输出
     * @param logType
     * @param tag
     * @param chunk
     */
    private void logChunk(int logType, String tag, String chunk) {
        String fortag = formatTag(tag);
        switch (logType){
            case ERROR :
                // 获取Android的log输出工具
                settings.getLogAdapter().e(fortag,chunk);
                break;
            case INFO  :
                settings.getLogAdapter().i(fortag,chunk);
                break;
            case WARN  :
                settings.getLogAdapter().w(fortag,chunk);
                break;
            case ASSERT:
                settings.getLogAdapter().wtf(fortag,chunk);
                break;
            case VERBOSE :
                settings.getLogAdapter().v(fortag,chunk);
                break;
            case DEBUG :
                break;
            default:
                settings.getLogAdapter().d(fortag,chunk);
                break;
        }

    }

    /**
     * 格式化tag 格式: tag-自己定义tag
     * @param tag
     * @return
     */
    private String formatTag(String tag) {
        if (!Helper.isEmpty(tag) && !Helper.equals(this.tag, tag)) {
            return this.tag + "-" + tag;
        }
        return this.tag;
    }

    /**
     * 获取类名
     * @param name
     * @return
     */
    public String getSimpleClassName(String name){
        int lastIndex = name.lastIndexOf('.');
        return name.substring(lastIndex+1);
    }

    /**
     * 获取tag
     * @return
     */
    public String getTag() {
        String tag = localTag.get();
        if(tag != null){
            localTag.remove();
            return tag;
        }
        return this.tag;
    }
}
