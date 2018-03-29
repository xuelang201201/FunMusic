package com.charles.funmusic.model;

public class Song {

    private final long mId;
    private final long mAlbumId;
    private final long mArtistId;
    private final String mTitle;
    private final String mArtist;
    private final String mAlbum;
    private final int mDuration;
    private final int mTrackNumber;

    public Song() {
        mId = -1;
        mAlbumId = -1;
        mArtistId = -1;
        mTitle = "";
        mArtist = "";
        mAlbum = "";
        mDuration = -1;
        mTrackNumber = -1;
    }

    public Song(long id, long albumId, long artistId, String title, String artist, String album, int duration, int trackNumber) {
        mId = id;
        mAlbumId = albumId;
        mArtistId = artistId;
        mAlbum = album;
        mArtist = artist;
        mDuration = duration;
        mTitle = title;
        mTrackNumber = trackNumber;
    }

    public long getId() {
        return mId;
    }

    public long getAlbumId() {
        return mAlbumId;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public long getArtistId() {
        return mArtistId;
    }

    public String getArtist() {
        return mArtist;
    }

    public int getDuration() {
        return mDuration;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getTrackNumber() {
        return mTrackNumber;
    }
}
