package com.charles.funmusic.service;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AlbumColumns;
import android.provider.MediaStore.Audio.Albums;
import android.util.Log;
import android.widget.Toast;

import com.charles.funmusic.IFunMusicService;
import com.charles.funmusic.R;
import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.helper.MusicPlaybackTrack;
import com.charles.funmusic.model.Music;

import java.util.Arrays;
import java.util.HashMap;
import java.util.WeakHashMap;

public class MusicPlayer {
    private static final WeakHashMap<Context, ServiceBinder> sConnectionMap;
    private static final long[] sEmptyList;
    public static IFunMusicService sService = null;
    private static ContentValues[] sContentValuesCache = null;

    static {
        sConnectionMap = new WeakHashMap<>();
        sEmptyList = new long[0];
    }

    public static ServiceToken bindToService(final Context context, final ServiceConnection callback) {
        Activity realActivity = ((Activity) context).getParent();
        if (realActivity == null) {
            realActivity = (Activity) context;
        }
        final ContextWrapper contextWrapper = new ContextWrapper(realActivity);
        contextWrapper.startService(new Intent(contextWrapper, MusicService.class));
        final ServiceBinder binder = new ServiceBinder(callback, contextWrapper.getApplicationContext());
        if (contextWrapper.bindService(new Intent().setClass(contextWrapper, MusicService.class),
                binder, 0)) {
            sConnectionMap.put(contextWrapper, binder);
            return new ServiceToken(contextWrapper);
        }
        return null;
    }

    public static void unbindFromService(final ServiceToken token) {
        if (token == null) {
            return;
        }
        final ContextWrapper contextWrapper = token.mWrappedContext;
        final ServiceBinder binder = sConnectionMap.remove(contextWrapper);
        if (binder == null) {
            return;
        }
        contextWrapper.unbindService(binder);
        if (sConnectionMap.isEmpty()) {
            sService = null;
        }
    }

    public static boolean isPlaybackServiceConnected() {
        return sService != null;
    }

    public static void initPlaybackServiceWithSettings(final Context context) {
        setShowAlbumArtOnLockScreen(true);
    }

    private static void setShowAlbumArtOnLockScreen(final boolean enabled) {
        try {
            if (sService != null) {
                sService.setLockScreenAlbumArt(enabled);
            }
        } catch (RemoteException ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * 下一首
     */
    public static void next() {
        try {
            if (sService != null) {
                sService.next();
            }
        } catch (final RemoteException ignored) {
            ignored.printStackTrace();
        }
    }

    public static void asyncNext(final Context context) {
        final Intent asyncNext = new Intent(context, MusicService.class);
        asyncNext.setAction(MusicService.ACTION_NEXT);
        context.startService(asyncNext);
    }

    /**
     * 上一首
     */
    public static void previous(final Context context, final boolean force) {
        final Intent previous = new Intent(context, MusicService.class);
        if (force) {
            previous.setAction(MusicService.ACTION_PREVIOUS_FORCE);
        } else {
            previous.setAction(MusicService.ACTION_PREVIOUS);
        }
        context.startService(previous);
    }

    /**
     * 播放或者暂停
     */
    public static void playOrPause() {
        try {
            if (sService != null) {
                if (sService.isPlaying()) {
                    sService.pause();
                } else {
                    sService.play();
                }
            }
        } catch (RemoteException ignored) {
            ignored.printStackTrace();
        }
    }

    public static boolean isTrackLocal() {
        try {
            if (sService != null) {
                return sService.isTrackLocal();
            }
        } catch (RemoteException ignored) {
            ignored.printStackTrace();
        }
        return false;
    }

    /**
     * 重复模式
     */
    public static void cycleRepeat() {
        try {
            if (sService != null) {
                if (sService.getShuffleMode() == MusicService.SHUFFLE_NORMAL) {
                    sService.setShuffleMode(MusicService.SHUFFLE_NONE);
                    sService.setRepeatMode(MusicService.REPEAT_CURRENT);
                } else {
                    switch (sService.getRepeatMode()) {
                        case MusicService.REPEAT_CURRENT:
                            sService.setRepeatMode(MusicService.REPEAT_CURRENT);
                            break;

                        case MusicService.REPEAT_ALL:
                            sService.setShuffleMode(MusicService.REPEAT_CURRENT);
                            if (sService.getShuffleMode() != MusicService.SHUFFLE_NONE) {
                                sService.setShuffleMode(MusicService.SHUFFLE_NONE);
                            }
                            break;

                        default:
                            sService.setRepeatMode(MusicService.REPEAT_NONE);
                            break;
                    }
                }
            }
        } catch (RemoteException ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * 随机模式
     */
    public static void cycleShuffle() {
        try {
            if (sService != null) {
                switch (sService.getShuffleMode()) {
                    case MusicService.SHUFFLE_NONE:
                        sService.setShuffleMode(MusicService.SHUFFLE_NORMAL);
                        if (sService.getRepeatMode() == MusicService.REPEAT_CURRENT) {
                            sService.setRepeatMode(MusicService.REPEAT_ALL);
                        }
                        break;

                    case MusicService.SHUFFLE_NORMAL:
                        sService.setShuffleMode(MusicService.SHUFFLE_NONE);
                        break;

                    case MusicService.SHUFFLE_AUTO:
                        sService.setShuffleMode(MusicService.SHUFFLE_NONE);
                        break;

                    default:
                        break;
                }
            }
        } catch (RemoteException ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * 是否正在播放
     */
    public static boolean isPlaying() {
        if (sService != null) {
            try {
                return sService.isPlaying();
            } catch (RemoteException ignored) {
                ignored.printStackTrace();
            }
        }
        return false;
    }

    public static int getShuffleMode() {
        if (sService != null) {
            try {
                return sService.getShuffleMode();
            } catch (RemoteException ignored) {
                ignored.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * 设置随机模式
     */
    public static void setShuffleMode(int mode) {
        try {
            if (sService != null) {
                sService.setShuffleMode(mode);
            }
        } catch (RemoteException ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * 获取重复模式
     */
    public static int getRepeatMode() {
        if (sService != null) {
            try {
                return sService.getRepeatMode();
            } catch (RemoteException ignored) {
                ignored.printStackTrace();
            }
        }
        return 0;
    }

    public static String getTrackName() {
        if (sService != null) {
            try {
                return sService.getTrackName();
            } catch (RemoteException ignored) {
                ignored.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 获取歌手名
     */
    public static String getArtist() {
        if (sService != null) {
            try {
                return sService.getArtistName();
            } catch (RemoteException ignored) {
                ignored.printStackTrace();
            }
        }
        return AppCache.getContext().getString(R.string.unknown_artist);
    }

    /**
     * 获取专辑名
     */
    public static String getAlbum() {
        if (sService != null) {
            try {
                return sService.getAlbumName();
            } catch (RemoteException ignored) {
                ignored.printStackTrace();
            }
        }
        return AppCache.getContext().getString(R.string.unknown_album);
    }

    public static String getAlbumPath() {
        if (sService != null) {
            try {
                return sService.getAlbumPath();
            } catch (RemoteException ignored) {
                ignored.printStackTrace();
            }
        }
        return null;
    }

    public static String[] getAlbumPathAll() {
        if (sService != null) {
            try {
                return sService.getAlbumPathAll();
            } catch (RemoteException ignored) {
                ignored.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 获取当前专辑的 id
     */
    public static long getCurrentAlbumId() {
        if (sService != null) {
            try {
                return sService.getAlbumId();
            } catch (RemoteException ignored) {
                ignored.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * 获取当前音源的 id
     */
    public static long getCurrentAudioId() {
        if (sService != null) {
            try {
                return sService.getAudioId();
            } catch (RemoteException ignored) {
                ignored.printStackTrace();
            }
        }
        return -1;
    }

    public static MusicPlaybackTrack getCurrentTrack() {
        if (sService != null) {
            try {
                return sService.getCurrentTrack();
            } catch (RemoteException ignored) {
                ignored.printStackTrace();
            }
        }
        return null;
    }

    public static MusicPlaybackTrack getTrack(int index) {
        if (sService != null) {
            try {
                return sService.getTrack(index);
            } catch (RemoteException ignored) {
                ignored.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 获取下一首音源 id
     */
    public static long getNextAudioId() {
        if (sService != null) {
            try {
                return sService.getNextAudioId();
            } catch (RemoteException ignored) {
                ignored.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * 获取上一首音源 id
     */
    public static long getPreviousAudioId() {
        if (sService != null) {
            try {
                return sService.getPreviousAudioId();
            } catch (RemoteException ignored) {
                ignored.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * 获取当前歌手 id
     */
    public static long getCurrentArtistId() {
        if (sService != null) {
            try {
                return sService.getArtistId();
            } catch (RemoteException ignored) {
                ignored.printStackTrace();
            }
        }
        return -1;
    }

    public static int getAudioSessionId() {
        if (sService != null) {
            try {
                return sService.getAudioSessionId();
            } catch (RemoteException ignored) {
                ignored.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * 获取列表
     */
    public static long[] getQueue() {
        try {
            if (sService != null) {
                return sService.getQueue();
            }
        } catch (RemoteException ignored) {
            ignored.printStackTrace();
        }
        return sEmptyList;
    }

    /**
     * 获取列表中特定位置上的内容
     */
    public static long getQueueItemAtPosition(int position) {
        try {
            if (sService != null) {
                return sService.getQueueItemAtPosition(position);
            }
        } catch (RemoteException ignored) {
            ignored.printStackTrace();
        }
        return -1;
    }

    /**
     * 获取列表的大小
     */
    public static int getQueueSize() {
        try {
            if (sService != null) {
                return sService.getQueueSize();
            }
        } catch (RemoteException ignored) {
            ignored.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取列表的位置
     */
    public static int getQueuePosition() {
        try {
            if (sService != null) {
                return sService.getQueuePosition();
            }
        } catch (RemoteException ignored) {
            ignored.printStackTrace();
        }
        return 0;
    }

    /**
     * 设置列表的位置
     */
    public static void setQueuePosition(int position) {
        if (sService != null) {
            try {
                sService.setQueuePosition(position);
            } catch (RemoteException ignored) {
                ignored.printStackTrace();
            }
        }
    }

    public static HashMap<Long, Music> getPlayInfos() {
        try {
            if (sService != null) {
                return (HashMap<Long, Music>) sService.getPlayInfos();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 刷新
     */
    public static void refresh() {
        try {
            if (sService != null) {
                sService.refresh();
            }
        } catch (RemoteException ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * 获取搜索历史大小
     */
    public static int getQueueHistorySize() {
        if (sService != null) {
            try {
                return sService.getQueueHistorySize();
            } catch (RemoteException ignored) {
                ignored.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * 获取搜索历史位置
     */
    public static int getQueueHistoryPosition(int position) {
        if (sService != null) {
            try {
                return sService.getQueueHistoryPosition(position);
            } catch (final RemoteException ignored) {
            }
        }
        return -1;
    }

    /**
     * 获取搜索历史列表
     */
    public static int[] getQueueHistoryList() {
        if (sService != null) {
            try {
                return sService.getQueueHistoryList();
            } catch (final RemoteException ignored) {
            }
        }
        return null;
    }

    public static int removeTrack(long id) {
        try {
            if (sService != null) {
                return sService.removeTrack(id);
            }
        } catch (RemoteException ignored) {
            ignored.printStackTrace();
        }
        return 0;
    }

    public static boolean removeTrackAtPosition(long id, int position) {
        try {
            if (sService != null) {
                return sService.removeTrackAtPosition(id, position);
            }
        } catch (RemoteException ignored) {
            ignored.printStackTrace();
        }
        return false;
    }

    public static void moveQueueItem(int from, int to) {
        try {
            if (sService != null) {
                sService.moveQueueItem(from, to);
            }
        } catch (RemoteException ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * 全部播放
     */
    public static synchronized void playAll(final HashMap<Long, Music> maps, final long[] musics,
                                            int position, final boolean forceShuffle) {
        if (musics == null || musics.length == 0 || sService == null) {
            return;
        }
        try {
            if (forceShuffle) {
                sService.setShuffleMode(MusicService.SHUFFLE_NORMAL);
            }
            final long currentId = sService.getAudioId();
            long playId = musics[position];
            Log.e("currentId", currentId + "");
            final int currentQueuePosition = getQueuePosition();
            if (position != -1) {
                final long[] playlist = getQueue();
                if (Arrays.equals(musics, playlist)) {
                    if (currentQueuePosition == position && currentId == musics[position]) {
                        sService.play();
                        return;
                    } else {
                        sService.setQueuePosition(position);
                        return;
                    }
                }
            }
            if (position < 0) {
                position = 0;
            }
            sService.open(maps, musics, forceShuffle ? -1 : position);
            sService.play();
            Log.e("time", System.currentTimeMillis() + "");
        } catch (final RemoteException ignored) {
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * 下一首播放
     */
    public static void playNext(Context context, final HashMap<Long, Music> map, final long[] list) {
        if (sService == null) {
            return;
        }
        try {
            for (long aList : list) {
                if (MusicPlayer.getCurrentAudioId() == aList) {
                    Log.i("MusicPlayer", "正在播放");
                } else {
                    MusicPlayer.removeTrack(aList);
                }
            }

            sService.enqueue(list, map, MusicService.NEXT);

            Toast.makeText(context, R.string.added_to_next_play, Toast.LENGTH_SHORT).show();
        } catch (RemoteException ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * 获取歌曲url
     */
    public static String getPath() {
        if (sService == null) {
            return null;
        }
        try {
            return sService.getPath();
        } catch (RemoteException ignored) {
            ignored.printStackTrace();
        }
        return null;
    }

    /**
     * 停止播放
     */
    public static void stop() {
        try {
            sService.stop();
        } catch (RemoteException ignored) {
            ignored.printStackTrace();
        }
    }

    public static int getSongCountForAlbumInt(Context context, long id) {
        int songCount = 0;
        if (id == -1) {
            return songCount;
        }

        Uri uri = ContentUris.withAppendedId(Albums.EXTERNAL_CONTENT_URI, id);
        Cursor cursor = context.getContentResolver().query(
                uri,
                new String[]{
                        AlbumColumns.NUMBER_OF_SONGS
                },
                null, null, null
        );
        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                if (!cursor.isNull(0)) {
                    songCount = cursor.getInt(0);
                }
            }
            cursor.close();
        }

        return songCount;
    }

    public static String getReleaseDateForAlbum(Context context, long id) {
        if (id == -1) {
            return null;
        }
        Uri uri = ContentUris.withAppendedId(Albums.EXTERNAL_CONTENT_URI, id);
        Cursor cursor = context.getContentResolver().query(
                uri,
                new String[]{
                        AlbumColumns.FIRST_YEAR
                },
                null, null, null
        );
        String releaseDate = null;
        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                releaseDate = cursor.getString(0);
            }
            cursor.close();
        }
        return releaseDate;
    }

    public static void seek(long position) {
        if (sService != null) {
            try {
                sService.seek(position);
            } catch (final RemoteException ignored) {
            }
        }
    }

    public static void seekRelative(long deltaInMs) {
        if (sService != null) {
            try {
                sService.seekRelative(deltaInMs);
            } catch (final RemoteException ignored) {
            } catch (final IllegalStateException ignored) {
                ignored.printStackTrace();
            }
        }
    }

    public static long position() {
        if (sService != null) {
            try {
                return sService.position();
            } catch (final RemoteException ignored) {
            } catch (final IllegalStateException ignored) {
                ignored.printStackTrace();
            }
        }
        return 0;
    }

    public static int secondPosition() {
        if (sService != null) {
            try {
                return sService.secondPosition();
            } catch (final RemoteException ignored) {
            } catch (final IllegalStateException ignored) {
                ignored.printStackTrace();
            }
        }
        return 0;
    }

    public static long duration() {
        if (sService != null) {
            try {
                return sService.duration();
            } catch (final RemoteException ignored) {
            } catch (final IllegalStateException ignored) {
                ignored.printStackTrace();
            }
        }
        return 0;
    }

    public static void clearQueue() {

        try {
            if(sService != null) {
                sService.removeTracks(0, Integer.MAX_VALUE);
            }
        } catch (final RemoteException ignored) {
        }
    }

    public static void addToQueue(Context context, long[] list, long sourceId) {
        if (sService == null) {
            return;
        }
        try {
            sService.enqueue(list, null, MusicService.LAST);
        } catch (RemoteException ignored) {
            ignored.printStackTrace();
        }
    }


    public static void addToPlaylist(final Context context, final long[] ids, final long playlistId) {
        final int size = ids.length;
        final ContentResolver resolver = context.getContentResolver();
        final String[] projection = new String[]{
                "max(" + "play_order" + ")",
        };
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        Cursor cursor = null;
        int base = 0;

        try {
            cursor = resolver.query(uri, projection, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                base = cursor.getInt(0) + 1;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        int numInserted = 0;
        for (int offSet = 0; offSet < size; offSet += 1000) {
            makeInsertItems(ids, offSet, 1000, base);
            numInserted += resolver.bulkInsert(uri, sContentValuesCache);
        }
        String message = context.getResources().getQuantityString(
                R.plurals.NNNtrackstoplaylist, numInserted, numInserted);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private static void makeInsertItems(long[] ids, int offset, int len, int base) {
        if (offset + len > ids.length) {
            len = ids.length - offset;
        }

        if (sContentValuesCache == null || sContentValuesCache.length != len) {
            sContentValuesCache = new ContentValues[len];
        }
        for (int i = 0; i < len; i++) {
            if (sContentValuesCache[i] == null) {
                sContentValuesCache[i] = new ContentValues();
            }
            sContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + offset + i);
            sContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, ids[offset + i]);
        }
    }

    public static long createPlaylist(Context context, String name) {
        if (name != null && name.length() > 0) {
            final ContentResolver resolver = context.getContentResolver();
            final String[] projection = new String[]{
                    MediaStore.Audio.PlaylistsColumns.NAME
            };
            final String selection = MediaStore.Audio.PlaylistsColumns.NAME + " = '" + name + "'";
            Cursor cursor = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    projection, selection, null, null);
            if (cursor != null && cursor.getCount() <= 0) {
                final ContentValues values = new ContentValues(1);
                values.put(MediaStore.Audio.PlaylistsColumns.NAME, name);
                final Uri uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                        values);
                if (uri != null) {
                    return Long.parseLong(uri.getLastPathSegment());
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            return -1;
        }
        return -1;
    }

    public static void openFile(final String path) {
        if (sService != null) {
            try {
                sService.openFile(path);
            } catch (RemoteException ignored) {
                ignored.printStackTrace();
            }
        }
    }

    public static void timer(int time) {
        if (sService == null) {
            return;
        }
        try {
            sService.timer(time);
        } catch (RemoteException ignored) {
            ignored.printStackTrace();
        }
    }

    private static final class ServiceBinder implements ServiceConnection {

        private final ServiceConnection mCallback;
        private final Context mContext;

        ServiceBinder(final ServiceConnection callback, final Context context) {
            mCallback = callback;
            mContext = context;
        }

        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            sService = IFunMusicService.Stub.asInterface(service);
            if (mCallback != null) {
                mCallback.onServiceConnected(name, service);
            }
            initPlaybackServiceWithSettings(mContext);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(name);
            }
            sService = null;
        }
    }

    public static final class ServiceToken {
        ContextWrapper mWrappedContext;

        ServiceToken(final ContextWrapper context) {
            mWrappedContext = context;
        }
    }
}