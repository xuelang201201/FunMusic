package com.charles.funmusic.utils.cursor;

import android.content.Context;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;

import com.charles.funmusic.service.MusicPlayer;

import java.util.Arrays;

public class PlayQueueCursor extends AbstractCursor {

    private static final String[] PROJECTION = new String[]{
            BaseColumns._ID,
            AudioColumns.TITLE,
            AudioColumns.ARTIST,
            AudioColumns.ALBUM_ID,
            AudioColumns.ALBUM,
            AudioColumns.TRACK,
            AudioColumns.ARTIST_ID,
            AudioColumns.DATA
    };

    private final Context mContext;
    private long[] mNowPlaying;
    private long[] mCursorIndexes;
    private int mSize;
    private Cursor mQueueCursor;

    public PlayQueueCursor(final Context context) {
        mContext = context;
        makeNowPlayingCursor();
    }

    @Override
    public int getCount() {
        return mSize;
    }

    @Override
    public boolean onMove(int oldPosition, int newPosition) {
        if (oldPosition == newPosition) {
            return true;
        }

        if (mNowPlaying == null || mCursorIndexes == null || newPosition >= mNowPlaying.length) {
            return false;
        }

        final long id = mNowPlaying[newPosition];
        final int cursorIndex = Arrays.binarySearch(mCursorIndexes, id);
        mQueueCursor.moveToPosition(cursorIndex);
        return true;
    }

    @Override
    public String[] getColumnNames() {
        return PROJECTION;
    }

    @Override
    public String getString(final int column) {
        try {
            return mQueueCursor.getString(column);
        } catch (final Exception ignored) {
            onChange(true);
            return "";
        }
    }

    @Override
    public short getShort(final int column) {
        return mQueueCursor.getShort(column);
    }

    @Override
    public int getInt(final int column) {
        try {
            return mQueueCursor.getInt(column);
        } catch (final Exception ignored) {
            onChange(true);
            return 0;
        }
    }

    @Override
    public long getLong(int column) {
        try {
            return mQueueCursor.getLong(column);
        } catch (final Exception ignored) {
            onChange(true);
            return 0;
        }
    }

    @Override
    public float getFloat(final int column) {
        return mQueueCursor.getFloat(column);
    }

    @Override
    public double getDouble(int column) {
        return mQueueCursor.getDouble(column);
    }

    @Override
    public int getType(int column) {
        return mQueueCursor.getType(column);
    }

    @Override
    public boolean isNull(int column) {
        return mQueueCursor.isNull(column);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void deactivate() {
        if (mQueueCursor != null) {
            mQueueCursor.deactivate();
        }
    }

    @Override
    public boolean requery() {
        makeNowPlayingCursor();
        return true;
    }

    @Override
    public void close() {
        try {
            if (mQueueCursor != null) {
                mQueueCursor.close();
                mQueueCursor = null;
            }
        } catch (final Exception ignored) {
        }
        super.close();
    }

    private void makeNowPlayingCursor() {
        mQueueCursor = null;
        mNowPlaying = MusicPlayer.getQueue();
        Log.d("queue", Arrays.toString(mNowPlaying) + ": " + mNowPlaying.length);
        mSize = mNowPlaying.length;
        Log.e("size", mSize + "");
        if (mSize == 0) {
            return;
        }

        final StringBuilder selection = new StringBuilder();
        selection.append(Media._ID + " IN (");
        for (int i = 0; i < mSize; i++) {
            selection.append(mNowPlaying[i]);
            if (i < mSize - 1) {
                selection.append(",");
            }
        }
        selection.append(")");

        mQueueCursor = mContext.getContentResolver().query(
                Media.EXTERNAL_CONTENT_URI, PROJECTION,
                selection.toString(), null, Media._ID);
        if (mQueueCursor != null) {
            Log.e("cursor", mQueueCursor.getCount() + "");
        }
        if (mQueueCursor == null) {
            mSize = 0;
            return;
        }

        final int playlistSize = mQueueCursor.getCount();
        mCursorIndexes = new long[playlistSize];
        mQueueCursor.moveToFirst();
        final int columnIndex = mQueueCursor.getColumnIndexOrThrow(Media._ID);
        for (int i = 0; i < playlistSize; i++) {
            mCursorIndexes[i] = mQueueCursor.getLong(columnIndex);
            mQueueCursor.moveToNext();
        }
        mQueueCursor.moveToFirst();

        int removed = 0;
        for (int i = mNowPlaying.length - 1; i >= 0; i--) {
            final long trackId = mNowPlaying[i];
            final int cursorIndex = Arrays.binarySearch(mCursorIndexes, trackId);
            if (cursorIndex < 0) {
                removed += MusicPlayer.removeTrack(trackId);
            }
        }
        if (removed > 0) {
            mNowPlaying = MusicPlayer.getQueue();
            mSize = mNowPlaying.length;
            if (mSize == 0) {
                mCursorIndexes = null;
            }
        }
    }
}