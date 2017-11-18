package com.charles.funmusic.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * 向上滑动控件顶部悬浮效果实现
 * <p>
 * ScrollView 并没有实现滚动监听，所以必须自行实现对ScrollView的监听，
 * 在onTouchEvent()方法中对滚动Y轴进行监听
 * ScrollView的滚动Y值进行监听
 */
public class SuspendScrollView extends ScrollView {

    private OnScrollListener mOnScrollListener;

    /**
     * 手指离开SuspendScrollView，SuspendScrollView还在继续滚动
     * 用来保存Y轴的距离，然后作比较
     */
    private static int mLastScrollY;

    public SuspendScrollView(Context context) {
        super(context);
    }

    public SuspendScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SuspendScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 设置滚动接口
     *
     * @param onScrollListener 滚动监听
     */
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        mOnScrollListener = onScrollListener;
    }

    /**
     * 用于手指离开SuspendScrollView的时候获取SuspendScrollView的Y轴的滚动距离，
     * 然后回调给onScroll方法
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int scrollY = SuspendScrollView.this.getScrollY();

            // 此时的距离和记录下的距离不相等，再隔5毫秒给handler发送消息
            if (mLastScrollY != scrollY) {
                mLastScrollY = scrollY;
                mHandler.sendMessageDelayed(mHandler.obtainMessage(), 5);
            }
            if (mOnScrollListener != null) {
                mOnScrollListener.onScroll(scrollY);
            }
        }
    };

    /**
     * 重写onTouchEvent，当手在SuspendScrollView上面的时候，
     * 直接将SuspendScrollView滚动的Y方向距离回调给onScroll方法，当抬起手的时候，
     * SuspendScrollView可能还在滚动，所以当抬起手隔20毫秒给handler发送消息，
     * 在handler处理SuspendScrollView滚动的距离
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(mLastScrollY = this.getScrollY());
        }

        if (ev.getAction() == MotionEvent.ACTION_UP) {
            mHandler.sendMessageDelayed(mHandler.obtainMessage(), 20);
        }

        return super.onTouchEvent(ev);
    }

    /**
     * 滚动的回调接口
     */
    public interface OnScrollListener {
        /**
         * 回调方法，返回SuspendScrollView的Y轴方向滚动的距离
         *
         * @param scrollY Y轴滚动距离
         */
        void onScroll(int scrollY);
    }
}
