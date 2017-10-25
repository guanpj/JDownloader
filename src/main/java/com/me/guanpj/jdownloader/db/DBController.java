package com.me.guanpj.jdownloader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.dao.Dao;
import com.me.guanpj.jdownloader.core.DownloadEntry;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Jie on 2017/4/29.
 */

public class DBController {

    private static DBController mInstance;
    private final Context mContext;
    private OrmDBHelper mHelper;
    private SQLiteDatabase mDB;

    private DBController(Context context) {
        mContext = context;
        mHelper = new OrmDBHelper(context);
        mDB = mHelper.getWritableDatabase();
    }

    public synchronized static DBController getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new DBController(context);
        }
        return mInstance;
    }

    public synchronized void createOrUpdate(DownloadEntry entry) {
        try {
            Dao<DownloadEntry, String> dao = mHelper.getDao(DownloadEntry.class);
            dao.createOrUpdate(entry);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized ArrayList<DownloadEntry> queryAll() {
        ArrayList<DownloadEntry> entries = null;
        try {
            Dao<DownloadEntry, String> dao = mHelper.getDao(DownloadEntry.class);
            entries = (ArrayList<DownloadEntry>) dao.query(dao.queryBuilder().prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return entries;
        }
    }

    public synchronized DownloadEntry queryForId(String id) {
        DownloadEntry entry = null;
        try {
            Dao<DownloadEntry, String> dao = mHelper.getDao(DownloadEntry.class);
            entry = dao.queryForId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return entry;
        }
    }

    public synchronized void deleteById(String id) {
        try {
            Dao<DownloadEntry, String> dao = mHelper.getDao(DownloadEntry.class);
            dao.deleteById(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
