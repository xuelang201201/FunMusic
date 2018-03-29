package com.charles.funmusic.fragment;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.model.Music;
import com.charles.funmusic.provider.MusicPlaybackState;
import com.charles.funmusic.service.MusicPlayer;
import com.charles.funmusic.utils.HandlerUtil;
import com.charles.funmusic.utils.loader.QueueLoader;
import com.charles.funmusic.widget.TintImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PlayQueueFragment extends AttachDialogFragment {

    @BindView(R.id.fragment_play_queue_number)
    TextView mPlaylistNumber;
    @BindView(R.id.fragment_play_queue_clear_all)
    TextView mClearAll;
    @BindView(R.id.fragment_play_queue_add_to_favorite)
    TextView mAddToPlaylist;
    @BindView(R.id.fragment_play_queue_recycler_view)
    RecyclerView mRecyclerView;

    private PlaylistAdapter mAdapter;
    private ArrayList<Music> mPlaylist;
    private int mCurrentPlayPosition = 0;
    private Handler mHandler;
    private PlayQueueListener mQueueListener;

    public void setQueueListener(PlayQueueListener listener) {
        mQueueListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置样式
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomDatePickerDialog);
        MusicPlaybackState musicPlaybackState = MusicPlaybackState.getInstance(mContext);
        mHandler = HandlerUtil.getInstance(mContext);
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置fragment高度、宽度
        int dialogHeight = (int) (mContext.getResources().getDisplayMetrics().heightPixels * 0.6);
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, dialogHeight);
            getDialog().setCanceledOnTouchOutside(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play_queue, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    public void init() {
        // 设置无标题
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 设置从底部弹出
        if (getDialog().getWindow() != null) {
            WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
            params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setAttributes(params);
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setHasFixedSize(true);

        new loadSongs().execute();
    }

    @OnClick({R.id.fragment_play_queue_add_to_favorite, R.id.fragment_play_queue_clear_all})
    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_play_queue_add_to_favorite:
                break;

            case R.id.fragment_play_queue_clear_all:
                MusicPlayer.clearQueue();
                MusicPlayer.stop();
                File file = new File(mContext.getCacheDir().getAbsolutePath() + "playlist");
                if (file.exists()) {
                    file.delete();
                }
                MusicPlaybackState.getInstance(mContext).clearQueue();
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
                dismiss();
                break;
        }
    }

    /**
     * 异步加载 RecyclerView
     */
    @SuppressLint("StaticFieldLeak")
    private class loadSongs extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (mContext != null) {
                try {
                    HashMap<Long, Music> play = MusicPlayer.getPlayInfos();
                    if (play != null && play.size() > 0) {
                        long[] queue = MusicPlayer.getQueue();
                        mPlaylist = new ArrayList<>();
                        for (long aQueue : queue) {
                            mPlaylist.add(play.get(aQueue));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mPlaylist != null && mPlaylist.size() > 0) {
                mAdapter = new PlaylistAdapter(mPlaylist);
                mRecyclerView.setAdapter(mAdapter);

                String str = "播放列表（" + mPlaylist.size() + "）";
                mPlaylistNumber.setText(str);

                for (int i = 0; i < mPlaylist.size(); i++) {
                    Music music = mPlaylist.get(i);

                    if (music != null && MusicPlayer.getCurrentAudioId() == music.getId()) {
                        mRecyclerView.scrollToPosition(i);
                    }
                }
            }
        }
    }

    class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistHolder> {
        private ArrayList<Music> mMusics = new ArrayList<>();

        PlaylistAdapter(ArrayList<Music> musics) {
            mMusics = musics;
        }

        void updateDataSet(ArrayList<Music> musics) {
            mMusics = musics;
        }

        @Override
        public PlaylistHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new PlaylistHolder(LayoutInflater.from(mContext)
                    .inflate(R.layout.fragment_play_queue, parent, false));
        }

        @Override
        public void onBindViewHolder(PlaylistHolder holder, @SuppressLint("RecyclerView") int position) {
            Music music = mMusics.get(position);
            holder.mTitle.setText(music.getTitle());
            String artistStr = " - " + music.getArtist();
            holder.mArtist.setText(artistStr);
            // 判断该条目音乐是否在播放
            Log.e("current", MusicPlayer.getCurrentAudioId() + ":" + music.getId());
            if (MusicPlayer.getCurrentAudioId() == music.getId()) {
                Log.e("current", MusicPlayer.getCurrentAudioId() + ":" + music.getId());
                holder.mPlayState.setVisibility(View.VISIBLE);
                holder.mPlayState.setImageResource(R.drawable.playing);
//                holder.mPlayState.setImageTintList(R.color.theme_color_primary);
                mCurrentPlayPosition = position;
            } else {
                holder.mPlayState.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return mMusics == null ? 0 : mMusics.size();
        }

        class PlaylistHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            @BindView(R.id.playlist_item_delete)
            ImageView mDelete;
            @BindView(R.id.playlist_item_title)
            TextView mTitle;
            @BindView(R.id.playlist_item_artist)
            TextView mArtist;
            @BindView(R.id.playlist_item_play_state)
            TintImageView mPlayState;

            PlaylistHolder(View itemView) {
                super(itemView);

                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(this);

                mDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        long deleteId = mMusics.get(position).getId();

                        notifyItemRemoved(position);
                        MusicPlayer.removeTrack(deleteId);

                        updateDataSet(QueueLoader.getQueueSongs(mContext));
                        if (mMusics == null) {
                            MusicPlayer.stop();
                        }
                        if (MusicPlayer.isPlaying() && (MusicPlayer.getCurrentAudioId() == deleteId)) {
                            MusicPlayer.next();
                        }
                        notifyDataSetChanged();
                        if (mMusics != null) {
                            String text = "播放列表（" + mMusics.size() + "）";
                            mPlaylistNumber.setText(text);
                        } else {
                            mPlaylistNumber.setText("播放列表");
                        }
                    }
                });
            }

            @Override
            public void onClick(View v) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final int position = getAdapterPosition();
                        if (position == -1) {
                            return;
                        }
                        long[] ids = new long[1];
                        ids[0] = mMusics.get(position).getId();
                        MusicPlayer.setQueuePosition(position);

                        if (mQueueListener != null) {
                            mQueueListener.onPlay(position);

                            notifyItemChanged(mCurrentPlayPosition);
                            notifyItemChanged(position);
                        }
                    }
                }, 70);
            }
        }
    }

    public interface PlayQueueListener {
        void onPlay(int position);
    }
}