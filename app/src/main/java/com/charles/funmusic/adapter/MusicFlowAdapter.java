package com.charles.funmusic.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.model.Music;
import com.charles.funmusic.model.OverFlowItem;
import com.charles.funmusic.widget.TintImageView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MusicFlowAdapter extends RecyclerView.Adapter<MusicFlowAdapter.ListItemViewHolder> implements View.OnClickListener {
    private List<OverFlowItem> mList;
    private Music mMusic;
    private Context mContext;
    private IOnRecyclerViewItemClickListener mOnItemClickListener = null;

    public MusicFlowAdapter(Context context, List<OverFlowItem> list, Music music) {
        mContext = context;
        mList = list;
        mMusic = music;
    }

    public Music getMusic() {
        return mMusic;
    }

    public void setOnItemClickListener(IOnRecyclerViewItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public ListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_flow_item, parent, false);
        ListItemViewHolder holder = new ListItemViewHolder(view);
        // 将创建的View注册点击事件
        view.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ListItemViewHolder holder, int position) {
        OverFlowItem item = mList.get(position);
        holder.mIcon.setImageResource(item.getAvatar());
//        holder.mIcon.setImageTintList(R.color.theme_color_primary);
        holder.mTitle.setText(item.getTitle());
        // 设置tag
        holder.itemView.setTag(position + "");
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            // 注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v, (String) v.getTag());
        }
    }

    class ListItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.music_flow_item_image_view)
        TintImageView mIcon;
        @BindView(R.id.music_flow_item_text_view)
        TextView mTitle;

        ListItemViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

    /**
     * 定义接口
     */
    public interface IOnRecyclerViewItemClickListener {
        void onItemClick(View view, String data);
    }
}
