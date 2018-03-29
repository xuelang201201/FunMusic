package com.charles.funmusic.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.charles.funmusic.constant.DownloadStatus;
import com.charles.funmusic.model.DownloadDBEntity;

import java.util.ArrayList;

public class DownloadStore {
    private static DownloadStore sInstance = null;
    private DatabaseHelper mDatabaseHelper = null;

    private DownloadStore(final Context context) {
        mDatabaseHelper = DatabaseHelper.getInstance(context);
    }

    public static synchronized DownloadStore getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new DownloadStore(context.getApplicationContext());
        }

        return sInstance;
    }

    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DownloadStoreColumns.NAME + " ("
                + DownloadStoreColumns.ID + " TEXT NOT NULL PRIMARY KEY,"
                + DownloadStoreColumns.TOTAL_SIZE + " INT NOT NULL,"
                + DownloadStoreColumns.FILE_LENGTH + " INT NOT NULL, "
                + DownloadStoreColumns.URL + " TEXT NOT NULL,"
                + DownloadStoreColumns.DIR + " TEXT NOT NULL,"
                + DownloadStoreColumns.FILE_NAME + " TEXT NOT NULL,"
                + DownloadStoreColumns.ARTIST_NAME + " TEXT NOT NULL,"
                + DownloadStoreColumns.DOWNLOAD_STATUS + " INT NOT NULL);");
    }


    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DownloadStoreColumns.NAME);
        onCreate(db);
    }

    public synchronized void insert(DownloadDBEntity entity) {
        Log.e("data_entity", " id = " + entity.getDownloadId());
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(8);
            values.put(DownloadStoreColumns.ID, entity.getDownloadId());
            values.put(DownloadStoreColumns.TOTAL_SIZE, entity.getTotalSize());
            values.put(DownloadStoreColumns.FILE_LENGTH, entity.getCompletedSize());
            values.put(DownloadStoreColumns.URL, entity.getUrl());
            values.put(DownloadStoreColumns.DIR, entity.getSaveDirPath());
            values.put(DownloadStoreColumns.FILE_NAME, entity.getFileName());
            values.put(DownloadStoreColumns.ARTIST_NAME, entity.getArtist());
            values.put(DownloadStoreColumns.DOWNLOAD_STATUS, entity.getDownloadStatus());
            database.replace(DownloadStoreColumns.NAME, null, values);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public synchronized void update(DownloadDBEntity entity) {

        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(6);
            values.put(DownloadStoreColumns.TOTAL_SIZE, entity.getTotalSize());
            values.put(DownloadStoreColumns.FILE_LENGTH, entity.getCompletedSize());
            values.put(DownloadStoreColumns.URL, entity.getUrl());
            values.put(DownloadStoreColumns.DIR, entity.getSaveDirPath());
            values.put(DownloadStoreColumns.FILE_NAME, entity.getFileName());
            values.put(DownloadStoreColumns.DOWNLOAD_STATUS, entity.getDownloadStatus());
            database.update(DownloadStoreColumns.NAME, values, DownloadStoreColumns.ID + " = ?",
                    new String[]{entity.getDownloadId()});
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public void deleteTask(String Id) {
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.delete(DownloadStoreColumns.NAME, DownloadStoreColumns.ID + " = ?", 
                new String[]{String.valueOf(Id)});
    }

    public void deleteTask(String[] Id) {
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.delete(DownloadStoreColumns.NAME, DownloadStoreColumns.ID + " = ?", Id);
    }

    public void deleteDowningTasks() {
        ArrayList<String> results = new ArrayList<>();
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = database.query(DownloadStoreColumns.NAME, null,
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                results.ensureCapacity(cursor.getCount());

                do {
                    if (cursor.getInt(7) != DownloadStatus.DOWNLOAD_STATUS_COMPLETED)
                        results.add(cursor.getString(0));

                } while (cursor.moveToNext());
            }
            String[] t = new String[results.size()];
            for (int i = 0; i < results.size(); i++) {
                t[i] = results.get(i);
            }
            final StringBuilder selection = new StringBuilder();
            selection.append(DownloadStoreColumns.ID + " IN (");
            for (int i = 0; i < t.length; i++) {
                selection.append(t[i]);
                if (i < t.length - 1) {
                    selection.append(",");
                }
            }
            selection.append(")");
            database.delete(DownloadStoreColumns.NAME, selection.toString(), null);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }


    }

    public synchronized void deleteAll() {
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.delete(DownloadStoreColumns.NAME, null, null);
    }

    public synchronized DownloadDBEntity getDownLoadedList(String Id) {
        Cursor cursor = null;
        DownloadDBEntity entity;
        try {
            cursor = mDatabaseHelper.getReadableDatabase().query(DownloadStoreColumns.NAME, null,
                    DownloadStoreColumns.ID + " = ?", new String[]{String.valueOf(Id)}, null, null, null);
            if (cursor == null) {
                return null;
            }

            if (cursor.moveToFirst()) {

                do {
                    entity = new DownloadDBEntity(cursor.getString(0), cursor.getLong(1), cursor.getLong(2),
                            cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getInt(7));
                } while (cursor.moveToNext());
                return entity;
            } else return null;

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public synchronized ArrayList<DownloadDBEntity> getDownLoadedListAllDowning() {
        ArrayList<DownloadDBEntity> results = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = mDatabaseHelper.getReadableDatabase().query(DownloadStoreColumns.NAME, null,
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                results.ensureCapacity(cursor.getCount());

                do {
                    if (cursor.getInt(7) != DownloadStatus.DOWNLOAD_STATUS_COMPLETED)
                        results.add(new DownloadDBEntity(cursor.getString(0), cursor.getLong(1), cursor.getLong(2),
                                cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getInt(7)));
                } while (cursor.moveToNext());
            }

            return results;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public synchronized String[] getDownLoadedListAllDowningIds() {
        ArrayList<String> results = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = mDatabaseHelper.getReadableDatabase().query(DownloadStoreColumns.NAME, null,
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                results.ensureCapacity(cursor.getCount());

                do {
                    if (cursor.getInt(7) != DownloadStatus.DOWNLOAD_STATUS_COMPLETED)
                        results.add(cursor.getString(0));

                } while (cursor.moveToNext());
            }
            String[] t = new String[results.size()];
            for (int i = 0; i < results.size(); i++) {
                t[i] = results.get(i);
            }
            return t;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public synchronized ArrayList<DownloadDBEntity> getDownLoadedListAll() {
        ArrayList<DownloadDBEntity> results = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = mDatabaseHelper.getReadableDatabase().query(DownloadStoreColumns.NAME, null,
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                results.ensureCapacity(cursor.getCount());

                do {

                    results.add(new DownloadDBEntity(cursor.getString(0), cursor.getLong(1), cursor.getLong(2),
                            cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getInt(7)));
                } while (cursor.moveToNext());
            }

            return results;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public interface DownloadStoreColumns {
        /* Table name */
        String NAME = "download_file_information";

        /* Album IDs column */
        String ID = "id";

        /* Time played column */
        String TOTAL_SIZE = "total_size";

        String FILE_LENGTH = "complete_length";

        String URL = "url";

        String DIR = "dir";
        String FILE_NAME = "file_name";
        String ARTIST_NAME = "artist";
        String DOWNLOAD_STATUS = "notification_type";
    }
}