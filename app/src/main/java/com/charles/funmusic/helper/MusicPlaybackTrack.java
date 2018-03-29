package com.charles.funmusic.helper;

import android.os.Parcel;
import android.os.Parcelable;

import com.charles.funmusic.model.Music;

/**
 * This is used by the music playback service to track the music tracks it is playing
 * It has extra meta data to determine where the track came from so that we can show the appropriate
 * song playing indicator
 */
public class MusicPlaybackTrack extends Music implements Parcelable {

    private long mId;
//    private long mSourceId;
//    private FunMusicUtil.IdType mSourceType;
    private int mSourcePosition;

    public MusicPlaybackTrack(long id, int sourcePosition) {
        mId = id;
//        mSourceId = sourceId;
//        mSourceType = type;
        mSourcePosition = sourcePosition;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public int getSourcePosition() {
        return mSourcePosition;
    }

    public void setSourcePosition(int sourcePosition) {
        mSourcePosition = sourcePosition;
    }

    public MusicPlaybackTrack(Parcel in) {
        mId = in.readLong();
//        mSourceId = in.readLong();
//        mSourceType = FunMusicUtil.IdType.getTypeById(in.readInt());
        mSourcePosition = in.readInt();
    }

    public static final Creator<MusicPlaybackTrack> CREATOR = new Creator<MusicPlaybackTrack>() {
        @Override
        public MusicPlaybackTrack createFromParcel(Parcel source) {
            return new MusicPlaybackTrack(source);
        }

        @Override
        public MusicPlaybackTrack[] newArray(int size) {
            return new MusicPlaybackTrack[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
//        dest.writeLong(mSourceId);
//        dest.writeInt(mSourceType.mId);
        dest.writeInt(mSourcePosition);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MusicPlaybackTrack) {
            MusicPlaybackTrack other = (MusicPlaybackTrack) o;
            return mId == other.mId
//                    && mSourceId == other.mSourceId
//                    && mSourceType == other.mSourceType
                    && mSourcePosition == other.mSourcePosition;
        }

        return super.equals(o);
    }
}
