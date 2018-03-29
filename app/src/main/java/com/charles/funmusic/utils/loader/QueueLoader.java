package com.charles.funmusic.utils.loader;

import android.content.Context;
import android.util.Log;

import com.charles.funmusic.model.Music;
import com.charles.funmusic.utils.cursor.PlayQueueCursor;

import java.util.ArrayList;

public class QueueLoader {

    public static ArrayList<Music> getQueueSongs(Context context) {

        final ArrayList<Music> musicQueues = new ArrayList<>();
        Log.e("QueueLoader", "created");
        PlayQueueCursor cursor = new PlayQueueCursor(context);

        while (cursor.moveToNext()) {
            Music music = new Music();
            music.setId(cursor.getInt(0));
            music.setAlbum(cursor.getString(4));
            music.setTitle(cursor.getString(1));
            music.setArtist(cursor.getString(2));
        }

        cursor.close();

        return musicQueues;
    }
}
