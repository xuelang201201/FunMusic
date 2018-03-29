package com.charles.funmusic.utils;

import android.content.Context;
import android.os.Handler;

import java.lang.ref.WeakReference;

public class HandlerUtil extends Handler {
    private WeakReference<Context> mContextReference;
    private static HandlerUtil sInstance = null;

    private HandlerUtil(Context context) {
        mContextReference = new WeakReference<>(context);
    }

    public static HandlerUtil getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new HandlerUtil(context.getApplicationContext());
        }
        return sInstance;
    }

    public WeakReference<Context> getContextReference() {
        return mContextReference;
    }

    public void setContextReference(WeakReference<Context> contextReference) {
        mContextReference = contextReference;
    }
}
