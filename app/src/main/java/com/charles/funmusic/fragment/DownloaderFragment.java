package com.charles.funmusic.fragment;

import android.os.Bundle;

import com.charles.funmusic.R;

public class DownloaderFragment extends BaseFragment {

    public static DownloaderFragment newInstance() {
        DownloaderFragment fragment = new DownloaderFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_downloader;
    }

    @Override
    public void init(Bundle savedInstanceState) {

    }
}
