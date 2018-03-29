package com.charles.funmusic.lyric;

import android.content.Context;

import java.util.List;


public interface ILrcView {
    /**
     * 初始化画笔，颜色，字体大小等设置
     */
    void init(Context context);

    /***
     * 设置数据源
     *
     * @param lrcRows 歌词行
     */
    void setLrcRows(List<LrcRow> lrcRows);

    /**
     * 指定时间
     *
     * @param progress          时间进度
     * @param fromSeekBarByUser 是否由用户触摸SeekBar触发
     */
    void seekTo(int progress, boolean fromSeekBar, boolean fromSeekBarByUser);

    /***
     * 设置歌词文字的缩放比例
     *
     * @param scalingFactor 缩放比例
     */
    void setLrcScalingFactor(float scalingFactor);

    /**
     * 重置
     */
    void reset();
}
