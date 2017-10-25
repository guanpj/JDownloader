package com.me.guanpj.jdownloader.core;

import com.me.guanpj.jdownloader.utility.Constant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Jie on 2017/4/23.
 */

public class DownloadThread implements Runnable {

    private int index;
    private File desFile;
    private String url;
    private int startPos;
    private int endPos;
    private boolean isSingleThread;
    private volatile boolean isPaused;
    private volatile boolean isCancelled;
    private volatile boolean isError;
    private DownloadEntry.DownloadStatus mStatus;
    private DownloadListener listener;

    public DownloadThread(int index, File desFile, String url, int startPos, int endPos, DownloadListener listener) {
        this.index = index;
        this.desFile = desFile;
        this.url = url;
        this.startPos = startPos;
        this.endPos = endPos;
        if(startPos == -1 && endPos == -1) {
            isSingleThread = true;
        }
        this.listener = listener;
    }

    @Override
    public void run() {
        mStatus = DownloadEntry.DownloadStatus.OnDownload;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            if(!isSingleThread) {
                connection.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
            }
            connection.setReadTimeout(Constant.READ_TIMEOUT);
            connection.setConnectTimeout(Constant.CONNECT_TIMEOUT);
            int responseCode = connection.getResponseCode();
            long contentLength = connection.getContentLength();
            RandomAccessFile raf = null;
            FileOutputStream fos = null;
            InputStream is = null;
            if(responseCode == HttpURLConnection.HTTP_PARTIAL) {
                raf = new RandomAccessFile(desFile, "rw");
                raf.seek(startPos);
                is = connection.getInputStream();
                int len = -1;
                byte[] buffer = new byte[2048];
                while ((len = is.read(buffer)) != -1) {
                    raf.write(buffer, 0, len);
                    listener.onDownloadProgressChange(index, len);
                    if(isPaused || isCancelled || isError) {
                        break;
                    }
                }
                raf.close();
                is.close();
            } else if(responseCode == HttpURLConnection.HTTP_OK) {
                fos = new FileOutputStream(desFile);
                is = connection.getInputStream();
                int len = -1;
                byte[] buffer = new byte[2048];
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    listener.onDownloadProgressChange(index, len);
                    if(isPaused || isCancelled || isError) {
                        break;
                    }
                }
                fos.close();
                is.close();
            } else {
                mStatus = DownloadEntry.DownloadStatus.OnError;
                listener.onDownloadError(index, "server error");
            }
            if (isPaused) {
                mStatus = DownloadEntry.DownloadStatus.OnPause;
                listener.onDownloadPause(index);
            } else if(isCancelled){
                mStatus = DownloadEntry.DownloadStatus.OnCancel;
                listener.onDownloadCancel(index);
            } else if (isError) {
                mStatus = DownloadEntry.DownloadStatus.OnError;
                listener.onDownloadError(index, "cancel manually by error");
            } else {
                mStatus = DownloadEntry.DownloadStatus.OnComplete;
                listener.onDownloadComplete(index);
            }
        } catch (Exception e) {
            if (isPaused) {
                mStatus = DownloadEntry.DownloadStatus.OnPause;
                listener.onDownloadPause(index);
            } else if(isCancelled){
                mStatus = DownloadEntry.DownloadStatus.OnCancel;
                listener.onDownloadCancel(index);
            } else {
                mStatus = DownloadEntry.DownloadStatus.OnError;
                listener.onDownloadError(index, e.getMessage());
            }
        } finally {
            if(null != connection) {
                connection.disconnect();
            }
        }
    }

    public void pause() {
        isPaused = true;
    }

    public void cancel() {
        isCancelled = true;
    }

    public void cancelByError() {
        isError = true;
    }

    public boolean isRunning() {
        return mStatus == DownloadEntry.DownloadStatus.OnDownload;
    }

    public boolean isPaused() {
        return mStatus == DownloadEntry.DownloadStatus.OnPause
                || mStatus == DownloadEntry.DownloadStatus.OnComplete;
    }

    public boolean isCancelled() {
        return mStatus == DownloadEntry.DownloadStatus.OnCancel
                || mStatus == DownloadEntry.DownloadStatus.OnComplete;
    }

    public boolean isCompleted() {
        return mStatus == DownloadEntry.DownloadStatus.OnComplete;
    }

    public boolean isError() {
        return mStatus == DownloadEntry.DownloadStatus.OnError;
    }

    interface DownloadListener {
        void onDownloadProgressChange(int index, int progress);
        void onDownloadPause(int index);
        void onDownloadComplete(int index);
        void onDownloadCancel(int index);
        void onDownloadError(int index, String message);
    }
}
