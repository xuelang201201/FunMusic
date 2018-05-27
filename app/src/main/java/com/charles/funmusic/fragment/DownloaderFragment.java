package com.charles.funmusic.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.charles.funmusic.R;

import butterknife.ButterKnife;

public class DownloaderFragment extends BaseFragment {

    public static DownloaderFragment newInstance() {
        DownloaderFragment fragment = new DownloaderFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_downloader, container, false);
        ButterKnife.bind(this, view);

        init();
        return view;
    }

    private void init() {
    }
}
