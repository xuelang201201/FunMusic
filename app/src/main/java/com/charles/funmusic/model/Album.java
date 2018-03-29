package com.charles.funmusic.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class Album implements Parcelable {

    private static final String KEY_ALBUM_NAME = "album";
    private static final String KEY_ALBUM_ID = "album_id";
    private static final String KEY_NUMBER_OF_SONGS = "number_of_songs";
    private static final String KEY_ALBUM_ART = "album_art";
    private static final String KEY_ALBUM_ARTIST = "album_artist";
    private static final String KEY_ALBUM_SORT = "album_sort";

    //专辑名称
    private String mAlbum;
    //专辑在数据库中的id
    private int mAlbumId = -1;
    //专辑的歌曲数目
    private int mNumberOfSongs = 0;
    //专辑封面图片路径
    private String mAlbumArt;
    private String mAlbumArtist;
    private String mAlbumSort;

    public String getAlbum() {
        return mAlbum;
    }

    public void setAlbum(String album) {
        mAlbum = album;
    }

    public int getAlbumId() {
        return mAlbumId;
    }

    public void setAlbumId(int albumId) {
        mAlbumId = albumId;
    }

    public int getNumberOfSongs() {
        return mNumberOfSongs;
    }

    public void setNumberOfSongs(int numberOfSongs) {
        mNumberOfSongs = numberOfSongs;
    }

    public String getAlbumArt() {
        return mAlbumArt;
    }

    public void setAlbumArt(String albumArt) {
        mAlbumArt = albumArt;
    }

    public String getAlbumArtist() {
        return mAlbumArtist;
    }

    public void setAlbumArtist(String albumArtist) {
        mAlbumArtist = albumArtist;
    }

    public String getAlbumSort() {
        return mAlbumSort;
    }

    public void setAlbumSort(String albumSort) {
        mAlbumSort = albumSort;
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {

        //读数据恢复数据
        @Override
        public Album createFromParcel(Parcel source) {
            Album info = new Album();
            Bundle bundle = source.readBundle(getClass().getClassLoader());
            info.mAlbum = bundle.getString(KEY_ALBUM_NAME);
            info.mAlbumArt = bundle.getString(KEY_ALBUM_ART);
            info.mNumberOfSongs = bundle.getInt(KEY_NUMBER_OF_SONGS);
            info.mAlbumId = bundle.getInt(KEY_ALBUM_ID);
            info.mAlbumArtist = bundle.getString(KEY_ALBUM_ARTIST);
            info.mAlbumSort = bundle.getString(KEY_ALBUM_SORT);
            return info;
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    //写数据保存数据
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_ALBUM_NAME, mAlbum);
        bundle.putString(KEY_ALBUM_ART, mAlbumArt);
        bundle.putInt(KEY_NUMBER_OF_SONGS, mNumberOfSongs);
        bundle.putInt(KEY_ALBUM_ID, mAlbumId);
        bundle.putString(KEY_ALBUM_ARTIST, mAlbumArtist);
        bundle.putString(KEY_ALBUM_SORT, mAlbumSort);
        dest.writeBundle(bundle);
    }

}
