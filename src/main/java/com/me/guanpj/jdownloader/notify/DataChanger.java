package com.me.guanpj.jdownloader.notify;

import android.content.Context;

import com.me.guanpj.jdownloader.core.DownloadEntry;
import com.me.guanpj.jdownloader.db.DBController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;

/**
 * Created by Jie on 2017/4/23.
 */

public class DataChanger extends Observable {

    private static DataChanger mInstance;
    private Context mContext;
    private LinkedHashMap<String, DownloadEntry> mDownloadEntries;

    private DataChanger(Context context) {
        mContext = context;
        mDownloadEntries = new LinkedHashMap<>();
    }

    public synchronized static DataChanger getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new DataChanger(context);
        }
        return mInstance;
    }

    public void postStatus(DownloadEntry entry) {
        mDownloadEntries.put(entry.id, entry);
        DBController.getInstance(mContext).createOrUpdate(entry);
        setChanged();
        notifyObservers(entry);
    }

    public ArrayList<DownloadEntry> getRecoverableDownloadEntries() {
        ArrayList<DownloadEntry> recoverabableEntries = null;
        for(Map.Entry<String, DownloadEntry> entry : mDownloadEntries.entrySet()) {
            if(entry.getValue().status == DownloadEntry.DownloadStatus.OnPause) {
                if(recoverabableEntries == null) {
                    recoverabableEntries = new ArrayList<>();
                }
                recoverabableEntries.add(entry.getValue());
            }
        }
        return recoverabableEntries;
    }

    public DownloadEntry getDownloadEntry(String id) {
        return mDownloadEntries.get(id);
    }

    public void addDownloadEntry(String id, DownloadEntry entry) {
        mDownloadEntries.put(id, entry);
    }

    public boolean containsDownloadEntry(String id) {
        return mDownloadEntries.containsKey(id);
    }

    public void deleteDownloadEntry(String id){
        mDownloadEntries.remove(id);
        DBController.getInstance(mContext).deleteById(id);
    }
}
