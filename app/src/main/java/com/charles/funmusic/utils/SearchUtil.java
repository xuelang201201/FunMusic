package com.charles.funmusic.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Audio.Media;
import android.text.TextUtils;

import com.charles.funmusic.model.Music;

import java.util.ArrayList;

public class SearchUtil {
    public static ArrayList<Music> searchSongs(Context context, String searchString) {
        return getSongsForCursor(makeSongCursor(context,
                "title LIKE ?",
                new String[]{"%" + searchString + "%"}));
    }

    private static ArrayList<Music> getSongsForCursor(Cursor cursor) {
        ArrayList arrayList = new ArrayList();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Music music = new Music();
                music.setId(cursor.getLong(cursor.getColumnIndex(Media._ID)));
                music.setAlbumId(cursor.getInt(cursor.getColumnIndex(Media.ALBUM_ID)));
                music.setTitle(cursor.getString(cursor.getColumnIndex(Media.TITLE)));
                music.setArtist(cursor.getString(cursor.getColumnIndex(Media.ARTIST)));
                music.setAlbum(cursor.getString(cursor.getColumnIndex(Media.ALBUM)));
                arrayList.add(music);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return arrayList;
    }

    private static Cursor makeSongCursor(Context context, String selection, String[] paramArrayOfString) {
        String selectionStatement = "is_music=1 AND title != ''";
        // final String songSortOrder = PreferencesUtility.getInstance(context).getSongSortOrder();

        if (!TextUtils.isEmpty(selection)) {
            selectionStatement = selectionStatement + " AND " + selection;
        }

        return context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        "_id", "title", "artist", "album",
                        "duration", "track", "artist_id", "album_id"
                }, selectionStatement, paramArrayOfString, null);
    }
}
