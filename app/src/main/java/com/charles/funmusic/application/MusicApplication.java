package com.charles.funmusic.application;

import android.app.Application;

import com.charles.funmusic.http.HttpInterceptor;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class MusicApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AppCache.init(this);
        initOkHttpUtils();
    }

    private void initOkHttpUtils() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(new HttpInterceptor())
                .build();
        OkHttpUtils.initClient(okHttpClient);
    }
}
