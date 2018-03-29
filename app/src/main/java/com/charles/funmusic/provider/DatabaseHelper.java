package com.charles.funmusic.provider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 最近播放，歌曲播放次数，搜索历史，播放列表，下载等数据库
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fun_music.db";
    private static final int VERSION = 1;
    @SuppressLint("StaticFieldLeak")
    private static DatabaseHelper sInstance = null;

    private final Context mContext;

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);

        mContext = context;
    }

    public static synchronized DatabaseHelper getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        MusicPlaybackState.getInstance(mContext).onCreate(db);
        RecentStore.getInstance(mContext).onCreate(db);
        SongPlayCount.getInstance(mContext).onCreate(db);
        SearchHistory.getInstance(mContext).onCreate(db);
        PlaylistInfo.getInstance(mContext).onCreate(db);
        PlaylistManager.getInstance(mContext).onCreate(db);
        DownloadStore.getInstance(mContext).onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        MusicPlaybackState.getInstance(mContext).onUpgrade(db, oldVersion, newVersion);
        RecentStore.getInstance(mContext).onUpgrade(db, oldVersion, newVersion);
        SongPlayCount.getInstance(mContext).onUpgrade(db, oldVersion, newVersion);
        SearchHistory.getInstance(mContext).onUpgrade(db, oldVersion, newVersion);
        PlaylistInfo.getInstance(mContext).onUpgrade(db, oldVersion, newVersion);
        PlaylistManager.getInstance(mContext).onUpgrade(db, oldVersion, newVersion);
        DownloadStore.getInstance(mContext).onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        MusicPlaybackState.getInstance(mContext).onDowngrade(db, oldVersion, newVersion);
        RecentStore.getInstance(mContext).onDowngrade(db, oldVersion, newVersion);
        SongPlayCount.getInstance(mContext).onDowngrade(db, oldVersion, newVersion);
        SearchHistory.getInstance(mContext).onDowngrade(db, oldVersion, newVersion);
        PlaylistInfo.getInstance(mContext).onDowngrade(db, oldVersion, newVersion);
        PlaylistManager.getInstance(mContext).onDowngrade(db, oldVersion, newVersion);
        DownloadStore.getInstance(mContext).onDowngrade(db, oldVersion, newVersion);
    }
}
