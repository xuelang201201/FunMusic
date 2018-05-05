package com.charles.funmusic.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.activity.MultipleActivity;
import com.charles.funmusic.activity.PlayingActivity;
import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.constant.Keys;
import com.charles.funmusic.enums.PlayModeEnum;
import com.charles.funmusic.model.Music;
import com.charles.funmusic.service.MusicPlayer;
import com.charles.funmusic.service.MusicService;
import com.charles.funmusic.utils.FileUtil;
import com.charles.funmusic.utils.HandlerUtil;
import com.charles.funmusic.utils.MusicUtil;
import com.charles.funmusic.utils.Preferences;
import com.charles.funmusic.utils.SortOrder;
import com.charles.funmusic.utils.comparator.MusicComparator;
import com.charles.funmusic.widget.SideBar;
import com.charles.funmusic.widget.TintImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SingleFragment extends BaseFragment {

    @BindView(R.id.fragment_common_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.fragment_common_empty)
    TextView mEmpty;
    @BindView(R.id.fragment_common_index_bar)
    SideBar mSideBar;
    @BindView(R.id.fragment_common_letter)
    TextView mLetter;
    @BindView(R.id.load_frame)
    FrameLayout mFrameLayout;

    private View mView;
    private MusicAdapter mAdapter;
    private static final String TAG = SingleFragment.class.getSimpleName();
    private boolean mIsAZSort = true;
    private Preferences mPreferences;
    private HashMap<String, Integer> mPositionMap = new HashMap<>();
    private boolean isFirstLoad = true;

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                mSideBar.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    public int getLayoutId() {
        return 0;
    }

    @Override
    public void init(Bundle savedInstanceState) {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = Preferences.getInstance(mContext);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_load_frame, container, false);
        mFrameLayout = view.findViewById(R.id.load_frame);
        View loadView = LayoutInflater.from(mContext).inflate(R.layout.layout_loading, mFrameLayout, false);
        mFrameLayout.addView(loadView);

        isFirstLoad = true;
        mIsAZSort = mPreferences.getSongSortOrder().equals(SortOrder.SongSortOrder.SONG_A_Z);

        if (getUserVisibleHint()) {
            loadView();
        }

        return view;
    }

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
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            loadView();
        }
    }

    private void loadView() {
        if (mView == null && mContext != null) {
            mView = LayoutInflater.from(mContext).inflate(R.layout.fragment_common, mFrameLayout, false);

            mLetter = mView.findViewById(R.id.fragment_common_letter);
            mRecyclerView = mView.findViewById(R.id.fragment_common_recycler_view);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            mAdapter = new MusicAdapter(null);
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setHasFixedSize(true);

            mSideBar = mView.findViewById(R.id.fragment_common_index_bar);
            mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
                @Override
                public void onTouchingLetterChanged(String s) {
                    mLetter.setText(s);
                    mSideBar.setView(mLetter);
                    if (mPositionMap.get(s) != null) {
                        int i = mPositionMap.get(s);
                        ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                                .scrollToPositionWithOffset(i + 1, 0);
                    }
                }
            });
            reloadAdapter();
            Log.e("MusicFragment", "load l");
        }
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void reloadAdapter() {
        if (mAdapter == null) {
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                mIsAZSort = mPreferences.getSongSortOrder().equals(SortOrder.SongSortOrder.SONG_A_Z);
                ArrayList<Music> songList = (ArrayList) MusicUtil.queryMusics(mContext, Keys.START_FROM_LOCAL);
                // 名称排序时，重新排序并加入位置信息
                if (mIsAZSort) {
                    Collections.sort(songList, new MusicComparator());
                    for (int i = 0; i < songList.size(); i++) {
                        if (mPositionMap.get(songList.get(i).getSort()) == null) {
                            mPositionMap.put(songList.get(i).getSort(), i);
                        }
                    }
                }
                mAdapter.updateDataSet(songList);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mAdapter.notifyDataSetChanged();
                if (mIsAZSort) {
                    mRecyclerView.addOnScrollListener(mOnScrollListener);
                } else {
                    mSideBar.setVisibility(View.INVISIBLE);
                    mRecyclerView.removeOnScrollListener(mOnScrollListener);
                }
                Log.e("MusicFragment", "load t");
                if (isFirstLoad) {
                    Log.e("MusicFragment", "load");
                    mFrameLayout.removeAllViews();
                    // frameLayout 创建了新的实例
                    ViewGroup p = (ViewGroup) mView.getParent();
                    if (p != null) {
                        p.removeAllViewsInLayout();
                    }
                    mFrameLayout.addView(mView);
                    isFirstLoad = false;
                }
            }
        }.execute();
    }

    public class MusicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        final static int FIRST_ITEM = 0;
        final static int ITEM = 1;
        private ArrayList<Music> mMusics;
        PlayMusic mPlayMusic;
        Handler mHandler;

        MusicAdapter(ArrayList<Music> musics) {
            mHandler = HandlerUtil.getInstance(mContext);
            mMusics = musics;
        }

        /**
         * 更新adapter数据
         */
        private void updateDataSet(ArrayList<Music> musics) {
            mMusics = musics;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == FIRST_ITEM) {
                return new CommonItemViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.header, parent, false));
            } else {
                return new ListItemViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.music_item, parent, false));
            }
        }

        /**
         * 判断布局类型
         */
        @Override
        public int getItemViewType(int position) {
            return position == FIRST_ITEM ? FIRST_ITEM : ITEM;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Music music = null;
            if (position > 0) {
                music = mMusics.get(position - 1);
            }
            if (holder instanceof ListItemViewHolder) {
                ((ListItemViewHolder) holder).mTitle.setText(music.getTitle());
                ((ListItemViewHolder) holder).mArtistAndAlbum.setText(FileUtil.getArtistAndAlbum(music.getArtist(), music.getAlbum()));

                // 判断该条目是否在播放
                if (MusicPlayer.getCurrentAudioId() == music.getId()) {
                    ((ListItemViewHolder) holder).mPlayState.setVisibility(View.VISIBLE);
                    ((ListItemViewHolder) holder).mPlayState.setImageResource(R.drawable.playing);
//                    ((ListItemViewHolder) holder).mPlayState.setImageTintList(R.color.theme_color_primary);
                } else {
                    ((ListItemViewHolder) holder).mPlayState.setVisibility(View.GONE);
                }
            } else if (holder instanceof CommonItemViewHolder) {
                String musicSize = "（共" + mMusics.size() + "首）";
                ((CommonItemViewHolder) holder).mTextView.setText(musicSize);

                ((CommonItemViewHolder) holder).mSelect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        multiple(-1);
                    }
                });
            }
        }

        /**
         * 多选
         */
        private void multiple(int position) {
            Intent intent = new Intent(mContext, MultipleActivity.class);
            intent.putParcelableArrayListExtra("ids", mMusics);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.putExtra("position", position);
            mContext.startActivity(intent);
        }

        @Override
        public int getItemCount() {
            return null != mMusics ? mMusics.size() + 1 : 0;
        }

        public class CommonItemViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener {
            @BindView(R.id.header_play_all_number)
            TextView mTextView;
            @BindView(R.id.header_select)
            ImageView mSelect;

            CommonItemViewHolder(View itemView) {
                super(itemView);

                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(this);

                changeFont(mTextView, false);
            }

            @Override
            public void onClick(View v) {
                if (mPlayMusic != null) {
                    mHandler.removeCallbacks(mPlayMusic);
                }
                if (getAdapterPosition() > -1) {
                    mPlayMusic = new PlayMusic(0);
                    mHandler.postDelayed(mPlayMusic, 70);
                }
            }
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder
                implements View.OnClickListener, View.OnLongClickListener {

            @BindView(R.id.music_item_title)
            TextView mTitle;
            @BindView(R.id.music_item_artist_and_album)
            TextView mArtistAndAlbum;
            @BindView(R.id.music_item_play_state)
            TintImageView mPlayState;
            @BindView(R.id.music_item_more)
            TintImageView mMoreOverFlow;

            ListItemViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);

                mMoreOverFlow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MoreFragment moreFragment = MoreFragment.newInstance(
                                mMusics.get(getAdapterPosition() - 1), Keys.MUSIC_OVERFLOW);
                        moreFragment.show(getFragmentManager(), "music");
                    }
                });
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);

                changeFont(mTitle, false);
                changeFont(mArtistAndAlbum, false);
            }

            @Override
            public void onClick(View v) {
                int position = getAdapterPosition() - 1;

                if (mPlayMusic != null) {
                    mHandler.removeCallbacks(mPlayMusic);
                }
                // 点击播放
                if (getAdapterPosition() > -1) {
                    mPlayMusic = new PlayMusic(position);
                    mHandler.postDelayed(mPlayMusic, 60);
                }
                // 正在播放点击跳转播放界面
                if (mMusics.get(position).getId() == MusicPlayer.getCurrentAudioId()) {
                    Intent intent = new Intent(AppCache.getContext(), PlayingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    AppCache.getContext().startActivity(intent);
                }
            }

            @Override
            public boolean onLongClick(View v) {
                multiple(getAdapterPosition() - 1);
                return false;
            }
        }

        class PlayMusic implements Runnable {
            int mPosition;

            PlayMusic(int position) {
                mPosition = position;
            }

            @Override
            public void run() {
                long[] musics = new long[mMusics.size()];
                HashMap<Long, Music> map = new HashMap();
                for (int i = 0; i < mMusics.size(); i++) {
                    Music music = mMusics.get(i);
                    musics[i] = music.getId();
                    music.setLocal(true);
                    music.setAlbumArt(MusicUtil.getAlbumArtUri(music.getAlbumId()) + "");
                    map.put(musics[i], mMusics.get(i));
                }
                if (mPosition > -1) {
                    MusicPlayer.playAll(map, musics, mPosition, false);
                }
            }
        }
    }
}
