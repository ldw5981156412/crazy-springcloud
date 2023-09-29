package com.crazymaker.springcloud.common.util;

import java.util.concurrent.locks.LockSupport;

public class ThreadUtil {
    /**
     * CPU核数
     **/
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * 线程睡眠
     *
     * @param millisecond 毫秒
     */
    public static void sleepMilliSeconds(long millisecond) {
        LockSupport.parkNanos(millisecond * 1000L * 1000L);
    }
}
