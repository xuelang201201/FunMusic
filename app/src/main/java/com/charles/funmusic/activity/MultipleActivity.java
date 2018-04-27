package com.charles.funmusic.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore.Audio.Media;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.constant.Actions;
import com.charles.funmusic.fragment.AddPlaylistFragment;
import com.charles.funmusic.model.Music;
import com.charles.funmusic.provider.PlaylistManager;
import com.charles.funmusic.service.MusicPlayer;
import com.charles.funmusic.service.MusicService;
import com.charles.funmusic.utils.FileUtil;
import com.charles.funmusic.utils.MusicUtil;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MultipleActivity extends BaseActivity {

    @BindView(R.id.header_view)
    View mHeaderView;
    @BindView(R.id.header_view_image_view)
    ImageView mBack;
    @BindView(R.id.header_view_title_text_view)
    TextView mNumberOfChosen;
    @BindView(R.id.header_view_text_right)
    TextView mSelectAll;
    @BindView(R.id.activity_multiple_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.activity_multiple_next_play)
    LinearLayout mNextPlay;
    @BindView(R.id.activity_multiple_add_to_playlist)
    LinearLayout mAddToPlaylist;
    @BindView(R.id.activity_multiple_delete)
    LinearLayout mDelete;

    private ArrayList<Music> mMusics;
    private MultipleAdapter mAdapter;

    private SparseBooleanArray mSelectedPositions = new SparseBooleanArray();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple);

        mNumberOfChosen.setText(R.string.selected_zero);
        mSelectAll.setVisibility(View.VISIBLE);
        mSelectAll.setText(R.string.select_all);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);

        new loadSongs().execute("");

        initSystemBar(mHeaderView);
    }

    @OnClick({R.id.header_view_image_view, R.id.header_view_text_right, R.id.activity_multiple_next_play, R.id.activity_multiple_add_to_playlist, R.id.activity_multiple_delete})
    public void onViewClicked(View view) {
        final ArrayList<Music> selectedItems = getSelectedItem();
        switch (view.getId()) {
            case R.id.header_view_image_view:
                onBackPressed();
                break;

            case R.id.header_view_text_right:
                String str = "已选择" + getSelectedItem().size() + "项";
                if (mSelectAll.getText() == getString(R.string.select_all)) {
                    for (int i = 0; i < mMusics.size(); i++) {
                        setItemChecked(i, true);
                    }
                    mAdapter.notifyDataSetChanged();

                    mSelectAll.setText(R.string.select_none);
                    mNumberOfChosen.setText(str);
                } else {
                    for (int i = 0; i < mMusics.size(); i++) {
                        setItemChecked(i, false);
                    }
                    mAdapter.notifyDataSetChanged();

                    mSelectAll.setText(R.string.select_all);
                    mNumberOfChosen.setText(str);
                }
                break;

            case R.id.activity_multiple_next_play:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<Music> selectedMusics = getSelectedItem();
                        long currentAudioId = MusicPlayer.getCurrentAudioId();

                        for (int i = 0; i < selectedMusics.size(); i++) {
                            if (selectedMusics.get(i).getId() == currentAudioId) {
                                selectedMusics.remove(i);
                                break;
                            }
                        }

                        final long[] list = new long[selectedMusics.size()];
                        HashMap<Long, Music> map = new HashMap();
                        for (int i = 0; i < selectedMusics.size(); i++) {
                            list[i] = selectedMusics.get(i).getId();
                            Music music = selectedMusics.get(i);
                            list[i] = music.getId();
                            music.setLocal(true);
                            music.setAlbumArt(MusicUtil.getAlbumArtUri(music.getAlbumId()) + "");
                            map.put(list[i], selectedMusics.get(i));
                        }

                        MusicPlayer.playNext(MultipleActivity.this, map, list);
                    }
                }, 100);
                break;

            case R.id.activity_multiple_add_to_playlist:
                long[] list = new long[selectedItems.size()];
                for (int i = 0; i < getSelectedItem().size(); i++) {
                    list[i] = selectedItems.get(i).getId();
                }
                AddPlaylistFragment.newInstance(list).show(getSupportFragmentManager(), "add");
                Intent intent = new Intent(MusicService.PLAYLIST_CHANGED);
                sendBroadcast(intent);
                break;

            case R.id.activity_multiple_delete:
                new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.sure_to_remove_from_local_list))
                        .setPositiveButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
                            @SuppressLint("StaticFieldLeak")
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new AsyncTask<Void, Void, Void>() {

                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        for (Music music : selectedItems) {
                                            if (MusicPlayer.getCurrentAudioId() == music.getId()) {
                                                if (MusicPlayer.getQueueSize() == 0) {
                                                    MusicPlayer.stop();
                                                } else {
                                                    MusicPlayer.next();
                                                }
                                            }
                                            Uri uri = ContentUris.withAppendedId(
                                                    Media.EXTERNAL_CONTENT_URI, music.getId());
                                            MultipleActivity.this.getContentResolver().delete(
                                                    uri, null, null);
                                            PlaylistManager.getInstance(MultipleActivity.this)
                                                    .deleteMusic(MultipleActivity.this,
                                                            music.getId());
                                        }
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void aVoid) {
                                        mAdapter.updateDateSet();
                                        mAdapter.notifyDataSetChanged();
                                        MultipleActivity.this.sendBroadcast(new Intent(Actions.ACTION_MUSIC_COUNT_CHANGED));
                                    }
                                }.execute();
                                dialog.dismiss();
                            }
                        }).setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    /**
     * 异步加载RecyclerView界面
     */
    @SuppressLint("StaticFieldLeak")
    private class loadSongs extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if (getIntent().getParcelableArrayListExtra("ids") != null) {
                mMusics = getIntent().getParcelableArrayListExtra("ids");
            }
            if (mMusics != null) {
                mAdapter = new MultipleAdapter(mMusics);
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            mRecyclerView.setAdapter(mAdapter);
        }

        @Override
        protected void onPreExecute() {
        }
    }

    void setItemChecked(int position, boolean isChecked) {
        mSelectedPositions.put(position, isChecked);
    }

    boolean isItemChecked(int position) {
        return mSelectedPositions.get(position);
    }

    public ArrayList<Music> getSelectedItem() {
        ArrayList<Music> selectItem = new ArrayList<>();
        for (int i = 0; i < mMusics.size(); i++) {
            if (isItemChecked(i)) {
                selectItem.add(mMusics.get(i));
            }
        }
        return selectItem;
    }

    public class MultipleAdapter extends RecyclerView.Adapter<MultipleAdapter.MultipleHolder> {

        private ArrayList<Music> mMusics;

        MultipleAdapter(ArrayList<Music> musics) {
            if (musics == null) {
                throw new IllegalArgumentException("Model data must not be null!");
            }
            mMusics = musics;
        }

        /**
         * 更新adapter的数据
         */
        public void updateDateSet() {
            mNumberOfChosen.setText(R.string.selected_zero);
            mMusics.removeAll(getSelectedItem());
            mSelectedPositions.clear();
        }

        @NonNull
        @Override
        public MultipleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.multiple_item, parent, false);
            return new MultipleHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MultipleHolder holder, @SuppressLint("RecyclerView") final int position) {
            Music music = mMusics.get(position);
            // 设置条目状态
            holder.mTitle.setText(music.getTitle());
            holder.mArtistAndAlbum.setText(FileUtil.getArtistAndAlbum(music.getArtist(), music.getAlbum()));
            holder.mCheckBox.setChecked(isItemChecked(position));
            holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onClick(View v) {
                    if (isItemChecked(position)) {
                        setItemChecked(position, false);
                    } else {
                        setItemChecked(position, true);
                    }
                    mNumberOfChosen.setText("已选择" + getSelectedItem().size() + "项");
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onClick(View v) {
                    if (isItemChecked(position)) {
                        setItemChecked(position, false);
                    } else {
                        setItemChecked(position, true);
                    }
                    notifyItemChanged(position);
                    mNumberOfChosen.setText("已选择" + getSelectedItem().size() + "项");
                }
            });
        }

        @Override
        public int getItemCount() {
            return mMusics == null ? 0 : mMusics.size();
        }

        public class MultipleHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.multiple_item_checkbox)
            CheckBox mCheckBox;
            @BindView(R.id.multiple_item_title)
            TextView mTitle;
            @BindView(R.id.multiple_item_artist_and_album)
            TextView mArtistAndAlbum;

            MultipleHolder(View itemView) {
                super(itemView);

                ButterKnife.bind(this, itemView);

                changeFont(mTitle, false);
                changeFont(mArtistAndAlbum, false);
            }
        }
    }
}
