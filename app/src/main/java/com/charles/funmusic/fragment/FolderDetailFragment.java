package com.charles.funmusic.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.activity.SelectActivity;
import com.charles.funmusic.constant.Keys;
import com.charles.funmusic.model.Music;
import com.charles.funmusic.service.MusicPlayer;
import com.charles.funmusic.utils.FileUtil;
import com.charles.funmusic.utils.HandlerUtil;
import com.charles.funmusic.utils.MusicUtil;
import com.charles.funmusic.utils.SystemUtil;
import com.charles.funmusic.widget.SideBar;
import com.charles.funmusic.widget.TintImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FolderDetailFragment extends BaseFragment {
    @BindView(R.id.header_view)
    View mHeaderView;
    @BindView(R.id.header_view_image_view)
    ImageView mBack;
    @BindView(R.id.header_view_title_text_view)
    TextView mTitle;
    @BindView(R.id.fragment_common_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.fragment_common_empty)
    TextView mEmpty;
    @BindView(R.id.fragment_common_index_bar)
    SideBar mSideBar;
    @BindView(R.id.fragment_common_letter)
    TextView mLetter;
    Unbinder unbinder;
    private String mFolderPath;
    private FolderDetailAdapter mAdapter;

    public static FolderDetailFragment newInstance(String id, boolean useTransition, String transitionName) {
        FolderDetailFragment fragment = new FolderDetailFragment();
        Bundle args = new Bundle();
        args.putString("folder_path", id);
        args.putBoolean("transition", useTransition);
        if (useTransition) {
            args.putString("transition_name", transitionName);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFolderPath = getArguments().getString("folder_path");
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_common;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        mHeaderView.setVisibility(View.VISIBLE);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new FolderDetailAdapter(null);
        mRecyclerView.setAdapter(mAdapter);
        reloadAdapter();

        mRecyclerView.setHasFixedSize(true);
        String folder = mFolderPath.substring(mFolderPath.lastIndexOf(File.separator),
                mFolderPath.length());
        mTitle.setText(folder.substring(folder.lastIndexOf(File.separator) + 1, folder.length()));

        changeFont(mTitle, true);

        initSystemBar(mHeaderView);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void reloadAdapter() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                List<Music> folders = MusicUtil.queryMusics(mContext, mFolderPath, Keys.START_FROM_FOLDER);
                mAdapter.updateDataSet(folders);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    @OnClick(R.id.header_view_image_view)
    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.header_view_image_view:
                getActivity().onBackPressed();
                break;
        }
    }

    class FolderDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int FIRST_ITEM = 0;
        private static final int ITEM = 1;
        private List<Music> mMusics;

        FolderDetailAdapter(List<Music> musics) {
            mMusics = musics;
        }

        void updateDataSet(List<Music> musics) {
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
                String playAll = "（共" + mMusics.size() + "首）";
                ((CommonItemViewHolder) holder).mPlayAll.setText(playAll);

                ((CommonItemViewHolder) holder).mSelect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, SelectActivity.class);
                        intent.putParcelableArrayListExtra("ids", (ArrayList) mMusics);
                        mContext.startActivity(intent);
                    }
                });
            }
            if (holder instanceof ListItemViewHolder) {
                Music music = mMusics.get(position - 1);
                ((ListItemViewHolder) holder).mTitle.setText(music.getTitle());
                ((ListItemViewHolder) holder).mArtistAndAlbum.setText(
                        FileUtil.getArtistAndAlbum(music.getArtist(), music.getAlbum()));
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
            }

            //播放文件夹
            @Override
            public void onClick(View v) {
                HandlerUtil.getInstance(mContext).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        long[] list = new long[mMusics.size()];
                        HashMap<Long, Music> maps = new HashMap();
                        for (int i = 0; i < mMusics.size(); i++) {
                            Music music = mMusics.get(i);
                            list[i] = music.getId();
                            music.setLocal(true);
                            music.setAlbumArt(MusicUtil.getAlbumArtUri(music.getId()) + "");
                            maps.put(list[i], mMusics.get(i));
                        }
                        MusicPlayer.playAll(maps, list, 0, false);
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
            TextView mArtistAndAlbum;
            @BindView(R.id.music_item_play_state)
            TintImageView mPlayState;

            ListItemViewHolder(View itemView) {
                super(itemView);

                ButterKnife.bind(this, itemView);

                //设置弹出菜单
                mMoreOverFlow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MoreFragment moreFragment = MoreFragment.newInstance(
                                mMusics.get(getAdapterPosition() - 1), Keys.FOLDER_OVERFLOW);
                        moreFragment.show(getFragmentManager(), "music");
                    }
                });
                itemView.setOnClickListener(this);

                changeFont(mTitle, false);
                changeFont(mArtistAndAlbum, false);
            }

            //播放歌曲
            @Override
            public void onClick(View v) {
                HandlerUtil.getInstance(mContext).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        long[] musics = new long[mMusics.size()];
                        HashMap<Long, Music> maps = new HashMap();
                        for (int i = 0; i < mMusics.size(); i++) {
                            Music music = mMusics.get(i);
                            musics[i] = music.getId();
                            music.setLocal(true);
                            music.setAlbumArt(MusicUtil.getAlbumArtUri(music.getAlbumId()) + "");
                            maps.put(musics[i], mMusics.get(i));
                        }
                        if (getAdapterPosition() > 0)
                            MusicPlayer.playAll(maps, musics, getAdapterPosition() - 1, false);
                    }
                }, 70);
            }
        }
    }
}
