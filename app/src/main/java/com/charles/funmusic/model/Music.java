package com.charles.funmusic.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class Music implements Parcelable {

    private static final String KEY_SONG_ID = "song_id";
    private static final String KEY_ALBUM_ID = "album_id";
    private static final String KEY_ALBUM_NAME = "album_name";
    private static final String KEY_ALBUM_ART = "album_art";
    private static final String KEY_DURATION = "duration";
    private static final String KEY_MUSIC_NAME = "music_name";
    private static final String KEY_ARTIST = "artist";
    private static final String KEY_ARTIST_ID = "artist_id";
    private static final String KEY_DATA = "data";
    private static final String KEY_FOLDER = "folder";
    private static final String KEY_SIZE = "size";
    private static final String KEY_LRC = "lrc";
    private static final String KEY_IS_LOCAL = "is_local";
    private static final String KEY_SORT = "sort";
    private static final String KEY_TRACK_NUMBER = "track_number";

    /**
     * 歌曲类型：本地/网络
     */
    private Type mType;
    /**
     * [本地歌曲]歌曲id
     */
    private long mId;
    /**
     * 音乐标题
     */
    private String mTitle;
    /**
     * 音乐标题的拼音
     */
    private String mTitlePinyin;
    /**
     * 艺术家
     */
    private String mArtist;
    
    private long mArtistId;
    /**
     * 艺术家的拼音
     */
    private String mArtistPinyin;
    /**
     * 专辑
     */
    private String mAlbum;
    /**
     * 专辑的拼音
     */
    private String mAlbumPinyin;
    /**
     * [本地歌曲]专辑id
     */
    private long mAlbumId;

    private String mAlbumArt;
    /**
     * [在线歌曲]专辑封面路径
     */
    private String mCoverUrl;
    /**
     * 持续时间
     */
    private long mDuration;
    /**
     * 音乐路径
     */
    private String mUrl;
    /**
     * 文件名
     */
    private String mFileName;
    /**
     * 文件大小
     */
    private float mFileSize;

    private String mFolder;

    private String mLrc;

    private boolean isLocal;

    private String mSort;

    private int mTrackNumber;

    public Music() {
    }

    public Music(long id) {
        mId = id;
    }

    public enum Type {
        LOCAL,
        ONLINE
    }

    public Type getType() {
        return mType;
    }

    public void setType(Type type) {
        mType = type;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTitlePinyin() {
        return mTitlePinyin;
    }

    public void setTitlePinyin(String titlePinyin) {
        mTitlePinyin = titlePinyin;
    }

    public String getArtist() {
        return mArtist;
    }

    public void setArtist(String artist) {
        mArtist = artist;
    }

    public long getArtistId() {
        return mArtistId;
    }

    public void setArtistId(long artistId) {
        mArtistId = artistId;
    }

    public String getArtistPinyin() {
        return mArtistPinyin;
    }

    public void setArtistPinyin(String artistPinyin) {
        mArtistPinyin = artistPinyin;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public void setAlbum(String album) {
        mAlbum = album;
    }

    public String getAlbumPinyin() {
        return mAlbumPinyin;
    }

    public void setAlbumPinyin(String albumPinyin) {
        mAlbumPinyin = albumPinyin;
    }

    public long getAlbumId() {
        return mAlbumId;
    }

    public void setAlbumId(long albumId) {
        mAlbumId = albumId;
    }

    public String getAlbumArt() {
        return mAlbumArt;
    }

    public void setAlbumArt(String albumArt) {
        mAlbumArt = albumArt;
    }

    public String getCoverUrl() {
        return mCoverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        mCoverUrl = coverUrl;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    public float getFileSize() {
        return mFileSize;
    }

    public void setFileSize(float fileSize) {
        mFileSize = fileSize;
    }

    public String getFolder() {
        return mFolder;
    }

    public void setFolder(String folder) {
        mFolder = folder;
    }

    public String getLrc() {
        return mLrc;
    }

    public void setLrc(String lrc) {
        mLrc = lrc;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }

    public String getSort() {
        return mSort;
    }

    public void setSort(String sort) {
        mSort = sort;
    }

    public int getTrackNumber() {
        return mTrackNumber;
    }

    public void setTrackNumber(int trackNumber) {
        mTrackNumber = trackNumber;
    }

    /**
     * 对比本地歌曲是否相同
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Music && this.getId() == ((Music) obj).getId();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putLong(KEY_SONG_ID, mId);
        bundle.putLong(KEY_ALBUM_ID, mAlbumId);
        bundle.putString(KEY_ALBUM_NAME, mAlbum);
        bundle.putString(KEY_ALBUM_ART, mAlbumArt);
        bundle.putLong(KEY_DURATION, mDuration);
        bundle.putString(KEY_MUSIC_NAME, mTitle);
        bundle.putString(KEY_ARTIST, mArtist);
        bundle.putLong(KEY_ARTIST_ID, mArtistId);
        bundle.putString(KEY_DATA, mUrl);
        bundle.putString(KEY_FOLDER, mFolder);
        bundle.putFloat(KEY_SIZE, mFileSize);
        bundle.putString(KEY_LRC, mLrc);
        bundle.putBoolean(KEY_IS_LOCAL, isLocal());
        bundle.putString(KEY_SORT, mSort);
        bundle.putInt(KEY_TRACK_NUMBER, mTrackNumber);
        dest.writeBundle(bundle);
    }

    public Music(long id, long albumId, long artistId, String title, String artist, String album, int duration, int trackNumber) {
        mId = id;
        mAlbumId = albumId;
        mArtistId = artistId;
        mAlbum = album;
        mArtist = artist;
        mDuration = duration;
        mTitle = title;
        mTrackNumber = trackNumber;
    }

    protected Music(Parcel in) {
        mId = in.readLong();
        mTitle = in.readString();
        mTitlePinyin = in.readString();
        mArtist = in.readString();
        mArtistId = in.readLong();
        mArtistPinyin = in.readString();
        mAlbum = in.readString();
        mAlbumPinyin = in.readString();
        mAlbumId = in.readLong();
        mAlbumArt = in.readString();
        mCoverUrl = in.readString();
        mDuration = in.readLong();
        mUrl = in.readString();
        mFileName = in.readString();
        mFileSize = in.readLong();
        mSort = in.readString();
        isLocal = in.readByte() != 0;
        mTrackNumber = in.readInt();
    }

    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel source) {
            Music music = new Music();
            Bundle bundle = source.readBundle(getClass().getClassLoader());
            music.mId = bundle.getLong(KEY_SONG_ID);
            music.mAlbumId = bundle.getLong(KEY_ALBUM_ID);
            music.mAlbum = bundle.getString(KEY_ALBUM_NAME);
            music.mDuration = bundle.getLong(KEY_DURATION);
            music.mTitle = bundle.getString(KEY_MUSIC_NAME);
            music.mArtist = bundle.getString(KEY_ARTIST);
            music.mArtistId = bundle.getLong(KEY_ARTIST_ID);
            music.mUrl = bundle.getString(KEY_DATA);
            music.mFolder = bundle.getString(KEY_FOLDER);
            music.mAlbumArt = bundle.getString(KEY_ALBUM_ART);
            music.mFileSize = bundle.getLong(KEY_SIZE);
            music.mLrc = bundle.getString(KEY_LRC);
            music.mSort = bundle.getString(KEY_SORT);
            music.isLocal = bundle.getBoolean(KEY_IS_LOCAL);
            music.mTrackNumber = bundle.getInt(KEY_TRACK_NUMBER);
            return music;
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };
}