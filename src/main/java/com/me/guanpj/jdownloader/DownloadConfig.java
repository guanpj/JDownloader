package com.me.guanpj.jdownloader;

import android.os.Environment;

import com.me.guanpj.jdownloader.utility.FileUtility;

import java.io.File;

/**
 * Created by Jie on 2017/5/19.
 */

public class DownloadConfig {
    private static DownloadConfig mInstance = null;
    private File downFile;
    private int max_download_tasks = 3;
    private int max_download_threads = 3;
    private File downloadDir = null;
    private int min_operate_interval = 1000 * 1;
    private boolean recoverDownloadWhenStart = true;
    //TODO
    private int max_retry_count = 3;

    private DownloadConfig(){
        downFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    public synchronized static DownloadConfig getInstance() {
        if (mInstance == null) {
            mInstance = new DownloadConfig();
        }
        return mInstance;
    }

    public File getDownloadFile(String url) {
        return new File(downFile, FileUtility.getMd5FileName(url));
    }

    public int getMaxDownloadTasks() {
        return max_download_tasks;
    }

    public void setMaxDownloadTasks(int max_download_tasks) {
        this.max_download_tasks = max_download_tasks;
    }

    public int getMaxDownloadThreads() {
        return max_download_threads;
    }

    public void setMaxDownloadThreads(int max_download_threads) {
        this.max_download_threads = max_download_threads;
    }

    public File getDownloadDir() {
        return downloadDir;
    }

    public void setDownloadDir(File downloadDir) {
        this.downloadDir = downloadDir;
    }

    public int getMinOperateInterval() {
        return min_operate_interval;
    }

    public void setMinOperateInterval(int min_operate_interval) {
        this.min_operate_interval = min_operate_interval;
    }

    public boolean isRecoverDownloadWhenStart() {
        return recoverDownloadWhenStart;
    }

    public void setRecoverDownloadWhenStart(boolean recoverDownloadWhenStart) {
        this.recoverDownloadWhenStart = recoverDownloadWhenStart;
    }

    public int getMaxRetryCount() {
        return max_retry_count;
    }

    public void setMaxRetryCount(int max_retry_count) {
        this.max_retry_count = max_retry_count;
    }

}
