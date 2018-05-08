package com.charles.funmusic.utils;

import android.provider.MediaStore;

/**
 * Holds all of the mSort orders for each list type.
 */
public final class SortOrder {

    /**
     * This class is never instantiated
     */
    public SortOrder() {
    }

    /**
     * Artist mSort order entries.
     */
    public interface ArtistSortOrder {
        /* Artist mSort order A-Z */
        String ARTIST_A_Z = MediaStore.Audio.Artists.DEFAULT_SORT_ORDER;

        /* Artist mSort order Z-A */
        String ARTIST_Z_A = ARTIST_A_Z + " DESC";

        /* Artist mSort order number of songs */
        String ARTIST_NUMBER_OF_SONGS = MediaStore.Audio.Artists.NUMBER_OF_TRACKS
                + " DESC";

        /* Artist mSort order number of albums */
        String ARTIST_NUMBER_OF_ALBUMS = MediaStore.Audio.Artists.NUMBER_OF_ALBUMS
                + " DESC";
    }

    /**
     * Album mSort order entries.
     */
    public interface AlbumSortOrder {
        /* Album mSort order A-Z */
        String ALBUM_A_Z = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;

        /* Album mSort order Z-A */
        String ALBUM_Z_A = ALBUM_A_Z + " DESC";

        /* Album mSort order songs */
        String ALBUM_NUMBER_OF_SONGS = MediaStore.Audio.Albums.NUMBER_OF_SONGS
                + " DESC";

        /* Album mSort order mArtist */
        String ALBUM_ARTIST = MediaStore.Audio.Albums.ARTIST;

        /* Album mSort order year */
        String ALBUM_YEAR = MediaStore.Audio.Albums.FIRST_YEAR + " DESC";

    }

    /**
     * Music mSort order entries.
     */
    public interface SongSortOrder {
        /* Music mSort order A-Z */
        String SONG_A_Z = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;

        /* Music mSort order Z-A */
        String SONG_Z_A = SONG_A_Z + " DESC";

        /* Music mSort order mArtist */
        String SONG_ARTIST = MediaStore.Audio.Media.ARTIST;

        /* Music mSort order album */
        String SONG_ALBUM = MediaStore.Audio.Media.ALBUM;

        /* Music mSort order year */
        String SONG_YEAR = MediaStore.Audio.Media.YEAR + " DESC";

        /* Music mSort order mDuration */
        String SONG_DURATION = MediaStore.Audio.Media.DURATION + " DESC";

        /* Music mSort order date */
        String SONG_DATE = MediaStore.Audio.Media.DATE_ADDED + " DESC";

        /* Music mSort order filename */
        String SONG_FILENAME = MediaStore.Audio.Media.DATA;
    }

    /**
     * Album song mSort order entries.
     */
    public interface AlbumSongSortOrder {
        /* Album song mSort order A-Z */
        String SONG_A_Z = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;

        /* Album song mSort order Z-A */
        String SONG_Z_A = SONG_A_Z + " DESC";

        /* Album song mSort order track list */
        String SONG_TRACK_LIST = MediaStore.Audio.Media.TRACK + ", "
                + MediaStore.Audio.Media.DEFAULT_SORT_ORDER;

        /* Album song mSort order mDuration */
        String SONG_DURATION = SongSortOrder.SONG_DURATION;

        /* Album Music mSort order year */
        String SONG_YEAR = MediaStore.Audio.Media.YEAR + " DESC";

        /* Album song mSort order filename */
        String SONG_FILENAME = SongSortOrder.SONG_FILENAME;
    }

    /**
     * Artist song mSort order entries.
     */
    public interface ArtistSongSortOrder {
        /* Artist song mSort order A-Z */
        String SONG_A_Z = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;

        /* Artist song mSort order Z-A */
        String SONG_Z_A = SONG_A_Z + " DESC";

        /* Artist song mSort order album */
        String SONG_ALBUM = MediaStore.Audio.Media.ALBUM;

        /* Artist song mSort order year */
        String SONG_YEAR = MediaStore.Audio.Media.YEAR + " DESC";

        /* Artist song mSort order mDuration */
        String SONG_DURATION = MediaStore.Audio.Media.DURATION + " DESC";

        /* Artist song mSort order date */
        String SONG_DATE = MediaStore.Audio.Media.DATE_ADDED + " DESC";

        /* Artist song mSort order filename */
        String SONG_FILENAME = SongSortOrder.SONG_FILENAME;
    }

    /**
     * Artist album mSort order entries.
     */
    public interface ArtistAlbumSortOrder {
        /* Artist album mSort order A-Z */
        String ALBUM_A_Z = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;

        /* Artist album mSort order Z-A */
        String ALBUM_Z_A = ALBUM_A_Z + " DESC";

        /* Artist album mSort order songs */
        String ALBUM_NUMBER_OF_SONGS = MediaStore.Audio.Artists.Albums.NUMBER_OF_SONGS
                + " DESC";

        /* Artist album mSort order year */
        String ALBUM_YEAR = MediaStore.Audio.Artists.Albums.FIRST_YEAR
                + " DESC";
    }

    public interface FolderSortOrder {
        String FOLDER_A_Z = "folder_az";
        String FOLDER_NUMBER = "folder_number";
    }

}
