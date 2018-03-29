package com.charles.funmusic.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;

import com.charles.funmusic.R;

public class PlayerSeekBar extends AppCompatSeekBar {

    private boolean isDrawLoading = false;
    private int mDegree = 0;
    private Matrix mMatrix = new Matrix();
    private Bitmap mLoading = BitmapFactory.decodeResource(getResources(), R.drawable.loading_circle);
    private Drawable mDrawable;

    public PlayerSeekBar(Context context) {
        super(context);
    }

    public PlayerSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setThumb(getContext().getResources().getDrawable(R.drawable.play_bar_thumb));
    }

    public void setLoading(boolean loading) {
        if (loading) {
            isDrawLoading = true;
            invalidate();
        }else {
            isDrawLoading = false;
        }
    }

    @Override
    public void setThumb(Drawable thumb) {
        Rect localRect = null;
        if (mDrawable != null) {
            localRect = mDrawable.getBounds();
        }
        super.setThumb(mDrawable);
        mDrawable = thumb;
        if ((localRect != null) && (mDrawable != null)) {
            mDrawable.setBounds(localRect);
        }
    }

    @Override
    public Drawable getThumb() {
        if (Build.VERSION.SDK_INT >= 16) {
            return super.getThumb();
        }
        return mDrawable;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isDrawLoading) {
            canvas.save();
            mDegree = ((int) (mDegree + 3.0F));
            mDegree %= 360;
            mMatrix.reset();
            mMatrix.postRotate(mDegree, mLoading.getWidth() / 2, mLoading.getHeight() / 2);
            canvas.translate(getPaddingLeft() + getThumb().getBounds().left + mDrawable.getIntrinsicWidth() / 2 - mLoading.getWidth() / 2 - getThumbOffset(), getPaddingTop() + getThumb().getBounds().top + mDrawable.getIntrinsicHeight() / 2 - mLoading.getHeight() / 2);
            canvas.drawBitmap(mLoading, mMatrix, null);
            canvas.restore();
            invalidate();
        }

    }
}
