package com.charles.funmusic.fragment;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.constant.Keys;
import com.charles.funmusic.model.Album;
import com.charles.funmusic.service.MusicPlayer;
import com.charles.funmusic.utils.FileUtil;
import com.charles.funmusic.utils.MusicUtil;
import com.charles.funmusic.utils.Preferences;
import com.charles.funmusic.utils.SortOrder;
import com.charles.funmusic.utils.comparator.AlbumComparator;
import com.charles.funmusic.widget.SideBar;
import com.charles.funmusic.widget.TintImageView;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlbumFragment extends BaseFragment {

    @BindView(R.id.fragment_common_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.fragment_common_empty)
    TextView mEmpty;
    @BindView(R.id.fragment_common_index_bar)
    SideBar mSideBar;
    @BindView(R.id.fragment_common_letter)
    TextView mLetter;

    private AlbumAdapter mAdapter;
    private boolean mIsAZSort = true;
    private HashMap<String, Integer> mPositionMap = new HashMap<>();
    private Preferences mPreferences;

    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                mSideBar.setVisibility(View.VISIBLE);
            }
        }
    };

    public static AlbumFragment newInstance(int title, String message) {
        AlbumFragment fragment = new AlbumFragment();
        Bundle args = new Bundle(2);
        args.putInt("EXTRA_TITLE", title);
        args.putString("EXTRA_MESSAGE", message);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = Preferences.getInstance(mContext);
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_common;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        mIsAZSort = mPreferences.getAlbumSortOrder().equals(SortOrder.AlbumSortOrder.ALBUM_A_Z);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new AlbumAdapter(null);
        mRecyclerView.setAdapter(mAdapter);
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                mLetter.setText(s);
                mSideBar.setView(mLetter);
                Log.e("scroll", " " + s);
                if (mPositionMap.get(s) != null) {
                    int i = mPositionMap.get(s);
                    Log.e("scroll_get", " " + i);
                    ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                            .scrollToPositionWithOffset(i, 0);
                }
            }
        });
        reloadAdapter();
    }

    /**
     * 更新adapter界面
     */
    @SuppressLint("StaticFieldLeak")
    @Override
    public void reloadAdapter() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(final Void... unused) {
                mIsAZSort = mPreferences.getAlbumSortOrder().equals(SortOrder.AlbumSortOrder.ALBUM_A_Z);
                Log.e("sort", mIsAZSort + "");
                List<Album> albums = MusicUtil.queryAlbums(mContext);
                if (mIsAZSort) {
                    Collections.sort(albums, new AlbumComparator());
                    for (int i = 0; i < albums.size(); i++) {
                        if (mPositionMap.get(albums.get(i).getAlbumSort()) == null) {
                            mPositionMap.put(albums.get(i).getAlbumSort(), i);
                        }
                    }
                }
                mAdapter.updateDataSet(albums);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (mIsAZSort) {
                    mRecyclerView.addOnScrollListener(mScrollListener);
                } else {
                    mSideBar.setVisibility(View.INVISIBLE);
                    mRecyclerView.removeOnScrollListener(mScrollListener);
                }
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    public class AlbumAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final static int FIRST_ITEM = 0;
        private final static int ITEM = 1;
        private List<Album> mAlbums;

        AlbumAdapter(List<Album> albums) {
            mAlbums = albums;
        }

        /**
         * 更新adapter的数据
         */
        void updateDataSet(List<Album> albums) {
            mAlbums = albums;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ListItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.album_item, parent, false));
        }

        @Override
        public int getItemViewType(int position) {
            return position == FIRST_ITEM ? FIRST_ITEM : ITEM;
        }

        /**
         * 将数据与界面进行绑定
         */
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Album album = mAlbums.get(position);
            ((ListItemViewHolder) holder).mAlbum.setText(FileUtil.getAlbum(album.getAlbum()));
            String countAndArtist = album.getNumberOfSongs() + "首 " + FileUtil.getArtist(album.getAlbumArtist());
            ((ListItemViewHolder) holder).mCountAndArtist.setText(countAndArtist);
            ((ListItemViewHolder) holder).mDraweeView.setImageURI(Uri.parse(album.getAlbumArt() + ""));
            // 根据播放中歌曲的专辑名判断当前专辑条目是否有播放的歌曲
            if (MusicPlayer.getArtist() != null && MusicPlayer.getAlbum().equals(album.getAlbum())) {
                ((ListItemViewHolder) holder).mMoreOverFlow.setImageResource(R.drawable.playing);
//                ((ListItemViewHolder) holder).mMoreOverFlow.setImageTintList(R.color.theme_color_primary);
            } else {
                ((ListItemViewHolder) holder).mMoreOverFlow.setImageResource(R.drawable.ic_list_more);
            }
        }

        /**
         * 条目数量
         */
        @Override
        public int getItemCount() {
            return mAlbums == null ? 0 : mAlbums.size();
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            @BindView(R.id.album_item_more)
            TintImageView mMoreOverFlow;
            @BindView(R.id.album_item_image_view)
            SimpleDraweeView mDraweeView;
            @BindView(R.id.album_item_album)
            TextView mAlbum;
            @BindView(R.id.album_item_count_and_artist)
            TextView mCountAndArtist;

            ListItemViewHolder(View itemView) {
                super(itemView);

                ButterKnife.bind(this, itemView);
                mMoreOverFlow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MoreFragment moreFragment = MoreFragment.newInstance(mAlbums.get(
                                getAdapterPosition()).getAlbumId() + "", Keys.ALBUM_OVERFLOW);
                        moreFragment.show(getFragmentManager(), "album");
                    }
                });
                itemView.setOnClickListener(this);

                changeFont(mAlbum, false);
                changeFont(mCountAndArtist, false);
            }

            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = ((AppCompatActivity) mContext).getSupportFragmentManager().beginTransaction();
                AlbumDetailFragment fragment = AlbumDetailFragment.newInstance(mAlbums.get(
                        getAdapterPosition()).getAlbumId(), false, null);
                transaction.hide(((AppCompatActivity) mContext).getSupportFragmentManager()
                        .findFragmentById(R.id.activity_local_music_tab_container));
                transaction.add(R.id.activity_local_music_tab_container, fragment);
                transaction.addToBackStack(null).commit();
            }
        }
    }
}
