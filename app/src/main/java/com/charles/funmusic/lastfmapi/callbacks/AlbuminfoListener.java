package com.charles.funmusic.lastfmapi.callbacks;

import com.charles.funmusic.lastfmapi.models.LastfmAlbum;

public interface AlbuminfoListener {

    void albumInfoSucess(LastfmAlbum album);

    void albumInfoFailed();

}
