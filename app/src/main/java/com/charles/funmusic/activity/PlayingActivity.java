package com.charles.funmusic.activity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.constant.Actions;
import com.charles.funmusic.constant.Keys;
import com.charles.funmusic.fragment.PlayQueueFragment;
import com.charles.funmusic.fragment.SimpleMoreFragment;
import com.charles.funmusic.lyric.DefaultLrcParser;
import com.charles.funmusic.lyric.LrcRow;
import com.charles.funmusic.lyric.LrcView;
import com.charles.funmusic.model.Music;
import com.charles.funmusic.provider.PlaylistManager;
import com.charles.funmusic.service.MusicPlayer;
import com.charles.funmusic.service.MusicService;
import com.charles.funmusic.utils.HandlerUtil;
import com.charles.funmusic.utils.MusicUtil;
import com.charles.funmusic.utils.Preferences;
import com.charles.funmusic.utils.ToastUtil;
import com.charles.funmusic.utils.loader.CoverLoader;
import com.charles.funmusic.widget.AlbumCoverView;
import com.charles.funmusic.widget.AlbumViewPager;
import com.charles.funmusic.widget.MarqueeTextView;
import com.charles.funmusic.widget.PlayerSeekBar;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class PlayingActivity extends BaseActivity {

    private static final int NEXT_MUSIC = 0;
    private static final int PREVIOUS_MUSIC = 1;
//    private static final int VIEWPAGER_SCROLL_TIME = 390;
//    private static final int TIME_DELAY = 500;

    private static final int LOOP = 0;
    private static final int SHUFFLE = 1;
    private static final int SINGLE = 2;

    @BindView(R.id.activity_playing_bg)
    ImageView mPlayingBg;
    @BindView(R.id.activity_playing_back)
    ImageView mBack;
    @BindView(R.id.activity_playing_music_title)
    MarqueeTextView mTitle;
    @BindView(R.id.activity_playing_artist)
    MarqueeTextView mArtist;
    @BindView(R.id.activity_playing_share)
    ImageView mShare;
    @BindView(R.id.activity_playing_view_pager)
    AlbumViewPager mViewPager;
    //    @BindView(R.id.activity_playing_needle)
//    ImageView mNeedle;
    @BindView(R.id.activity_playing_volume_seek)
    SeekBar mVolumeSeekBar;
    @BindView(R.id.activity_playing_target_lrc)
    TextView mTryGetLrc;
    @BindView(R.id.activity_playing_lyric_view)
    LrcView mLyricView;
    @BindView(R.id.activity_playing_lrc_view_container)
    RelativeLayout mLrcViewContainer;
    @BindView(R.id.play_fav)
    ImageView mFav;
    @BindView(R.id.play_download)
    ImageView mDownload;
    @BindView(R.id.play_more)
    ImageView mMore;
    @BindView(R.id.play_current_time)
    TextView mCurrentTime;
    @BindView(R.id.play_seek_bar)
    PlayerSeekBar mProgressSeekBar;
    @BindView(R.id.play_duration)
    TextView mDuration;
    @BindView(R.id.play_play_mode)
    ImageView mPlayMode;
    @BindView(R.id.play_prev)
    ImageView mPrev;
    @BindView(R.id.play_play_or_pause)
    ImageView mPlayOrPause;
    @BindView(R.id.play_next)
    ImageView mNext;
    @BindView(R.id.play_playlist)
    ImageView mPlaylist;
    @BindView(R.id.activity_playing_middle_container)
    FrameLayout mAlbumLayout;
    @BindView(R.id.activity_playing_play_controller)
    LinearLayout mPlayController;
    @BindView(R.id.activity_playing_container)
    LinearLayout mPlayingContainer;
    @BindView(R.id.activity_playing_head_container)
    LinearLayout mHeadContainer;
    @BindView(R.id.activity_playing_album_cover_view)
    AlbumCoverView mAlbumCoverView;

    private ObjectAnimator mRotateAnim;
    //    private ObjectAnimator mNeedleAnim;
//    private AnimatorSet mAnimatorSet;
//    private FragmentAdapter mAdapter;
//    private BitmapFactory.Options mNewOpts;
    private View mActiveView;
    private PlaylistManager mPlaylistManager;
    private WeakReference<View> mViewWeakReference = new WeakReference<>(null);
    private boolean isFav = false;
    /**
     * 判断ViewPager由手动滑动，还是setCurrentItem换页
     */
//    private boolean isNextOrPreSetPage = false;
    private Handler mPlayHandler;
    private long mLastAlbumId;
//    private boolean isPrint = true;
//    private String TAG = PlayingActivity.class.getSimpleName();

//    private Bitmap mBitmap;

    private LrcView.OnLrcClickListener mOnLrcClickListener = new LrcView.OnLrcClickListener() {
        @Override
        public void onClick() {
            if (mLrcViewContainer.getVisibility() == View.VISIBLE) {
                mLrcViewContainer.setVisibility(View.INVISIBLE);
                mAlbumLayout.setVisibility(View.VISIBLE);
                mPlayController.setVisibility(View.VISIBLE);
            }
        }
    };
    private LrcView.OnSeekToListener mOnSeekToListener = new LrcView.OnSeekToListener() {
        @Override
        public void onSeekTo(int progress) {
            MusicPlayer.seek(progress);
        }
    };
    private Runnable mUpAlbumRunnable = new Runnable() {
        @Override
        public void run() {
//            new setBlurRedAlbumArt().execute();
        }
    };
    private Runnable mUpdateProgress = new Runnable() {
        @Override
        public void run() {
            long position = MusicPlayer.position();
            long duration = MusicPlayer.duration();
            if (duration > 0 && duration < 627080716) {
                mProgressSeekBar.setProgress((int) (1000 * position / duration));
                mCurrentTime.setText(MusicUtil.makeTimeString(position));
            }

            if (MusicPlayer.isPlaying()) {
                mProgressSeekBar.postDelayed(mUpdateProgress, 200);
            } else {
                mProgressSeekBar.removeCallbacks(mUpdateProgress);
            }
        }
    };

    @Override
    protected void showQuickControl(boolean show) {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);
        mPlaylistManager = PlaylistManager.getInstance(this);

        initView();
        loadOther();
//        setViewPager();
        initLrcView();

        mHandler = HandlerUtil.getInstance(this);

        mHandler.postDelayed(mUpAlbumRunnable, 0);
        PlayMusic playThread = new PlayMusic();
        playThread.start();

        initSystemBar(mPlayingContainer);
    }

    @SuppressLint("WrongConstant")
    private void initView() {
        initPlayMode(); // 初始化播放模式

        mAlbumCoverView.initNeedle(MusicPlayer.isPlaying());

//        mNeedleAnim = ObjectAnimator.ofFloat(mNeedle, "rotation", -25, 0);
//        mNeedleAnim.setDuration(200);
//        mNeedleAnim.setRepeatMode(0);
//        mNeedleAnim.setInterpolator(new LinearInterpolator());

        mProgressSeekBar.setIndeterminate(false);
        mProgressSeekBar.setProgress(1);
        mProgressSeekBar.setMax(1000);

        setCoverAndBg(MusicPlayer.getCurrentTrack());
    }

    private void initPlayMode() {
        switch (Preferences.getPlayMode()) {
            case LOOP: // LOOP
                mPlayMode.setImageResource(R.drawable.selector_play_btn_loop);
                break;
            case SHUFFLE: // SHUFFLE
                mPlayMode.setImageResource(R.drawable.selector_play_btn_shuffle);
                break;
            case SINGLE: // SINGLE
                mPlayMode.setImageResource(R.drawable.selector_play_btn_loop_single);
                break;
        }
    }

    /**
     * 设置播放界面专辑封面和背景
     *
     * @param music 正在播放的歌曲
     */
    private void setCoverAndBg(final Music music) {
        mAlbumCoverView.setCoverBitmap(CoverLoader.getInstance().loadRound(music));
        mPlayingBg.setImageBitmap(CoverLoader.getInstance().loadBlur(music));
    }

    private void loadOther() {
        setSeekBarListener();
    }

//    private void setViewPager() {
//        mViewPager.setOffscreenPageLimit(2);
//        PlayBarPagerTransformer transformer = new PlayBarPagerTransformer();
//        mAdapter = new FragmentAdapter(getSupportFragmentManager());
//        mViewPager.setAdapter(mAdapter);
//        mViewPager.setPageTransformer(true, transformer);
//
//        // 改变ViewPager动画时间
//        try {
//            Field field = ViewPager.class.getDeclaredField("mScroller");
//            field.setAccessible(true);
//            MyScroller scroller = new MyScroller(mViewPager.getContext().getApplicationContext(),
//                    new LinearInterpolator());
//            field.set(mViewPager, scroller);
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//
//        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                if (position < 1) { // 首位之前，跳转到末尾（N）
//                    MusicPlayer.setQueuePosition(MusicPlayer.getQueue().length);
//                    mViewPager.setCurrentItem(MusicPlayer.getQueue().length, false);
//                    isNextOrPreSetPage = false;
//                    return;
//                } else if (position > MusicPlayer.getQueue().length) { // 末位之前，跳转到首位（1）
//                    MusicPlayer.setQueuePosition(0);
//                    // false：不显示跳转过程的动画
//                    mViewPager.setCurrentItem(1, false);
//                    isNextOrPreSetPage = false;
//                    return;
//                } else {
//                    if (!isNextOrPreSetPage) {
//                        if (position < MusicPlayer.getQueuePosition() + 1) {
//                            Message msg = new Message();
//                            msg.what = PREVIOUS_MUSIC;
//                            mPlayHandler.sendMessageDelayed(msg, TIME_DELAY);
//                        } else if (position > MusicPlayer.getQueuePosition() + 1) {
//                            Message msg = new Message();
//                            msg.what = NEXT_MUSIC;
//                            mPlayHandler.sendMessageDelayed(msg, TIME_DELAY);
//                        }
//                    }
//                }
//                isNextOrPreSetPage = false;
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//        });
//    }

    private void setSeekBarListener() {
        mProgressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = (int) (progress * MusicPlayer.duration() / 1000);
                mLyricView.seekTo(progress, true, fromUser);
                if (fromUser) {
                    MusicPlayer.seek(progress);
                    mCurrentTime.setText(MusicUtil.makeTimeString(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void initLrcView() {
        mLyricView.setOnSeekToListener(mOnSeekToListener);
        mLyricView.setOnLrcClickListener(mOnLrcClickListener);
        mViewPager.setOnSingleTouchListener(new AlbumViewPager.OnSingleTouchListener() {
            @Override
            public void onSingleTouch(View v) {
                if (mAlbumLayout.getVisibility() == View.VISIBLE) {
                    mAlbumLayout.setVisibility(View.INVISIBLE);
                    mLrcViewContainer.setVisibility(View.VISIBLE);
                    mPlayController.setVisibility(View.INVISIBLE);
                }
            }
        });

        final AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager != null) {
            int v = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mVolumeSeekBar.setMax(maxVol);
            mVolumeSeekBar.setProgress(v);
            mVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress,
                            AudioManager.ADJUST_SAME);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 设置ViewPager的默认值
        mViewPager.setCurrentItem(MusicPlayer.getQueuePosition() + 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLastAlbumId = -1;
        if (MusicPlayer.isTrackLocal()) {
            updateBuffer(100);
        } else {
            updateBuffer(MusicPlayer.secondPosition());
        }
        mHandler.postDelayed(mUpdateProgress, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayHandler.removeCallbacksAndMessages(null);
        mPlayHandler.getLooper().quit();
        mPlayHandler = null;

        mProgressSeekBar.removeCallbacks(mUpdateProgress);
        stopAnim();
    }

    @Override
    public void updateBuffer(int percent) {
        mProgressSeekBar.setSecondaryProgress(percent * 10);
    }

    @Override
    public void loading(boolean l) {
        mProgressSeekBar.setLoading(l);
    }

    @Override
    public void updateQueue() {
        if (MusicPlayer.getQueueSize() == 0) {
            MusicPlayer.stop();
            finish();
            return;
        }
//        mAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(MusicPlayer.getQueuePosition() + 1, false);
    }

    private void updateFav(boolean isFav) {
        if (isFav) {
            mFav.setImageResource(R.drawable.ic_play_loved);
        } else {
            mFav.setImageResource(R.drawable.ic_play_love);
        }
    }

    @Override
    public void updateLrc() {
        List<LrcRow> list = getLrcRows();
        if (list != null && list.size() > 0) {
            mTryGetLrc.setVisibility(View.INVISIBLE);
            mLyricView.setLrcRows(list);
        } else {
            mTryGetLrc.setVisibility(View.VISIBLE);
            mLyricView.reset();
        }
    }

    @Override
    public void updateTrack() {
        mHandler.removeCallbacks(mUpAlbumRunnable);
        if (MusicPlayer.getCurrentAlbumId() != mLastAlbumId) {
            mHandler.postDelayed(mUpAlbumRunnable, 1600);
        }

        isFav = false;
        long[] favLists = mPlaylistManager.getPlaylistIds(Keys.FAVORITE_PLAYLIST);
        long currentId = MusicPlayer.getCurrentAudioId();
        for (long i : favLists) {
            if (currentId == i) {
                isFav = true;
                break;
            }
        }
        updateFav(isFav);
        updateLrc();

        mTitle.setText(MusicPlayer.getTrackName());
        mArtist.setText(MusicPlayer.getArtist());
        mDuration.setText(MusicUtil.makeShortTimeString(
                PlayingActivity.this.getApplication(), MusicPlayer.duration() / 1000));

        changeFont(mTitle, false);
        changeFont(mArtist, false);
    }

    @SuppressLint("ObjectAnimatorBinding")
    @Override
    public void updateTrackInfo() {
        if (MusicPlayer.getQueueSize() == 0) {
            return;
        }

        if (mViewPager.getAdapter() != null) {
            Fragment fragment = (Fragment) mViewPager.getAdapter().instantiateItem(
                    mViewPager, mViewPager.getCurrentItem());
            View v = fragment.getView();
            if (mViewWeakReference.get() != v && v != null) {
                ((ViewGroup) v).setAnimationCacheEnabled(false);
                if (mViewWeakReference != null) {
                    mViewWeakReference.clear();
                }
                mViewWeakReference = new WeakReference<>(v);
                mActiveView = mViewWeakReference.get();
            }
        }

        if (mActiveView != null) {
            mRotateAnim = (ObjectAnimator) mActiveView.getTag(R.id.tag_animator);
        }

//        mAnimatorSet = new AnimatorSet();
        if (MusicPlayer.isPlaying()) {
            mProgressSeekBar.removeCallbacks(mUpdateProgress);
            mProgressSeekBar.postDelayed(mUpdateProgress, 200);
//            if (mAnimatorSet != null && mRotateAnim != null && !mRotateAnim.isRunning()) {
//                if (mNeedleAnim == null) {
//                    mNeedleAnim = ObjectAnimator.ofFloat(mNeedle, "rotation", -30, 0);
//                    mNeedleAnim.setDuration(200);
//                    mNeedleAnim.setInterpolator(new LinearInterpolator());
//                }
//                mAnimatorSet.play(mNeedleAnim).before(mRotateAnim);
//                mAnimatorSet.start();
//            }
        } else {
            mProgressSeekBar.removeCallbacks(mUpdateProgress);
//            if (mNeedleAnim != null) {
//                mNeedleAnim.reverse();
//                mNeedleAnim.end();
//            }

            if (mRotateAnim != null && mRotateAnim.isRunning()) {
                mRotateAnim.cancel();
                float valueAvatar = (float) mRotateAnim.getAnimatedValue();
                mRotateAnim.setFloatValues(valueAvatar, 360F + valueAvatar);
            }
        }

//        isNextOrPreSetPage = false;
//        if (MusicPlayer.getQueuePosition() + 1 != mViewPager.getCurrentItem()) {
//            mViewPager.setCurrentItem(MusicPlayer.getQueuePosition() + 1);
//            isNextOrPreSetPage = true;
//        }
    }

    private List<LrcRow> getLrcRows() {

        List<LrcRow> rows = null;
        InputStream is = null;
        try {
            is = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/FunMusic/lyric/" + MusicPlayer.getCurrentAudioId());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (is == null) {
                return null;
            }
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            rows = DefaultLrcParser.getIstance().getLrcRows(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }

    private void stopAnim() {
        mActiveView = null;

        if (mRotateAnim != null) {
            mRotateAnim.end();
            mRotateAnim = null;
        }
//        if (mNeedleAnim != null) {
//            mNeedleAnim.end();
//            mNeedleAnim = null;
//        }
//        if (mAnimatorSet != null) {
//            mAnimatorSet.end();
//            mAnimatorSet = null;
//        }
    }

    @OnClick({R.id.activity_playing_back, R.id.activity_playing_share, R.id.play_fav, R.id.play_download, R.id.play_more, R.id.play_play_mode, R.id.play_prev, R.id.play_play_or_pause, R.id.play_next, R.id.play_playlist, R.id.activity_playing_target_lrc})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.activity_playing_back:
                onBackPressed();
                break;

            case R.id.activity_playing_share:
                share();
                break;

            case R.id.play_fav:
                favorite();
                break;

            case R.id.play_download:
                break;

            case R.id.play_more:
                more();
                break;

            case R.id.play_play_mode:
                MusicPlayer.cycleRepeat();
                updatePlayMode();
                break;

            case R.id.play_prev:
                prev();
                break;

            case R.id.play_play_or_pause:
                playOrPause();
                break;

            case R.id.play_next:
                next();
                break;

            case R.id.play_playlist:
                showPlaylist();
                break;

            case R.id.activity_playing_target_lrc:
                getLrcAndCover();
                break;
        }
    }

    private void updatePlayMode() {

        if (MusicPlayer.getShuffleMode() == MusicService.SHUFFLE_NORMAL) {
            mPlayMode.setImageResource(R.drawable.selector_play_btn_shuffle);
            ToastUtil.show(getString(R.string.mode_shuffle));
            Preferences.savePlayMode(SHUFFLE);
        } else {
            switch (MusicPlayer.getRepeatMode()) {
                case MusicService.REPEAT_ALL:
                    mPlayMode.setImageResource(R.drawable.selector_play_btn_loop);
                    ToastUtil.show(getString(R.string.mode_loop));
                    Preferences.savePlayMode(LOOP);
                    break;

                case MusicService.REPEAT_CURRENT:
                    mPlayMode.setImageResource(R.drawable.selector_play_btn_loop_single);
                    ToastUtil.show(getString(R.string.mode_loop_single));
                    Preferences.savePlayMode(SINGLE);
                    break;
            }
        }
    }

    private void share() {
        Music music = MusicUtil.getMusics(PlayingActivity.this,
                MusicPlayer.getCurrentAudioId());
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        if (music != null) {
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + music.getUrl()));
        }
        shareIntent.setType("audio/*");
        this.startActivity(Intent.createChooser(shareIntent, getString(R.string.share_to)));
    }

    private void more() {
        SimpleMoreFragment moreFragment = SimpleMoreFragment.newInstance(
                MusicPlayer.getCurrentAudioId());
        moreFragment.show(getSupportFragmentManager(), "music");
    }

    private void favorite() {
        if (isFav) {
            mPlaylistManager.removeItem(PlayingActivity.this, Keys.FAVORITE_PLAYLIST,
                    MusicPlayer.getCurrentAudioId());
            mFav.setImageResource(R.drawable.ic_play_love);
            isFav = false;
        } else {
            try {
                Music music = MusicPlayer.getPlayInfos().get(MusicPlayer.getCurrentAudioId());
                mPlaylistManager.insertMusic(
                        PlayingActivity.this, Keys.FAVORITE_PLAYLIST, music);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mFav.setImageResource(R.drawable.ic_play_loved);
            isFav = true;
        }

        Intent intent = new Intent(Actions.ACTION_PLAYLIST_COUNT_CHANGED);
        sendBroadcast(intent);
    }

    private void prev() {
        Message msg = new Message();
        msg.what = PREVIOUS_MUSIC;
        mPlayHandler.sendMessage(msg);
    }

    private void playOrPause() {
        if (MusicPlayer.isPlaying()) {
            mPlayOrPause.setImageResource(R.drawable.selector_play_btn_pause);
        } else {
            mPlayOrPause.setImageResource(R.drawable.selector_play_btn_play);
        }
        if (MusicPlayer.getQueueSize() != 0) {
            MusicPlayer.playOrPause();
        }
    }

    private void next() {
//        if (mRotateAnim != null) {
//            mRotateAnim.end();
//            mRotateAnim = null;
//        }

        Message msg = new Message();
        msg.what = NEXT_MUSIC;
        mPlayHandler.sendMessage(msg);
    }

    private void showPlaylist() {
        PlayQueueFragment playQueueFragment = new PlayQueueFragment();
        playQueueFragment.show(getSupportFragmentManager(), "playlist_fragment");
    }

    private void getLrcAndCover() {
        Intent intent = new Intent();
        intent.setAction(MusicService.TRY_GET_TRACK_INFO);
        sendBroadcast(intent);
        ToastUtil.show("正在获取信息");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopAnim();
        mProgressSeekBar.removeCallbacks(mUpdateProgress);
    }

//    private class PlayBarPagerTransformer implements ViewPager.PageTransformer {
//
//        @Override
//        public void transformPage(@NonNull View page, float position) {
//            if (position == 0) {
//                if (MusicPlayer.isPlaying()) {
//                    mRotateAnim = (ObjectAnimator) page.getTag(R.id.tag_animator);
//                    if (mRotateAnim != null && !mRotateAnim.isRunning() && mNeedleAnim != null) {
//                        mAnimatorSet = new AnimatorSet();
//                        mAnimatorSet.play(mNeedleAnim).before(mRotateAnim);
//                        mAnimatorSet.start();
//                    }
//                }
//            } else if (position == -1 || position == -2 || position == 1) {
//                mRotateAnim = (ObjectAnimator) page.getTag(R.id.tag_animator);
//                if (mRotateAnim != null) {
//                    mRotateAnim.setFloatValues(0);
//                    mRotateAnim.end();
//                    mRotateAnim = null;
//                }
//            } else {
//                if (mNeedleAnim != null) {
//                    mNeedleAnim.reverse();
//                    mNeedleAnim.end();
//                }
//
//                mRotateAnim = (ObjectAnimator) page.getTag(R.id.tag_animator);
//                if (mRotateAnim != null) {
//                    mRotateAnim.cancel();
//                    float valueAvatar = (float) mRotateAnim.getAnimatedValue();
//                    mRotateAnim.setFloatValues(valueAvatar, 360f + valueAvatar);
//                }
//            }
//        }
//    }
//
//    @SuppressLint("StaticFieldLeak")
//    private class setBlurRedAlbumArt extends AsyncTask<Void, Void, Drawable> {
//
//        long mAlbumId = MusicPlayer.getCurrentAlbumId();
//
//        @Override
//        protected Drawable doInBackground(Void... loadedImage) {
//            mLastAlbumId = mAlbumId;
//            Drawable drawable = null;
//            mBitmap = null;
//            if (mNewOpts == null) {
//                mNewOpts = new BitmapFactory.Options();
//                mNewOpts.inSampleSize = 6;
//                mNewOpts.inPreferredConfig = Bitmap.Config.RGB_565;
//            }
//            if (!MusicPlayer.isTrackLocal()) {
//                if (isPrint) {
//                    Log.d(TAG, "music is net");
//                }
//
//                if (getAlbumPath() == null) {
//                    if (isPrint) {
//                        Log.d(TAG, "getAlbumPath() is null");
//                    }
//                    mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.play_page_default_bg);
//                    drawable = ImageUtil.createBlurredImageFromBitmap(mBitmap, PlayingActivity.this.getApplication(), 3);
//                    return drawable;
//                }
//
//                ImageRequest imageRequest = ImageRequestBuilder
//                        .newBuilderWithSource(Uri.parse(getAlbumPath()))
//                        .setProgressiveRenderingEnabled(true)
//                        .build();
//
//                ImagePipeline imagePipeline = Fresco.getImagePipeline();
//                DataSource<CloseableReference<CloseableImage>> dataSource =
//                        imagePipeline.fetchDecodedImage(imageRequest, PlayingActivity.this);
//
//                dataSource.subscribe(new BaseBitmapDataSubscriber() {
//                    @Override
//                    protected void onNewResultImpl(@Nullable Bitmap bitmap) {
//                        // You can use the bitmap in only limited ways
//                        // No need to do any cleanup.
//                        if (bitmap != null) {
//                            mBitmap = bitmap;
//                            if (isPrint) {
//                                Log.d(TAG, "getAlbumPath() bitmap success");
//                            }
//                        }
//                    }
//
//                    @Override
//                    protected void onFailureImpl(DataSource dataSource) {
//                        // No cleanup required here.
//                        if (isPrint) {
//                            Log.d(TAG, "getAlbumPath() bitmap failed");
//                        }
//                        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_default_album_cover);
//                    }
//                }, CallerThreadExecutor.getInstance());
//
//                if (mBitmap != null) {
//                    drawable = ImageUtil.createBlurredImageFromBitmap(mBitmap, PlayingActivity.this.getApplication(), 3);
//                }
//
//            } else {
//                try {
//                    mBitmap = null;
//                    Bitmap bitmap;
//                    Uri art = Uri.parse(getAlbumPath());
//                    if (isPrint) {
//                        Log.d(TAG, "album is local");
//                    }
//                    if (art != null) {
//                        ParcelFileDescriptor fd = null;
//                        try {
//                            fd = getContentResolver().openFileDescriptor(art, "r");
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        }
//                        if (fd != null) {
//                            bitmap = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, mNewOpts);
//                        } else {
//                            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_default_album_cover, mNewOpts);
//                        }
//                    } else {
//                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_default_album_cover, mNewOpts);
//                    }
//                    if (bitmap != null) {
//                        drawable = ImageUtil.createBlurredImageFromBitmap(bitmap, PlayingActivity.this.getApplication(), 3);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            return drawable;
//        }
//
//        @Override
//        protected void onPostExecute(Drawable result) {
//            if (mAlbumId != MusicPlayer.getCurrentAlbumId()) {
//                this.cancel(true);
//                return;
//            }
//            setDrawable(result);
//        }
//    }
//
//    private void setDrawable(Drawable result) {
//        if (result != null) {
//            if (mPlayingBg.getDrawable() != null) {
//                final TransitionDrawable td = new TransitionDrawable(
//                        new Drawable[]{mPlayingBg.getDrawable(), result});
//
//                mPlayingBg.setImageDrawable(td);
//                // 去除过度绘制
//                td.setCrossFadeEnabled(true);
//                td.startTransition(200);
//            } else {
//                mPlayingBg.setImageDrawable(result);
//            }
//        }
//    }
//
//    private class FragmentAdapter extends FragmentStatePagerAdapter {
//
//        private int mChildCount = 0;
//
//        FragmentAdapter(FragmentManager fm) {
//            super(fm);
//        }
//
//        @Override
//        public Fragment getItem(int position) {
//            if (position == MusicPlayer.getQueue().length + 1 || position == 0) {
//                return RoundFragment.newInstance("");
//            }
//            return RoundFragment.newInstance(MusicPlayer.getAlbumPathAll()[position - 1]);
//        }
//
//        @Override
//        public int getCount() {
//            // 左右各加一个
//            return MusicPlayer.getQueue().length + 2;
//        }
//
//        @Override
//        public void notifyDataSetChanged() {
//            mChildCount = getCount();
//            super.notifyDataSetChanged();
//        }
//
//        @Override
//        public int getItemPosition(@NonNull Object object) {
//            if (mChildCount > 0) {
//                mChildCount--;
//                return POSITION_NONE;
//            }
//            return super.getItemPosition(object);
//        }
//    }

//    public class MyScroller extends Scroller {
//
//        private int mAnimTime = VIEWPAGER_SCROLL_TIME;
//
//        MyScroller(Context context, Interpolator interpolator) {
//            super(context, interpolator);
//        }
//
//        @Override
//        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
//            super.startScroll(startX, startY, dx, dy, mAnimTime);
//        }
//
//        @Override
//        public void startScroll(int startX, int startY, int dx, int dy) {
//            super.startScroll(startX, startY, dx, dy, mAnimTime);
//        }
//    }

    private class PlayMusic extends Thread {

        @SuppressLint("HandlerLeak")
        @Override
        public void run() {
            if (Looper.myLooper() == null) {
                Looper.prepare();
            }

            mPlayHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case PREVIOUS_MUSIC:
                            MusicPlayer.previous(PlayingActivity.this, true);
                            break;

                        case NEXT_MUSIC:
                            MusicPlayer.next();
                            break;

                        case 3:
                            MusicPlayer.setQueuePosition(msg.arg1);
                            break;
                    }
                }
            };
            Looper.loop();
        }
    }
}