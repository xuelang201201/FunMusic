package com.charles.funmusic.fragment;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.activity.DownloadActivity;
import com.charles.funmusic.activity.FavoriteActivity;
import com.charles.funmusic.activity.LocalMusicActivity;
import com.charles.funmusic.activity.PlaylistActivity;
import com.charles.funmusic.activity.PlaylistManagerActivity;
import com.charles.funmusic.activity.RecentPlayActivity;
import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.constant.Keys;
import com.charles.funmusic.model.MyFragmentItem;
import com.charles.funmusic.model.Playlist;
import com.charles.funmusic.provider.DownloadStore;
import com.charles.funmusic.provider.PlaylistInfo;
import com.charles.funmusic.utils.MusicUtil;
import com.charles.funmusic.utils.SystemUtil;
//import com.charles.funmusic.utils.ThemeUtils;
import com.charles.funmusic.utils.loader.TopTracksLoader;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 本地音乐列表和一些基础设置
 */
public class MyFragment extends BaseFragment {

    @BindView(R.id.fragment_my_recycler_view)
    RecyclerView mRecyclerView;
    /**
     * 下拉刷新layout
     */
    @BindView(R.id.fragment_my_swipe_refresh)
    SwipeRefreshLayout mSwipeRefresh;
    private MyFragmentAdapter mAdapter;
    private List<MyFragmentItem> mList = new ArrayList<>();
    /**
     * playlist管理类
     */
    private PlaylistInfo mPlaylistInfo;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            reloadAdapter();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPlaylistInfo = PlaylistInfo.getInstance(mContext);
        if (SystemUtil.isLollipop() && ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 0);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_my;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
//        mSwipeRefresh.setColorSchemeColors(ThemeUtils.getColorById(mContext, R.color.theme_color_primary));
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadAdapter();
            }
        });
        // 先给adapter设置空数据，异步加载好后更新数据，防止RecyclerView no attach
        mAdapter = new MyFragmentAdapter(mContext);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        // 设置没有item动画
        ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        reloadAdapter();

        ((Activity) mContext).getWindow().setBackgroundDrawableResource(R.color.background_material_light_1);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            // 相当于Fragment的onResume
            reloadAdapter();
        }
    }

    private void setInfo(String title, int count, int resourceId, int position) {
        MyFragmentItem information = new MyFragmentItem();
        information.setTitle(title);
        information.setCount(count);
        information.setResourceId(resourceId);
        if (mList.size() < 4) {
            mList.add(new MyFragmentItem());
        }
        // 将新的info对象加入到信息列表中
        mList.set(position, information);
    }

    private void setMusicInfo() {
        if (SystemUtil.isLollipop() && ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            loadCount(false);
        } else {
            loadCount(true);
        }
    }

    private void loadCount(boolean has) {
        int localMusicCount = 0;
        int recentMusicCount = 0;
        int downloadCount = 0;
        int favoriteCount = 0;
        if (has) {
            try {
                localMusicCount = MusicUtil.queryMusics(mContext, Keys.START_FROM_LOCAL).size();
                recentMusicCount = TopTracksLoader.getCount(AppCache.getContext(),
                        TopTracksLoader.QueryType.RecentSongs);
                downloadCount = DownloadStore.getInstance(mContext).getDownLoadedListAll().size();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        setInfo(mContext.getResources().getString(R.string.local_musics),
                localMusicCount, R.drawable.ic_local, 0);
        setInfo(mContext.getResources().getString(R.string.recent_play),
                recentMusicCount, R.drawable.ic_recent, 1);
        setInfo(mContext.getResources().getString(R.string.download_management),
                downloadCount, R.drawable.ic_download, 2);
        setInfo(mContext.getResources().getString(R.string.favorites),
                favoriteCount, R.drawable.ic_favorite, 3);
    }

    @SuppressLint("StaticFieldLeak")
    public void reloadAdapter() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                ArrayList results = new ArrayList();
                setMusicInfo();
                ArrayList<Playlist> playLists = mPlaylistInfo.getPlaylist();
                results.addAll(mList);
                results.add(mContext.getResources().getString(R.string.created_play_lists));
                results.addAll(playLists);
                if (mAdapter == null) {
                    mAdapter = new MyFragmentAdapter(mContext);
                }
                mAdapter.updateResults(results, playLists);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (mContext == null) {
                    return;
                }
                mAdapter.notifyDataSetChanged();
                mSwipeRefresh.setRefreshing(false);
            }
        }.execute();
    }

    @Override
    public void changeTheme() {
        super.changeTheme();
//        mSwipeRefresh.setColorSchemeColors(ThemeUtils.getColorById(mContext, R.color.theme_color_primary));
    }

    class MyFragmentAdapter extends RecyclerView.Adapter<MyFragmentAdapter.ItemHolder> {

        private ArrayList<Playlist> mPlayLists = new ArrayList<>();
        private boolean isCreatedExpanded = true;
        private Context mContext;
        private ArrayList mItemResults = new ArrayList();
        private boolean isLoveList = true;

        MyFragmentAdapter(Context context) {
            mContext = context;
        }

        void updateResults(ArrayList itemResults, ArrayList<Playlist> playLists) {
            isLoveList = true;
            mItemResults = itemResults;
            mPlayLists = playLists;
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case 0:
                    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_my_item, parent, false);
                    return new ItemHolder(itemView);
                case 1:
                    if (isLoveList) {
                        View playlistView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_my_playlist_love_item, parent, false);
                        return new ItemHolder(playlistView);
                    }
                    View playlistView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_my_playlist_item, parent, false);
                    return new ItemHolder(playlistView);
                case 2:
                    View loveView = LayoutInflater.from(parent.getContext()).inflate(R.layout.expandable_item, parent, false);
                    return new ItemHolder(loveView);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            switch (getItemViewType(position)) {
                case 0:
                    MyFragmentItem item = (MyFragmentItem) mItemResults.get(position);
                    holder.mItemTitle.setText(item.getTitle());
                    String count = "(" + item.getCount() + ")";
                    holder.mCount.setText(count);
                    holder.mImage.setImageResource(item.getResourceId());
                    setOnListener(holder, position);
                    break;
                case 1:
                    Playlist playlist = (Playlist) mItemResults.get(position);
                    if (isCreatedExpanded && playlist.getAuthor().equals("local")) {
                        if (playlist.getAlbumArt() != null) {
                            holder.mAlbumArt.setImageURI(Uri.parse(playlist.getAlbumArt()));
                        }
                        holder.mTitle.setText(playlist.getName());
                        String songCount = playlist.getSongCount() + "首";
                        holder.mSongCount.setText(songCount);
                    }
                    setOnPlaylistListener(holder, position, playlist.getId(), playlist.getAlbumArt(), playlist.getName());
                    isLoveList = false;
                    break;
                case 2:
                    String sectionItem = "创建的歌单" + "(" + mPlayLists.size() + ")";
                    holder.mSectionItem.setText(sectionItem);
                    holder.mSectionImg.setImageResource(R.drawable.ic_list_arrow_right);
                    setSectionListener(holder, position);
                    break;
            }
        }

        @Override
        public void onViewRecycled(ItemHolder holder) {
        }

        @Override
        public int getItemCount() {
            if (mItemResults == null) {
                return 0;
            }
            if (!isCreatedExpanded && mPlayLists != null) {
                mItemResults.removeAll(mPlayLists);
            }
            return mItemResults.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (getItemCount() == 0) {
                return -1;
            }
            if (mItemResults.get(position) instanceof MyFragmentItem) {
                return 0;
            }
            if (mItemResults.get(position) instanceof Playlist) {
                return 1;
            }
            return 2;
        }

        private void setOnListener(ItemHolder holder, final int position) {
            switch (position) {
                case 0:
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(mContext, LocalMusicActivity.class);
                                    intent.putExtra("page_number", 0);
                                    mContext.startActivity(intent);
                                }
                            }, 60);
                        }
                    });
                    break;
                case 1:
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(mContext, RecentPlayActivity.class);
                                    mContext.startActivity(intent);
                                }
                            }, 60);
                        }
                    });
                    break;
                case 2:
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(mContext, DownloadActivity.class);
                                    mContext.startActivity(intent);
                                }
                            }, 60);
                        }
                    });
                    break;
                case 3:
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, FavoriteActivity.class);
                            mContext.startActivity(intent);
                        }
                    });
                    break;
            }
        }

        private void setOnPlaylistListener(ItemHolder holder, final int position, final long playListId, final String albumArt, final String playlistName) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(mContext, PlaylistActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            intent.putExtra("is_local", true);
                            intent.putExtra("playlist_id", playListId + "");
                            intent.putExtra("album_art", albumArt);
                            intent.putExtra("playlist_name", playlistName);
                            mContext.startActivity(intent);
                        }
                    }, 60);
                }
            });

            holder.mMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO
                }
            });
        }

        private void setSectionListener(final ItemHolder holder, int position) {
            holder.mSectionMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, PlaylistManagerActivity.class);
                    mContext.startActivity(intent);
                }
            });
        }

        class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            //            @BindView(R.id.fragment_my_item_title)
            TextView mItemTitle;
            //            @BindView(R.id.fragment_my_playlist_item_title)
            TextView mTitle;
            //            @BindView(R.id.fragment_my_item_count)
            TextView mCount;
            //            @BindView(R.id.fragment_my_playlist_item_count)
            TextView mSongCount;
            //            @BindView(R.id.expandable_item_title)
            TextView mSectionItem;
            //            @BindView(R.id.fragment_my_playlist_item_menu)
            ImageView mMenu;
            //            @BindView(R.id.expandable_item_image)
            ImageView mSectionImg;
            //            @BindView(R.id.expandable_item_menu)
            ImageView mSectionMenu;
            //            @BindView(R.id.fragment_my_playlist_item_image)
            SimpleDraweeView mAlbumArt;
            //            @BindView(R.id.fragment_my_item_image_view)
            ImageView mImage;

            ItemHolder(View itemView) {
                super(itemView);
//                ButterKnife.bind(this, itemView);

                mItemTitle = itemView.findViewById(R.id.fragment_my_item_title);
                mTitle = itemView.findViewById(R.id.fragment_my_playlist_item_title);
                mCount = itemView.findViewById(R.id.fragment_my_item_count);
                mSongCount = itemView.findViewById(R.id.fragment_my_playlist_item_count);
                mSectionItem = itemView.findViewById(R.id.expandable_item_title);
                mMenu = itemView.findViewById(R.id.fragment_my_playlist_item_menu);
                mSectionImg = itemView.findViewById(R.id.expandable_item_image);
                mSectionMenu = itemView.findViewById(R.id.expandable_item_menu);
                mAlbumArt = itemView.findViewById(R.id.fragment_my_playlist_item_image);
                mImage = itemView.findViewById(R.id.fragment_my_item_image_view);

                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                ObjectAnimator animator;
                animator = ObjectAnimator.ofFloat(mSectionImg, "rotation", 90, 0);
                animator.setDuration(100);
                animator.setRepeatCount(0);
                animator.setInterpolator(new LinearInterpolator());
                switch (getItemViewType()) {
                    case 2:
                        if (isCreatedExpanded) {
                            mItemResults.removeAll(mPlayLists);
                            updateResults(mItemResults, mPlayLists);
                            notifyItemRangeRemoved(5, mPlayLists.size());
                            animator.start();
                            isCreatedExpanded = false;
                        }
                        break;
                }
            }
        }
    }
}