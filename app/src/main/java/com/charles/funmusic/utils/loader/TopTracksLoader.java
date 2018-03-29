package com.charles.funmusic.utils.loader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.charles.funmusic.provider.RecentStore;
import com.charles.funmusic.provider.SongPlayCount;
import com.charles.funmusic.utils.cursor.SortedCursor;

import java.util.ArrayList;

public class TopTracksLoader extends SongLoader {

    private static final int NUMBER_OF_SONGS = 100;
    private static QueryType sQueryType;
    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

    public TopTracksLoader(final Context context, QueryType type) {
        sContext = context;
        sQueryType = type;
    }

    public static Cursor getCursor(Context context, QueryType queryType) {
        sQueryType = queryType;
        sContext = context;
        return getCursor();
    }

    public static int getCount(Context context, QueryType queryType) {
        Cursor cursor = getCursor(context, queryType);
        if (cursor != null) {
            return cursor.getCount();
        } else {
            return 0;
        }
    }

    public static Cursor getCursor() {
        SortedCursor cursor = null;
        if (sQueryType == QueryType.TopTracks) {
            cursor = makeTopTracksCursor(sContext);
        } else if (sQueryType == QueryType.RecentSongs) {
            cursor = makeRecentTracksCursor(sContext);
        }

        if (cursor != null) {
            ArrayList<Long> missingIds = cursor.getMissingIds();
            if (missingIds != null && missingIds.size() > 0) {
                for (long id : missingIds) {
                    if (sQueryType == QueryType.TopTracks) {
                        SongPlayCount.getInstance(sContext).removeItem(id);
                    } else if (sQueryType == QueryType.RecentSongs) {
                        RecentStore.getInstance(sContext).removeItem(id);
                    }
                }
            }
        }
        return cursor;
    }

    private static SortedCursor makeTopTracksCursor(final Context context) {
        Cursor cursor = SongPlayCount.getInstance(context).getTopPlayedResults(NUMBER_OF_SONGS);
        try {
            return makeSortedCursor(context, cursor, cursor.getColumnIndex(
                    SongPlayCount.SongPlayCountColumns.ID));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static SortedCursor makeRecentTracksCursor(final Context context) {
        Cursor cursor = RecentStore.getInstance(context).queryRecentIds(null);

        try {
            return makeSortedCursor(context, cursor, cursor.getColumnIndex(
                    SongPlayCount.SongPlayCountColumns.ID));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static SortedCursor makeSortedCursor(final Context context, final Cursor cursor, final int column) {
        if (cursor != null && cursor.moveToFirst()) {
            StringBuilder selection = new StringBuilder();
            selection.append(BaseColumns._ID);
            selection.append(" IN (");

            long[] order = new long[cursor.getCount()];

            long id = cursor.getLong(column);
            selection.append(id);
            order[cursor.getPosition()] = id;

            while (cursor.moveToNext()) {
                selection.append(",");

                id = cursor.getLong(column);
                order[cursor.getPosition()] = id;
                selection.append(String.valueOf(id));
            }
            selection.append(")");
            Cursor songCursor = makeSongCursor(context, selection.toString(), null);
            if (songCursor != null) {
                return new SortedCursor(songCursor, order, BaseColumns._ID, null);
            }
        }
        return null;
    }

    public enum QueryType {
        TopTracks,
        RecentSongs,
    }
}
