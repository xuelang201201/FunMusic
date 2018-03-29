package com.charles.funmusic.lastfmapi.models;

import com.google.gson.annotations.SerializedName;

public class ArtistInfo {

    private static final String ARTIST = "artist";

    @SerializedName(ARTIST)
    public LastFmArtist mArtist;

}
