package com.charles.funmusic.utils;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio.AlbumColumns;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.Audio.Media;

import com.charles.funmusic.R;
import com.charles.funmusic.model.Music;
import com.github.promeg.pinyinhelper.Pinyin;

import java.util.ArrayList;
import java.util.List;

/**
 * 歌曲工具类
 */
public class MusicUtil {
    private static final String SELECTION =
            AudioColumns.SIZE + ">= ? AND " + AudioColumns.DURATION + " >= ?";

    /**
     * 扫描歌曲
     *
     * @param context 上下文
     * @return 歌曲合集
     */
    public static List<Music> scanMusic(Context context) {
        List<Music> musics = new ArrayList<>();

//        long filterSize = ParseUtil.parseLong(Preferences.getFilterSize()) * 1024;
//        long filterTime = ParseUtil.parseLong(Preferences.getFilterTime()) * 1000;

        Cursor cursor = context.getContentResolver().query(
                Media.EXTERNAL_CONTENT_URI,
                new String[]{
                        BaseColumns._ID,
                        AudioColumns.IS_MUSIC,
                        AudioColumns.TITLE,
                        AudioColumns.ARTIST,
                        AlbumColumns.ALBUM,
                        AudioColumns.ALBUM_ID,
                        AudioColumns.DATA,
                        AudioColumns.DISPLAY_NAME,
                        AudioColumns.SIZE,
                        AudioColumns.DURATION
                },
                null,
//                SELECTION,
//                new String[] {
//                        String.valueOf(filterSize),
//                        String.valueOf(filterTime)
//                },
                null,
                Media.DEFAULT_SORT_ORDER
        );

        if (cursor == null) {
            return musics;
        }

        int i = 0;
        while (cursor.moveToNext()) {
            // 是否为音乐，魅族手机始终为0
            int isMusic = cursor.getInt(cursor.getColumnIndex(AudioColumns.IS_MUSIC));
            if (!SystemUtil.isFlyme() && isMusic == 0) {
                continue;
            }

            long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
            String title = cursor.getString(cursor.getColumnIndex(AudioColumns.TITLE));
            String artist = cursor.getString(cursor.getColumnIndex(AudioColumns.ARTIST));
            String album = cursor.getString(cursor.getColumnIndex(AudioColumns.ALBUM));
            long albumId = cursor.getLong(cursor.getColumnIndex(AudioColumns.ALBUM_ID));
            long duration = cursor.getLong(cursor.getColumnIndex(Media.DURATION));
            String path = cursor.getString(cursor.getColumnIndex(AudioColumns.DATA));
            String fileName = cursor.getString(cursor.getColumnIndex(AudioColumns.DISPLAY_NAME));
            long fileSize = cursor.getLong(cursor.getColumnIndex(Media.SIZE));

            Music music = new Music();
            music.setId(id);
            music.setType(Music.Type.LOCAL);
            music.setTitle(title);
            music.setTitlePinyin(Pinyin.toPinyin(title, ","));
            if ("<unknown>".equals(artist)) {
                music.setArtist(context.getString(R.string.unknown_artist));
            } else {
                music.setArtist(artist);
            }
            music.setArtistPinyin(Pinyin.toPinyin(artist, ","));
            music.setAlbum(album);
            music.setAlbumPinyin(Pinyin.toPinyin(album, ","));
            music.setAlbumId(albumId);
            music.setDuration(duration);
            music.setUrl(path);
            music.setFileName(fileName);
            music.setFileSize(fileSize);
            if (++i <= 18) {
                // 只加载前18首的缩略图
                CoverLoader.getInstance().loadThumbnail(music);
            }
            musics.add(music);
        }
        cursor.close();

        return musics;
    }

    static Uri getMediaStoreAlbumCoverUri(long albumId) {
        Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(artworkUri, albumId);
    }

    public static boolean isAudioControlPanelAvailable(Context context) {
        return isIntentAvailable(context, new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL));
    }

    private static boolean isIntentAvailable(Context context, Intent intent) {
        return context.getPackageManager().resolveActivity(intent, PackageManager.GET_RESOLVED_FILTER) != null;
    }
}
