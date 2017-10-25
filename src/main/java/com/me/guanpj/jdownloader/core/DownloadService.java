package com.me.guanpj.jdownloader.core;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.me.guanpj.jdownloader.DownloadConfig;
import com.me.guanpj.jdownloader.utility.Constant;
import com.me.guanpj.jdownloader.db.DBController;
import com.me.guanpj.jdownloader.notify.DataChanger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by Jie on 2017/4/23.
 */

public class DownloadService extends Service {

    public static final int NOTIFY_CONNECTING = 0;
    public static final int NOTIFY_DOWNLOADING = 1;
    public static final int NOTIFY_UPDATING = 2;
    public static final int NOTIFY_PAUSED_OR_CANCELLED = 3;
    public static final int NOTIFY_COMPLETED = 4;
    public static final int NOTIFY_ERROR = 5;

    private Map<String, DownloadTask> mDownloadingTasks;
    private ExecutorService mExecutors;
    private LinkedBlockingDeque<DownloadEntry> mWaitingQueue;
    private DataChanger mDataChanger;
    private DBController mDBControler;
    private NetworkStateReceiver mNetworkStateReceiver;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DownloadEntry entry = (DownloadEntry) msg.obj;
            switch (msg.what) {
                case NOTIFY_PAUSED_OR_CANCELLED:
                case NOTIFY_COMPLETED:
                case NOTIFY_ERROR:
                    checkAndDoNext(entry);
                    break;

            }
            mDataChanger.postStatus(entry);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDownloadingTasks = new HashMap<>();
        mExecutors = Executors.newCachedThreadPool();
        mWaitingQueue = new LinkedBlockingDeque<>();
        mDataChanger = DataChanger.getInstance(getApplicationContext());
        mDBControler = DBController.getInstance(getApplicationContext());

        initReceiver();
        initDownload();
    }

    private void initReceiver() {
        mNetworkStateReceiver = new NetworkStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkStateReceiver, filter);
    }

    private void initDownload() {
        ArrayList<DownloadEntry> downloadEntries = mDBControler.queryAll();
        if(downloadEntries != null && downloadEntries.size() > 0) {
            for (DownloadEntry downloadEntry : downloadEntries) {
                if(downloadEntry.status == DownloadEntry.DownloadStatus.OnDownload ||
                        downloadEntry.status == DownloadEntry.DownloadStatus.OnWait) {
                    if(DownloadConfig.getInstance().isRecoverDownloadWhenStart()) {
                        if(downloadEntry.isSupportRange) {
                            downloadEntry.status = DownloadEntry.DownloadStatus.OnPause;
                        } else {
                            downloadEntry.status = DownloadEntry.DownloadStatus.OnIdle;
                            downloadEntry.reset();
                        }
                    }
                    addDownload(downloadEntry);
                } else {
                    if(downloadEntry.isSupportRange) {
                        downloadEntry.status = DownloadEntry.DownloadStatus.OnPause;
                    } else {
                        downloadEntry.status = DownloadEntry.DownloadStatus.OnIdle;
                        downloadEntry.reset();
                    }
                    mDBControler.createOrUpdate(downloadEntry);
                }
                mDataChanger.addDownloadEntry(downloadEntry.id, downloadEntry);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            DownloadEntry entry = (DownloadEntry) intent.getSerializableExtra(Constant.KEY_DOWNLOAD_ENTRY);
            if(null != entry && mDataChanger.containsDownloadEntry(entry.id)) {
                entry = mDataChanger.getDownloadEntry(entry.id);
            }

            int action = intent.getIntExtra(Constant.KEY_DOWNLOAD_ACTION, -1);
            if(action != -1) {
                doAction(action, entry);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void doAction(int action, DownloadEntry entry) {
        switch (action) {
            case Constant.KEY_DOWNLOAD_ACTION_ADD:
                addDownload(entry);
                break;
            case Constant.KEY_DOWNLOAD_ACTION_PAUSE:
                pauseDownload(entry);
                break;
            case Constant.KEY_DOWNLOAD_ACTION_RESUME:
                resumeDownload(entry);
                break;
            case Constant.KEY_DOWNLOAD_ACTION_CANCEL:
                cancelDownload(entry);
                break;
            case Constant.KEY_DOWNLOAD_ACTION_PAUSE_ALL:
                pauseAll();
                break;
            case Constant.KEY_DOWNLOAD_ACTION_RECOVER_ALL:
                recoverAll();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNetworkStateReceiver);
    }

    private void checkAndDoNext(DownloadEntry entry) {
        mDownloadingTasks.remove(entry);
        DownloadEntry nextEntry = mWaitingQueue.poll();
        if(nextEntry != null) {
            addDownload(nextEntry);
        }
    }

    private void addDownload(DownloadEntry entry) {
        if(mDownloadingTasks.size() >= Constant.MAX_DOWNLOAD_COUNT) {
            mWaitingQueue.offer(entry);
            entry.status = DownloadEntry.DownloadStatus.OnWait;
            mDataChanger.postStatus(entry);
        } else {
            startDownload(entry);
        }
    }

    private void startDownload(DownloadEntry entry) {
        DownloadTask task = new DownloadTask(entry, mExecutors, mHandler);
        task.start();
        mDownloadingTasks.put(entry.id, task);
    }

    private void pauseDownload(DownloadEntry entry) {
        DownloadTask task = mDownloadingTasks.remove(entry.id);
        if(task != null) {
            task.pause();
        } else {
            mWaitingQueue.remove(entry);
            entry.status = DownloadEntry.DownloadStatus.OnPause;
            mDataChanger.postStatus(entry);
        }
    }

    private void resumeDownload(DownloadEntry entry) {
        addDownload(entry);
    }

    private void cancelDownload(DownloadEntry entry) {
        DownloadTask task = mDownloadingTasks.remove(entry.id);
        if(task != null) {
            task.cancel();
        } else {
            mWaitingQueue.remove(entry);
            entry.status = DownloadEntry.DownloadStatus.OnCancel;
            mDataChanger.postStatus(entry);
        }
    }

    private void pauseAll() {
        while (mWaitingQueue.iterator().hasNext()) {
            DownloadEntry entry = mWaitingQueue.poll();
            entry.status = DownloadEntry.DownloadStatus.OnPause;
            mDataChanger.postStatus(entry);
        }
        for(Map.Entry<String, DownloadTask> entry : mDownloadingTasks.entrySet()) {
            entry.getValue().pause();
        }
        mDownloadingTasks.clear();
    }

    private void recoverAll() {
        ArrayList<DownloadEntry> recoverableEntries = DataChanger.getInstance(getApplicationContext()).getRecoverableDownloadEntries();
        if(recoverableEntries != null && recoverableEntries.size() > 0) {
            for (DownloadEntry recoverableEntry : recoverableEntries) {
                addDownload(recoverableEntry);
            }
        }
    }

    private void pauseAllByNet() {
        while (mWaitingQueue.iterator().hasNext()) {
            DownloadEntry entry = mWaitingQueue.poll();
            entry.status = DownloadEntry.DownloadStatus.OnInterrupt;
            mDataChanger.postStatus(entry);
        }
        for(Map.Entry<String, DownloadTask> entry : mDownloadingTasks.entrySet()) {
            entry.getValue().pause();
        }
        mDownloadingTasks.clear();
    }

    private void recoverAllByNet() {
        ArrayList<DownloadEntry> recoverableEntries = DataChanger.getInstance(getApplicationContext()).getRecoverableDownloadEntries();
        if(recoverableEntries != null && recoverableEntries.size() > 0) {
            for (DownloadEntry recoverableEntry : recoverableEntries) {
                if(recoverableEntry.status == DownloadEntry.DownloadStatus.OnInterrupt) {
                    addDownload(recoverableEntry);
                }
            }
        }
    }

    class NetworkStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if(null != wifiInfo && wifiInfo.isConnected()) {
                Toast.makeText(context, "WIFI 已连接", Toast.LENGTH_SHORT).show();
                recoverAllByNet();
            } else {
                Toast.makeText(context, "WIFI 已断开", Toast.LENGTH_SHORT).show();
                pauseAllByNet();
            }
        }
    }
}
