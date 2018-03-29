package com.charles.funmusic.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AlbumColumns;
import android.provider.MediaStore.Audio.Albums;
import android.provider.MediaStore.Audio.Artists;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.Files.FileColumns;

import com.charles.funmusic.R;
import com.charles.funmusic.model.Album;
import com.charles.funmusic.model.Artist;
import com.charles.funmusic.model.Folder;
import com.charles.funmusic.model.Music;
import com.charles.funmusic.utils.loader.CoverLoader;
import com.github.promeg.pinyinhelper.Pinyin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.charles.funmusic.constant.Keys.START_FROM_ALBUM;
import static com.charles.funmusic.constant.Keys.START_FROM_ARTIST;
import static com.charles.funmusic.constant.Keys.START_FROM_FOLDER;
import static com.charles.funmusic.constant.Keys.START_FROM_LOCAL;

/**
 * 歌曲工具类
 */
public class MusicUtil {
    private static final String SELECTION =
            AudioColumns.SIZE + ">= ? AND " + AudioColumns.DURATION + " >= ?";

    private static final int FILTER_SIZE = 1024 * 1024; // 1MB
    private static final int FILTER_DURATION = 60 * 1000; // 1分钟

    private static String[] PROJECTION_MUSIC = new String[]{
            Media._ID, Media.TITLE, Media.DATA, Media.ALBUM_ID, Media.ALBUM,
            Media.ARTIST, Media.ARTIST_ID, Media.DURATION, Media.SIZE
    };

    private static String[] PROJECTION_ALBUM = new String[]{
            Albums._ID, Albums.ALBUM_ART, Albums.ALBUM,
            Albums.NUMBER_OF_SONGS, Albums.ARTIST
    };

    private static String[] PROJECTION_ARTIST = new String[]{
            Artists.ARTIST, Artists.NUMBER_OF_TRACKS, Artists._ID
    };

    private static String[] PROJECTION_FOLDER = new String[]{
            FileColumns.DATA
    };

    /**
     * 获取包含音频文件的文件夹信息
     *
     * @param context 上下文对象
     * @return 包含音频文件的文件夹
     */
    public static List<Folder> queryFolders(Context context) {
        Uri uri = MediaStore.Files.getContentUri("external");
        ContentResolver cr = context.getContentResolver();
        // 查询语句：检索出例如.mp3为后缀名，时长如大于1分钟，文件大小如大于1MB的媒体文件
        String selection = FileColumns.MEDIA_TYPE + " = " + FileColumns.MEDIA_TYPE_AUDIO
                + " and " + "(" + FileColumns.DATA + " like'%.mp3' or " + Media.DATA
                + " like'%.wma')" + " and " + Media.SIZE + " > " + FILTER_SIZE +
                " and " + Media.DURATION + " > " + FILTER_DURATION
                + ") group by ( " + FileColumns.PARENT;

        return getFolders(cr.query(uri, PROJECTION_FOLDER, selection, null, null));
    }

    /**
     * 获取歌手信息
     *
     * @param context 上下文对象
     * @return 歌手信息
     */
    public static List<Artist> queryArtists(Context context) {
        Uri uri = Artists.EXTERNAL_CONTENT_URI;
        ContentResolver cr = context.getContentResolver();
        String where = Artists._ID + " in (select distinct " + Media.ARTIST_ID
                + " from audio_meta where (1=1 )" + " and " + Media.SIZE
                + " > " + FILTER_SIZE + " and " + Media.DURATION + " > "
                + FILTER_DURATION + ")";

        return getArtists(cr.query(uri, PROJECTION_ARTIST, where, null,
                Preferences.getInstance(context).getArtistSortOrder()));
    }

    /**
     * 获取专辑信息
     *
     * @param context 上下文对象
     * @return 专辑信息
     */
    public static List<Album> queryAlbums(Context context) {
        ContentResolver cr = context.getContentResolver();
        String where = Albums._ID + " in (select distinct " + Media.ALBUM_ID
                + " from audio_meta where (1=1)" + " and " + Media.SIZE
                + " > " + FILTER_SIZE + " and " + Media.DURATION
                + " > " + FILTER_DURATION + ")";

        // Media.ALBUM_KEY 按专辑名称排序
        return getAlbums(cr.query(Albums.EXTERNAL_CONTENT_URI, PROJECTION_ALBUM, where,
                null, Preferences.getInstance(context).getAlbumSortOrder()));
    }

    /**
     * 根据界面的不同，使用不同的查询方法
     *
     * @param context 上下文对象
     * @param from    从哪个界面查
     * @return 音乐列表
     */
    public static List<Music> queryMusics(Context context, int from) {
        return queryMusics(context, null, from);
    }

    /**
     * 获取音乐
     *
     * @param context 上下文对象
     * @param id      歌曲的 id
     * @param from    来自哪里
     * @return 歌曲列表
     */
    public static ArrayList<Music> queryMusics(Context context, String id, int from) {
        Uri uri = Media.EXTERNAL_CONTENT_URI;
        ContentResolver cr = context.getContentResolver();

        StringBuilder select = new StringBuilder(" 1=1 and title != ''");
        // 查询语句：检索出如.mp3为后缀名，时长如大于1分钟，文件大小如大于1MB的媒体文件
        select.append(" and " + Media.SIZE + " > " + FILTER_SIZE);
        select.append(" and " + Media.DURATION + " > " + FILTER_DURATION);

        final String songSortOrder = Preferences.getInstance(context).getSongSortOrder();

        switch (from) {
            case START_FROM_LOCAL:
                return getMusicsCursor(cr.query(uri, PROJECTION_MUSIC,
                        select.toString(), null, songSortOrder));

            case START_FROM_ARTIST:
                select.append(" and " + Media.ARTIST_ID + " = ").append(id);
                return getMusicsCursor(cr.query(uri, PROJECTION_MUSIC, select.toString(),
                        null, Preferences.getInstance(context).getArtistSortOrder()));

            case START_FROM_ALBUM:
                select.append(" and " + Media.ALBUM_ID + " = ").append(id);
                return getMusicsCursor(cr.query(uri, PROJECTION_MUSIC, select.toString(),
                        null, Preferences.getInstance(context).getAlbumSortOrder()));

            case START_FROM_FOLDER:
                ArrayList<Music> musics = new ArrayList<>();
                List<Music> list = getMusicsCursor(cr.query(Media.EXTERNAL_CONTENT_URI,
                        PROJECTION_MUSIC, select.toString(), null, null));
                for (Music music : list) {
                    if (music.getUrl().substring(0, music.getUrl().lastIndexOf(File.separator)).equals(id)) {
                        musics.add(music);
                    }
                }
                return musics;

            default:
                return null;
        }
    }

    private static List<Folder> getFolders(Cursor cursor) {
        List<Folder> folders = new ArrayList<>();
        while (cursor.moveToNext()) {
            Folder folder = new Folder();
            String filePath = cursor.getString(cursor.getColumnIndex(FileColumns.DATA));
            folder.setFolderPath(filePath.substring(0, filePath.lastIndexOf(File.separator)));
            folder.setFolderName(folder.getFolderPath().substring(
                    folder.getFolderPath().lastIndexOf(File.separator) + 1));
            folder.setFolderSort(Pinyin.toPinyin(folder.getFolderName().charAt(0))
                    .substring(0, 1).toUpperCase());
            folders.add(folder);
        }
        cursor.close();
        return folders;
    }

    private static List<Artist> getArtists(Cursor cursor) {
        List<Artist> artists = new ArrayList<>();
        while (cursor.moveToNext()) {
            Artist artist = new Artist();
            artist.setArtist(cursor.getString(cursor.getColumnIndex(Artists.ARTIST)));
            artist.setNumberOfTracks(cursor.getInt(cursor.getColumnIndex(Artists.NUMBER_OF_TRACKS)));
            artist.setArtistId(cursor.getLong(cursor.getColumnIndex(Artists._ID)));
            artist.setArtistSort(Pinyin.toPinyin(artist.getArtist().charAt(0))
                    .substring(0, 1).toUpperCase());
            artists.add(artist);
        }
        cursor.close();
        return artists;
    }

    private static List<Album> getAlbums(Cursor cursor) {
        List<Album> albums = new ArrayList<>();
        while (cursor.moveToNext()) {
            Album album = new Album();
            album.setAlbum(cursor.getString(cursor.getColumnIndex(Albums.ALBUM)));
            album.setAlbumId(cursor.getInt(cursor.getColumnIndex(Albums._ID)));
            album.setNumberOfSongs(cursor.getInt(cursor.getColumnIndex(Albums.NUMBER_OF_SONGS)));
            album.setAlbumArt(getAlbumArtUri(album.getAlbumId()) + "");
            album.setAlbumArtist(cursor.getString(cursor.getColumnIndex(Albums.ARTIST)));
            album.setAlbumSort(Pinyin.toPinyin(
                    album.getAlbum().charAt(0)).substring(0, 1).toUpperCase());
            albums.add(album);
        }
        cursor.close();
        return albums;
    }

    public static Music getMusics(Context context, long id) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(Media.EXTERNAL_CONTENT_URI, PROJECTION_MUSIC,
                "_id = " + String.valueOf(id), null, null);
        if (cursor == null) {
            return null;
        }
        Music music = new Music();
        while (cursor.moveToNext()) {
            music.setId(cursor.getInt(cursor.getColumnIndex(Media._ID)));
            music.setAlbumId(cursor.getLong(cursor.getColumnIndex(Media.ALBUM_ID)));
            music.setAlbum(cursor.getString(cursor.getColumnIndex(Albums.ALBUM)));
            music.setAlbumArt(getAlbumArtUri(music.getAlbumId()) + "");
            music.setDuration(cursor.getLong(cursor.getColumnIndex(Media.DURATION)));
            music.setTitle(cursor.getString(cursor.getColumnIndex(Media.TITLE)));
            music.setFileSize(cursor.getLong(cursor.getColumnIndex(Media.SIZE)));
            music.setArtist(cursor.getString(cursor.getColumnIndex(Media.ARTIST)));
            music.setArtistId(cursor.getLong(cursor.getColumnIndex(Media.ARTIST_ID)));
            music.setUrl(cursor.getString(cursor.getColumnIndex(Media.DATA)));
            music.setFolder(music.getUrl().substring(0, music.getUrl().lastIndexOf(File.separator)));
            music.setSort(Pinyin.toPinyin(music.getTitle().charAt(0)).substring(0, 1).toUpperCase());
        }
        cursor.close();
        return music;
    }

    public static String getAlbumArt(Context context, long id) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, PROJECTION_MUSIC, "_id = " + String.valueOf(id), null, null);
        if (cursor == null) {
            return null;
        }

        long albumId = -1;
        if (cursor.moveToNext()) {
            albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
        }

        if (albumId != -1) {
            cursor = cr.query(Albums.EXTERNAL_CONTENT_URI, PROJECTION_ALBUM, Albums._ID + " = " + String.valueOf(albumId), null, null);
        }

        if (cursor == null) {
            return null;
        }

        String albumArt = "";
        if (cursor.moveToNext()) {
            albumArt = cursor.getString(cursor.getColumnIndex(Albums.ALBUM_ART));
        }
        cursor.close();

        return albumArt;
    }

    public static List<Music> getMusics(Context context, long[] id) {
        final StringBuilder selection = new StringBuilder();
        selection.append(Media._ID + " IN (");
        for (int i = 0; i < id.length; i++) {
            selection.append(id[i]);
            if (i < id.length - 1) {
                selection.append(",");
            }
        }
        selection.append(")");

        Cursor cursor = context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, PROJECTION_MUSIC,
                selection.toString(), null, null);
        if (cursor == null) {
            return null;
        }
        ArrayList<Music> musics = new ArrayList<>();
        musics.ensureCapacity(id.length);
        for (long l : id) {
            musics.add(null);
        }

        while (cursor.moveToNext()) {
            Music music = new Music();
            music.setId(cursor.getInt(cursor.getColumnIndex(Media._ID)));
            music.setAlbumId(cursor.getInt(cursor.getColumnIndex(Media.ALBUM_ID)));
            music.setAlbum(cursor.getString(cursor.getColumnIndex(Albums.ALBUM)));
            music.setAlbumArt(getAlbumArtUri(music.getAlbumId()) + "");
            music.setTitle(cursor.getString(cursor.getColumnIndex(Media.TITLE)));
            music.setArtist(cursor.getString(cursor.getColumnIndex(Media.ARTIST)));
            music.setArtistId(cursor.getLong(cursor.getColumnIndex(Media.ARTIST_ID)));
            music.setLocal(true);
            for (int i = 0; i < id.length; i++) {
                if (id[i] == music.getId()) {
                    musics.set(i, music);
                }
            }
        }
        cursor.close();
        return musics;
    }

    private static ArrayList<Music> getMusicsCursor(Cursor cursor) {
        if (cursor == null) {
            return null;
        }

        ArrayList<Music> musics = new ArrayList<>();
        while (cursor.moveToNext()) {
            Music music = new Music();
            music.setId(cursor.getInt(cursor.getColumnIndex(Media._ID)));
            music.setAlbumId(cursor.getInt(cursor.getColumnIndex(Media.ALBUM_ID)));
            music.setAlbum(cursor.getString(cursor.getColumnIndex(Albums.ALBUM)));
            music.setAlbumArt(getAlbumArtUri(music.getAlbumId()) + "");
            music.setDuration(cursor.getLong(cursor.getColumnIndex(Media.DURATION)));
            music.setTitle(cursor.getString(cursor.getColumnIndex(Media.TITLE)));
            music.setArtist(cursor.getString(cursor.getColumnIndex(Media.ARTIST)));
            music.setArtistId(cursor.getLong(cursor.getColumnIndex(Media.ARTIST_ID)));
            music.setUrl(cursor.getString(cursor.getColumnIndex(Media.DATA)));
            music.setFolder(music.getUrl().substring(0, music.getUrl().lastIndexOf(File.separator)));
            music.setFileSize(cursor.getLong(cursor.getColumnIndex(Media.SIZE)));
            music.setLocal(true);
            music.setSort(Pinyin.toPinyin(music.getTitle().charAt(0)).substring(0, 1).toUpperCase());
            musics.add(music);
        }
        cursor.close();
        return musics;
    }

    public static Uri getAlbumArtUri(long albumId) {
        return ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"), albumId);
    }

    public static Uri getAlbumUri(Context context, long id) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(Media.EXTERNAL_CONTENT_URI, PROJECTION_MUSIC,
                "_id =" + String.valueOf(id), null, null);
        long musicId = -3;
        if (cursor == null) {
            return null;
        }
        if (cursor.moveToFirst()) {
            musicId = cursor.getInt(cursor.getColumnIndex(Media.ALBUM_ID));
        }

        cursor.close();
        return getAlbumArtUri(musicId);
    }

    public static Artist getArtists(Context context, long id) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(Artists.EXTERNAL_CONTENT_URI, PROJECTION_ARTIST,
                "_id =" + String.valueOf(id), null, null);
        if (cursor == null) {
            return null;
        }
        Artist artist = new Artist();
        while (cursor.moveToNext()) {
            artist.setArtist(cursor.getString(cursor.getColumnIndex(Artists.ARTIST)));
            artist.setNumberOfTracks(cursor.getInt(cursor.getColumnIndex(Artists.NUMBER_OF_TRACKS)));
        }
        cursor.close();
        return artist;
    }

    public static Album getAlbums(Context context, long id) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(Albums.EXTERNAL_CONTENT_URI, PROJECTION_ALBUM,
                "_id =" + String.valueOf(id), null, null);
        if (cursor == null) {
            return null;
        }
        Album album = new Album();
        while (cursor.moveToNext()) {
            album.setAlbum(cursor.getString(cursor.getColumnIndex(Albums.ALBUM)));
            album.setAlbumArt(cursor.getString(cursor.getColumnIndex(Albums.ALBUM_ART)));
        }
        cursor.close();
        return album;

    }

    public static String makeTimeString(long milliSecs) {
        long m = milliSecs / (60 * 1000);
        long s = (milliSecs % (60 * 1000)) / 1000;

        return (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s : s);
    }

    public static String makeShortTimeString(final Context context, long seconds) {
        long hours, minutes;

        hours = seconds / 3600;
        seconds %= 3600;
        minutes = seconds / 60;
        seconds %= 60;

        final String durationFormat = context.getResources().getString(
                hours == 0 ? R.string.duration_format_short : R.string.duration_format_long);
        return String.format(durationFormat, hours, minutes, seconds);
    }

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

    public static boolean isAudioControlPanelAvailable(Context context) {
        return isIntentAvailable(context, new Intent(
                AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL));
    }

    private static boolean isIntentAvailable(Context context, Intent intent) {
        return context.getPackageManager().resolveActivity(
                intent, PackageManager.GET_RESOLVED_FILTER) != null;
    }
}
