package com.charles.funmusic.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.helper.MusicPlaybackTrack;
import com.charles.funmusic.model.Music;
import com.charles.funmusic.utils.MusicUtil;

import java.util.ArrayList;

public class PlaylistManager {
    private static PlaylistManager sInstance = null;

    private DatabaseHelper mDatabaseHelper = null;

    public PlaylistManager(final Context context) {
        mDatabaseHelper = DatabaseHelper.getInstance(context);
    }

    public static synchronized PlaylistManager getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new PlaylistManager(context.getApplicationContext());
        }
        return sInstance;
    }

    //建立播放列表表设置播放列表id和歌曲id为复合主键
    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + PlaylistColumns.NAME + " ("
                + PlaylistColumns.PLAYLIST_ID + " LONG NOT NULL," + PlaylistColumns.TRACK_ID + " LONG NOT NULL,"
                + PlaylistColumns.TRACK_NAME + " CHAR NOT NULL," + PlaylistColumns.ALBUM_ID + " LONG,"
                + PlaylistColumns.ALBUM_NAME + " CHAR," + PlaylistColumns.ALBUM_ART + " CHAR,"
                + PlaylistColumns.ARTIST_ID + " LONG," + PlaylistColumns.ARTIST_NAME + " CHAR,"
                + PlaylistColumns.IS_LOCAL + " BOOLEAN ," + PlaylistColumns.PATH + " CHAR,"
                + PlaylistColumns.TRACK_ORDER + " LONG NOT NULL, primary key ( " + PlaylistColumns.PLAYLIST_ID
                + ", " + PlaylistColumns.TRACK_ID + "));");
    }

    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PlaylistColumns.NAME);
        onCreate(db);
    }

    public synchronized void insert(Context context, long playlistId, long id, int order) {
        ArrayList<MusicPlaybackTrack> m = getPlaylist(playlistId);
        for (int i = 0; i < m.size(); i++) {
            if (m.get(i).getId() == id)
                return;
        }

        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(3);
            values.put(PlaylistColumns.PLAYLIST_ID, playlistId);
            values.put(PlaylistColumns.TRACK_ID, id);
            values.put(PlaylistColumns.TRACK_ORDER, getPlaylist(playlistId).size());
            database.insert(PlaylistColumns.NAME, null, values);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        PlaylistInfo playlistInfo = PlaylistInfo.getInstance(context);
        playlistInfo.update(playlistId, getPlaylist(playlistId).size());

    }

    public synchronized void insertMusic(Context context, long playlistId, Music music) {
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(11);
            values.put(PlaylistColumns.PLAYLIST_ID, playlistId);
            values.put(PlaylistColumns.TRACK_ID, music.getId());
            values.put(PlaylistColumns.TRACK_ORDER, getPlaylist(playlistId).size());
            values.put(PlaylistColumns.TRACK_NAME, music.getTitle());
            values.put(PlaylistColumns.ALBUM_ID, music.getAlbumId());
            values.put(PlaylistColumns.ALBUM_NAME, music.getAlbum());
            values.put(PlaylistColumns.ALBUM_ART, music.getAlbumArt());
            values.put(PlaylistColumns.ARTIST_NAME, music.getArtist());
            values.put(PlaylistColumns.ARTIST_ID, music.getArtistId());
            values.put(PlaylistColumns.PATH, music.getUrl());
            values.put(PlaylistColumns.IS_LOCAL, music.isLocal());
            database.insertWithOnConflict(PlaylistColumns.NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        PlaylistInfo playlistInfo = PlaylistInfo.getInstance(context);
        String albumArt = music.getAlbumArt();
        if (music.isLocal()) {
            if (albumArt.equals(MusicUtil.getAlbumArt(AppCache.getContext(), music.getId()))) {
                playlistInfo.update(playlistId, getPlaylist(playlistId).size(), music.getAlbumArt());
            } else {
                playlistInfo.update(playlistId, getPlaylist(playlistId).size());
            }
        } else if (!albumArt.isEmpty()) {
            playlistInfo.update(playlistId, getPlaylist(playlistId).size(), music.getAlbumArt());
        } else {
            playlistInfo.update(playlistId, getPlaylist(playlistId).size());
        }
    }

    public synchronized void insertLists(Context context, long playlistId, ArrayList<Music> musics) {
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.beginTransaction();
        int len = musics.size();
        try {
            for (int i = 0; i < len; i++) {
                Music music = musics.get(i);
                ContentValues values = new ContentValues(11);
                values.put(PlaylistColumns.PLAYLIST_ID, playlistId);
                values.put(PlaylistColumns.TRACK_ID, music.getId());
                values.put(PlaylistColumns.TRACK_ORDER, getPlaylist(playlistId).size());
                values.put(PlaylistColumns.TRACK_NAME, music.getTitle());
                values.put(PlaylistColumns.ALBUM_ID, music.getAlbumId());
                values.put(PlaylistColumns.ALBUM_NAME, music.getAlbum());
                values.put(PlaylistColumns.ALBUM_ART, music.getAlbumArt());
                values.put(PlaylistColumns.ARTIST_NAME, music.getArtist());
                values.put(PlaylistColumns.ARTIST_ID, music.getArtistId());
                values.put(PlaylistColumns.PATH, music.getUrl());
                values.put(PlaylistColumns.IS_LOCAL, music.isLocal());
                database.insertWithOnConflict(PlaylistColumns.NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }

            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }

        PlaylistInfo playlistInfo = PlaylistInfo.getInstance(context);
        String albumArt = null;
        for (int i = len - 1; i >= 0; i--) {
            Music music = musics.get(i);
            albumArt = music.getAlbumArt();
            if (music.isLocal()) {
                String art = MusicUtil.getAlbumArt(AppCache.getContext(), music.getId());
                if (art != null) {
                    break;
                } else {
                    albumArt = null;
                }
            } else if (!albumArt.isEmpty()) {
                break;
            }

        }
        if (albumArt != null) {
            playlistInfo.update(playlistId, getPlaylist(playlistId).size(), albumArt);
        } else {
            playlistInfo.update(playlistId, getPlaylist(playlistId).size());
        }
    }

    public synchronized void update(long playlistId, long id, int order) {

        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(1);
            values.put(PlaylistColumns.TRACK_ORDER, order);
            database.update(PlaylistColumns.NAME, values, PlaylistColumns.PLAYLIST_ID + " = ?" + " AND " +
                    PlaylistColumns.TRACK_ID + " = ?", new String[]{playlistId + "", id + ""});
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public synchronized void update(long playlistId, long[] ids, int[] order) {

        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.beginTransaction();
        try {
            for (int i = 0; i < order.length; i++) {
                ContentValues values = new ContentValues(1);
                values.put(PlaylistColumns.TRACK_ORDER, order[i]);
                database.update(PlaylistColumns.NAME, values, PlaylistColumns.PLAYLIST_ID + " = ?" + " AND " +
                        PlaylistColumns.TRACK_ID + " = ?", new String[]{playlistId + "", ids[i] + ""});
            }

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public void removeItem(Context context, final long playlistId, long songId) {
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.delete(PlaylistColumns.NAME, PlaylistColumns.PLAYLIST_ID + " = ?" + " AND " + PlaylistColumns.TRACK_ID + " = ?", new String[]{
                String.valueOf(playlistId), String.valueOf(songId)
        });

        PlaylistInfo playlistInfo = PlaylistInfo.getInstance(context);
        playlistInfo.update(playlistId, getPlaylist(playlistId).size());
    }

    public void delete(final long playlistId) {
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.delete(PlaylistColumns.NAME, PlaylistColumns.PLAYLIST_ID + " = ?", new String[]
                {String.valueOf(playlistId)});
    }

    public synchronized void deleteMusicInfo(Context context, final long playlistId, final long musicid) {
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();

        Cursor cursor = null;
        try {
            cursor = mDatabaseHelper.getReadableDatabase().query(PlaylistColumns.NAME, null,
                    PlaylistColumns.PLAYLIST_ID + " = ? and" + PlaylistColumns.TRACK_ID + " = ?", new String[]{
                            String.valueOf(playlistId), String.valueOf(musicid)
                    }, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                long[] deletedPlaylistIds = new long[cursor.getCount()];
                int i = 0;

                do {
                    deletedPlaylistIds[i] = cursor.getLong(0);
                    i++;
                } while (cursor.moveToNext());

                PlaylistInfo.getInstance(context).updatePlaylistMusicCount(deletedPlaylistIds);
            }


        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    //删除播放列表中的记录的音乐 ，删除本地文件时调用
    public synchronized void deleteMusic(Context context, final long musicId) {
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();

        Cursor cursor = null;
        try {
            cursor = mDatabaseHelper.getReadableDatabase().query(PlaylistColumns.NAME, null,
                    PlaylistColumns.TRACK_ID + " = " + String.valueOf(musicId), null, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                long[] deletedPlaylistIds = new long[cursor.getCount()];
                int i = 0;

                do {
                    deletedPlaylistIds[i] = cursor.getLong(0);
                    i++;
                } while (cursor.moveToNext());

                PlaylistInfo.getInstance(context).updatePlaylistMusicCount(deletedPlaylistIds);
            }

        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        database.delete(PlaylistColumns.NAME, PlaylistColumns.TRACK_ID + " = ?", new String[]
                {String.valueOf(musicId)});
    }

    public void deleteAll() {
        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.delete(PlaylistColumns.NAME, null, null);
    }

    public long[] getPlaylistIds(final long playlistId) {
        long[] results = null;

        Cursor cursor = null;
        try {
            cursor = mDatabaseHelper.getReadableDatabase().query(PlaylistColumns.NAME, null,
                    PlaylistColumns.PLAYLIST_ID + " = " + String.valueOf(playlistId), null, null, null, PlaylistColumns.TRACK_ORDER + " ASC ", null);
            if (cursor != null) {
                int len = cursor.getCount();
                results = new long[len];
                if (cursor.moveToFirst()) {
                    for (int i = 0; i < len; i++) {
                        results[i] = cursor.getLong(1);
                        cursor.moveToNext();
                    }

                }
            }

            return results;

        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public ArrayList<MusicPlaybackTrack> getPlaylist(final long playlistId) {
        ArrayList<MusicPlaybackTrack> results = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = mDatabaseHelper.getReadableDatabase().query(PlaylistColumns.NAME, null,
                    PlaylistColumns.PLAYLIST_ID + " = " + String.valueOf(playlistId), null, null, null, PlaylistColumns.TRACK_ORDER + " ASC ", null);
            if (cursor != null && cursor.moveToFirst()) {
                results.ensureCapacity(cursor.getCount());

                do {
                    results.add(new MusicPlaybackTrack(cursor.getLong(1), cursor.getInt(0)));
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

    public ArrayList<Music> getMusics(final long playlistId) {
        ArrayList<Music> results = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = mDatabaseHelper.getReadableDatabase().query(PlaylistColumns.NAME, null,
                    PlaylistColumns.PLAYLIST_ID + " = " + String.valueOf(playlistId), null, null, null, PlaylistColumns.TRACK_ORDER + " ASC ", null);
            if (cursor != null && cursor.moveToFirst()) {
                results.ensureCapacity(cursor.getCount());

                do {
                    Music music = new Music();
                    music.setId(cursor.getLong(1));
                    music.setTitle(cursor.getString(2));
                    music.setAlbumId(cursor.getInt(3));
                    music.setAlbum(cursor.getString(4));
                    music.setAlbumArt(cursor.getString(5));
                    music.setArtistId(cursor.getLong(6));
                    music.setArtist(cursor.getString(7));
                    music.setLocal(cursor.getInt(8) > 0);
                    results.add(music);
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

    public interface PlaylistColumns {
        /* Table name */
        String NAME = "play_lists";

        /* Album IDs column */
        String PLAYLIST_ID = "playlist_id";

        /* Time played column */
        String TRACK_ID = "track_id";

        String TRACK_ORDER = "track_order";

        String TRACK_NAME = "track_name";

        String ARTIST_NAME = "artist_name";

        String ARTIST_ID = "artist_id";

        String ALBUM_NAME = "album_name";

        String ALBUM_ID = "album_id";

        String IS_LOCAL = "is_local";

        String PATH = "path";

        String ALBUM_ART = "album_art";
    }
}
