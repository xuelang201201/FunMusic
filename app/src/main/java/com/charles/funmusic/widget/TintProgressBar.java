package com.charles.funmusic.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.charles.funmusic.utils.TintManager;

public class TintProgressBar extends ProgressBar implements Tintable {
    private AppCompatProgressBarHelper mProgressBarHelper;

    public TintProgressBar(Context context) {
        this(context, null);
    }

    public TintProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.progressBarStyle);
    }

    public TintProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        TintManager tintManage = TintManager.get(context);

        mProgressBarHelper = new AppCompatProgressBarHelper(this, tintManage);
        mProgressBarHelper.loadFromAttribute(attrs, defStyleAttr);
    }


    public void setProgressTintList(ColorStateList tint) {
        if (mProgressBarHelper != null) {
            mProgressBarHelper.setSupportProgressTint(tint);
        }
    }

    @Override
    public void tint() {
        if (mProgressBarHelper != null) {
            mProgressBarHelper.tint();
        }
    }
}
