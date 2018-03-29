package com.charles.funmusic.lastfmapi;

import android.content.Context;
import android.util.Log;

import com.charles.funmusic.lastfmapi.callbacks.ArtistInfoListener;
import com.charles.funmusic.lastfmapi.models.AlbumInfo;
import com.charles.funmusic.lastfmapi.models.AlbumQuery;
import com.charles.funmusic.lastfmapi.models.ArtistInfo;
import com.charles.funmusic.lastfmapi.models.ArtistQuery;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LastFmClient {

    public static final String BASE_API_URL = "http://ws.audioscrobbler.com/2.0";
    private static final Object sLock = new Object();
    private static LastFmClient sInstance;
    private LastFmRestService mRestService;

    public static LastFmClient getInstance(Context context) {
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new LastFmClient();
                sInstance.mRestService = RestServiceFactory.create(context, BASE_API_URL, LastFmRestService.class);
            }
            return sInstance;
        }
    }

    public void getAlbumInfo(AlbumQuery albumQuery) {
        mRestService.getAlbumInfo(albumQuery.mArtist, albumQuery.mALbum, new Callback<AlbumInfo>() {
            @Override
            public void success(AlbumInfo albumInfo, Response response) {

            }

            @Override
            public void failure(RetrofitError error) {

                error.printStackTrace();
            }
        });
    }

    public void getArtistInfo(ArtistQuery artistQuery, final ArtistInfoListener listener) {
        mRestService.getArtistInfo(artistQuery.mArtist, new Callback<ArtistInfo>() {
            @Override
            public void success(ArtistInfo artistInfo, Response response) {
                listener.artistInfoSuccess(artistInfo.mArtist);
            }

            @Override
            public void failure(RetrofitError error) {
                listener.artistInfoFailed();
                Log.d("lol", "failed");
                error.printStackTrace();
            }
        });
    }
}