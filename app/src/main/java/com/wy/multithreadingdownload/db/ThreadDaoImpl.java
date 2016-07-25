package com.wy.multithreadingdownload.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wy.multithreadingdownload.entity.ThreadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wy on 2016/7/22.
 */
public class ThreadDaoImpl implements ThreadDao {
    private DBHelper mHelper = null;

    public ThreadDaoImpl(Context context) {
        mHelper = new DBHelper(context);
    }

    @Override
    public void insertThread(ThreadInfo threadInfo) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("insert into thread_info(thread_id,url,start,end,finished)values(?,?,?,?,?)",
                new Object[]{threadInfo.getId(), threadInfo.getUrl(), threadInfo.getStart(), threadInfo.getEnd(), threadInfo.getFinished()});

        db.close();
    }

    @Override
    public void deleteThread(String url, int thread_id) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("delete from thread_info where url=? and thread_id=?",
                new Object[]{url, thread_id});

        db.close();
    }

    @Override
    public void updateThread(String url, int thread_id, int finished) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL("update thread_info set finished=? where url=? and thread_id=?",
                new Object[]{finished, url, thread_id});
        db.close();
    }

    @Override
    public List<ThreadInfo> getThreads(String url) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        List<ThreadInfo> list = new ArrayList<ThreadInfo>();

        Cursor cursor = db.rawQuery("select * from  thread_info where url=? ", new String[]{url});

        while (cursor.moveToNext()) {
            ThreadInfo threadInfo = new ThreadInfo(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4));
            list.add(threadInfo);
        }
        cursor.close();

        db.close();
        return list;
    }

    @Override
    public boolean isExists(String url, int thread_id) {
        SQLiteDatabase db = mHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery("select * from  thread_info where url=? and thread_id=?", new String[]{url, String.valueOf(thread_id)});
        boolean exists = cursor.moveToNext();
        cursor.close();

        db.close();
        return exists;
    }
}
