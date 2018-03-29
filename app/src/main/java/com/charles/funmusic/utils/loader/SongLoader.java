package com.charles.funmusic.utils.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.Audio.Playlists.Members;
import android.text.TextUtils;
import android.view.GestureDetector;

import com.charles.funmusic.model.Song;
import com.charles.funmusic.utils.Preferences;

import java.util.ArrayList;

public class SongLoader {

    private static final long[] sEmptyList = new long[0];

    public static ArrayList<Song> getSongsForCursor(Cursor cursor) {
        ArrayList arrayList = new ArrayList();
        if ((cursor != null) && (cursor.moveToFirst())) {
            do {
                long id = cursor.getLong(0);
                String title = cursor.getString(1);
                String artist = cursor.getString(2);
                String album = cursor.getString(3);
                int duration = cursor.getInt(4);
                int trackNumber = cursor.getInt(5);
                long artistId = cursor.getLong(6);
                long albumId = cursor.getLong(7);

                arrayList.add(new Song(id, albumId, artistId, title, artist, album, duration, trackNumber));
            } while (cursor.moveToNext());
            if (cursor != null) {
                cursor.close();
            }
        }
        return arrayList;
    }

    public static Song getSongForCursor(Cursor cursor) {
        Song song = new Song();
        if ((cursor != null) && (cursor.moveToFirst())) {
            long id = cursor.getLong(0);
            String title = cursor.getString(1);
            String artist = cursor.getString(2);
            String album = cursor.getString(3);
            int duration = cursor.getInt(4);
            int trackNumber = cursor.getInt(5);
            long artistId = cursor.getInt(6);
            long albumId = cursor.getLong(7);

            song = new Song(id, albumId, artistId, title, artist, album, duration, trackNumber);
        }

        if (cursor != null) {
            cursor.close();
        }
        return song;
    }

    public static long[] getSongListForCursor(Cursor cursor) {
        if (cursor == null) {
            return sEmptyList;
        }
        final int len = cursor.getCount();
        final long[] list = new long[len];
        cursor.moveToFirst();
        int columnIndex;
        try {
            columnIndex = cursor.getColumnIndexOrThrow(Members.AUDIO_ID);
        } catch (final IllegalArgumentException notPlaylist) {
            columnIndex = cursor.getColumnIndexOrThrow(BaseColumns._ID);
        }
        for (int i = 0; i < len; i++) {
            list[i] = cursor.getLong(columnIndex);
            cursor.moveToNext();
        }
        cursor.close();
        return list;
    }

    public static ArrayList<Song> getAllSongs(Context context) {
        return getSongsForCursor(makeSongCursor(context, null, null));
    }

    public static Song getSongForId(Context context, long id) {
        return getSongForCursor(makeSongCursor(context, "_id=" + String.valueOf(id), null));
    }

    public static ArrayList<Song> searchSongs(Context context, String searchString) {
        return getSongsForCursor(makeSongCursor(context, "title LIKE ?", new String[]{"%" + searchString + "%"}));
    }

    public static Cursor makeSongCursor(Context context, String selection, String[] paramArrayOfString) {
        String selectionStatement = "is_music=1 AND title != ''";
        final String songSortOrder = Preferences.getInstance(context).getSongSortOrder();

        if (!TextUtils.isEmpty(selection)) {
            selectionStatement = selectionStatement + " AND " + selection;
        }
        return context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        "_id", "title", "artist", "album",
                        "duration", "track", "artist_id", "album_id"
                }, selectionStatement, paramArrayOfString, songSortOrder);
    }
}