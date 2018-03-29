package com.charles.funmusic.application;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.charles.funmusic.model.Music;
import com.charles.funmusic.service.MusicService;
import com.charles.funmusic.utils.loader.CoverLoader;
import com.charles.funmusic.utils.Preferences;
import com.charles.funmusic.utils.ScreenUtil;
import com.charles.funmusic.utils.ToastUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class AppCache {
    private Context mContext;
    private MusicService mMusicService;
    private final List<Music> mMusics = new ArrayList<>();
    private final List<Activity> mActivityStack = new ArrayList<>();
    private static Gson sGson;

    public static Gson gsonInstance() {
        if (sGson == null) {
            sGson = new Gson();
        }
        return sGson;
    }

    private AppCache() {
    }

    private static class SingletonHolder {
        @SuppressLint("StaticFieldLeak")
        private static AppCache sAppCache = new AppCache();
    }

    private static AppCache getInstance() {
        return SingletonHolder.sAppCache;
    }

    public static void init(Application application) {
        getInstance().onInit(application);
    }

    private void onInit(Application application) {
        mContext = application.getApplicationContext();
        ToastUtil.init(mContext);
        Preferences.init(mContext);
        ScreenUtil.init(mContext);
        CoverLoader.getInstance().init(mContext);
        application.registerActivityLifecycleCallbacks(new ActivityLifecycle());
    }

    public static Context getContext() {
        return getInstance().mContext;
    }

    public static MusicService getMusicService() {
        return getInstance().mMusicService;
    }

    public static void setMusicService(MusicService service) {
        getInstance().mMusicService = service;
    }

    public static List<Music> getMusics() {
        return getInstance().mMusics;
    }

    public static void clearStack() {
        List<Activity> activityStack = getInstance().mActivityStack;
        for (int i = activityStack.size() - 1; i >= 0; i--) {
            Activity activity = activityStack.get(i);
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
        activityStack.clear();
    }

    private static class ActivityLifecycle implements Application.ActivityLifecycleCallbacks {
        private static final String TAG = "Activity";

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            Log.i(TAG, "onCreate: " + activity.getClass().getSimpleName());
            getInstance().mActivityStack.add(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            Log.i(TAG, "onDestroy: " + activity.getClass().getSimpleName());
            getInstance().mActivityStack.remove(activity);
        }
    }
}