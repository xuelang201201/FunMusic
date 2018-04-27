package com.charles.funmusic.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.fragment.SimpleMoreFragment;
import com.charles.funmusic.model.Music;
import com.charles.funmusic.service.MusicPlayer;
import com.charles.funmusic.utils.FontUtil;
import com.charles.funmusic.utils.MusicUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchHolder> {

    private Context mContext;
    private List<Music> mSearchResults = new ArrayList<>();

    public SearchAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public SearchAdapter.SearchHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.music_item, parent, false);
        return new SearchHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.SearchHolder holder, int position) {
        Music music = mSearchResults.get(position);
        holder.mTitle.setText(music.getTitle());
        holder.mArtist.setText(music.getArtist());
        setOnPopupMenuListener(holder, position);
    }

    @Override
    public void onViewRecycled(@NonNull SearchHolder holder) {
    }

    @Override
    public int getItemCount() {
        return mSearchResults.size();
    }

    private void setOnPopupMenuListener(SearchHolder holder, final int position) {
        holder.mMore.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SimpleMoreFragment moreFragment = SimpleMoreFragment.newInstance(mSearchResults.get(position).getId());
                moreFragment.show(((AppCompatActivity) mContext).getSupportFragmentManager(), "music");
            }
        });
    }

    public void updateSearchResults(List searchResults) {
        mSearchResults = searchResults;
    }

    public class SearchHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.music_item_title)
        TextView mTitle;
        @BindView(R.id.music_item_artist_and_album)
        TextView mArtist;
        @BindView(R.id.music_item_play_state)
        ImageView mPlayState;
        @BindView(R.id.music_item_more)
        ImageView mMore;

        SearchHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);

            FontUtil fontUtil = new FontUtil();

            fontUtil.changeFont(mContext, mTitle);
            mTitle.getPaint().setFakeBoldText(false);

            fontUtil.changeFont(mContext, mArtist);
            mArtist.getPaint().setFakeBoldText(false);
        }

        @Override
        public void onClick(View v) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long[] list = new long[mSearchResults.size()];
                    HashMap<Long, Music> map = new HashMap();
                    for (int i = 0; i < mSearchResults.size(); i++) {
                        Music music = mSearchResults.get(i);
                        list[i] = music.getId();
                        music.setLocal(true);
                        music.setAlbumArt(MusicUtil.getAlbumArtUri(music.getAlbumId()) + "");
                        map.put(list[i], mSearchResults.get(i));
                    }
                    MusicPlayer.playAll(map, list, getAdapterPosition(), false);
                }
            }).start();
        }
    }
}
