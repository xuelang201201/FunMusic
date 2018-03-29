package com.charles.funmusic.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.charles.funmusic.helper.MusicPlaybackTrack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * This keeps track of the music playback and history state of the playback service
 */
public class MusicPlaybackState {
    private static MusicPlaybackState sInstance = null;

    private DatabaseHelper mDatabaseHelper = null;

    private MusicPlaybackState(final Context context) {
        mDatabaseHelper = DatabaseHelper.getInstance(context);
    }

    public static synchronized MusicPlaybackState getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new MusicPlaybackState(context.getApplicationContext());
        }
        return sInstance;
    }

    public void onCreate(final SQLiteDatabase db) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ");
        builder.append(PlaybackQueueColumns.NAME);
        builder.append("(");

        builder.append(PlaybackQueueColumns.TRACK_ID);
        builder.append(" LONG NOT NULL,");

//        builder.append(PlaybackQueueColumns.SOURCE_ID);
//        builder.append(" LONG NOT NULL,");
//
//        builder.append(PlaybackQueueColumns.SOURCE_TYPE);
//        builder.append(" INT NOT NULL,");

        builder.append(PlaybackQueueColumns.SOURCE_POSITION);
        builder.append(" INT NOT NULL);");

        db.execSQL(builder.toString());

        builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ");
        builder.append(PlaybackHistoryColumns.NAME);
        builder.append("(");

        builder.append(PlaybackHistoryColumns.POSITION);
        builder.append(" INT NOT NULL);");

        db.execSQL(builder.toString());
    }

    void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        // this table was created in version 2 so call the onCreate method if we hit that scenario
        if (oldVersion < 2 && newVersion >= 2) {
            onCreate(db);
        }
    }

    void onDowngrade(SQLiteDatabase db, int oldVesion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + PlaybackQueueColumns.NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PlaybackHistoryColumns.NAME);
        onCreate(db);
    }

    public synchronized void insert(MusicPlaybackTrack track) {
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(2);
            values.put(PlaybackQueueColumns.TRACK_ID, track.getId());
            values.put(PlaybackQueueColumns.SOURCE_POSITION, track.getSourcePosition());
            database.insert(PlaybackQueueColumns.NAME, null, values);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

    }

    public synchronized void delete(long id) {
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            database.delete(PlaybackQueueColumns.NAME, PlaybackQueueColumns.TRACK_ID + " = " + id, null);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

    }

    public synchronized void clearQueue() {
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        try {
            database.delete(PlaybackQueueColumns.NAME, null, null);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void saveState(final ArrayList<MusicPlaybackTrack> queue,
                                       LinkedList<Integer> history) {
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.beginTransaction();

        try {
            database.delete(PlaybackQueueColumns.NAME, null, null);
            database.delete(PlaybackHistoryColumns.NAME, null, null);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        final int NUM_PROCESS = 20;
        int position = 0;
        while (position < queue.size()) {
            database.beginTransaction();
            try {
                for (int i = position; i < queue.size() && i < position + NUM_PROCESS; i++) {
                    MusicPlaybackTrack track = queue.get(i);
//                    ContentValues values = new ContentValues(4);
                    ContentValues values = new ContentValues(2);

                    values.put(PlaybackQueueColumns.TRACK_ID, track.getId());
//                    values.put(PlaybackQueueColumns.SOURCE_ID, track.mSourceId);
//                    values.put(PlaybackQueueColumns.SOURCE_TYPE, track.mSourceType.mId);
                    values.put(PlaybackQueueColumns.SOURCE_POSITION, track.getSourcePosition());

                    database.insert(PlaybackQueueColumns.NAME, null, values);
                }
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
                position += NUM_PROCESS;
            }
        }

        if (history != null) {
            Iterator<Integer> iterator = history.iterator();
            while (iterator.hasNext()) {
                database.beginTransaction();
                try {
                    for (int i = 0; iterator.hasNext() && i < NUM_PROCESS; i++) {
                        ContentValues values = new ContentValues(1);
                        values.put(PlaybackHistoryColumns.POSITION, iterator.next());

                        database.insert(PlaybackHistoryColumns.NAME, null, values);
                    }

                    database.setTransactionSuccessful();
                } finally {
                    database.endTransaction();
                }
            }
        }
    }

    public ArrayList<MusicPlaybackTrack> getQueue() {
        ArrayList<MusicPlaybackTrack> results = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = mDatabaseHelper.getReadableDatabase().query(PlaybackQueueColumns.NAME, null,
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                results.ensureCapacity(cursor.getCount());

                do {
//                    results.add(new MusicPlaybackTrack(cursor.getLong(0), cursor.getLong(1),
//                            FunMusicUtil.IdType.getTypeById(cursor.getInt(2)), cursor.getInt(3)));

                    results.add(new MusicPlaybackTrack(cursor.getLong(0), cursor.getInt(1)));

                } while (cursor.moveToNext());
            }

            return results;
        } finally {
            if (cursor != null) {
                cursor.close();
//                cursor = null;
            }
        }
    }

    public LinkedList<Integer> getHistory(final int playlistSize) {
        LinkedList<Integer> results = new LinkedList<>();

        Cursor cursor = null;
        try {
            cursor = mDatabaseHelper.getReadableDatabase().query(PlaybackHistoryColumns.NAME, null,
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int pos = cursor.getInt(0);
                    if (pos >= 0 && pos < playlistSize) {
                        results.add(pos);
                    }
                } while (cursor.moveToNext());
            }

            return results;
        } finally {
            if (cursor != null) {
                cursor.close();
//                cursor = null;
            }
        }
    }

    public class PlaybackQueueColumns {

        public static final String NAME = "playback_queue";
        public static final String TRACK_ID = "track_id";
//        public static final String SOURCE_ID = "source_id";
//        public static final String SOURCE_TYPE = "source_type";
        public static final String SOURCE_POSITION = "source_position";
    }

    public class PlaybackHistoryColumns {

        public static final String NAME = "playback_history";

        public static final String POSITION = "position";
    }
}
