package com.charles.funmusic.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.adapter.MusicAdapter;
import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.constant.Keys;
import com.charles.funmusic.model.Music;

import java.util.Random;

import butterknife.BindView;
import butterknife.Unbinder;

public class SingleFragment extends BaseFragment {

    @BindView(R.id.fragment_single_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.fragment_single_empty)
    TextView mEmpty;
    Unbinder unbinder;

    private MusicAdapter mAdapter;
    private static final String TAG = SingleFragment.class.getSimpleName();

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        int position = mRecyclerView.getScrollY();
        int offset = (mRecyclerView.getChildAt(0) == null) ? 0 : mRecyclerView.getChildAt(0).getTop();

        if (mView != null) {
            outState.putInt(Keys.LOCAL_MUSIC_POSITION, position);
            outState.putInt(Keys.LOCAL_MUSIC_OFFSET, offset);
        }
        Log.d(TAG, mView + ", onSaveInstanceState: " + outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_single;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        if (getPlayService().getPlayingMusic() != null && getPlayService().getPlayingMusic().getType() == Music.Type.LOCAL) {
            mRecyclerView.smoothScrollToPosition(getPlayService().getPlayingPosition());
        }

        initRecyclerView(savedInstanceState);
        updateView();
    }

    private void initRecyclerView(Bundle savedInstanceState) {

        mRecyclerView.setLayoutManager(new LinearLayoutManager(AppCache.getContext()));
        mAdapter = new MusicAdapter(AppCache.getContext(), new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                AppCache.getPlayService().play(position - 1);
            }
        }, new MusicAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {

            }
        });

        mRecyclerView.setAdapter(mAdapter);

        Log.d(TAG, "onCreateView state: " + savedInstanceState);
        if (savedInstanceState != null) {
            int position = savedInstanceState.getInt(Keys.LOCAL_MUSIC_POSITION);
            int offset = savedInstanceState.getInt(Keys.LOCAL_MUSIC_OFFSET);
            mRecyclerView.setScrollY(position);
            ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(position, offset);
        }

        if (!AppCache.getMusics().isEmpty()) {
            addHeaderView(mRecyclerView);
            addFooterView(mRecyclerView);
        }
    }

    private void updateView() {
        if (AppCache.getMusics().isEmpty()) {
            mEmpty.setVisibility(View.INVISIBLE);
        } else {
            mEmpty.setVisibility(View.GONE);
        }

        mAdapter.updatePlayingPosition(getPlayService());
        mAdapter.notifyDataSetChanged();
    }

    private void addHeaderView(RecyclerView recyclerView) {
        View header = LayoutInflater.from(getActivity()).inflate(R.layout.header, recyclerView, false);
        mAdapter.setHeaderView(header);
        TextView randomPlay = header.findViewById(R.id.header_random_play);
        changeFont(randomPlay, false);
        randomPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long seed = System.nanoTime();
                int position = new Random(seed).nextInt(AppCache.getMusics().size());
                getPlayService().play(position);
            }
        });
        ImageView sort = header.findViewById(R.id.header_sort);
        sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortDialog();
            }
        });
    }

    private void showSortDialog() {
        if (getFragmentManager() != null) {
            SortDialogFragment sortDialog = new SortDialogFragment();
            sortDialog.show(getFragmentManager(), "sort");
        }
    }

    private void addFooterView(RecyclerView recyclerView) {
        View footer = LayoutInflater.from(getActivity()).inflate(R.layout.footer, recyclerView, false);
        mAdapter.setFooterView(footer);
        TextView footerText = footer.findViewById(R.id.footer_view_text);
        String size = "共" + AppCache.getMusics().size() + "首歌曲";
        footerText.setText(size);
        changeFont(footerText, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
