package com.charles.funmusic.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.activity.UserInfoActivity;
import com.charles.funmusic.adapter.AutoLinearLayoutManager;
import com.charles.funmusic.adapter.MusicAdapter;
import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.constant.Keys;
import com.charles.funmusic.model.Music;
import com.charles.funmusic.widget.CircleImageView;
import com.charles.funmusic.widget.SuspendScrollView;

import java.util.Random;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 本地音乐列表和一些基础设置
 */
public class MyFragment extends BaseFragment implements SuspendScrollView.OnScrollListener, IOnFocusListenable {

    @BindView(R.id.fragment_my_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.fragment_my_random_play)
    TextView mRandomPlay;
    @BindView(R.id.fragment_my_empty)
    TextView mEmpty;
    @BindView(R.id.fragment_my_avatar)
    CircleImageView mAvatar;
    @BindView(R.id.fragment_my_username)
    TextView mUsername;
    @BindView(R.id.fragment_my_can_hide_layout)
    LinearLayout mCanHideLayout;
    @BindView(R.id.fragment_my_sort)
    ImageView mSort;
    @BindView(R.id.fragment_my_display)
    ImageView mDisplay;
    @BindView(R.id.fragment_my_suspend_child_layout)
    LinearLayout mSuspendChildLayout;
    @BindView(R.id.fragment_my_suspend_layout)
    LinearLayout mSuspendLayout;
    @BindView(R.id.fragment_my_scrollview)
    SuspendScrollView mScrollview;
    @BindView(R.id.fragment_my_top_layout)
    LinearLayout mTopLayout;
    @BindView(R.id.fragment_my_user_info_layout)
    LinearLayout mUserInfoLayout;
    @BindView(R.id.fragment_my_top_layout_divider)
    View mTopDivider;

    private MusicAdapter mAdapter;

    private int mTop;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_my;
    }

    @Override
    public void initView() {
        if (getPlayService().getPlayingMusic() != null && getPlayService().getPlayingMusic().getType() == Music.Type.LOCAL) {
            mRecyclerView.smoothScrollToPosition(getPlayService().getPlayingPosition());
        }
        updateView();
        initRecyclerView();

        changeFont(mRandomPlay, true);
    }

    private void updateView() {
        if (AppCache.getMusics().isEmpty()) {
            mEmpty.setVisibility(View.VISIBLE);
        } else {
            mEmpty.setVisibility(View.GONE);
        }
    }

    private void initRecyclerView() {
        mRecyclerView.setLayoutManager(new AutoLinearLayoutManager(getActivity()));
        mScrollview.setOnScrollListener(this);
        mScrollview.smoothScrollTo(0, 0);
        mRecyclerView.setNestedScrollingEnabled(false);
        mAdapter = new MusicAdapter(getActivity(), new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                getPlayService().play(position);
            }
        }, new MusicAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        addFooterView(mRecyclerView);
    }

    private void addFooterView(RecyclerView recyclerView) {
        View footer = LayoutInflater.from(getActivity()).inflate(R.layout.footer_view, recyclerView, false);
        mAdapter.setFooterView(footer);
        TextView footerText = footer.findViewById(R.id.footer_view_text);
        String size = "共" + AppCache.getMusics().size() + "首歌曲";
        footerText.setText(size);
        changeFont(footerText, false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        int position = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        int offset = (mRecyclerView.getChildAt(0) == null) ? 0 : mRecyclerView.getChildAt(0).getTop();
        outState.putInt(Keys.LOCAL_MUSIC_POSITION, position);
        outState.putInt(Keys.LOCAL_MUSIC_OFFSET, offset);
        super.onSaveInstanceState(outState);
    }

    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                int position = savedInstanceState.getInt(Keys.LOCAL_MUSIC_POSITION);
                int offset = savedInstanceState.getInt(Keys.LOCAL_MUSIC_OFFSET);
                ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(position, offset);
            }
        });
    }

    @OnClick({R.id.fragment_my_random_play, R.id.fragment_my_sort,
            R.id.fragment_my_display, R.id.fragment_my_user_info_layout})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_my_random_play:
                int position = new Random().nextInt(AppCache.getMusics().size());
                getPlayService().play(position);
                break;

            case R.id.fragment_my_sort:
                showSortDialog();
                break;

            case R.id.fragment_my_display:
                break;

            case R.id.fragment_my_user_info_layout:
                Intent intent = new Intent(getActivity(), UserInfoActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void showSortDialog() {
        SortDialogFragment sortDialog = new SortDialogFragment();
        sortDialog.show(getFragmentManager(), "sort");
    }

    public void onItemPlay() {
        updateView();
        if (getPlayService().getPlayingMusic().getType() == Music.Type.LOCAL) {
            mRecyclerView.smoothScrollToPosition(getPlayService().getPlayingPosition());
        }
    }

    public void onMusicListUpdate() {
        updateView();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            mTop = mCanHideLayout.getBottom(); // 获取suspendLayout的顶部位置
        }
    }

    /**
     * 监听滚动Y值变化，通过addView和removeView来实现悬停效果
     * @param scrollY Y轴滚动距离
     */
    @Override
    public void onScroll(int scrollY) {
        if (scrollY >= mTop) {
            if (mSuspendChildLayout.getParent() != mTopLayout) {
                mSuspendLayout.removeView(mSuspendChildLayout);
                mTopLayout.addView(mSuspendChildLayout);
                mTopDivider.setVisibility(View.VISIBLE);
            }
        } else {
            if (mSuspendChildLayout.getParent() != mSuspendLayout) {
                mTopLayout.removeView(mSuspendChildLayout);
                mSuspendLayout.addView(mSuspendChildLayout);
                mTopDivider.setVisibility(View.INVISIBLE);
            }
        }
    }
}