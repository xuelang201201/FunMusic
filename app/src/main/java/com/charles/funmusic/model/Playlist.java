package com.charles.funmusic.model;

/**
 * 播放列表
 */
public class Playlist {

    private long mId;
    private String mName;
    private int mSongCount;
    private String mAlbumArt;
    private String mAuthor;

    public Playlist(long id, String name, int songCount, String albumArt, String author) {
        mId = id;
        mName = name;
        mSongCount = songCount;
        mAlbumArt = albumArt;
        mAuthor = author;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getSongCount() {
        return mSongCount;
    }

    public void setSongCount(int songCount) {
        mSongCount = songCount;
    }

    public String getAlbumArt() {
        return mAlbumArt;
    }

    public void setAlbumArt(String albumArt) {
        mAlbumArt = albumArt;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }
}
