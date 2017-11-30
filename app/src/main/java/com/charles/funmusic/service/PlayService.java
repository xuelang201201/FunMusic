package com.charles.funmusic.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.application.Notifier;
import com.charles.funmusic.constant.Actions;
import com.charles.funmusic.enums.PlayModeEnum;
import com.charles.funmusic.model.Music;
import com.charles.funmusic.receiver.NoisyAudioStreamReceiver;
import com.charles.funmusic.utils.MusicUtil;
import com.charles.funmusic.utils.Preferences;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * 播放歌曲的Service
 */
public class PlayService extends Service implements MediaPlayer.OnCompletionListener {
    private static final String TAG = "Service";
    private static final long TIME_UPDATE = 300L;

    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PLAYING = 2;
    private static final int STATE_PAUSE = 3;

    private final NoisyAudioStreamReceiver mNoisyReceiver = new NoisyAudioStreamReceiver();
    private final IntentFilter mNoisyFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private final Handler mHandler = new Handler();
    private MediaPlayer mPlayer = new MediaPlayer();
    private AudioFocusManager mAudioFocusManager;
    private MediaSessionManager mMediaSessionManager;
    private OnPlayerEventListener mListener;
    /**
     * 正在播放的歌曲[本地|网络]
     */
    private Music mPlayingMusic;
    /**
     * 正在播放的本地歌曲的序号
     */
    private int mPlayingPosition = -1;
    private int mPosition;

    private int mPlayState = STATE_IDLE;

    public OnPlayerEventListener getOnPlayEventListener() {
        return mListener;
    }

    public void setOnPlayEventListener(OnPlayerEventListener listener) {
        mListener = listener;
    }

    public boolean isPlaying() {
        return mPlayState == STATE_PLAYING;
    }

    public boolean isPausing() {
        return mPlayState == STATE_PAUSE;
    }

    public boolean isPreparing() {
        return mPlayState == STATE_PREPARING;
    }

    public boolean isIdle() {
        return mPlayState == STATE_IDLE;
    }

    /**
     * 获取正在播放的本地歌曲的序号
     */
    public int getPlayingPosition() {
        return mPlayingPosition;
    }

    /**
     * 获取正在播放的歌曲[本地|网络]
     */
    public Music getPlayingMusic() {
        return mPlayingMusic;
    }

    /**
     * 删除或下载歌曲后刷新正在播放的本地歌曲的序号
     */
    public void updatePlayingPosition() {
        int position = 0;
        long id = Preferences.getCurrentSongId();
        for (int i = 0; i < AppCache.getMusics().size(); i++) {
            if (AppCache.getMusics().get(i).getId() == id) {
                position = i;
                break;
            }
        }
        mPlayingPosition = position;
        Preferences.saveCurrentSongId(AppCache.getMusics().get(mPlayingPosition).getId());
    }

    public int getAudioSessionId() {
        return mPlayer.getAudioSessionId();
    }

    public long getCurrentPosition() {
        if (isPlaying() || isPausing()) {
            return mPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: " + getClass().getSimpleName());
        mAudioFocusManager = new AudioFocusManager(this);
        mMediaSessionManager = new MediaSessionManager(this);
        mPlayer.setOnCompletionListener(this);
        Notifier.init(this);
        QuitTimer.getInstance().init(this, mHandler, new EventCallback<Long>() {
            @Override
            public void onEvent(Long aLong) {
                if (mListener != null) {
                    mListener.onTimer(aLong);
                }
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new PlayBinder();
    }

    /**
     * 启动Service
     *
     * @param context 上下文
     * @param action  要求Service执行的参数
     */
    public static void startCommand(Context context, String action) {
        Intent intent = new Intent(context, PlayService.class);
        intent.setAction(action);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case Actions.ACTION_MEDIA_PLAY_PAUSE:
                    playPause(); // 播放/暂停
                    break;
                case Actions.ACTION_MEDIA_NEXT:
                    next(); // 下一首
                    break;
                case Actions.ACTION_MEDIA_PREVIOUS:
                    prev(); // 上一首
                    break;
                case Actions.ACTION_MEDIA_EXIT:
                    exit(); // 退出
                    break;
            }
        }
        return START_NOT_STICKY;
    }

    private void exit() {
        AppCache.clearStack();
        quit();
    }

    /**
     * 更新本地歌曲列表
     *
     * @param callback 事件回调
     */
    public void updateMusicList(final EventCallback<Void> callback) {
        new AsyncTask<Void, Void, List<Music>>() {

            @Override
            protected List<Music> doInBackground(Void... params) {
                return MusicUtil.scanMusic(PlayService.this);
            }

            @Override
            protected void onPostExecute(List<Music> musics) {
                AppCache.getMusics().clear();
                AppCache.getMusics().addAll(musics);

                if (!AppCache.getMusics().isEmpty()) {
                    updatePlayingPosition();
                    mPlayingMusic = AppCache.getMusics().get(mPlayingPosition);
                }

                if (mListener != null) {
                    mListener.onMusicListUpdate();
                }

                if (callback != null) {
                    callback.onEvent(null);
                }
            }
        }.execute();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        next();
    }

    /**
     * 播放指定位置的歌曲
     *
     * @param position 位置
     */
    public void play(int position) {
        mPosition = position;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (AppCache.getMusics().isEmpty()) {
                    return;
                }

                if (mPosition < 0) {
                    mPosition = AppCache.getMusics().size() - 1;
                } else if (mPosition >= AppCache.getMusics().size()) {
                    mPosition = 0;
                }

                mPlayingPosition = mPosition;
                Music music = AppCache.getMusics().get(mPlayingPosition);
                Preferences.saveCurrentSongId(music.getId());
                play(music);
            }
        }).start();
    }

    /**
     * 播放指定音频文件
     *
     * @param music 音频文件
     */
    public void play(final Music music) {
        mPlayingMusic = music;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mPlayer.reset();
                    mPlayer.setDataSource(music.getUrl());
                    mPlayer.prepareAsync();
                    mPlayState = STATE_PREPARING;
                    mPlayer.setOnPreparedListener(mPreparedListener);
                    mPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
                    if (mListener != null) {
                        mListener.onChange(music);
                    }
                    Notifier.showPlay(music);
                    mMediaSessionManager.updateMetaData(mPlayingMusic);
                    mMediaSessionManager.updatePlaybackState();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {

        @Override
        public void onPrepared(MediaPlayer mp) {
            if (isPreparing()) {
                start();
            }
        }
    };

    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            if (mListener != null) {
                mListener.onBufferingUpdate(percent);
            }
        }
    };

    /**
     * 播放/暂停
     */
    public void playPause() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isPreparing()) {
                    stop();
                } else if (isPlaying()) {
                    pause();
                } else if (isPausing()) {
                    start();
                } else {
                    play(getPlayingPosition());
                }
            }
        });
    }

    void start() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!isPreparing() && !isPausing()) {
                    return;
                }

                if (mAudioFocusManager.requestAudioFocus()) {
                    mPlayer.start();
                    mPlayState = STATE_PLAYING;
                    mHandler.post(mPublishRunnable);
                    Notifier.showPlay(mPlayingMusic);
                    registerReceiver(mNoisyReceiver, mNoisyFilter);

                    if (mListener != null) {
                        mListener.onPlayerStart();
                    }
                }
            }
        });
    }

    void pause() {
        if (!isPlaying()) {
            return;
        }

        mPlayer.pause();
        mPlayState = STATE_PAUSE;
        mHandler.removeCallbacks(mPublishRunnable);
        Notifier.showPause(mPlayingMusic);
        mMediaSessionManager.updatePlaybackState();
        unregisterReceiver(mNoisyReceiver);

        if (mListener != null) {
            mListener.onPlayerPause();
        }
    }

    void stop() {
        if (isIdle()) {
            return;
        }

        pause();
        mPlayer.reset();
        mPlayState = STATE_IDLE;
    }

    public void next() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (AppCache.getMusics().isEmpty()) {
                    return;
                }

                PlayModeEnum mode = PlayModeEnum.valueOf(Preferences.getPlayMode());
                switch (mode) {
                    case SHUFFLE:
                        long seed = System.nanoTime();
                        mPlayingPosition = new Random(seed).nextInt(AppCache.getMusics().size());
                        play(mPlayingPosition);
//                        Collections.shuffle(AppCache.getMusics(), new Random(seed));
                        break;

                    case SINGLE:
                        play(mPlayingPosition);
                        break;

                    case LOOP:
                    default:
                        play(mPlayingPosition + 1);
                        break;
                }
            }
        });
    }

    public void prev() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (AppCache.getMusics().isEmpty()) {
                    return;
                }

                PlayModeEnum mode = PlayModeEnum.valueOf(Preferences.getPlayMode());
                switch (mode) {
                    case SHUFFLE:
                        long seed = System.nanoTime();
                        mPlayingPosition = new Random(seed).nextInt(AppCache.getMusics().size());
//                        mPlayingPosition = new Random().nextInt(AppCache.getMusics().size());
                        play(mPlayingPosition);
                        break;
                    case SINGLE:
                        play(mPlayingPosition);
                        break;
                    case LOOP:
                    default:
                        play(mPlayingPosition - 1);
                        break;
                }
            }
        });
    }

    /**
     * 跳转到指定的时间位置
     *
     * @param msec 时间
     */
    public void seekTo(int msec) {
        if (isPlaying() || isPausing()) {
            mPlayer.seekTo(msec);
            mMediaSessionManager.updatePlaybackState();
            if (mListener != null) {
                mListener.onPublish(msec);
            }
        }
    }

    private Runnable mPublishRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying() && mListener != null) {
                mListener.onPublish(mPlayer.getCurrentPosition());
            }
            mHandler.postDelayed(this, TIME_UPDATE);
        }
    };

    public void quit() {
        stop();
        QuitTimer.getInstance().stop();
        stopSelf();
    }

    public class PlayBinder extends Binder {
        public PlayService getService() {
            return PlayService.this;
        }
    }

    @Override
    public void onDestroy() {
        mPlayer.reset();
        mPlayer.release();
        mPlayer = null;
        mAudioFocusManager.abandonAudioFocus();
        mMediaSessionManager.release();
        Notifier.cancelAll();
        AppCache.setPlayService(null);
        super.onDestroy();
        Log.i(TAG, "onDestroy: " + getClass().getSimpleName());
    }
}
