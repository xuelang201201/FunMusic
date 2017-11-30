package com.charles.funmusic.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.service.EventCallback;
import com.charles.funmusic.widget.CircleImageView;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 本地音乐列表和一些基础设置
 */
public class MyFragment extends BaseFragment implements EventCallback {

    public static final int SHOW_LOCAL_MUSIC_FRAGMENT = 0;
    public static final int SHOW_RECENT_PLAY_FRAGMENT = 1;
    public static final int SHOW_DOWNLOADER_FRAGMENT = 2;
    public static final int SHOW_FAVORITE_FRAGMENT = 3;
    public static final int SHOW_MUSIC_LIST_FRAGMENT = 4;
    public static final int SHOW_PLAY_FRAGMENT = 5;

    @BindView(R.id.fragment_my_avatar)
    CircleImageView mAvatar;
    @BindView(R.id.fragment_my_username)
    TextView mUsername;
    @BindView(R.id.fragment_my_user_info_layout)
    LinearLayout mUserInfoLayout;
    @BindView(R.id.fragment_my_local)
    LinearLayout mLocal;
    @BindView(R.id.fragment_my_local_text)
    TextView mLocalText;
    @BindView(R.id.fragment_my_recent)
    LinearLayout mRecent;
    @BindView(R.id.fragment_my_recent_text)
    TextView mRecentText;
    @BindView(R.id.fragment_my_download)
    LinearLayout mDownload;
    @BindView(R.id.fragment_my_download_text)
    TextView mDownloadText;
    @BindView(R.id.fragment_my_favorite)
    LinearLayout mFavorite;
    @BindView(R.id.fragment_my_favorite_text)
    TextView mFavoriteText;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_my;
    }

    @Override
    public void initView(Bundle savedInstanceState) {

    }

    @OnClick({R.id.fragment_my_local, R.id.fragment_my_recent, R.id.fragment_my_download, R.id.fragment_my_favorite})
    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_my_local:
                onEvent(SHOW_LOCAL_MUSIC_FRAGMENT);
                break;

            case R.id.fragment_my_recent:
                onEvent(SHOW_RECENT_PLAY_FRAGMENT);
                break;

            case R.id.fragment_my_download:
                onEvent(SHOW_DOWNLOADER_FRAGMENT);
                break;

            case R.id.fragment_my_favorite:
                onEvent(SHOW_FAVORITE_FRAGMENT);
                break;
        }
    }

    /**
     * 在触发事件的地方调用接口，给其设置参数
     * @param o 要启动的fragment的索引
     */
    @SuppressWarnings("unchecked")
    @Override
    public void onEvent(Object o) {
        mListener.onEvent(o);
    }
}