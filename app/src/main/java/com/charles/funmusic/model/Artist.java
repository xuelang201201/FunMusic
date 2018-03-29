package com.charles.funmusic.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class Artist implements Parcelable {

    private static final String KEY_ARTIST_NAME = "artist_name";
    private static final String KEY_NUMBER_OF_TRACKS = "number_of_tracks";
    private static final String KEY_ARTIST_ID = "artist_id";
    private static final String KEY_ARTIST_SORT = "artist_sort";

    private String mArtist;
    private int mNumberOfTracks;
    private long mArtistId;
    private String mArtistSort;

    public String getArtist() {
        return mArtist;
    }

    public void setArtist(String artist) {
        mArtist = artist;
    }

    public int getNumberOfTracks() {
        return mNumberOfTracks;
    }

    public void setNumberOfTracks(int numberOfTracks) {
        mNumberOfTracks = numberOfTracks;
    }

    public long getArtistId() {
        return mArtistId;
    }

    public void setArtistId(long artistId) {
        mArtistId = artistId;
    }

    public String getArtistSort() {
        return mArtistSort;
    }

    public void setArtistSort(String artistSort) {
        mArtistSort = artistSort;
    }

    public static final Creator<Artist> CREATOR = new Creator<Artist>() {

        @Override
        public Artist createFromParcel(Parcel source) {
            Bundle bundle = source.readBundle(getClass().getClassLoader());
            Artist info = new Artist();
            info.mArtist = bundle.getString(KEY_ARTIST_NAME);
            info.mNumberOfTracks = bundle.getInt(KEY_NUMBER_OF_TRACKS);
            info.mArtistId = bundle.getLong(KEY_ARTIST_ID);
            info.mArtistSort = bundle.getString(KEY_ARTIST_SORT);
            return info;
        }

        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_ARTIST_NAME, mArtist);
        bundle.putInt(KEY_NUMBER_OF_TRACKS, mNumberOfTracks);
        bundle.putLong(KEY_ARTIST_ID, mArtistId);
        bundle.putString(KEY_ARTIST_SORT, mArtistSort);
        dest.writeBundle(bundle);
    }

}
