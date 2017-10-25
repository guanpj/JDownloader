package com.me.guanpj.jdownloader;

import android.content.Context;
import android.content.Intent;

import com.me.guanpj.jdownloader.core.DownloadEntry;
import com.me.guanpj.jdownloader.core.DownloadService;
import com.me.guanpj.jdownloader.utility.Constant;
import com.me.guanpj.jdownloader.notify.DataChanger;
import com.me.guanpj.jdownloader.notify.DataWatcher;

import java.io.File;

/**
 * Created by Jie on 2017/4/23.
 */

public class DownloadManager {

    private static DownloadManager mInstance;
    private final Context mContext;
    private long mLastOperatedTime = 0;
    private static final int MIN_OPERATE_INTERVAL = 1000 * 1;

    private DownloadManager(Context context) {
        mContext = context;
        context.startService(new Intent(context, DownloadService.class));
    }

    public synchronized static DownloadManager getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new DownloadManager(context);
        }
        return mInstance;
    }

    private boolean checkIfExecutable() {
        long tmp = System.currentTimeMillis();
        if (tmp - mLastOperatedTime > MIN_OPERATE_INTERVAL) {
            mLastOperatedTime = tmp;
            return true;
        }
        return false;
    }

    public void add(DownloadEntry entry) {
        if (!checkIfExecutable())
            return;
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constant.KEY_DOWNLOAD_ENTRY, entry);
        intent.putExtra(Constant.KEY_DOWNLOAD_ACTION, Constant.KEY_DOWNLOAD_ACTION_ADD);
        mContext.startService(intent);
    }

    public void pause(DownloadEntry entry) {
        if (!checkIfExecutable())
            return;
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constant.KEY_DOWNLOAD_ENTRY, entry);
        intent.putExtra(Constant.KEY_DOWNLOAD_ACTION, Constant.KEY_DOWNLOAD_ACTION_PAUSE);
        mContext.startService(intent);
    }

    public void resume(DownloadEntry entry) {
        if (!checkIfExecutable())
            return;
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constant.KEY_DOWNLOAD_ENTRY, entry);
        intent.putExtra(Constant.KEY_DOWNLOAD_ACTION, Constant.KEY_DOWNLOAD_ACTION_RESUME);
        mContext.startService(intent);
    }

    public void cancel(DownloadEntry entry) {
        if (!checkIfExecutable())
            return;
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constant.KEY_DOWNLOAD_ENTRY, entry);
        intent.putExtra(Constant.KEY_DOWNLOAD_ACTION, Constant.KEY_DOWNLOAD_ACTION_CANCEL);
        mContext.startService(intent);
    }

    public void pauseAll() {
        if (!checkIfExecutable())
            return;
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constant.KEY_DOWNLOAD_ACTION, Constant.KEY_DOWNLOAD_ACTION_PAUSE_ALL);
        mContext.startService(intent);
    }

    public void recoverAll() {
        if (!checkIfExecutable())
            return;
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constant.KEY_DOWNLOAD_ACTION, Constant.KEY_DOWNLOAD_ACTION_RECOVER_ALL);
        mContext.startService(intent);
    }

    public void addObserver(DataWatcher wacther) {
        DataChanger.getInstance(mContext).addObserver(wacther);
    }

    public void deleteObserver(DataWatcher wacther) {
        DataChanger.getInstance(mContext).deleteObserver(wacther);
    }

    public DownloadEntry getDownloadEntry(String id) {
        return DataChanger.getInstance(mContext).getDownloadEntry(id);
    }

    public boolean containsDownloadEntry(String id) {
        return DataChanger.getInstance(mContext).containsDownloadEntry(id);
    }

    public void deleteDownloadEntry(boolean forceDelete, String id) {
        DataChanger.getInstance(mContext).deleteDownloadEntry(id);
        if (forceDelete){
            File file = DownloadConfig.getInstance().getDownloadFile(id);
            if (file.exists())
                file.delete();
        }
    }
}
