package com.charles.funmusic.fragment;

import android.os.Bundle;

import com.charles.funmusic.R;

public class FavoriteFragment extends BaseFragment {

    public static FavoriteFragment newInstance() {
        FavoriteFragment fragment = new FavoriteFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_favorite;
    }

    @Override
    public void init(Bundle savedInstanceState) {

    }
}
