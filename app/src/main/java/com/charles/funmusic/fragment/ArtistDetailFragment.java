package com.charles.funmusic.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.activity.MultipleActivity;
import com.charles.funmusic.constant.Keys;
import com.charles.funmusic.model.Artist;
import com.charles.funmusic.model.Music;
import com.charles.funmusic.service.MusicPlayer;
import com.charles.funmusic.utils.HandlerUtil;
import com.charles.funmusic.utils.MusicUtil;
import com.charles.funmusic.widget.SideBar;
import com.charles.funmusic.widget.TintImageView;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArtistDetailFragment extends BaseFragment {

    @BindView(R.id.header_view)
    View mHeaderView;
    @BindView(R.id.fragment_common_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.fragment_common_empty)
    TextView mEmpty;
    @BindView(R.id.fragment_common_index_bar)
    SideBar mSideBar;
    @BindView(R.id.fragment_common_letter)
    TextView mLetter;
    @BindView(R.id.header_view_image_view)
    ImageView mBack;
    @BindView(R.id.header_view_title_text_view)
    TextView mTitle;
    @BindView(R.id.header_view_text_right)
    TextView mRight;

    private long mArtistId = -1;
    private ArtistDetailAdapter mAdapter;

    public static ArtistDetailFragment newInstance(long id) {
        ArtistDetailFragment fragment = new ArtistDetailFragment();
        Bundle args = new Bundle();
        args.putLong("artist_id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mArtistId = getArguments().getLong("artist_id");
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_common;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        mHeaderView.setVisibility(View.VISIBLE);

        Artist artist = MusicUtil.getArtists(mContext, mArtistId);
        mTitle.setText(artist.getArtist());
        changeFont(mTitle, true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new ArtistDetailAdapter(null);
        mRecyclerView.setAdapter(mAdapter);
        reloadAdapter();

        initSystemBar(mHeaderView);

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void reloadAdapter() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                ArrayList<Music> artists = MusicUtil.queryMusics(mContext, mArtistId + "", Keys.START_FROM_ARTIST);
                mAdapter.updateDataSet(artists);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    class ArtistDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final static int FIRST_ITEM = 0;
        private final static int ITEM = 1;

        private ArrayList<Music> mMusics;

        ArtistDetailAdapter(ArrayList<Music> musics) {
            mMusics = musics;
        }

        /**
         * 更新adapter的数据
         */
        void updateDataSet(ArrayList<Music> musics) {
            mMusics = musics;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof CommonItemViewHolder) {
                String text = "（共" + mMusics.size() + "首）";
                ((CommonItemViewHolder) holder).mPlayAll.setText(text);
                ((CommonItemViewHolder) holder).mSelect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, MultipleActivity.class);
                        intent.putParcelableArrayListExtra("ids", mMusics);
                        mContext.startActivity(intent);
                    }
                });
            }
            if (holder instanceof ListItemViewHolder) {
                Music music = mMusics.get(position - 1);
                ((ListItemViewHolder) holder).mTitle.setText(music.getTitle());
                ((ListItemViewHolder) holder).mArtist.setText(music.getArtist());
                // 判断该条目音乐是否在播放
                if (MusicPlayer.getCurrentAudioId() == music.getId()) {
                    ((ListItemViewHolder) holder).mPlayState.setVisibility(View.VISIBLE);
                    ((ListItemViewHolder) holder).mPlayState.setImageResource(R.drawable.playing);
//                    ((ListItemViewHolder) holder).mPlayState.setImageTintList(R.color.theme_color_primary);
                } else {
                    ((ListItemViewHolder) holder).mPlayState.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return null != mMusics ? mMusics.size() + 1 : 0;
        }

        class CommonItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            @BindView(R.id.header_play_all_number)
            TextView mPlayAll;
            @BindView(R.id.header_select)
            ImageView mSelect;

            CommonItemViewHolder(View itemView) {
                super(itemView);

                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(this);

                changeFont(mPlayAll, false);
            }

            /**
             * 播放歌手所有歌曲
             */
            @Override
            public void onClick(View v) {
                HandlerUtil.getInstance(mContext).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        long[] musics = new long[mMusics.size()];
                        @SuppressLint("UseSparseArrays") HashMap<Long, Music> maps = new HashMap<>();
                        for (int i = 0; i < mMusics.size(); i++) {
                            Music music = mMusics.get(i);
                            musics[i] = music.getId();
                            music.setLocal(true);
                            music.setAlbumArt(MusicUtil.getAlbumArtUri(music.getId()) + "");
                            maps.put(musics[i], mMusics.get(i));
                        }
                        MusicPlayer.playAll(maps, musics, 0, false);
                    }
                }, 70);
            }
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            @BindView(R.id.music_item_more)
            ImageView mMoreOverFlow;
            @BindView(R.id.music_item_title)
            TextView mTitle;
            @BindView(R.id.music_item_artist_and_album)
            TextView mArtist;
            @BindView(R.id.music_item_play_state)
            TintImageView mPlayState;

            public ListItemViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);

                itemView.setOnClickListener(this);
                // 设置弹出菜单
                mMoreOverFlow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MoreFragment moreFragment = MoreFragment.newInstance(
                                mMusics.get(getAdapterPosition() - 1), Keys.MUSIC_OVERFLOW);
                        moreFragment.show(getFragmentManager(), "music");
                    }
                });

                changeFont(mTitle, false);
                changeFont(mArtist, false);
            }

            @Override
            public void onClick(View v) {
                HandlerUtil.getInstance(mContext).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        long[] musics = new long[mMusics.size()];
                        @SuppressLint("UseSparseArrays") HashMap<Long, Music> maps = new HashMap<>();
                        for (int i = 0; i < mMusics.size(); i++) {
                            Music music = mMusics.get(i);
                            musics[i] = music.getId();
                            music.setLocal(true);
                            music.setAlbumArt(MusicUtil.getAlbumArtUri(music.getAlbumId()) + "");
                            maps.put(musics[i], mMusics.get(i));
                        }
                        if (getAdapterPosition() > 0) {
                            MusicPlayer.playAll(maps, musics, getAdapterPosition() - 1, false);
                        }
                    }
                }, 60);
            }
        }
    }
}
