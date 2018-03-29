package com.charles.funmusic.fragment;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
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
import com.charles.funmusic.adapter.MusicFlowAdapter;
import com.charles.funmusic.adapter.OverFlowAdapter;
import com.charles.funmusic.constant.Actions;
import com.charles.funmusic.constant.Keys;
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

public class MoreFragment extends AttachDialogFragment {
    @BindView(R.id.fragment_more_pop_list_title)
    TextView mTopTitle;
    @BindView(R.id.fragment_more_pop_list)
    RecyclerView mRecyclerView;

    private int mType;
    private double mHeightPercent;
    private ArrayList<Music> mMusics = null;
    private MusicFlowAdapter mMusicFlowAdapter;
    private Music mMusic;
    private OverFlowAdapter mCommonAdapter;
    private List<OverFlowItem> mList = new ArrayList<>();
    private String mArgs, mArtist, mAlbum;
    private Context mContext;
    private Handler mHandler;

    public static MoreFragment newInstance(String id, int startFrom, String albumId, String artistId) {
        MoreFragment fragment = new MoreFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        args.putString("album_id", albumId);
        args.putString("artist_id", artistId);
        args.putInt("type", startFrom);
        fragment.setArguments(args);
        return fragment;
    }


    public static MoreFragment newInstance(String id, int startFrom) {
        MoreFragment fragment = new MoreFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        args.putInt("type", startFrom);
        fragment.setArguments(args);
        return fragment;
    }


    public static MoreFragment newInstance(Music music, int startFrom) {
        MoreFragment fragment = new MoreFragment();
        Bundle args = new Bundle();
        args.putParcelable("music", music);
        args.putInt("type", startFrom);
        fragment.setArguments(args);
        return fragment;
    }

    public static MoreFragment newInstance(Music info, long playlistId) {
        MoreFragment fragment = new MoreFragment();
        Bundle args = new Bundle();
        args.putParcelable("music", info);
        args.putLong("playlist_id", playlistId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomDatePickerDialog);
        mContext = getContext();
    }

    @Override
    public void onStart() {
        super.onStart();
        int dialogHeight = (int) (mContext.getResources().getDisplayMetrics().heightPixels * mHeightPercent);
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, dialogHeight);
        getDialog().setCanceledOnTouchOutside(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mHandler = HandlerUtil.getInstance(mContext);
        // 设置无标题
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 设置从底部弹出
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setAttributes(params);
        if (getArguments() != null) {
            mType = getArguments().getInt("type");
            mArgs = getArguments().getString("id");
            long playlistId = getArguments().getLong("playlist_id");
        }
        // 布局
        View view = inflater.inflate(R.layout.fragment_more, container, false);
        ButterKnife.bind(this, view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setHasFixedSize(true);
        getList();
        setClick();
        return view;
    }

    private void getList() {
        if (mType == Keys.MUSIC_OVERFLOW) {
//            long musicId = Long.parseLong(mArgs.trim());
            mMusic = getArguments().getParcelable("music");
            if (mMusic == null) {
                mMusic = new Music();
            }
            mArtist = mMusic.getArtist();
            String albumId = mMusic.getAlbumId() + "";
            mAlbum = mMusic.getAlbum();
            String title = getString(R.string.song_name) + mMusic.getTitle();
            mTopTitle.setText(title);
            mHeightPercent = 0.6;
            setMusicInfo();
            mMusicFlowAdapter = new MusicFlowAdapter(mContext, mList, mMusic);
        } else {
            switch (mType) {
                case Keys.ARTIST_OVERFLOW:
                    String artistId = mArgs;
                    mMusics = MusicUtil.queryMusics(mContext, artistId, Keys.START_FROM_ARTIST);
                    String artistStr = getString(R.string.artist_name) + mMusics.get(0).getArtist();
                    mTopTitle.setText(artistStr);
                    break;
                case Keys.ALBUM_OVERFLOW:
                    String albumId = mArgs;
                    mMusics = MusicUtil.queryMusics(mContext, albumId, Keys.START_FROM_ALBUM);
                    String albumStr = getString(R.string.album_name) + mMusics.get(0).getAlbum();
                    mTopTitle.setText(albumStr);
                    break;
                case Keys.FOLDER_OVERFLOW:
                    String folder = mArgs;
                    mMusics = MusicUtil.queryMusics(mContext, folder, Keys.START_FROM_FOLDER);
                    String folderStr = getString(R.string.folder_name) + folder;
                    mTopTitle.setText(folderStr);
                    break;
            }
            setCommonInfo();
            mHeightPercent = 0.30;
            mCommonAdapter = new OverFlowAdapter(mContext, mList, mMusics);
        }
        changeFont(mTopTitle, false);
    }

    private void setClick() {
        if (mMusicFlowAdapter != null) {
            mMusicFlowAdapter.setOnItemClickListener(new MusicFlowAdapter.IOnRecyclerViewItemClickListener() {
                @SuppressLint("StaticFieldLeak")
                @Override
                public void onItemClick(View view, final String data) {
                    switch (Integer.parseInt(data)) {
                        case 0:
                            mHandler.postDelayed(new Runnable() {
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
                            dismiss();
                            break;
                        case 1:
                            ArrayList<Music> musics = new ArrayList<>();
                            musics.add(mMusic);
                            AddNetPlaylistFragment.newInstance(musics).show(getFragmentManager(), "add");
                            dismiss();
                            break;
                        case 2:
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + mMusic.getUrl()));
                            shareIntent.setType("audio/*");
                            mContext.startActivity(Intent.createChooser(shareIntent, getString(R.string.share_to)));
                            dismiss();
                            break;
                        case 3:
                            new AlertDialog.Builder(mContext)
                                    .setTitle(getString(R.string.sure_to_delete_music))
                                    .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            try {
                                                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mMusic.getId());
                                                mContext.getContentResolver().delete(uri, null, null);

                                                if (MusicPlayer.getCurrentAudioId() == mMusic.getId()) {
                                                    if (MusicPlayer.getQueueSize() == 0) {
                                                        MusicPlayer.stop();
                                                    } else {
                                                        MusicPlayer.next();
                                                    }
                                                }

                                                mHandler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        PlaylistManager.getInstance(mContext).deleteMusic(mContext, mMusic.getId());
                                                        mContext.sendBroadcast(new Intent(Actions.ACTION_MUSIC_COUNT_CHANGED));
                                                    }
                                                }, 200);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            dismiss();
                                        }
                                    })
                                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dismiss();
                                        }
                                    }).show();
                            dismiss();
                            break;
                        case 4:
//                            if (mMusic.isLocal()) {
//                                new AsyncTask<Void, Void, Void>() {
//
//                                    @Override
//                                    protected Void doInBackground(Void... params) {
//                                        ArrayList<SearchArtist> artistResults = new ArrayList<>();
//                                        try {
//                                            JsonObject jsonObject = HttpUtil.getResposeJsonObject(BMA.Search.searchMerge(mMusic.getArtist(), 1, 50)).get("result").getAsJsonObject();
//                                            JsonObject artistObject = jsonObject.get("artist_info").getAsJsonObject();
//                                            JsonArray artistArray = artistObject.get("artist_list").getAsJsonArray();
//                                            for (JsonElement o : artistArray) {
//                                                SearchArtist artist = AppCache.gsonInstance().fromJson(o, SearchArtist.class);
//                                                artistResults.add(artist);
//                                            }
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//                                        if (artistResults.size() == 0) {
//                                            mHandler.post(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    ToastUtil.show("没有找到该艺术家");
//                                                }
//                                            });
//
//                                        } else {
//                                            SearchArtist info = artistResults.get(0);
//                                            Intent intent = new Intent(mContext, ArtistDetailActivity.class);
//                                            intent.putExtra("artist_id", info.getArtistId());
//                                            intent.putExtra("artist", info.getAuthor());
//                                            mContext.startActivity(intent);
//                                        }
//                                        return null;
//                                    }
//                                }.execute();
//                            } else {
//
//                                Intent intent = new Intent(mContext, ArtistDetailActivity.class);
//                                intent.putExtra("artist_id", mMusic.getArtistId() + "");
//                                intent.putExtra("artist", mMusic.getArtist());
//                                mContext.startActivity(intent);
//                            }
//                            dismiss();
                            break;
                        case 5:
//                            if (mMusic.isLocal()) {
//                                new AsyncTask<Void, Void, Void>() {
//
//                                    @Override
//                                    protected Void doInBackground(Void... params) {
//                                        ArrayList<SearchAlbum> albumResults = new ArrayList<SearchAlbum>();
//                                        try {
//
//                                            JsonObject jsonObject = HttpUtil.getResposeJsonObject(BMA.Search.searchMerge(mMusic.getAlbum(), 1, 10)).get("result").getAsJsonObject();
//                                            JsonObject albumObject = jsonObject.get("album_info").getAsJsonObject();
//                                            JsonArray albumArray = albumObject.get("album_list").getAsJsonArray();
//                                            for (JsonElement o : albumArray) {
//                                                SearchAlbum albumInfo = AppCache.gsonInstance().fromJson(o, SearchAlbum.class);
//                                                albumResults.add(albumInfo);
//                                            }
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        }
//
//                                        if (albumResults.size() == 0) {
//                                            mHandler.post(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    Toast.makeText(mContext, "没有找到所属专辑", Toast.LENGTH_SHORT).show();
//                                                }
//                                            });
//
//                                        } else {
//                                            SearchAlbum info = albumResults.get(0);
//                                            Intent intent = new Intent(mContext, AlbumsDetailActivity.class);
//                                            intent.putExtra("album_id", info.getAlbumId());
//                                            intent.putExtra("album_art", info.getPicSmall());
//                                            intent.putExtra("album", info.getTitle());
//                                            intent.putExtra("album_detail", info.getAlbumDesc());
//                                            mContext.startActivity(intent);
//                                        }
//                                        return null;
//                                    }
//                                };
//                            } else {
//
//                                Intent intent = new Intent(mContext, AlbumsDetailActivity.class);
//                                intent.putExtra("album_id", mMusic.getAlbumId() + "");
//                                intent.putExtra("album_art", mMusic.getAlbumArt());
//                                intent.putExtra("album", mMusic.getAlbum());
//                                mContext.startActivity(intent);
//                            }
//                            dismiss();
                            break;
                        case 6:
                            if (mMusic.isLocal()) {
                                new AlertDialog.Builder(mContext).setTitle(getString(R.string.sure_to_set_ringtone))
                                        .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Uri ringUri = Uri.parse("file://" + mMusic.getUrl());
                                                RingtoneManager.setActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_NOTIFICATION, ringUri);
                                                dialog.dismiss();
                                                ToastUtil.show(getString(R.string.set_ringtone_success));
                                                dismiss();
                                            }
                                        })
                                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                            }
                            break;
                        case 7:
                            MusicDetailFragment detailFragment = MusicDetailFragment.newInstance(mMusic);
                            detailFragment.show(getActivity().getSupportFragmentManager(), "detail");
                            dismiss();
                            break;
                        default:
                            break;
                    }
                }
            });
            mRecyclerView.setAdapter(mMusicFlowAdapter);
            return;
        }

        mCommonAdapter.setOnItemClickListener(new OverFlowAdapter.IOnRecyclerViewItemClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onItemClick(View view, String data) {
                switch (Integer.parseInt(data)) {
                    case 0:
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                @SuppressLint("UseSparseArrays") HashMap<Long, Music> map = new HashMap<>();
                                int len = mMusics.size();
                                long[] listId = new long[len];
                                for (int i = 0; i < len; i++) {
                                    Music music = mMusics.get(i);
                                    listId[i] = music.getId();
                                    map.put(listId[i], music);
                                }

                                MusicPlayer.playAll(map, listId, 0, false);
                            }
                        }, 60);
                        dismiss();
                        break;
                    case 1:
                        AddNetPlaylistFragment.newInstance(mMusics).show(getFragmentManager(), "add");
                        dismiss();
                        break;
                    case 2:
                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... params) {
                                for (Music music : mMusics) {
                                    if (MusicPlayer.getCurrentAudioId() == music.getId()) {
                                        if (MusicPlayer.getQueueSize() == 0) {
                                            MusicPlayer.stop();
                                        } else {
                                            MusicPlayer.next();
                                        }
                                    }
                                    Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, music.getId());
                                    mContext.getContentResolver().delete(uri, null, null);
                                    PlaylistManager.getInstance(mContext).deleteMusic(mContext, music.getId());
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void v) {
                                mContext.sendBroadcast(new Intent(Actions.ACTION_MUSIC_COUNT_CHANGED));
                            }
                        }.execute();
                        dismiss();
                        break;
                }
            }
        });
        mRecyclerView.setAdapter(mCommonAdapter);
    }

    /**
     * 设置音乐overflow条目
     */
    private void setMusicInfo() {
        setInfo("下一首播放", R.drawable.ic_next_play);
        setInfo("收藏到歌单", R.drawable.ic_add_to_playlist);
        setInfo("分享", R.drawable.ic_lay_share);
        setInfo("删除", R.drawable.ic_delete);
        setInfo("歌手：" + mArtist, R.drawable.ic_artist);
        setInfo("专辑：" + mAlbum, R.drawable.ic_album);
        setInfo("设为铃声", R.drawable.ic_ring);
        setInfo("查看歌曲信息", R.drawable.ic_document);
    }

    /**
     * 设置专辑，艺术家，文件夹overflow条目
     */
    private void setCommonInfo() {
        setInfo("播放", R.drawable.ic_lay_play);
        setInfo("收藏到歌单", R.drawable.ic_add_to_playlist);
        setInfo("删除", R.drawable.ic_delete);
    }

    private void setInfo(String title, int id) {
//        mList.clear();
        OverFlowItem information = new OverFlowItem();
        information.setTitle(title);
        information.setAvatar(id);
        // 将新的info对象加入到信息列表中
        mList.add(information);
    }
}
