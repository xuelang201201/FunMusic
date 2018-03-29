package com.charles.funmusic.constant;

public interface Keys {
    String VIEW_PAGER_INDEX = "view_pager_index";
    String LOCAL_MUSIC_POSITION = "local_music_position";
    String LOCAL_MUSIC_OFFSET = "local_music_offset";
    String PLAYLIST_POSITION = "playlist_position";
    String PLAYLIST_OFFSET = "playlist_offset";
    String PACKAGE = "com.charles.funmusic";

    int MUSIC_OVERFLOW = 0;
    int ARTIST_OVERFLOW = 1;
    int ALBUM_OVERFLOW = 2;
    int FOLDER_OVERFLOW = 3;

    // 歌手和专辑列表，点击都会进入详细页
    // 此时要传递参数表明是从哪里进入的
    int START_FROM_ARTIST = 1;
    int START_FROM_ALBUM = 2;
    int START_FROM_LOCAL = 3;
    int START_FROM_FOLDER = 4;

    int FAVORITE_PLAYLIST = 10;
}
