package com.charles.funmusic.lastfmapi.callbacks;

import com.charles.funmusic.lastfmapi.models.LastFmArtist;

public interface ArtistInfoListener {

    void artistInfoSuccess(LastFmArtist artist);

    void artistInfoFailed();

}
