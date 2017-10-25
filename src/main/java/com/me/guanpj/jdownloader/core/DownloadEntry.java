package com.me.guanpj.jdownloader.core;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.types.IntegerObjectType;
import com.j256.ormlite.table.DatabaseTable;
import com.me.guanpj.jdownloader.DownloadConfig;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Jie on 2017/4/23.
 */

@DatabaseTable(tableName = "downloadentry")
public class DownloadEntry implements Serializable {

    @DatabaseField(id = true)
    public String id;
    @DatabaseField
    public String name;
    @DatabaseField
    public String url;
    @DatabaseField
    public DownloadStatus status = DownloadStatus.OnIdle;
    @DatabaseField
    public int currentLength;
    @DatabaseField
    public int totalLength;
    @DatabaseField
    public boolean isSupportRange;
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    public HashMap<Integer, Integer> ranges;
    @DatabaseField
    public int downloadPercent;

    public DownloadEntry() {

    }

    public DownloadEntry(String url) {
        this.url = url;
        this.id = url;
        this.name = url.substring(url.lastIndexOf("/") + 1);
    }

    public void reset() {
        currentLength = 0;
        ranges = null;
        downloadPercent = 0;
        File file = DownloadConfig.getInstance().getDownloadFile(url);
        if (file.exists()){
            file.delete();
        }
    }

    public enum DownloadStatus{OnIdle, OnWait, OnConnect, OnDownload, OnPause, OnInterrupt, OnResume, OnComplete, OnCancel, OnError}

    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == this.hashCode();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name + " is " + status.name() + " with " + currentLength + "/" + totalLength;
    }
}
