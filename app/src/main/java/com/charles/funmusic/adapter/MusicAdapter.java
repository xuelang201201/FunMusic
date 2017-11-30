package com.charles.funmusic.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.model.Music;
import com.charles.funmusic.service.PlayService;
import com.charles.funmusic.utils.FileUtil;
import com.charles.funmusic.utils.FontUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 本地音乐适配器
 */
public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_FOOTER = 1;
    private static final int TYPE_NORMAL = 2;

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private int mPlayingPosition;
    private View mFooterView;
    private View mHeaderView;

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    public MusicAdapter() {
    }

    public MusicAdapter(Context context, OnItemClickListener onItemClickListener, OnItemLongClickListener onItemLongClickListener) {
        mContext = context;
        mOnItemClickListener = onItemClickListener;
        mOnItemLongClickListener = onItemLongClickListener;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public View getHeaderView() {
        return mHeaderView;
    }

    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyItemInserted(0);
    }

    public View getFooterView() {
        return mFooterView;
    }

    public void setFooterView(View footerView) {
        mFooterView = footerView;
        notifyItemInserted(getItemCount() - 1);
    }

    /**
     * 创建View，如果是HeaderView或者是FooterView，直接在Holder中返回
     *
     * @param parent   父容器
     * @param viewType view的类型：header,footer,normal
     * @return holder
     */
    @Override
    public MusicHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderView != null && viewType == TYPE_HEADER) {
            return new MusicHolder(mHeaderView);
        }

        if (mFooterView != null && viewType == TYPE_FOOTER) {
            return new MusicHolder(mFooterView);
        }

        View view = mLayoutInflater.inflate(R.layout.music_item, parent, false);
        return new MusicHolder(view);
    }

    /**
     * 绑定view，根据返回的position的类型，从而进行绑定
     *
     * @param holder   MusicHolder
     * @param position item的位置
     */
    @Override
    public void onBindViewHolder(MusicHolder holder, int position) {

    }

    @Override
    public void onBindViewHolder(MusicHolder holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {

            if (getItemViewType(position) == TYPE_NORMAL) {
                // 这里加载数据的时候要注意，是从position - 1开始，因为position == 0 已经被header占用了
                Music music = AppCache.getMusics().get(position - 1);
                holder.itemView.setTag(position);

                // 向控件内填充数据
//                String artist;
//                String album;
//
//                if ("<unknown>".equals(music.getArtist())) {
//                    artist = mContext.getString(R.string.unknown_artist);
//                } else {
//                    artist = music.getArtist();
//                }
//                if ("Music".equals(music.getAlbum()) || "0".equals(music.getAlbum())) {
//                    album = mContext.getString(R.string.unknown_album);
//                } else {
//                    album = music.getAlbum();
//                }
                String artistAndAlbum = FileUtil.getArtistAndAlbum(music.getArtist(), music.getAlbum());
                holder.mArtistAndAlbum.setText(artistAndAlbum);
                holder.mTitle.setText(music.getTitle());

//            Bitmap cover = CoverLoader.getInstance().loadThumbnail(music);
//            holder.mCover.setImageBitmap(cover);

                if (position - 1 == mPlayingPosition) {
                    holder.mPlaying.setVisibility(View.VISIBLE);
                } else {
                    holder.mPlaying.setVisibility(View.INVISIBLE);
                }
            }
        } else {
            if (position - 1 == mPlayingPosition) {
                holder.mPlaying.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 返回View中item的个数，item的个数加上HeaderView和FooterView
     *
     * @return
     */
    @Override
    public int getItemCount() {
        if (mHeaderView == null && mFooterView == null) {
            return AppCache.getMusics().size();
        } else if (mHeaderView != null && mFooterView == null) {
            return AppCache.getMusics().size() + 1;
        } else if (mHeaderView == null && mFooterView != null) {
            return AppCache.getMusics().size() + 1;
        } else {
            return AppCache.getMusics().size() + 2;
        }
    }

    /**
     * 重写这个方法，是加入Header和Footer的关键，
     * 通过判断item的类型，从而绑定不同的view
     *
     * @param position item的位置
     * @return header、footer 或者 normal
     */
    @Override
    public int getItemViewType(int position) {
        if (mHeaderView == null && mFooterView == null) {
            return TYPE_NORMAL;
        }

        if (position == 0) {
            // 第一个item应该加载Header
            return TYPE_HEADER;
        }

        if (position == getItemCount() - 1) {
            // 最后一个，应该加载Footer
            return TYPE_FOOTER;
        }
        return TYPE_NORMAL;
    }

    public void updatePlayingPosition(PlayService playService) {
        if (playService.getPlayingMusic() != null && playService.getPlayingMusic().getType() == Music.Type.LOCAL) {
            mPlayingPosition = playService.getPlayingPosition();
        } else {
            mPlayingPosition = -1;
        }
    }

    class MusicHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        @BindView(R.id.music_item_title)
        TextView mTitle;
        @BindView(R.id.music_item_artist_and_album)
        TextView mArtistAndAlbum;
        //        @BindView(R.id.music_item_cover)
//        ImageView mCover;
        @BindView(R.id.music_item_playing)
        View mPlaying;

        MusicHolder(View itemView) {
            super(itemView);

            // 如果是HeaderView或者是FooterView，直接返回
            if (itemView == mHeaderView) {
                return;
            }
            if (itemView == mFooterView) {
                return;
            }

            ButterKnife.bind(this, itemView);

            // 设置字体
            FontUtil util = new FontUtil();
            util.changeFont(mContext, mTitle);
            util.changeFont(mContext, mArtistAndAlbum);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mOnItemClickListener != null) {
                int position = (int) view.getTag();
                if (mPlayingPosition != position - 1) {
                    mPlaying.setVisibility(View.VISIBLE);
                    if (mPlayingPosition != -1) {
                        notifyItemChanged(mPlayingPosition, 0);
                    }
                    mPlayingPosition = position - 1;
                }
                mOnItemClickListener.onItemClick(position);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (mOnItemLongClickListener != null) {
                int position = (int) view.getTag();
                mOnItemLongClickListener.onItemLongClick(position);
            }
            return true;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }
}