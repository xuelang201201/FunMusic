package com.charles.funmusic.model;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchArtist implements Parcelable {
    private String mArtistId;
    private String mAuthor;
    private String mTingUId;
    private String mAvatarMiddle;
    private int mAlbumNum;
    private int mSongNum;
    private String mCountry;
    private String mArtistDesc;
    private String mArtistSource;

    public String getArtistId() {
        return mArtistId;
    }

    public void setArtistId(String artistId) {
        mArtistId = artistId;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String author) {
        mAuthor = author;
    }

    public String getTingUId() {
        return mTingUId;
    }

    public void setTingUId(String tingUId) {
        mTingUId = tingUId;
    }

    public String getAvatarMiddle() {
        return mAvatarMiddle;
    }

    public void setAvatarMiddle(String avatarMiddle) {
        mAvatarMiddle = avatarMiddle;
    }

    public int getAlbumNum() {
        return mAlbumNum;
    }

    public void setAlbumNum(int albumNum) {
        mAlbumNum = albumNum;
    }

    public int getSongNum() {
        return mSongNum;
    }

    public void setSongNum(int songNum) {
        mSongNum = songNum;
    }

    public String getCountry() {
        return mCountry;
    }

    public void setCountry(String country) {
        mCountry = country;
    }

    public String getArtistDesc() {
        return mArtistDesc;
    }

    public void setArtistDesc(String artistDesc) {
        mArtistDesc = artistDesc;
    }

    public String getArtistSource() {
        return mArtistSource;
    }

    public void setArtistSource(String artistSource) {
        mArtistSource = artistSource;
    }

    private SearchArtist(Parcel in) {
        mArtistId = in.readString();
        mAuthor = in.readString();
        mTingUId = in.readString();
        mAvatarMiddle = in.readString();
        mAlbumNum = in.readInt();
        mCountry = in.readString();
        mArtistDesc = in.readString();
        mArtistSource = in.readString();
    }

    public static final Creator<SearchArtist> CREATOR = new Creator<SearchArtist>() {
        @Override
        public SearchArtist createFromParcel(Parcel in) {
            return new SearchArtist(in);
        }

        @Override
        public SearchArtist[] newArray(int size) {
            return new SearchArtist[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mArtistId);
        dest.writeString(mAuthor);
        dest.writeString(mTingUId);
        dest.writeString(mAvatarMiddle);
        dest.writeInt(mAlbumNum);
        dest.writeInt(mSongNum);
        dest.writeString(mCountry);
        dest.writeString(mArtistDesc);
        dest.writeString(mArtistSource);
    }
}
