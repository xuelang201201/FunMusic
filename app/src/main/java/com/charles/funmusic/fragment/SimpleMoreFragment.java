package com.charles.funmusic.fragment;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.charles.funmusic.activity.AlbumsDetailActivity;
import com.charles.funmusic.activity.ArtistDetailActivity;
import com.charles.funmusic.adapter.MusicFlowAdapter;
import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.json.SearchAlbumInfo;
import com.charles.funmusic.json.SearchArtistInfo;
import com.charles.funmusic.model.Music;
import com.charles.funmusic.model.OverFlowItem;
import com.charles.funmusic.net.BMA;
import com.charles.funmusic.net.HttpUtil;
import com.charles.funmusic.service.MusicPlayer;
import com.charles.funmusic.utils.FileUtil;
import com.charles.funmusic.utils.HandlerUtil;
import com.charles.funmusic.utils.MusicUtil;
import com.charles.funmusic.utils.SystemUtil;
import com.charles.funmusic.utils.ToastUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
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
                            artistDetail();
                            dismiss();
                            break;
                        case 1:
                            albumDetail();
                            dismiss();
                            break;
                        case 2:
                            addToPlaylist();
                            dismiss();
                            break;
                        case 3:
                            share();
                            dismiss();
                            break;
                        case 4:
                            setAsRingtone();
                            dismiss();
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

    /**
     * 跳转歌手详细页
     */
    @SuppressLint("StaticFieldLeak")
    private void artistDetail() {
        if (mMusic.isLocal()) {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    ArrayList<SearchArtistInfo> artistResults = new ArrayList<>();
                    try {
                        JsonObject jsonObject = HttpUtil.getResposeJsonObject(BMA.Search.searchMerge(mMusic.getArtist(), 1, 50)).get("result").getAsJsonObject();
                        JsonObject artistObject = jsonObject.get("artist_info").getAsJsonObject();
                        JsonArray artistArray = artistObject.get("artist_list").getAsJsonArray();
                        for (JsonElement o : artistArray) {
                            SearchArtistInfo artist = AppCache.gsonInstance().fromJson(o, SearchArtistInfo.class);
                            artistResults.add(artist);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (artistResults.size() == 0) {
                        HandlerUtil.getInstance(mContext).post(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.show("没有找到该艺术家");
                            }
                        });

                    } else {
                        SearchArtistInfo info = artistResults.get(0);
                        Intent intent = new Intent(AppCache.getContext(), ArtistDetailActivity.class);
                        intent.putExtra("artist_id", info.getArtist_id());
                        intent.putExtra("artist", info.getAuthor());
                        AppCache.getContext().startActivity(intent);
                    }
                    return null;
                }
            }.execute();
        } else {
            Intent intent = new Intent(AppCache.getContext(), ArtistDetailActivity.class);
            intent.putExtra("artist_id", mMusic.getArtistId() + "");
            intent.putExtra("artist", mMusic.getArtist());
            AppCache.getContext().startActivity(intent);
        }
    }

    /**
     * 跳转专辑详细页
     */
    @SuppressLint("StaticFieldLeak")
    private void albumDetail() {
        if (mMusic.isLocal()) {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    ArrayList<SearchAlbumInfo> albumResults = new ArrayList<>();
                    try {

                        JsonObject jsonObject = HttpUtil.getResposeJsonObject(BMA.Search.searchMerge(mMusic.getAlbum(), 1, 10)).get("result").getAsJsonObject();
                        JsonObject albumObject = jsonObject.get("album_info").getAsJsonObject();
                        JsonArray albumArray = albumObject.get("album_list").getAsJsonArray();
                        for (JsonElement o : albumArray) {
                            SearchAlbumInfo albumInfo = AppCache.gsonInstance().fromJson(o, SearchAlbumInfo.class);
                            albumResults.add(albumInfo);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (albumResults.size() == 0) {
                        HandlerUtil.getInstance(getActivity()).post(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.show("没有找到所属专辑");
                            }
                        });

                    } else {
                        SearchAlbumInfo info = albumResults.get(0);
                        Intent intent = new Intent(AppCache.getContext(), AlbumsDetailActivity.class);
                        intent.putExtra("album_id", info.getAlbum_id());
                        intent.putExtra("album_art", info.getPic_small());
                        intent.putExtra("album", info.getTitle());
                        intent.putExtra("album_detail", info.getAlbum_desc());
                        AppCache.getContext().startActivity(intent);
                    }
                    return null;
                }
            };
        } else {

            Intent intent = new Intent(mContext, AlbumsDetailActivity.class);
            intent.putExtra("album_id", mMusic.getAlbumId() + "");
            intent.putExtra("album_art", mMusic.getAlbumArt());
            intent.putExtra("album", mMusic.getAlbum());
            mContext.startActivity(intent);
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

    private void setAsRingtone() {
        Uri ringUri = Uri.parse("file://" + mMusic.getUrl());
        RingtoneManager.setActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_RINGTONE, ringUri);
        ToastUtil.show(getString(R.string.set_ringtone_success));
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
        setInfo("歌手：" + FileUtil.getArtist(mMusic.getArtist()), R.drawable.ic_artist);
        setInfo("专辑：" + FileUtil.getAlbum(mMusic.getAlbum()), R.drawable.ic_album);
        setInfo("收藏到歌单", R.drawable.ic_add_to_playlist);
        setInfo("分享", R.drawable.ic_lay_share);
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
