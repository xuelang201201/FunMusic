package com.charles.funmusic.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 建立最近播放数据库，设置存取数据方法
 */
public class RecentStore {

    private static final int MAX_ITEMS_IN_DB = 100;

    private static RecentStore sInstance = null;

    private DatabaseHelper mDatabaseHelper = null;

    private RecentStore(final Context context) {
        mDatabaseHelper = DatabaseHelper.getInstance(context);
    }

    public static synchronized RecentStore getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new RecentStore(context.getApplicationContext());
        }
        return sInstance;
    }

    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + RecentStoreColumns.NAME + " ("
                + RecentStoreColumns.ID + " LONG NOT NULL," + RecentStoreColumns.TIME_PLAYED
                + " LONG NOT NULL);");
    }

    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + RecentStoreColumns.NAME);
        onCreate(db);
    }

    public void addSongId(final long songId) {
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.beginTransaction();

        try {

            Cursor mostRecentItem = null;
            try {
                mostRecentItem = queryRecentIds("1");
                if (mostRecentItem != null && mostRecentItem.moveToFirst()) {
                    if (songId == mostRecentItem.getLong(0)) {
                        return;
                    }
                }
            } finally {
                if (mostRecentItem != null) {
                    mostRecentItem.close();
                    mostRecentItem = null;
                }
            }

            final ContentValues values = new ContentValues(2);
            values.put(RecentStoreColumns.ID, songId);
            values.put(RecentStoreColumns.TIME_PLAYED, System.currentTimeMillis());
            database.insert(RecentStoreColumns.NAME, null, values);

            Cursor oldest = null;
            try {
                oldest = database.query(RecentStoreColumns.NAME,
                        new String[]{RecentStoreColumns.TIME_PLAYED}, null, null, null, null,
                        RecentStoreColumns.TIME_PLAYED + " ASC");

                if (oldest != null && oldest.getCount() > MAX_ITEMS_IN_DB) {
                    oldest.moveToPosition(oldest.getCount() - MAX_ITEMS_IN_DB);
                    long timeOfRecordToKeep = oldest.getLong(0);

                    database.delete(RecentStoreColumns.NAME,
                            RecentStoreColumns.TIME_PLAYED + " < ?",
                            new String[]{String.valueOf(timeOfRecordToKeep)});

                }
            } finally {
                if (oldest != null) {
                    oldest.close();
                    oldest = null;
                }
            }
        } finally {
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }


    public void removeItem(final long songId) {
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.delete(RecentStoreColumns.NAME, RecentStoreColumns.ID + " = ?", new String[]{
                String.valueOf(songId)
        });

    }

    public void deleteAll() {
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.delete(RecentStoreColumns.NAME, null, null);
    }


    public Cursor queryRecentIds(final String limit) {
        final SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        return database.query(RecentStoreColumns.NAME,
                new String[]{RecentStoreColumns.ID}, null, null, null, null,
                RecentStoreColumns.TIME_PLAYED + " DESC", limit);
    }

    public interface RecentStoreColumns {
        /* Table name */
        String NAME = "recent_history";

        /* Album IDs column */
        String ID = "song_id";

        /* Time played column */
        String TIME_PLAYED = "time_played";
    }
}
