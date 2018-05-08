package com.charles.funmusic.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.LayoutRes;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.charles.funmusic.IFunMusicService;
import com.charles.funmusic.R;
import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.constant.Actions;
import com.charles.funmusic.fragment.QuickControlsFragment;
import com.charles.funmusic.service.MusicPlayer;
import com.charles.funmusic.service.MusicService;
import com.charles.funmusic.utils.FontUtil;
import com.charles.funmusic.utils.ScreenUtil;
import com.charles.funmusic.utils.ToastUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import butterknife.ButterKnife;

import static com.charles.funmusic.service.MusicPlayer.sService;

/**
 * 基类
 * 如果继承本类，需要在 layout 中添加{@link Toolbar}，并将 AppTheme 继承 Theme.AppCompat.NoActionBar。
 */
public abstract class BaseActivity extends AppCompatActivity implements ServiceConnection {

    private MusicPlayer.ServiceToken mToken;
    /**
     * receiver 接受播放状态变化等
     */
    private PlaybackStatus mPlaybackStatus;
    /**
     * 底部播放控制栏
     */
    private QuickControlsFragment mFragment;
    private ArrayList<MusicStateListener> mStateListener = new ArrayList<>();
    protected Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * 沉浸式状态栏
     */
    public void initSystemBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int top = ScreenUtil.getStatusBarHeight();
            view.setPadding(0, top, 0, 0);
        }
    }

    /**
     * 更新播放队列
     */
    public void updateQueue() {
    }

    /**
     * 更新歌曲状态信息
     */
    public void updateTrackInfo() {
        for (final MusicStateListener listener : mStateListener) {
            if (listener != null) {
                listener.reloadAdapter();
                listener.updateTrackInfo();
            }
        }
    }

    /**
     * fragment界面刷新
     */
    public void refreshUI() {
        for (final MusicStateListener listener : mStateListener) {
            if (listener != null) {
                listener.reloadAdapter();
            }
        }
    }

    public void updateTime() {
        for (final MusicStateListener listener : mStateListener) {
            if (listener != null) {
                listener.updateTime();
            }
        }
    }

    /**
     * 歌曲切换
     */
    public void updateTrack() {
    }

    public void updateLrc() {
    }

    /**
     * 更新歌曲缓冲进度值
     *
     * @param percent 从0~100
     */
    public void updateBuffer(int percent) {
    }

    public void changeTheme() {
        for (final MusicStateListener listener : mStateListener) {
            if (listener != null) {
                listener.changeTheme();
            }
        }
    }

    /**
     * 歌曲是否加载中
     *
     * @param l 是否加载中
     */
    public void loading(boolean l) {
    }

    /**
     * 取消保存状态
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * 取消保存状态
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * 显示或关闭底部播放控制栏
     *
     * @param isShow true 显示
     *               false 关闭
     */
    protected void showQuickControl(boolean isShow) {
        Log.d("BaseActivity", MusicPlayer.getQueue().length + "");
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (isShow) {
            if (mFragment == null) {
                mFragment = QuickControlsFragment.newInstance();
                ft.add(R.id.play_bar, mFragment).commitAllowingStateLoss();
            } else {
                ft.show(mFragment).commitAllowingStateLoss();
            }
        } else {
            if (mFragment != null) {
                ft.hide(mFragment).commitAllowingStateLoss();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToken = MusicPlayer.bindToService(this, this);
        mPlaybackStatus = new PlaybackStatus(this);

        setSystemBarTransparent();
        getWindow().setBackgroundDrawable(null);

        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicService.PLAY_STATE_CHANGED);
        filter.addAction(MusicService.META_CHANGED);
        filter.addAction(MusicService.QUEUE_CHANGED);
        filter.addAction(Actions.ACTION_MUSIC_COUNT_CHANGED);
        filter.addAction(MusicService.TRACK_PREPARED);
        filter.addAction(MusicService.BUFFER_UP);
        filter.addAction(Actions.ACTION_EMPTY_LIST);
        filter.addAction(MusicService.MUSIC_CHANGED);
        filter.addAction(MusicService.LRC_UPDATED);
        filter.addAction(Actions.ACTION_PLAYLIST_COUNT_CHANGED);
        filter.addAction(MusicService.MUSIC_LOADING);
        registerReceiver(mPlaybackStatus, new IntentFilter(filter));
        showQuickControl(true);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        initView();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        initView();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        initView();
    }

    private void initView() {
        ButterKnife.bind(this);
    }

    @Override
    protected void onPause() {
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        super.onPause();
    }

    @Override
    public void onServiceConnected(final ComponentName name, final IBinder service) {
        sService = IFunMusicService.Stub.asInterface(service);
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
        sService = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        unbindService();
        try {
            unregisterReceiver(mPlaybackStatus);
        } catch (final Throwable ignored) {
        }
        mStateListener.clear();
    }

    private void unbindService() {
        if (mToken != null) {
            MusicPlayer.unbindFromService(mToken);
            mToken = null;
        }
    }

    public void setMusicStateListener(final MusicStateListener status) {
        if (status == this) {
            throw new UnsupportedOperationException("Override the method, don't add a listener");
        }

        if (status != null) {
            mStateListener.add(status);
        }
    }

    public void removeMusicStateListener(final MusicStateListener status) {
        if (status != null) {
            mStateListener.remove(status);
        }
    }

    public MusicService getMusicService() {
        MusicService musicService = AppCache.getMusicService();
        if (musicService == null) {
            throw new NullPointerException("music service is null");
        }
        return musicService;
    }

    private void setSystemBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // LOLLIPOP解决方案
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // KITKAT解决方案
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 改变字体
     *
     * @param textView TextView
     * @param isBold   是否加粗
     */
    protected void changeFont(TextView textView, boolean isBold) {
        FontUtil fontUtil = new FontUtil();
        fontUtil.changeFont(AppCache.getContext(), textView);
        if (isBold) {
            textView.getPaint().setFakeBoldText(true); // 设置为粗体
        } else {
            textView.getPaint().setFakeBoldText(false); // 不设置为粗体
        }
    }

    /**
     * 跳转activity
     *
     * @param context 上下文对象
     * @param cls     对象Activity
     */
    protected void startActivity(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        context.startActivity(intent);
        this.overridePendingTransition(0, 0);
    }

    /**
     * 显示软键盘
     */
    public void showSoftInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) AppCache
                .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 隐藏软键盘
     */
    public void hideSoftInput() {
        InputMethodManager manager = (InputMethodManager) AppCache
                .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(new View(this).getWindowToken(), 0);
        }
    }

    private final static class PlaybackStatus extends BroadcastReceiver {

        private final WeakReference<BaseActivity> mReference;

        private PlaybackStatus(final BaseActivity activity) {
            mReference = new WeakReference<>(activity);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BaseActivity baseActivity = mReference.get();
            if (baseActivity != null) {
                if (action != null) {
                    switch (action) {
                        case MusicService.META_CHANGED:
                            baseActivity.updateTrackInfo();
                            break;

                        case MusicService.PLAY_STATE_CHANGED:
                            break;

                        case MusicService.TRACK_PREPARED:
                            baseActivity.updateTime();
                            break;

                        case MusicService.BUFFER_UP:
                            baseActivity.updateBuffer(intent.getIntExtra("progress", 0));
                            break;

                        case MusicService.MUSIC_LOADING:
                            baseActivity.loading(intent.getBooleanExtra("is_loading", false));
                            break;

                        case MusicService.REFRESH:
                            break;

                        case Actions.ACTION_MUSIC_COUNT_CHANGED:
                            baseActivity.refreshUI();
                            break;

                        case MusicService.QUEUE_CHANGED:
                            baseActivity.updateQueue();
                            break;

                        case Actions.ACTION_PLAYLIST_COUNT_CHANGED:
                            baseActivity.refreshUI();
                            break;

                        case MusicService.TRACK_ERROR:
                            @SuppressLint({"StringFormatInvalid", "LocalSuppress"}) final String errorMsg = context.getString(R.string.exit, intent.getStringExtra(MusicService.TrackErrorExtra.TRACK_NAME));
                            ToastUtil.show(errorMsg);
                            break;

                        case MusicService.MUSIC_CHANGED:
                            baseActivity.updateTrack();
                            break;

                        case MusicService.LRC_UPDATED:
                            baseActivity.updateLrc();
                            break;
                    }
                }
            }
        }
    }
}
