package com.me.guanpj.jdownloader.notify;

import com.me.guanpj.jdownloader.core.DownloadEntry;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Jie on 2017/4/23.
 */

public abstract class DataWatcher implements Observer {
    @Override
    public void update(Observable observable, Object object) {
        if(object instanceof DownloadEntry) {
            notifyUpdate((DownloadEntry) object);
        }
    }

    protected abstract void notifyUpdate(DownloadEntry object);
}
