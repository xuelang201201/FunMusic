package com.charles.funmusic.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.charles.funmusic.R;
import com.charles.funmusic.fragment.ArtistFragment;
import com.charles.funmusic.fragment.LocalMusicFragment;

public class LocalMusicActivity extends BaseActivity {

    private int mPage, mArtistId, mAlbumId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() != null) {
            mPage = getIntent().getIntExtra("page_number", 0);
            mArtistId = getIntent().getIntExtra("artist", 0);
            mAlbumId = getIntent().getIntExtra("album", 0);
        }
        setContentView(R.layout.activity_local_music);

        if (mArtistId != 0) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            ArtistFragment fragment = ArtistFragment.newInstance(mArtistId);
            transaction.hide(getSupportFragmentManager().findFragmentById(R.id.activity_local_music_tab_container));
            transaction.add(R.id.activity_local_music_tab_container, fragment);
            transaction.addToBackStack(null).commit();
        }

        if (mAlbumId != 0) {

        }

        String[] title = {"单曲", "歌手", "专辑", "文件夹"};
        LocalMusicFragment fragment = LocalMusicFragment.newInstance(mPage, title);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.activity_local_music_tab_container, fragment);
        transaction.commitAllowingStateLoss();
    }
}
