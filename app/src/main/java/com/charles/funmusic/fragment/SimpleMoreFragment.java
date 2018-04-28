package com.charles.funmusic.fragment;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore.Audio.Media;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.activity.PlayingActivity;
import com.charles.funmusic.adapter.MusicFlowAdapter;
import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.constant.Actions;
import com.charles.funmusic.model.Music;
import com.charles.funmusic.model.OverFlowItem;
import com.charles.funmusic.provider.PlaylistManager;
import com.charles.funmusic.service.MusicPlayer;
import com.charles.funmusic.utils.HandlerUtil;
import com.charles.funmusic.utils.MusicUtil;
import com.charles.funmusic.utils.ToastUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SimpleMoreFragment extends AttachDialogFragment {

    @BindView(R.id.fragment_simple_more_pop_list_title)
    TextView mTopTitle;
    @BindView(R.id.fragment_simple_more_pop_list)
    RecyclerView mRecyclerView;

    private long mArgs;
    private MusicFlowAdapter mAdapter;
    private Music mMusic;
    /**
     * 声明一个list，动态存储要显示的信息
     */
    private List<OverFlowItem> mList = new ArrayList<>();

    public static SimpleMoreFragment newInstance(long id) {
        SimpleMoreFragment fragment = new SimpleMoreFragment();
        Bundle args = new Bundle();
        args.putLong("id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomDatePickerDialog);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_simple_more, container, false);
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
        if (getArguments() != null) {
            mArgs = getArguments().getLong("id");
        }
        // 布局
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setHasFixedSize(true);
        getList();
        setClick();
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置fragment高度、宽度
        double heightPercent = 0.5;
        int dialogHeight = (int) (mContext.getResources().getDisplayMetrics().heightPixels * heightPercent);
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, dialogHeight);
            getDialog().setCanceledOnTouchOutside(true);
        }
    }

    private void getList() {
        long musicId = mArgs;
        mMusic = MusicUtil.getMusics(mContext, musicId);
        String title = null;
        if (mMusic != null) {
            title = mMusic.getTitle();
        }
        if (title == null) {
            title = MusicPlayer.getTrackName();
        }
        String str = "歌曲：" + title;
        mTopTitle.setText(str);
        setMusicInfo();
        mAdapter = new MusicFlowAdapter(mContext, mList, mMusic);
    }

    private void setClick() {
        if (mAdapter != null) {
            mAdapter.setOnItemClickListener(new MusicFlowAdapter.IOnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, String data) {
                    switch (Integer.parseInt(data)) {
                        case 0:
                            nextPlay();
                            dismiss();
                            break;
                        case 1:
                            addToPlaylist();
                            dismiss();
                            break;
                        case 2:
                            share();
                            dismiss();
                            break;
                        case 3:
                            delete();
                            dismiss();
                            break;
                        case 4:
                            setAsRingtone();
                            break;
                        case 5:
                            detail();
                            dismiss();
                            break;
                    }
                }
            });
        }
    }

    private void nextPlay() {
        if (mMusic.isLocal()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mMusic.getId() == MusicPlayer.getCurrentAudioId()) {
                        return;
                    }
                    long[] ids = new long[1];
                    ids[0] = mMusic.getId();
                    @SuppressLint("UseSparseArrays") HashMap<Long, Music> map = new HashMap<>();
                    map.put(ids[0], mMusic);
                    MusicPlayer.playNext(mContext, map, ids);
                }
            }, 100);
        }
    }

    private void addToPlaylist() {
        AddPlaylistFragment.newInstance(mMusic).show(getChildFragmentManager(), "add");
    }

    private void share() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + mMusic.getAlbumArt()));
        shareIntent.setType("audio/*");
        mContext.startActivity(Intent.createChooser(shareIntent, getString(R.string.share_to)));
    }

    private void delete() {
        if (mMusic.isLocal()) {
            new AlertDialog.Builder(getActivity()).setTitle(R.string.sure_to_delete_music)
                    .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Uri uri = ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, mMusic.getId());
                            mContext.getContentResolver().delete(uri, null, null);
                            if (MusicPlayer.getCurrentAudioId() == mMusic.getId()) {
                                if (MusicPlayer.getQueueSize() == 0) {
                                    MusicPlayer.stop();
                                } else {
                                    MusicPlayer.next();
                                }
                            }

                            HandlerUtil.getInstance(mContext).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    PlaylistManager.getInstance(mContext).deleteMusic(mContext, mMusic.getId());
                                    mContext.sendBroadcast(new Intent(Actions.ACTION_MUSIC_COUNT_CHANGED));
                                }
                            }, 200);
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        }
    }

    private void setAsRingtone() {
        if (mMusic.isLocal()) {
            new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.sure_to_set_ringtone))
                    .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Uri ringUri = Uri.parse("file://" + mMusic.getUrl());
                            RingtoneManager.setActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_RINGTONE, ringUri);
                            dialog.dismiss();
                            ToastUtil.show(getString(R.string.set_ringtone_success));
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), null).show();
        }
    }

    private void detail() {
        MusicDetailFragment detailFragment = MusicDetailFragment.newInstance(mMusic);
        if (getActivity() != null) {
            detailFragment.show(getActivity().getSupportFragmentManager(), "detail");
        }
    }

    /**
     * 设置音乐overflow条目
     */
    private void setMusicInfo() {
        // 设置list，RecyclerView要显示的内容
        setInfo("下一首播放", R.drawable.ic_next_play);
        setInfo("收藏到歌单", R.drawable.ic_add_to_playlist);
        setInfo("分享", R.drawable.ic_lay_share);
        setInfo("删除", R.drawable.ic_delete);
        setInfo("设为铃声", R.drawable.ic_ring);
        setInfo("查看歌曲信息", R.drawable.ic_document);
    }

    /**
     * 为info设置数据，并放入list
     *
     * @param title 内容
     * @param id    图标id
     */
    private void setInfo(String title, int id) {
        OverFlowItem information = new OverFlowItem();
        information.setTitle(title);
        information.setAvatar(id);
        mList.add(information);
    }
}
