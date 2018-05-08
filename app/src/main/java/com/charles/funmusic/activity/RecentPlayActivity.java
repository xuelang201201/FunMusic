package com.charles.funmusic.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.constant.Keys;
import com.charles.funmusic.fragment.MoreFragment;
import com.charles.funmusic.model.Music;
import com.charles.funmusic.provider.RecentStore;
import com.charles.funmusic.service.MusicPlayer;
import com.charles.funmusic.utils.FileUtil;
import com.charles.funmusic.utils.HandlerUtil;
import com.charles.funmusic.utils.MusicUtil;
import com.charles.funmusic.utils.loader.SongLoader;
import com.charles.funmusic.utils.loader.TopTracksLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RecentPlayActivity extends BaseActivity {

    @BindView(R.id.header_view)
    View mHeaderView;
    @BindView(R.id.header_view_image_view)
    ImageView mBack;
    @BindView(R.id.header_view_title_text_view)
    TextView mTitle;
    @BindView(R.id.header_view_text_right)
    TextView mClearAll;
    @BindView(R.id.activity_recent_play_recycler_view)
    RecyclerView mRecyclerView;

    private RecentPlayAdapter mAdapter;
    private List<Music> mMusics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_play);

        mMusics = SongLoader.getSongsForCursor(TopTracksLoader.getCursor());

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);

        mClearAll.setVisibility(View.VISIBLE);
        mClearAll.setText(getString(R.string.clear_all));
        mTitle.setText(getString(R.string.recent_play));

        new loadSongs().execute("");

        initSystemBar(mHeaderView);
    }

    @Override
    public void updateTrack() {
        if (mAdapter != null) {
            mAdapter.updateDataSet(mMusics);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class loadSongs extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... strings) {
            mAdapter = new RecentPlayAdapter(mMusics);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String s) {
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @OnClick({R.id.header_view_image_view, R.id.header_view_text_right})
    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.header_view_image_view:
                onBackPressed();
                break;

            case R.id.header_view_text_right:
                RecentStore.getInstance(this).deleteAll();
                mAdapter.updateDataSet(mMusics);
                break;
        }
    }

    public class RecentPlayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final static int FIRST_ITEM = 0;
        private final static int ITEM = 1;
        private List<Music> mMusics;

        RecentPlayAdapter(List<Music> musics) {
            if (musics == null) {
                throw new IllegalArgumentException("model data must not be null");
            }
            mMusics = musics;
        }

        void updateDataSet(List<Music> musics) {
            mMusics = musics;
            notifyDataSetChanged();
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
                if (music != null) {
                    ((ListItemViewHolder) holder).mTitle.setText(music.getTitle());
                    ((ListItemViewHolder) holder).mArtistAndAlbum.setText(FileUtil.getArtistAndAlbum(music.getArtist(), music.getAlbum()));

                    // 判断该条目音乐是否在播放
                    if (MusicPlayer.getCurrentAudioId() == music.getId()) {
                        ((ListItemViewHolder) holder).mPlayState.setVisibility(View.VISIBLE);
                        ((ListItemViewHolder) holder).mPlayState.setImageResource(R.drawable.playing);
                    } else {
                        ((ListItemViewHolder) holder).mPlayState.setVisibility(View.GONE);
                    }
                }
            } else if (holder instanceof CommonItemViewHolder) {
                String playAll = "（共" + mMusics.size() + "首）";
                ((CommonItemViewHolder) holder).mPlayAll.setText(playAll);
                ((CommonItemViewHolder) holder).mSelect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(RecentPlayActivity.this, MultipleActivity.class);
                        intent.putParcelableArrayListExtra("ids", (ArrayList) mMusics);
                        startActivity(intent);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return null != mMusics ? mMusics.size() + 1 : 0;
        }

        public class CommonItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            @BindView(R.id.header_play_all_number)
            TextView mPlayAll;
            @BindView(R.id.header_select)
            ImageView mSelect;

            CommonItemViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                HandlerUtil.getInstance(RecentPlayActivity.this).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        long[] songs = new long[mMusics.size()];
                        @SuppressLint("UseSparseArrays") HashMap<Long, Music> maps = new HashMap<>();
                        for (int i = 0; i < mMusics.size(); i++) {
                            Music music = MusicUtil.getMusics(
                                    RecentPlayActivity.this, mMusics.get(i).getId());
                            if (music != null) {
                                songs[i] = music.getId();
                                music.setLocal(true);
                                music.setAlbumArt(MusicUtil.getAlbumArtUri(music.getAlbumId()) + "");
                            }
                            maps.put(songs[i], music);
                        }
                        MusicPlayer.playAll(maps, songs, 0, false);
                    }
                }, 70);
            }
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            @BindView(R.id.music_item_more)
            ImageView mMoreOverFlow;
            @BindView(R.id.music_item_play_state)
            ImageView mPlayState;
            @BindView(R.id.music_item_title)
            TextView mTitle;
            @BindView(R.id.music_item_artist_and_album)
            TextView mArtistAndAlbum;

            public ListItemViewHolder(View itemView) {
                super(itemView);

                ButterKnife.bind(this, itemView);
                mMoreOverFlow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MoreFragment moreFragment = MoreFragment.newInstance(
                                mMusics.get(getAdapterPosition() - 1), Keys.MUSIC_OVERFLOW);
                        moreFragment.show(getSupportFragmentManager(), "song");
                    }
                });
                itemView.setOnClickListener(this);

                changeFont(mTitle, false);
                changeFont(mArtistAndAlbum, false);
            }

            @Override
            public void onClick(View v) {
                HandlerUtil.getInstance(RecentPlayActivity.this).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        long[] musics = new long[mMusics.size()];
                        @SuppressLint("UseSparseArrays") HashMap<Long, Music> maps = new HashMap<>();
                        for (int i = 0; i < mMusics.size(); i++) {
                            Music music = MusicUtil.getMusics(
                                    RecentPlayActivity.this, mMusics.get(i).getId());
                            if (music != null) {
                                musics[i] = music.getId();
                                music.setLocal(true);
                                music.setAlbumArt(MusicUtil.getAlbumArtUri(music.getAlbumId()) + "");
                            }
                            maps.put(musics[i], music);
                        }
                        if (getAdapterPosition() > 0) {
                            MusicPlayer.playAll(maps, musics, getAdapterPosition() - 1, false);
                        }
                    }
                }, 70);
            }
        }
    }
}