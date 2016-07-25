package com.wy.multithreadingdownload.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wy.multithreadingdownload.entity.FileInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by wy on 2016/7/22.
 */
public class DownloadService extends Service {
    private static final String TAG = "DownloadService";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_UPDATE= "ACTION_UPDATE";
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloads/";
    public static final int MSG_INIT = 0;
    private DownloadTask mTask;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //获取activity传来的参数
        if (ACTION_START.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            Log.e(TAG, "ACTION_START:" + fileInfo.toString());
            //启动线程
            new InitThread(fileInfo).start();

        } else if (ACTION_STOP.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            Log.e(TAG, "ACTION_STOP:" + fileInfo.toString());
            if (mTask != null) {
                mTask.isPause = true;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_INIT:
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    Log.e(TAG, "handleMessage: "+fileInfo.toString());
                    //启动下载任务
                    
                    mTask = new DownloadTask(DownloadService.this,fileInfo);
                    mTask.download();

                    break;
            }
        }
    };

    class InitThread extends Thread {
        public FileInfo fileInfo;

        public InitThread(FileInfo fileInfo) {
            this.fileInfo = fileInfo;
        }

        @Override
        public void run() {
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            try {
                URL url = new URL(fileInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                int length = -1;
                if (conn.getResponseCode() == 200) {
                    //获得文件长度
                    length = conn.getContentLength();
                }
                if (length <= 0) {
                    return;
                }
                File dir = new File(DOWNLOAD_PATH);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                //在本地创建文件
                File file = new File(dir, fileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                //设置文件长度
                raf.setLength(length);
                fileInfo.setLength(length);
                mHandler.obtainMessage(MSG_INIT, fileInfo).sendToTarget();

            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                try {
                    conn.disconnect();
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
