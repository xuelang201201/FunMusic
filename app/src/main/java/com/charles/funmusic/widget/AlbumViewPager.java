package com.charles.funmusic.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 播放界面的ViewPager
 */
public class AlbumViewPager extends ViewPager {

    PointF mDownPoint = new PointF();
    OnSingleTouchListener mOnSingleTouchListener;

    public AlbumViewPager(Context context) {
        super(context);
    }

    public AlbumViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnSingleTouchListener(OnSingleTouchListener onSingleTouchListener) {
        mOnSingleTouchListener = onSingleTouchListener;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 记录按下时候的坐标
                mDownPoint.x = ev.getX();
                mDownPoint.y = ev.getY();
                if (this.getChildCount() > 1) {
                    // 有内容，多于1个时
                    // 通知其父控件，现在进行的是本控件的操作，不允许拦截
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;

            case MotionEvent.ACTION_UP:
                // 在up时判断是否按下和松手的坐标为一个点
                if (PointF.length(ev.getX() - mDownPoint.x,
                        ev.getY() - mDownPoint.y) < (float) 5.0) {
                    onSingleTouch(this);
                    return true;
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void onSingleTouch(View v) {
        if (mOnSingleTouchListener != null) {
            mOnSingleTouchListener.onSingleTouch(v);
        }
    }

    public interface OnSingleTouchListener {
        void onSingleTouch(View v);
    }
}
