package com.charles.funmusic.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.TextViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.model.MenuItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenuItemAdapter extends BaseAdapter {
    private final int mIconSize;
    private LayoutInflater mLayoutInflater;
    private Context mContext;

    public MenuItemAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;

        mIconSize = context.getResources().getDimensionPixelSize(R.dimen.drawer_icon_size);
    }

    private List<MenuItem> mItems = new ArrayList<>(
            Arrays.asList(
                    new MenuItem(R.drawable.ic_skin, "个性换肤"),
                    new MenuItem(R.drawable.ic_timer, "定时停止播放"),
                    new MenuItem(R.drawable.ic_setting, "设置"),
                    new MenuItem(R.drawable.ic_exit, "退出"),
                    new MenuItem(R.drawable.ic_skin, "个性换肤"),
                    new MenuItem(R.drawable.ic_timer, "定时停止播放"),
                    new MenuItem(R.drawable.ic_setting, "设置"),
                    new MenuItem(R.drawable.ic_exit, "退出"),
                    new MenuItem(R.drawable.ic_skin, "个性换肤"),
                    new MenuItem(R.drawable.ic_timer, "定时停止播放"),
                    new MenuItem(R.drawable.ic_setting, "设置"),
                    new MenuItem(R.drawable.ic_exit, "退出"),
                    new MenuItem(R.drawable.ic_skin, "个性换肤"),
                    new MenuItem(R.drawable.ic_timer, "定时停止播放"),
                    new MenuItem(R.drawable.ic_setting, "设置"),
                    new MenuItem(R.drawable.ic_exit, "退出"),
                    new MenuItem(R.drawable.ic_skin, "个性换肤"),
                    new MenuItem(R.drawable.ic_timer, "定时停止播放"),
                    new MenuItem(R.drawable.ic_setting, "设置"),
                    new MenuItem(R.drawable.ic_exit, "退出")
            )
    );

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MenuItem item = mItems.get(position);
        switch (item.getType()) {
            case MenuItem.TYPE_NORMAL:
                if (convertView == null) {
                    convertView = mLayoutInflater.inflate(R.layout.design_drawer_item, parent, false);
                }
                TextView itemView = (TextView) convertView;
                itemView.setText(item.getName());
                Drawable icon = mContext.getResources().getDrawable(item.getIcon());
                if (icon != null) {
                    icon.setBounds(0, 0, mIconSize, mIconSize);
                    TextViewCompat.setCompoundDrawablesRelative(itemView, icon, null, null, null);
                }
                break;

            case MenuItem.TYPE_NO_ICON:
                if (convertView == null) {
                    convertView = mLayoutInflater.inflate(R.layout.design_drawer_item_subheader, parent, false);
                }
                TextView subHeader = (TextView) convertView;
                subHeader.setText(item.getName());
                break;

            case MenuItem.TYPE_SEPARATOR:
                if (convertView == null) {
                    convertView = mLayoutInflater.inflate(R.layout.design_drawer_item_separator, parent, false);
                }
                break;
        }
        return convertView;
    }
}
