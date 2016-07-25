package com.wy.multithreadingdownload.service;

import android.content.Context;
import android.content.Intent;

import com.wy.multithreadingdownload.db.ThreadDao;
import com.wy.multithreadingdownload.db.ThreadDaoImpl;
import com.wy.multithreadingdownload.entity.FileInfo;
import com.wy.multithreadingdownload.entity.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by wy on 2016/7/22.
 * 下载任务类
 */
public class DownloadTask {
    private Context mContext;
    private FileInfo mFileInfo;
    private ThreadDao mDao;
    private int mFinished = 0;
    public boolean isPause;

    public DownloadTask(Context mContext, FileInfo mFileInfo) {
        this.mContext = mContext;
        this.mFileInfo = mFileInfo;
        mDao = new ThreadDaoImpl(mContext);
    }

    public void download() {
        //读取数据库的线程信息
        List<ThreadInfo> threadInfos = mDao.getThreads(mFileInfo.getUrl());
        ThreadInfo threadInfo = null;
        if (threadInfos.size() == 0) {
            threadInfo = new ThreadInfo(0, mFileInfo.getUrl(), 0, mFileInfo.getLength(), 0);
        } else {
            threadInfo = threadInfos.get(0);
        }
        //创建子线程开始下载
        new DownloadThread(threadInfo).start();

    }

    /**
     * 下载线程
     */
    class DownloadThread extends Thread {
        private ThreadInfo mThreadInfo;

        public DownloadThread(ThreadInfo mThreadInfo) {
            this.mThreadInfo = mThreadInfo;
        }
        @Override
        public void run() {
            //向数据库里面插入线程信息
            if (mDao.isExists(mThreadInfo.getUrl(), mThreadInfo.getId())) {
                mDao.insertThread(mThreadInfo);

            }
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            InputStream is = null;
            try {
                URL url = new URL(mThreadInfo.getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setRequestMethod("GET");
                //设置下载位置
                int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
                conn.setRequestProperty("Range", "bytes=" + start + "-" + mThreadInfo.getEnd());
                //设置文件写入位置
                File file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);
                Intent intent = new Intent(DownloadService.ACTION_UPDATE);
                mFinished += mThreadInfo.getFinished();
                //开始下载
                if (conn.getResponseCode() == 206) {//206 部分下载
                    //读取数据
                    is = conn.getInputStream();
                    byte[] buffer = new byte[1024 * 4];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    while ((len = is.read(buffer)) != -1) {
                        //写入文件
                        raf.write(buffer, 0, len);
                        //把下载进度发送广播给activity
                        mFinished += len;
                        if (System.currentTimeMillis() - time > 500) {
                            time = System.currentTimeMillis();
                            intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
                            mContext.sendBroadcast(intent);
                        }
                        //在下载暂停时保存下载进度到数据库
                        if (isPause) {
                            mDao.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mFinished);
                            return;
                        }
                    }
                    //删除线程信息
                    mDao.deleteThread(mThreadInfo.getUrl(), mThreadInfo.getId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    conn.disconnect();
                    raf.close();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
