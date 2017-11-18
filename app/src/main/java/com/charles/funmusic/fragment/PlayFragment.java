package com.charles.funmusic.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.adapter.PlayPagerAdapter;
import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.constant.Actions;
import com.charles.funmusic.enums.PlayModeEnum;
import com.charles.funmusic.model.Music;
import com.charles.funmusic.service.OnPlayerEventListener;
import com.charles.funmusic.utils.Preferences;
import com.charles.funmusic.utils.ScreenUtil;
import com.charles.funmusic.utils.SystemUtil;
import com.charles.funmusic.utils.ToastUtil;
import com.charles.funmusic.widget.AlbumCoverView;
import com.charles.funmusic.widget.IndicatorLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 正在播放界面
 */
public class PlayFragment extends BaseFragment implements OnPlayerEventListener, SeekBar.OnSeekBarChangeListener, ViewPager.OnPageChangeListener {

    @BindView(R.id.fragment_play_head_container)
    View mHeadContainer;
    @BindView(R.id.fragment_play_back)
    ImageView mBack;
    @BindView(R.id.fragment_play_music_title)
    TextView mTitle;
    @BindView(R.id.fragment_play_share)
    ImageView mShare;
    @BindView(R.id.fragment_play_artist)
    TextView mArtist;
    @BindView(R.id.fragment_play_view_pager)
    ViewPager mViewPager;
    @BindView(R.id.fragment_play_indicator)
    IndicatorLayout mIndicator;
    @BindView(R.id.fragment_play_current_time)
    TextView mCurrentTime;
    @BindView(R.id.fragment_play_seek_bar)
    SeekBar mSeekBar;
    @BindView(R.id.fragment_play_duration)
    TextView mDuration;
    @BindView(R.id.fragment_play_play_mode)
    ImageView mPlayMode;
    @BindView(R.id.fragment_play_prev)
    ImageView mPrev;
    @BindView(R.id.fragment_play_play_or_pause)
    ImageView mPlayOrPause;
    @BindView(R.id.fragment_play_next)
    ImageView mNext;

    private View mLrcViewFull;
    private AlbumCoverView mAlbumCoverView;

    private List<View> mViewPagerContent;
    private int mLastProgress;
    private boolean isDraggingProgress;

    private BroadcastReceiver mVolumeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    @Override
    public int getLayoutId() {
        return R.layout.fragment_play;
    }

    @Override
    public void initView() {
        initSystemBar();
        initViewPager();
        mIndicator.create(mViewPagerContent.size());
        initPlayMode();
        onChangeImpl(getPlayService().getPlayingMusic());

        mSeekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Actions.VOLUME_CHANGED_ACTION);
        AppCache.getContext().registerReceiver(mVolumeReceiver, filter);
    }

    /**
     * 沉浸式状态栏
     */
    private void initSystemBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int top = ScreenUtil.getStatusBarHeight();
            mHeadContainer.setPadding(0, top, 0, 0);
        }
    }

    private void initViewPager() {
        View coverView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_play_page_cover, null);
        View lrcView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_play_page_lrc, null);
        mAlbumCoverView = coverView.findViewById(R.id.fragment_play_page_cover_album_cover_view);
        mLrcViewFull = lrcView.findViewById(R.id.fragment_play_page_lrc_lrc_view);
        mAlbumCoverView.initNeedle(getPlayService().isPlaying());
        mViewPagerContent = new ArrayList<>(2);
        mViewPagerContent.add(coverView);
        mViewPagerContent.add(mLrcViewFull);
        mViewPager.setAdapter(new PlayPagerAdapter(mViewPagerContent));
    }

    private void initPlayMode() {
        int mode = Preferences.getPlayMode();
        mPlayMode.setImageLevel(mode);
    }

    private void onChangeImpl(Music music) {
        if (music == null) {
            return;
        }

        String artist;

        if ("<unknown>".equals(music.getArtist())) {
            artist = AppCache.getContext().getString(R.string.unknown_artist);
        } else {
            artist = music.getArtist();
        }

        mTitle.setText(music.getTitle());
        changeFont(mTitle,false);
        mArtist.setText(artist);
        changeFont(mArtist, false);
        mSeekBar.setProgress((int) getPlayService().getCurrentPosition());
        mSeekBar.setSecondaryProgress(0);
        mSeekBar.setMax((int) music.getDuration());
        mLastProgress = 0;
        mCurrentTime.setText(R.string.play_time_start);
        mDuration.setText(formatTime(music.getDuration()));
        if (getPlayService().isPlaying() || getPlayService().isPreparing()) {
            mPlayOrPause.setSelected(true);
        } else {
            mPlayOrPause.setSelected(false);
        }
    }

    @OnClick({R.id.fragment_play_back, R.id.fragment_play_play_mode, R.id.fragment_play_play_or_pause, R.id.fragment_play_next, R.id.fragment_play_prev})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_play_back:
                onBackPressed();
                break;

            case R.id.fragment_play_play_mode:
                switchPlayMode();
                break;

            case R.id.fragment_play_play_or_pause:
                playOrPause();
                break;

            case R.id.fragment_play_next:
                next();
                break;

            case R.id.fragment_play_prev:
                prev();
                break;
        }
    }

    /**
     * 返回activity
     */
    public void onBackPressed() {
        getActivity().onBackPressed();
        mBack.setEnabled(false);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBack.setEnabled(true);
            }
        }, 300);
    }

    /**
     * 切换播放模式
     */
    private void switchPlayMode() {
        PlayModeEnum mode = PlayModeEnum.valueOf(Preferences.getPlayMode());
        switch (mode) {
            case LOOP:
                mode = PlayModeEnum.SHUFFLE;
                ToastUtil.show(getString(R.string.mode_shuffle));
                break;

            case SHUFFLE:
                mode = PlayModeEnum.SINGLE;
                ToastUtil.show(getString(R.string.mode_loop_single));
                break;

            case SINGLE:
                mode = PlayModeEnum.LOOP;
                ToastUtil.show(getString(R.string.mode_loop));
                break;
        }
        Preferences.savePlayMode(mode.value());
        initPlayMode();
    }

    /**
     * 播放或者暂停
     */
    private void playOrPause() {
        getPlayService().playPause();
    }

    /**
     * 下一首
     */
    private void next() {
        mAlbumCoverView.pause();
        getPlayService().next();
    }

    /**
     * 上一首
     */
    private void prev() {
        mAlbumCoverView.pause();
        getPlayService().prev();
    }

    @Override
    public void onChange(Music music) {
        mAlbumCoverView.pause();
        onChangeImpl(music);
    }

    @Override
    public void onPlayerStart() {
        mAlbumCoverView.start();
        mPlayOrPause.setSelected(true);

    }

    @Override
    public void onPlayerPause() {
        mAlbumCoverView.pause();
        mPlayOrPause.setSelected(false);
    }

    /**
     * 更新播放进度
     * @param progress 进度
     */
    @Override
    public void onPublish(int progress) {
        if (! isDraggingProgress) {
            mSeekBar.setProgress(progress);
        }
    }

    @Override
    public void onBufferingUpdate(int percent) {
        mSeekBar.setSecondaryProgress(mSeekBar.getMax() * 100 / percent);
    }

    @Override
    public void onTimer(long remain) {

    }

    @Override
    public void onMusicListUpdate() {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar == mSeekBar) {
            if (Math.abs(progress - mLastProgress) >= DateUtils.SECOND_IN_MILLIS) {
                mCurrentTime.setText(formatTime(progress));
                mLastProgress = progress;
            }
        }
    }

    /**
     * 格式化时间
     */
    private String formatTime(long time) {
        return SystemUtil.formatTime("mm:ss", time);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (seekBar == mSeekBar) {
            isDraggingProgress = true;
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar == mSeekBar) {
            isDraggingProgress = false;
            if (getPlayService().isPlaying() || getPlayService().isPausing()) {
                int progress = seekBar.getProgress();
                getPlayService().seekTo(progress);
            } else {
                seekBar.setProgress(0);
            }
        }
    }

    @Override
    public void onDestroy() {
        AppCache.getContext().unregisterReceiver(mVolumeReceiver);
        super.onDestroy();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mIndicator.setCurrent(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
