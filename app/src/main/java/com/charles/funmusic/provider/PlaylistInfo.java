package com.charles.funmusic.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.charles.funmusic.model.Playlist;

import java.util.ArrayList;

public class PlaylistInfo {

    private static PlaylistInfo sInstance = null;

    private DatabaseHelper mMusicDatabase = null;

    private PlaylistInfo(final Context context) {
        mMusicDatabase = DatabaseHelper.getInstance(context);
    }

    public static synchronized PlaylistInfo getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new PlaylistInfo(context.getApplicationContext());
        }

        return sInstance;
    }

    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + PlaylistInfoColumns.NAME + " ("
                + PlaylistInfoColumns.PLAYLIST_ID + " LONG NOT NULL," + PlaylistInfoColumns.PLAYLIST_NAME + " CHAR NOT NULL,"
                + PlaylistInfoColumns.SONG_COUNT + " INT NOT NULL, " + PlaylistInfoColumns.ALBUM_ART + " CHAR, "
                + PlaylistInfoColumns.AUTHOR + " CHAR );");
    }

    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PlaylistInfoColumns.NAME);
        onCreate(db);
    }


    public synchronized void addPlaylist(long playlistId, String name, int count, String albumArt, String author) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(5);
            values.put(PlaylistInfoColumns.PLAYLIST_ID, playlistId);
            values.put(PlaylistInfoColumns.PLAYLIST_NAME, name);
            values.put(PlaylistInfoColumns.SONG_COUNT, count);
            values.put(PlaylistInfoColumns.ALBUM_ART, albumArt);
            values.put(PlaylistInfoColumns.AUTHOR, author);

            database.insert(PlaylistInfoColumns.NAME, null, values);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public synchronized void addPlaylist(ArrayList<Playlist> playLists) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();

        try {
            for (int i = 0; i < playLists.size(); i++) {
                ContentValues values = new ContentValues(5);
                values.put(PlaylistInfoColumns.PLAYLIST_ID, playLists.get(i).getId());
                values.put(PlaylistInfoColumns.PLAYLIST_NAME, playLists.get(i).getName());
                values.put(PlaylistInfoColumns.SONG_COUNT, playLists.get(i).getSongCount());
                values.put(PlaylistInfoColumns.ALBUM_ART, playLists.get(i).getAlbumArt());
                values.put(PlaylistInfoColumns.AUTHOR, playLists.get(i).getAuthor());

                database.insert(PlaylistInfoColumns.NAME, null, values);
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }


    public synchronized void updatePlaylist(long playlistId, int oldCount) {
        ArrayList<Playlist> results = getPlaylist();
        int countt = 0;
        for (int i = 0; i < results.size(); i++) {
            if (results.get(i).getId() == playlistId) {
                countt = results.get(i).getSongCount();
            }
        }
        countt = countt + oldCount;
        update(playlistId, countt);

    }

    public synchronized void update(long playlistid, int count) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(2);
            values.put(PlaylistInfoColumns.PLAYLIST_ID, playlistid);
            //values.put(PlaylistInfoColumns.PLAYLIST_NAME, name);
            values.put(PlaylistInfoColumns.SONG_COUNT, count);
            database.update(PlaylistInfoColumns.NAME, values, PlaylistInfoColumns.PLAYLIST_ID + " = " + playlistid, null);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public synchronized void update(long playlistId, int count, String album) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(3);
            values.put(PlaylistInfoColumns.PLAYLIST_ID, playlistId);
            values.put(PlaylistInfoColumns.SONG_COUNT, count);
            values.put(PlaylistInfoColumns.ALBUM_ART, album);
            database.update(PlaylistInfoColumns.NAME, values, PlaylistInfoColumns.PLAYLIST_ID + " = " + playlistId, null);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    //删除本地文件时更新播放列表歌曲数量信息
    public void updatePlaylistMusicCount(long[] PlaylistId) {

        SQLiteDatabase database = null;

        final StringBuilder selection = new StringBuilder();
        selection.append(PlaylistInfoColumns.PLAYLIST_ID + " IN (");
        for (int i = 0; i < PlaylistId.length; i++) {
            selection.append(PlaylistId[i]);
            if (i < PlaylistId.length - 1) {
                selection.append(",");
            }
        }
        selection.append(")");

        Cursor cursor = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(PlaylistInfoColumns.NAME, null,
                    selection.toString(), null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                database = mMusicDatabase.getWritableDatabase();
                database.beginTransaction();

                do {
                    int count = cursor.getInt(cursor.getColumnIndex(PlaylistInfoColumns.SONG_COUNT)) - 1;
                    long playlistId = cursor.getLong(cursor.getColumnIndex(PlaylistInfoColumns.PLAYLIST_ID));
                    if (count == 0) {
                        database.delete(PlaylistInfoColumns.NAME, PlaylistInfoColumns.PLAYLIST_ID + " = ?", new String[]
                                {String.valueOf(playlistId)});
                    } else {
                        ContentValues values = new ContentValues(2);
                        values.put(PlaylistInfoColumns.PLAYLIST_ID, playlistId);
                        values.put(PlaylistInfoColumns.SONG_COUNT, count);
                        database.update(PlaylistInfoColumns.NAME, values, PlaylistInfoColumns.PLAYLIST_ID + " = " + playlistId, null);
                    }
                    // update(playlistId,count);

                } while (cursor.moveToNext());

                database.setTransactionSuccessful();
            }

        } finally {
            database.endTransaction();
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

    }


    public void deletePlaylist(final long PlaylistId) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(PlaylistInfoColumns.NAME, PlaylistInfoColumns.PLAYLIST_ID + " = ?", new String[]
                {String.valueOf(PlaylistId)});
    }

    public synchronized boolean hasPlaylist(final long PlaylistId) {

        Cursor cursor = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(PlaylistInfoColumns.NAME, null,
                    PlaylistInfoColumns.PLAYLIST_ID + " = ?", new String[]
                            {String.valueOf(PlaylistId)}, null, null, null);

            return cursor != null && cursor.moveToFirst();

        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public synchronized void deletePlaylist(final long[] PlaylistId) {

        final StringBuilder selection = new StringBuilder();
        selection.append(PlaylistInfoColumns.PLAYLIST_ID + " IN (");
        for (int i = 0; i < PlaylistId.length; i++) {
            selection.append(PlaylistId[i]);
            if (i < PlaylistId.length - 1) {
                selection.append(",");
            }
        }
        selection.append(")");


        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(PlaylistInfoColumns.NAME, selection.toString(), null);
    }

    public void deleteAll() {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(PlaylistInfoColumns.NAME, null, null);
    }


    public synchronized ArrayList<Playlist> getPlaylist() {
        ArrayList<Playlist> results = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(PlaylistInfoColumns.NAME, null,
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                results.ensureCapacity(cursor.getCount());

                do {
                    if (cursor.getString(4).equals("local"))
                        results.add(new Playlist(cursor.getLong(0), cursor.getString(1), cursor.getInt(2),
                                cursor.getString(3), cursor.getString(4)));
                } while (cursor.moveToNext());
            }

            return results;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public synchronized ArrayList<Playlist> getNetPlaylist() {
        ArrayList<Playlist> results = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(PlaylistInfoColumns.NAME, null,
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                results.ensureCapacity(cursor.getCount());

                do {
                    if (!cursor.getString(4).equals("local"))
                        results.add(new Playlist(cursor.getLong(0), cursor.getString(1), cursor.getInt(2), cursor.getString(3), cursor.getString(4)));
                } while (cursor.moveToNext());
            }

            return results;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public interface PlaylistInfoColumns {
        /* Table name */
        String NAME = "playlist_info";

        /* Album IDs column */
        String PLAYLIST_ID = "playlist_id";

        /* Time played column */
        String PLAYLIST_NAME = "playlist_name";

        String SONG_COUNT = "count";

        String ALBUM_ART = "album_art";

        String AUTHOR = "author";
    }

}
