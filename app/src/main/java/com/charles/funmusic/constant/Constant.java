package com.charles.funmusic.constant;

/**
 * 常量类
 */
public interface Constant {
    enum Position {LEFT, CENTER, RIGHT}

    /**
     * Activity发出的广播：播放或暂停
     */
    String ACTION_PLAY_OR_PAUSE = "cn.tedu.intent.action.PLAY_OR_PAUSE";
    /**
     * Activity发出的广播：播放上一首
     */
    String ACTION_PREVIOUS = "cn.tedu.intent.action.PLAY_PREVIOUS";
    /**
     * Activity发出的广播：播放下一首
     */
    String ACTION_NEXT = "cn.tedu.intent.action.NEXT";
    /**
     * Activity发出的广播：播放指定的歌曲
     */
    String ACTION_PLAY = "cn.tedu.intent.action.PLAY";
    /**
     * Activity发出的广播：从指定的位置开始播放歌曲，用于拖拽进度条之后
     */
    String ACTION_PLAY_FROM_PERCENT = "cn.tedu.intent.action.ACTION_PLAY_FROM_PERCENT";
    /**
     * Service发出的广播：设置界面为播放状态
     */
    String ACTION_SET_PLAY_STATE = "cn.tedu.intent.action.SET_PLAY_STATE";
    /**
     * Service发出的广播：设置界面为暂停状态
     */
    String ACTION_SET_PAUSE_STATE = "cn.tedu.intent.action.SET_PAUSE_STATE";
    /**
     * Service发出的广播:更新播放进度
     */
    String ACTION_UPDATE_PROGRESS = "cn.tedu.intent.action.UPDATE_PROGRESS";
    /**
     * Extra：歌曲的position
     */
    String EXTRA_POSITION = "cn.tedu.intent.extra.POSITION";
    /**
     * Extra：歌曲的总时长
     */
    String EXTRA_DURATION = "cn.tedu.intent.extra.DURATION";
    /**
     * Extra：歌曲当前播放到的时间
     */
    String EXTRA_CURRENT_POSITON = "cn.tedu.intent.extra.CURRENT_POSITION";
    /**
     * Extra:歌曲当前播放到的进度的百分比
     */
    String EXTRA_PERCENT = "cn.tedu.intent.extra.PERCENT";
}
