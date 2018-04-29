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
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.constant.Keys;
import com.charles.funmusic.lastfmapi.LastFmClient;
import com.charles.funmusic.lastfmapi.callbacks.ArtistInfoListener;
import com.charles.funmusic.lastfmapi.models.ArtistQuery;
import com.charles.funmusic.lastfmapi.models.LastFmArtist;
import com.charles.funmusic.model.Artist;
import com.charles.funmusic.service.MusicPlayer;
import com.charles.funmusic.utils.FileUtil;
import com.charles.funmusic.utils.MusicUtil;
import com.charles.funmusic.utils.Preferences;
import com.charles.funmusic.utils.SortOrder;
import com.charles.funmusic.utils.comparator.ArtistComparator;
import com.charles.funmusic.widget.SideBar;
import com.charles.funmusic.widget.TintImageView;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArtistFragment extends BaseFragment {

    @BindView(R.id.fragment_common_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.fragment_common_empty)
    TextView mEmpty;
    @BindView(R.id.fragment_common_index_bar)
    SideBar mSideBar;
    @BindView(R.id.fragment_common_letter)
    TextView mLetter;

    private ArtistAdapter mAdapter;
    private Preferences mPreferences;
    private boolean mIsAZSort = true;
    private HashMap<String, Integer> mPositionMap = new HashMap<>();

    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                mSideBar.setVisibility(View.VISIBLE);
            }
        }
    };

    public static ArtistFragment newInstance(long id) {
        ArtistFragment fragment = new ArtistFragment();
        Bundle args = new Bundle();
        args.putLong("artist_id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = Preferences.getInstance(mContext);
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_common;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new ArtistAdapter(null);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        mIsAZSort = mPreferences.getArtistSortOrder().equals(SortOrder.ArtistSortOrder.ARTIST_A_Z);
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
            protected Void doInBackground(Void... unused) {
                mIsAZSort = mPreferences.getArtistSortOrder().equals(SortOrder.ArtistSortOrder.ARTIST_A_Z);
                List<Artist> artists = MusicUtil.queryArtists(mContext);
                if (mIsAZSort) {
                    Collections.sort(artists, new ArtistComparator());
                    for (int i = 0; i < artists.size(); i++) {
                        if (mPositionMap.get(artists.get(i).getArtistSort()) == null) {
                            mPositionMap.put(artists.get(i).getArtistSort(), i);
                        }
                    }
                    if (artists != null) {
                        mAdapter.updateDataSet(artists);
                    }
                }
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

    public class ArtistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<Artist> mArtists;

        ArtistAdapter(List<Artist> artists) {
            mArtists = artists;
        }

        void updateDataSet(List<Artist> artists) {
            mArtists = artists;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.artist_item, parent, false);
            return new ListItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
            Artist artist = mArtists.get(position);
            // 设置条目状态
            ((ListItemViewHolder) holder).mArtist.setText(FileUtil.getArtist(artist.getArtist()));
            String count = artist.getNumberOfTracks() + "首";
            ((ListItemViewHolder) holder).mCount.setText(count);

            // 根据播放中歌曲的歌手名判断当前歌手专辑条目是否有播放的歌曲
            if (MusicPlayer.getCurrentArtistId() == artist.getArtistId()) {
                ((ListItemViewHolder) holder).mMoreOverFlow.setImageResource(R.drawable.playing);
//                ((ListItemViewHolder) holder).mMoreOverFlow.setImageTintList(R.color.theme_color_primary);
            } else {
                ((ListItemViewHolder) holder).mMoreOverFlow.setImageResource(R.drawable.ic_list_more);
            }

            // lastFm api加载歌手图片
            LastFmClient.getInstance(mContext).getArtistInfo(new ArtistQuery(artist.getArtist()),
                    new ArtistInfoListener() {
                @Override
                public void artistInfoSuccess(LastFmArtist artist) {
                    if (artist != null && artist.mArtwork != null) {
                        ((ListItemViewHolder) holder).mDraweeView.setImageURI(
                                Uri.parse(artist.mArtwork.get(2).mUrl));
                    }
                }

                @Override
                public void artistInfoFailed() {
                }
            });
        }

        @Override
        public int getItemCount() {
            return mArtists == null ? 0 : mArtists.size();
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            @BindView(R.id.artist_item_image_view)
            SimpleDraweeView mDraweeView;
            @BindView(R.id.artist_item_artist)
            TextView mArtist;
            @BindView(R.id.artist_item_count)
            TextView mCount;
            @BindView(R.id.artist_item_more)
            TintImageView mMoreOverFlow;

            ListItemViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);

                mMoreOverFlow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MoreFragment moreFragment = MoreFragment.newInstance(mArtists.get(
                                getAdapterPosition()).getArtistId() + "", Keys.ARTIST_OVERFLOW);
                        moreFragment.show(getFragmentManager(), "");
                    }
                });

                // 为每个条目设置监听
                itemView.setOnClickListener(this);

                changeFont(mArtist, false);
                changeFont(mCount, false);
            }

            @Override
            public void onClick(View v) {
                if (getAdapterPosition() != -1) {
                    FragmentTransaction transaction = ((AppCompatActivity) mContext)
                            .getSupportFragmentManager().beginTransaction();
                    ArtistDetailFragment fragment = ArtistDetailFragment.newInstance(
                            mArtists.get(getAdapterPosition()).getArtistId());
                    transaction.hide(((AppCompatActivity)mContext).getSupportFragmentManager()
                            .findFragmentById(R.id.activity_local_music_tab_container));
                    transaction.add(R.id.activity_local_music_tab_container, fragment);
                    transaction.addToBackStack(null).commit();
                }
            }
        }
    }
}
