package com.me.guanpj.jdownloader.utility;

/**
 * Created by Jie on 2017/5/21.
 */

public class TickTack {
    private static TickTack mInstance;
    private long mLastStamp;

    private TickTack() {
    }

    public synchronized static TickTack getInstance() {
        if(mInstance == null) {
            mInstance = new TickTack();
        }
        return mInstance;
    }

    public synchronized boolean needNotify() {
        long stamp = System.currentTimeMillis();
        if(stamp - mLastStamp > 1000) {
            mLastStamp = stamp;
            return true;
        }
        return false;
    }
}
