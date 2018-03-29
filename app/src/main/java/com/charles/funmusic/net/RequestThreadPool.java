package com.charles.funmusic.net;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RequestThreadPool {

    private static RequestThreadPool mInstance;
    private ThreadPoolExecutor mThreadPoolExec;
    private static final int KEEP_ALIVE = 10;
    private BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();

    public static synchronized void post(Runnable runnable) {
        if (mInstance == null) {
            mInstance = new RequestThreadPool();
        }
        mInstance.mThreadPoolExec.execute(runnable);
    }

    private RequestThreadPool() {
        int coreNum = Runtime.getRuntime().availableProcessors();
        int MAX_POOL_SIZE = coreNum * 2;
        mThreadPoolExec = new ThreadPoolExecutor(
                coreNum,
                MAX_POOL_SIZE,
                KEEP_ALIVE,
                TimeUnit.SECONDS,
                workQueue);
    }

    public static boolean isTerminated(){
        return mInstance.mThreadPoolExec.isTerminated();
    }

    public static void finish() {
        if(mInstance != null){
            mInstance.mThreadPoolExec.shutdownNow();
            mInstance.mThreadPoolExec.purge();
            mInstance.mThreadPoolExec = null;
            mInstance = null;
        }
    }
}