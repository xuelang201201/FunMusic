package com.charles.funmusic.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.charles.funmusic.R;

/**
 * SharedPreferences工具类
 */
public class Preferences {
    private static final String MUSIC_ID = "music_id";
    private static final String PLAY_MODE = "play_mode";
    private static final String SPLASH_URL = "splash_url";
    private static final String NIGHT_MODE = "night_mode";
    private static final String SORT_WAY = "sort_way";

    private static final String ARTIST_SORT_ORDER = "artist_sort_order";
    private static final String ALBUM_SORT_ORDER = "album_sort_order";
    private static final String SONG_SORT_ORDER = "song_sort_order";
    private static final String FOLDER_SORT_ORDER = "folder_sort_order";
    private static final String DOWN_MUSIC_BIT = "down_music_bit";

    private static Context sContext;
    private static Preferences sInstance;
    private static SharedPreferences sPreferences;

    public Preferences(Context context) {
        sPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void init(Context context) {
        sContext = context.getApplicationContext();
    }

    public static Preferences getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new Preferences(context.getApplicationContext());
        }
        return sInstance;
    }

    public void setPlayLink(long id, String link) {
        final SharedPreferences.Editor editor = sPreferences.edit();
        editor.putString(id + "", link);
        editor.apply();
    }

    public String getPlayLink(long id) {
        return sPreferences.getString(id + "", null);
    }

    public void setDownMusicBit(int bit) {
        final SharedPreferences.Editor editor = sPreferences.edit();
        editor.putInt(DOWN_MUSIC_BIT, bit);
        editor.apply();
    }

    public int getDownMusicBit() {
        return getInt(DOWN_MUSIC_BIT, 192);
    }

    public static long getCurrentSongId() {
        return getLong(MUSIC_ID, -1);
    }

    public static void saveCurrentSongId(long id) {
        saveLong(MUSIC_ID, id);
    }

    public static int getPlayMode() {
        return getInt(PLAY_MODE, 0);
    }

    public static void savePlayMode(int mode) {
        saveInt(PLAY_MODE, mode);
    }

    public static String getSplashUrl() {
        return getString(SPLASH_URL, "");
    }

    public static void saveSplashUrl(String url) {
        saveString(SPLASH_URL, url);
    }

    public static int getSortWay() {
        return getInt(SORT_WAY, 0);
    }

    public static void saveSortWay(int sortWay) {
        saveInt(SORT_WAY, sortWay);
    }

    public static boolean enableMobileNetworkPlay() {
        return getBoolean(sContext.getString(R.string.setting_key_mobile_network_play), false);
    }

    public static void saveMobileNetworkPlay(boolean enable) {
        saveBoolean(sContext.getString(R.string.setting_key_mobile_network_play), enable);
    }

    public static boolean enableMobileNetworkDownload() {
        return getBoolean(sContext.getString(R.string.setting_key_mobile_network_download), false);
    }

    public static boolean isNightMode() {
        return getBoolean(NIGHT_MODE, false);
    }

    public static void saveNightMode(boolean on) {
        saveBoolean(NIGHT_MODE, on);
    }

    @SuppressLint("StaticFieldLeak")
    private void saveSortOrder(final String key, final String value) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                final SharedPreferences.Editor editor = sPreferences.edit();
                editor.putString(key, value);
                editor.apply();

                return null;
            }
        };
    }

    public final String getArtistSortOrder() {
        return Preferences.getString(ARTIST_SORT_ORDER, SortOrder.ArtistSortOrder.ARTIST_A_Z);
    }

    public void saveArtistSortOrder(String value) {
        saveSortOrder(ARTIST_SORT_ORDER, value);
    }

    public final String getAlbumSortOrder() {
        return Preferences.getString(ALBUM_SORT_ORDER, SortOrder.AlbumSortOrder.ALBUM_A_Z);
    }

    public void saveAlbumSortOrder(final String value) {
        saveSortOrder(ALBUM_SORT_ORDER, value);
    }

    public final String getSongSortOrder() {
        return Preferences.getString(SONG_SORT_ORDER, SortOrder.SongSortOrder.SONG_A_Z);
    }

    public void saveSongSortOrder(final String value) {
        saveSortOrder(SONG_SORT_ORDER, value);
    }

    public final String getFolderSortOrder() {
        return Preferences.getString(FOLDER_SORT_ORDER, SortOrder.FolderSortOrder.FOLDER_A_Z);
    }

    public void saveFolderSortOrder(final String value) {
        saveSortOrder(FOLDER_SORT_ORDER, value);
    }

    public static String getFilterSize() {
        return getString(sContext.getString(R.string.setting_key_filter_size), "0");
    }

    public static void saveFilterSize(String value) {
        saveString(sContext.getString(R.string.setting_key_filter_size), value);
    }

    public static String getFilterTime() {
        return getString(sContext.getString(R.string.setting_key_filter_time), "0");
    }

    public static void saveFilterTime(String value) {
        saveString(sContext.getString(R.string.setting_key_filter_time), value);
    }

    private static boolean getBoolean(String key, boolean defValue) {
        return getPreferences().getBoolean(key, defValue);
    }

    private static void saveBoolean(String key, boolean value) {
        getPreferences().edit().putBoolean(key, value).apply();
    }

    private static int getInt(String key, int defValue) {
        return getPreferences().getInt(key, defValue);
    }

    private static void saveInt(String key, int value) {
        getPreferences().edit().putInt(key, value).apply();
    }

    private static long getLong(String key, long defValue) {
        return getPreferences().getLong(key, defValue);
    }

    private static void saveLong(String key, long value) {
        getPreferences().edit().putLong(key, value).apply();
    }

    private static String getString(String key, @Nullable String defValue) {
        return getPreferences().getString(key, defValue);
    }

    private static void saveString(String key, @Nullable String value) {
        getPreferences().edit().putString(key, value).apply();
    }

    private static SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(sContext);
    }
}
