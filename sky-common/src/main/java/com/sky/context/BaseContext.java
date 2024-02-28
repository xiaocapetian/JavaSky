package com.sky.context;

public class BaseContext {
/*这是把ThreadLocal封装的一个工具类
* ThreadLocal是一个线程中的局部变量*/
    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }

    public static void removeCurrentId() {
        threadLocal.remove();
    }

}
