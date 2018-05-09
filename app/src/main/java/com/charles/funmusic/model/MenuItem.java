package com.charles.funmusic.model;

import android.text.TextUtils;

public class MenuItem {

    private static final int NO_ICON = 0;
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_NO_ICON = 1;
    public static final int TYPE_SEPARATOR = 2;

    private int mType;
    private String mName;
    private int mIcon;

    public MenuItem(String name) {
        this(NO_ICON, name);
    }

    public MenuItem(int icon, String name) {
        mIcon = icon;
        mName = name;

        if (icon == NO_ICON && TextUtils.isEmpty(name)) {
            mType = TYPE_SEPARATOR;
        } else if (icon == NO_ICON) {
            mType = TYPE_NO_ICON;
        } else {
            mType = TYPE_NORMAL;
        }

        if (mType != TYPE_SEPARATOR && TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException("You need set a name for a non-SEPARATOR item");
        }
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getIcon() {
        return mIcon;
    }

    public void setIcon(int icon) {
        mIcon = icon;
    }
}
