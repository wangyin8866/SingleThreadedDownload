package com.wy.multithreadingdownload.db;

import com.wy.multithreadingdownload.entity.ThreadInfo;

import java.util.List;

/**
 * Created by wy on 2016/7/22.
 * 数据访问接口
 */
public interface ThreadDao {
    /**
     * 插入线程信息
     */
    void insertThread(ThreadInfo threadInfo);
    /**
     * 删除线程
     */
    void deleteThread(String url,int thread_id);
    /**
     * 跟新线程下载进度
     */
    void updateThread(String url,int thread_id,int finished);

    /**
     * 查询文件的线程信息
     * @param url
     * @return
     */
    List<ThreadInfo> getThreads(String url);
    /**
     * 线程信息是否存在
     */
    boolean isExists(String url,int thread_id);


}
