package com.charles.funmusic.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.constant.Actions;
import com.charles.funmusic.model.Music;
import com.charles.funmusic.model.Playlist;
import com.charles.funmusic.provider.PlaylistInfo;
import com.charles.funmusic.provider.PlaylistManager;
import com.charles.funmusic.utils.MusicUtil;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddPlaylistFragment extends AttachDialogFragment {
    @BindView(R.id.fragment_add_playlist_layout)
    LinearLayout mContainer;
    @BindView(R.id.fragment_add_playlist_recycler_view)
    RecyclerView mRecyclerView;

    private PlaylistInfo mPlaylistInfo;
    private PlaylistManager mPlaylistManager;
    private ArrayList<Music> mMusics;
    private String mAuthor;
    private EditText mEditText;

    public static AddPlaylistFragment newInstance(ArrayList<Music> musics, String author) {
        AddPlaylistFragment fragment = new AddPlaylistFragment();
        Bundle args = new Bundle();
        args.putString("author", author);
        args.putParcelableArrayList("musics", musics);
        fragment.setArguments(args);
        return fragment;
    }

    public static AddPlaylistFragment newInstance(ArrayList<Music> musics) {
        AddPlaylistFragment fragment = new AddPlaylistFragment();
        Bundle args = new Bundle();
        args.putString("author", "local");
        args.putParcelableArrayList("musics", musics);
        fragment.setArguments(args);
        return fragment;
    }

    public static AddPlaylistFragment newInstance(long[] songList) {
        AddPlaylistFragment fragment = new AddPlaylistFragment();
        Bundle args = new Bundle();
        args.putLongArray("songs", songList);
        fragment.setArguments(args);
        return fragment;
    }

    public static AddPlaylistFragment newInstance(Music music) {
        ArrayList<Music> musics = new ArrayList<>();
        musics.add(music);
        return AddPlaylistFragment.newInstance(musics);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomDatePickerDialog);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_playlist, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    public void init() {
        // 设置无标题
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (getArguments() != null) {
            mMusics = getArguments().getParcelableArrayList("musics");
            mAuthor = getArguments().getString("author");
        }
        mPlaylistInfo = PlaylistInfo.getInstance(mContext);
        mPlaylistManager = PlaylistManager.getInstance(mContext);

        ArrayList<Playlist> playLists = mPlaylistInfo.getPlaylist();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        AddPlaylistAdapter adapter = new AddPlaylistAdapter(playLists);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置fragment高度、宽度
        int dialogHeight = (int) (mContext.getResources().getDisplayMetrics().heightPixels * 0.65);
        int dialogWidth = (int) (mContext.getResources().getDisplayMetrics().widthPixels * 0.77);
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
            getDialog().setCanceledOnTouchOutside(true);
        }
    }

    @OnClick({R.id.fragment_add_playlist_layout})
    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_add_playlist_layout:
                createNewPlaylist();
                break;
        }
    }

    @SuppressLint("InflateParams")
    private void createNewPlaylist() {
        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setView(mContext.getLayoutInflater().inflate(R.layout.dialog_new_playlist, null));
        alertDialog.show();
        Window window = alertDialog.getWindow();
        if (window != null) {
            window.setContentView(R.layout.dialog_new_playlist);
            final ImageView clear = window.findViewById(R.id.dialog_new_playlist_clear);
            mEditText = window.findViewById(R.id.dialog_new_playlist_edit_text);
            final TextView confirm = window.findViewById(R.id.dialog_new_playlist_confirm);
            mEditText.requestFocus();
            mEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!TextUtils.isEmpty(s)) {
                        clear.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mEditText.getText().clear();
                            }
                        });
                        confirm.setClickable(true);
                    } else {
                        clear.setVisibility(View.GONE);
                        confirm.setClickable(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
            window.findViewById(R.id.dialog_new_playlist_cancel)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    confirm();
                    alertDialog.dismiss();
                }
            });
        }
    }

    private void confirm() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("add_play", "here");
                String albumArt = null;
                for (Music music : mMusics) {
                    albumArt = music.getAlbumArt();
                    if (music.isLocal()) {
                        if (albumArt.equals(MusicUtil.getAlbumArt(AppCache.getContext(), music.getId()))) {
                            break;
                        }
                    } else if (!TextUtils.isEmpty(albumArt)) {
                        break;
                    }
                }
                long playListId = mEditText.getText().hashCode();
                mPlaylistInfo.addPlaylist(playListId, mEditText.getText().toString(),
                        mMusics.size(), albumArt, mAuthor);
                mPlaylistManager.insertLists(mContext, playListId, mMusics);
                Intent intent = new Intent(Actions.ACTION_PLAYLIST_COUNT_CHANGED);
                AppCache.getContext().sendBroadcast(intent);
            }
        }).start();
    }

    class AddPlaylistAdapter extends RecyclerView.Adapter<AddPlaylistAdapter.AddPlaylistHolder> {
        ArrayList<Playlist> mPlayLists;

        AddPlaylistAdapter(ArrayList<Playlist> playLists) {
            mPlayLists = playLists;
        }

        @Override
        public AddPlaylistHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new AddPlaylistHolder(LayoutInflater.from(mContext).inflate(
                    R.layout.love_playlist_item, parent, false));
        }

        @Override
        public void onBindViewHolder(AddPlaylistHolder holder, int position) {
            Playlist playlist = mPlayLists.get(position);
            holder.mTitle.setText(playlist.getName());
            String count = playlist.getSongCount() + "";
            holder.mCount.setText(count);
            Uri uri = Uri.parse(playlist.getAlbumArt());
            holder.mImageView.setImageURI(uri);
        }

        @Override
        public int getItemCount() {
            return mPlayLists.size();
        }

        class AddPlaylistHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            @BindView(R.id.love_playlist_item_image)
            SimpleDraweeView mImageView;
            @BindView(R.id.love_playlist_item_title)
            TextView mTitle;
            @BindView(R.id.love_playlist_item_count)
            TextView mCount;

            AddPlaylistHolder(View itemView) {
                super(itemView);

                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                final Playlist playlist = mPlayLists.get(getAdapterPosition());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mPlaylistManager.insertLists(mContext, playlist.getId(), mMusics);
                            Intent intent = new Intent(Actions.ACTION_MUSIC_COUNT_CHANGED);
                            mContext.sendBroadcast(intent);
                            dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }
}
