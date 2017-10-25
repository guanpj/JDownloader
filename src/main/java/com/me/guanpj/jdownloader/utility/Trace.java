package com.me.guanpj.jdownloader.utility;

import android.util.Log;

/**
 * Created by Jie on 2017/4/23.
 */

public class Trace {
    public static final String TAG = "gpj";
    private static final boolean DEBUG = true;

    public static void d(String msg) {
        if (DEBUG)
            Log.e(TAG, msg);
    }

    public static void e(String msg) {
        if (DEBUG)
            Log.e(TAG, msg);
    }
}
