package com.me.guanpj.jdownloader.utility;

/**
 * Created by Jie on 2017/4/23.
 */

public class Constant {
    public static final String KEY_DOWNLOAD_ENTRY = "key_download_entry";
    public static final String KEY_DOWNLOAD_ACTION = "key_download_action";
    public static final String KEY_APP_ENTRY = "key_app_entry";
    public static final int KEY_DOWNLOAD_ACTION_ADD = 1;
    public static final int KEY_DOWNLOAD_ACTION_PAUSE = 2;
    public static final int KEY_DOWNLOAD_ACTION_RESUME = 3;
    public static final int KEY_DOWNLOAD_ACTION_CANCEL = 4;
    public static final int KEY_DOWNLOAD_ACTION_PAUSE_ALL = 5;

    public static final int KEY_DOWNLOAD_ACTION_RECOVER_ALL = 6;
    public static final int MAX_DOWNLOAD_COUNT = 3;
    public static final int MAX_DOWNLOAD_THREAD = 3;
    public static final int READ_TIMEOUT = 10 * 1000;
    public static final int CONNECT_TIMEOUT = 10 * 1000;
}
