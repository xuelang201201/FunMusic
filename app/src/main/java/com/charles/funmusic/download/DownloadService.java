package com.charles.funmusic.download;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.charles.funmusic.R;
import com.charles.funmusic.activity.DownloaderActivity;
import com.charles.funmusic.constant.DownloadStatus;
import com.charles.funmusic.constant.Keys;
import com.charles.funmusic.model.DownloadDBEntity;
import com.charles.funmusic.net.HttpUtil;
import com.charles.funmusic.provider.DownloadStore;
import com.charles.funmusic.utils.SystemUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadService extends Service {
    public static final String ADD_DOWNLOAD_TASK = "com.charles.funmusic.download_task_add";
    public static final String ADD_MULTI_DOWNLOAD_TASK = "com.charles.funmusic.multi_download_task_add";
    public static final String CANCEL_DOWNLOAD_TASK = "com.charles.funmusic.cancel_task";
    public static final String CANCEL_ALL_DOWNLOAD_TASK = "com.charles.funmusic.cancel_all_task";
    public static final String START_ALL_DOWNLOAD_TASK = "com.charles.funmusic.start_all_task";
    public static final String RESUME_START_DOWNLOAD_TASK = "com.charles.funmusic.resume_start_task";
    public static final String PAUSE_TASK = "com.charles.funmusic.pause_task";
    public static final String PAUSE_ALL_TASK = "com.charles.funmusic.pause_all_task";
    public static final String UPDATE_DOWNLOAD_STATUS = "com.charles.funmusic.update_download";
    public static final String TASK_START_DOWNLOAD = "com.charles.funmusic.task_start";
    public static final String TASKS_CHANGED = "com.charles.funmusic.task_changes";

    private boolean d = true;
    private static final String TAG = "DownloadService";
    private static DownloadStore sDownloadStore;
    private ExecutorService mExecutorService;
    private static ArrayList<String> sPrepareTaskList = new ArrayList<>();
    private int mDownTaskCount = 0;
    private int mDownTaskDownloaded = -1;
    private DownloadTask mDownloadTask;
    private NotificationManager mNotificationManager;
    private Context mContext;
    private int mNotificationId = 10;
    private boolean mIsForeground;
    private DownloadTaskListener mListener = new DownloadTaskListener() {
        @Override
        public void onPrepare(DownloadTask downloadTask) {
            if (d) {
                Log.d(TAG, TAG + "task onPrepare");
            }
        }

        @Override
        public void onStart(DownloadTask downloadTask) {
            if (d) {
                Log.d(TAG, TAG + " task onStart");
            }
            Intent intent = new Intent(TASK_START_DOWNLOAD);
            intent.putExtra("complete_size", downloadTask.getCompletedSize());
            intent.putExtra("total_size", downloadTask.getTotalSize());
            intent.setPackage(Keys.PACKAGE);
            sendBroadcast(intent);
        }

        @Override
        public void onDownloading(DownloadTask downloadTask) {
            Intent intent = new Intent(UPDATE_DOWNLOAD_STATUS);
            intent.putExtra("complete_size", downloadTask.getCompletedSize());
            intent.putExtra("total_size", downloadTask.getTotalSize());
            intent.setPackage(Keys.PACKAGE);
            sendBroadcast(intent);
        }

        @Override
        public void onPause(DownloadTask downloadTask) {
            if (d) {
                Log.d(TAG, TAG + " task onPause");
            }
            sendIntent(TASKS_CHANGED);
            if (sPrepareTaskList.size() > 0) {
                if(mDownloadTask != null)
                sPrepareTaskList.remove(mDownloadTask.getId());
            }
            mDownloadTask = null;
            upDateNotification();
            startTask();
        }

        @Override
        public void onCancel(DownloadTask downloadTask) {
            if (d) {
                Log.d(TAG, TAG + " task onCancel");
            }
            sendIntent(TASKS_CHANGED);
            if (sPrepareTaskList.size() > 0) {
                if(mDownloadTask != null)
                sPrepareTaskList.remove(mDownloadTask.getId());
            }
            mDownloadTask = null;
            upDateNotification();
            startTask();
        }

        @Override
        public void onCompleted(DownloadTask downloadTask) {
            sendIntent(TASKS_CHANGED);
            if (d) {
                Log.d(TAG, TAG + " task Completed");
            }
            if (sPrepareTaskList.size() > 0) {
                if(mDownloadTask != null)
                sPrepareTaskList.remove(mDownloadTask.getId());
            }
            mDownloadTask = null;
            mDownTaskDownloaded++;
            if (d) {
                Log.d(TAG, "complete task and start");
            }
            startTask();

        }

        @Override
        public void onError(DownloadTask downloadTask, int errorCode) {
            if (d) {
                Log.d(TAG, TAG + " task onError");
            }
            startTask();
        }
    };

    public static ArrayList<String> getPrepareTasks() {
        return sPrepareTaskList;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (d) {
            Log.d(TAG, TAG + " on_create");
        }
        mContext = this;
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mExecutorService = Executors.newSingleThreadExecutor();
        sDownloadStore = DownloadStore.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (d) {
            Log.d(TAG, TAG + " on_start_command");
        }
        if(intent == null){
            mNotificationManager.cancel(mNotificationId);
        }
        String action = null;
        try {
            action = intent.getAction();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        if (action == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        switch (action) {
            case ADD_DOWNLOAD_TASK:
                String name = intent.getStringExtra("name");
                String artist = intent.getStringExtra("artist");
                String url = intent.getStringExtra("url");
                addDownloadTask(name, artist, url);
                break;
            case ADD_MULTI_DOWNLOAD_TASK:
                String[] names = intent.getStringArrayExtra("names");
                String[] artists = intent.getStringArrayExtra("artists");
                ArrayList<String> urls = intent.getStringArrayListExtra("urls");
                addDownloadTask(names, artists, urls);
                break;
            case RESUME_START_DOWNLOAD_TASK:
                String taskId = intent.getStringExtra("download_id");
                Log.d(TAG, "resume task = " + taskId);
                resume(taskId);
                break;
            case PAUSE_TASK:
                String taskId1 = intent.getStringExtra("download_id");
                Log.d(TAG, "pause task = " + taskId1);
                pause(taskId1);
                break;
            case CANCEL_DOWNLOAD_TASK:
                String taskId3 = intent.getStringExtra("download_id");
                Log.d(TAG, "cancel task = " + taskId3);
                cancel(taskId3);
                break;
            case CANCEL_ALL_DOWNLOAD_TASK:
                if (sPrepareTaskList.size() > 1) {
                    sPrepareTaskList.clear();
                    if(mDownloadTask != null)
                    sPrepareTaskList.add(mDownloadTask.getId());
                }
                if(mDownloadTask != null)
                cancel(mDownloadTask.getId());
                sDownloadStore.deleteDowningTasks();
                sendIntent(TASKS_CHANGED);
                break;
            case START_ALL_DOWNLOAD_TASK:
                String[] ids = sDownloadStore.getDownLoadedListAllDowningIds();
                for (String id : ids) {
                    if (!sPrepareTaskList.contains(id)) {
                        sPrepareTaskList.add(id);
                    }
                }
                startTask();
                break;
            case PAUSE_ALL_TASK:

                if (sPrepareTaskList.size() > 1) {
                    sPrepareTaskList.clear();
                    if(mDownloadTask != null)
                    sPrepareTaskList.add(mDownloadTask.getId());
                }
                if(mDownloadTask != null)
                pause(mDownloadTask.getId());
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mNotificationManager.cancel(mNotificationId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String getDownSave() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/remusic/");
            if (!file.exists()) {
                boolean r = file.mkdirs();
                if (!r) {
                    Toast.makeText(mContext, "储存卡无法创建文件", Toast.LENGTH_SHORT).show();
                    return null;
                }
                return file.getAbsolutePath() + "/";
            }
            return file.getAbsolutePath() + "/";
        } else {
            Toast.makeText(mContext, "没有储存卡", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void addDownloadTask(String[] names, String[] artists, ArrayList<String> urls) {

        if (d) {
            Log.d(TAG, "add task name = " + Arrays.toString(names) + "  taskId = " + (urls).hashCode() + "  task artist = " + Arrays.toString(artists));
        }
        int len = urls.size();
        for (int i = 0; i < len; i++) {
            DownloadDBEntity dbEntity = new DownloadDBEntity((urls.get(i)).hashCode() + "", 0L,
                    0L, urls.get(i), getDownSave(), names[i], artists[i], DownloadStatus.DOWNLOAD_STATUS_INIT);
            sDownloadStore.insert(dbEntity);
            sPrepareTaskList.add(dbEntity.getDownloadId());
            mDownTaskCount++;
        }
        Toast.makeText(mContext,"已加入到下载", Toast.LENGTH_SHORT).show();
        upDateNotification();
        if (mDownloadTask != null) {
            Log.d(TAG, "add task wrong, current task is not null");
            return;
        }

        startTask();

    }

    private void addDownloadTask(String name, String artist, String url) {


        Log.d(TAG, "add task name = " + name + "  taskId = " + (url).hashCode() + "  task artsit = " + artist);
        DownloadDBEntity dbEntity = new DownloadDBEntity((url).hashCode() + "", 0L,
                0L, url, getDownSave(), name, artist, DownloadStatus.DOWNLOAD_STATUS_INIT);
        sDownloadStore.insert(dbEntity);
        sPrepareTaskList.add(dbEntity.getDownloadId());
        mDownTaskCount++;
        upDateNotification();
        Toast.makeText(mContext,"已加入到下载", Toast.LENGTH_SHORT).show();
        if (mDownloadTask != null) {
            Log.d(TAG, "add task wrong, current task is not null");
            return;
        }

        startTask();

    }

    private void upDateNotification() {
        if (mDownloadTask == null) {
            return;
        }
        if (!mIsForeground) {
            startForeground(mNotificationId, getNotification(false));
            mIsForeground = true;
        } else {
            mNotificationManager.notify(mNotificationId, getNotification(false));
        }
    }

    private void cancelNotification() {
        Log.d(TAG, " cancelnotification");
        stopForeground(true);
        mIsForeground = false;
        mNotificationManager.notify(mNotificationId, getNotification(true));
        mDownTaskCount = 0;
        mDownTaskDownloaded = -1;

    }


    public void startTask() {
        Log.d(TAG, TAG + " start task task size = " + sPrepareTaskList.size());
        if (mDownloadTask != null) {
            Log.d(TAG, "start task wrong, current task is running");
            return;
        }
        if (sPrepareTaskList.size() > 0) {
            DownloadTask downloadTask = null;
            Log.d(TAG, sPrepareTaskList.get(0));
            DownloadDBEntity entity = sDownloadStore.getDownLoadedList(sPrepareTaskList.get(0));

            if (entity != null) {
                Log.d(TAG, "entity id = " + entity.getDownloadId());
                downloadTask = DownloadTask.parse(entity, mContext);
            }
            if (downloadTask == null) {
                Log.d(TAG, "can't create download_task");
                return;
            }
            Log.d(TAG, "start task ,task name = " + downloadTask.getFileName() + "  taskId = " + downloadTask.getId());
            if (downloadTask.getDownloadStatus() != DownloadStatus.DOWNLOAD_STATUS_COMPLETED) {
                downloadTask.setDownloadStatus(DownloadStatus.DOWNLOAD_STATUS_PREPARE);
                downloadTask.setDownloadStore(sDownloadStore);
                downloadTask.setHttpClient(HttpUtil.mOkHttpClient);
                downloadTask.addDownloadListener(mListener);
                mExecutorService.submit(downloadTask);
                mDownloadTask = downloadTask;
                upDateNotification();
                sendIntent(TASKS_CHANGED);
            }
        } else {
            Log.d(TAG, " no task");
            cancelNotification();
        }
    }

    /**
     * if return null,the task does not exist
     */
    public void resume(String taskId) {

        mDownTaskCount++;
        sPrepareTaskList.add(taskId);
        upDateNotification();
        sendIntent(TASKS_CHANGED);
        if (mDownloadTask == null) {
            startTask();
        }

        Log.d(TAG, "resume task = " + taskId);
    }


    public void cancel(String taskId) {
        if (mDownloadTask != null) {
            if (taskId.equals(mDownloadTask.getId())) {
                mDownloadTask.cancel();
                mDownloadTask.setDownloadStatus(DownloadStatus.DOWNLOAD_STATUS_CANCEL);
            }
        }

        if (sPrepareTaskList.contains(taskId)) {
            mDownTaskCount--;
            sPrepareTaskList.remove(taskId);
        }

        if (sPrepareTaskList.size() == 0) {
            mDownloadTask = null;
        }
        sDownloadStore.deleteTask(taskId);
        upDateNotification();
        sendIntent(TASKS_CHANGED);
        Log.d(TAG, "cancel task = " + taskId);
    }

    public void pause(String taskId) {
        mDownTaskCount--;

        if (mDownloadTask != null && taskId.equals(mDownloadTask.getId())) {
            mDownloadTask.pause();
        }
        sPrepareTaskList.remove(taskId);
        if (sPrepareTaskList.size() == 0) {
            mDownloadTask = null;
        }
        upDateNotification();
        sendIntent(TASKS_CHANGED);
    }

    private Notification getNotification(boolean complete) {

        if (mDownTaskCount == 0) {
            mDownTaskCount = sPrepareTaskList.size();
        }
        Log.d(TAG, "notification download_task_count = " + mDownTaskCount);
        if (mDownTaskDownloaded == -1) {
            mDownTaskDownloaded = 0;
        }
        RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.download_notification);

        PendingIntent pendingIntent = PendingIntent.getActivity(this.getApplicationContext(), 0,
                new Intent(this.getApplicationContext(), DownloaderActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        final Intent nowPlayingIntent = new Intent();
        nowPlayingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        nowPlayingIntent.setComponent(new ComponentName("com.charles.funmusic", "com.charles.funmusic.activity.DownloaderActivity"));
        PendingIntent clickIntent = PendingIntent.getActivity(this,0,nowPlayingIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setImageViewResource(R.id.image, R.drawable.ic_default_album_cover);
        if(complete){
            remoteViews.setTextViewText(R.id.title, "funmusic" );
            remoteViews.setTextViewText(R.id.text, "下载完成，点击查看" );
            remoteViews.setTextViewText(R.id.time, showDate());
        }else {
            remoteViews.setTextViewText(R.id.title, "下载进度：" + mDownTaskDownloaded + "/" + mDownTaskCount);
            remoteViews.setTextViewText(R.id.text, "正在下载：" + mDownloadTask.getFileName());
            remoteViews.setTextViewText(R.id.time, showDate());
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "")
                .setContent(remoteViews)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(clickIntent);

        if (SystemUtil.isJellyBeanMR1()) {
            builder.setShowWhen(false);
        }
        return builder.build();
    }

    public static String showDate() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sDateFormat = new SimpleDateFormat("a hh:mm");
        return sDateFormat.format(new Date());
    }

    private void sendIntent(String action){
        Intent intent = new Intent();
        intent.setAction(action);
        intent.setPackage(Keys.PACKAGE);
        sendBroadcast(intent);
    }
}