package com.charles.funmusic.fragment;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.constant.Keys;
import com.charles.funmusic.model.Folder;
import com.charles.funmusic.model.Music;
import com.charles.funmusic.service.MusicPlayer;
import com.charles.funmusic.utils.MusicUtil;
import com.charles.funmusic.utils.Preferences;
import com.charles.funmusic.utils.SortOrder;
import com.charles.funmusic.utils.comparator.FolderComparator;
import com.charles.funmusic.utils.comparator.FolderCountComparator;
import com.charles.funmusic.widget.SideBar;
import com.charles.funmusic.widget.TintImageView;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FolderFragment extends BaseFragment {

    @BindView(R.id.fragment_common_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.fragment_common_empty)
    TextView mEmpty;
    @BindView(R.id.fragment_common_index_bar)
    SideBar mSideBar;
    @BindView(R.id.fragment_common_letter)
    TextView mLetter;

    private Preferences mPreferences;
    private boolean mIsAZSort = true;
    private HashMap<String, Integer> mPositionMap = new HashMap<>();
    private FolderAdapter mAdapter;

    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                mSideBar.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = Preferences.getInstance(mContext);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_common, container, false);
        ButterKnife.bind(this, view);

        init();

        return view;
    }

    private void init() {
        mIsAZSort = mPreferences.getFolderSortOrder().equals(SortOrder.FolderSortOrder.FOLDER_A_Z);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new FolderAdapter(null);
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

    @Override
    public void onResume() {
        super.onResume();
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
            protected Void doInBackground(final Void... unused) {
                mIsAZSort = mPreferences.getFolderSortOrder().equals(SortOrder.FolderSortOrder.FOLDER_A_Z);
                Log.e("sort", "folder" + mIsAZSort);
                List<Folder> folders = MusicUtil.queryFolders(mContext);
                for (int i = 0; i < folders.size(); i++) {
                    List<Music> folderList = MusicUtil.queryMusics(AppCache.getContext(),
                            folders.get(i).getFolderPath(), Keys.START_FROM_FOLDER);
                    folders.get(i).setFolderCount(folderList.size());
                }
                if (mIsAZSort) {
                    Collections.sort(folders, new FolderComparator());
                    for (int i = 0; i < folders.size(); i++) {
                        if (mPositionMap.get(folders.get(i).getFolderSort()) == null) {
                            mPositionMap.put(folders.get(i).getFolderSort(), i);
                        }
                    }
                } else {
                    Collections.sort(folders, new FolderCountComparator());
                }
                mAdapter.updateDataSet(folders);
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

    public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ListItemViewHolder> {

        private List<Folder> mFolders;

        FolderAdapter(List<Folder> folders) {
            mFolders = folders;
        }

        void updateDataSet(List<Folder> folders) {
            mFolders = folders;
        }

        @Override
        public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.folder_item, parent, false);
            return new ListItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ListItemViewHolder holder, int position) {
            Folder folder = mFolders.get(position);
            if (folder.getFolderPath().contains("/netease/cloudmusic/")) {
                String folderText = folder.getFolderName() + "（网易云音乐）";
                holder.mFolder.setText(folderText);
            } else {
                holder.mFolder.setText(folder.getFolderName());
            }
            String countAndPath = folder.getFolderCount() + "首 " + folder.getFolderPath();
            holder.mCountAndPath.setText(countAndPath);
            holder.mImageView.setImageResource(R.drawable.ic_folder);
            // 根据播放中歌曲的专辑名判断当前专辑条目是否有播放的歌曲
            String folderPath = null;
            if (MusicPlayer.getPath() != null && MusicPlayer.getTrackName() != null) {
                folderPath = MusicPlayer.getPath().substring(
                        0, MusicPlayer.getPath().lastIndexOf(File.separator));
            }

            if (folderPath != null && folderPath.equals(folder.getFolderPath())) {
                holder.mMoreOverFlow.setImageResource(R.drawable.playing);
//                holder.mMoreOverFlow.setImageTintList(R.color.theme_color_primary);
            } else {
                holder.mMoreOverFlow.setImageResource(R.drawable.ic_list_more);
            }
        }

        @Override
        public int getItemCount() {
            return mFolders == null ? 0 : mFolders.size();
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            @BindView(R.id.folder_item_image_view)
            ImageView mImageView;
            @BindView(R.id.folder_item_more)
            TintImageView mMoreOverFlow;
            @BindView(R.id.folder_item_folder)
            TextView mFolder;
            @BindView(R.id.folder_item_count_and_path)
            TextView mCountAndPath;

            ListItemViewHolder(View itemView) {
                super(itemView);

                ButterKnife.bind(this, itemView);

                mMoreOverFlow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MoreFragment moreFragment = MoreFragment.newInstance(mFolders.get(
                                getAdapterPosition()).getFolderName(), Keys.FOLDER_OVERFLOW);
                        moreFragment.show(getFragmentManager(), "music");
                    }
                });

                itemView.setOnClickListener(this);

                changeFont(mFolder, false);
                changeFont(mCountAndPath, false);
            }

            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = ((AppCompatActivity) mContext)
                        .getSupportFragmentManager().beginTransaction();
                FolderDetailFragment fragment = FolderDetailFragment.newInstance(mFolders.get(
                        getAdapterPosition()).getFolderPath(), false, null);
                transaction.hide(((AppCompatActivity) mContext).getSupportFragmentManager()
                        .findFragmentById(R.id.activity_local_music_tab_container));
                transaction.add(R.id.activity_local_music_tab_container, fragment);
                transaction.addToBackStack(null).commit();
            }
        }
    }
}
