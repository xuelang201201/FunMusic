package com.charles.funmusic.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.charles.funmusic.activity.BaseActivity;
import com.charles.funmusic.activity.MusicStateListener;
import com.charles.funmusic.activity.SplashActivity;
import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.service.MusicService;
import com.charles.funmusic.utils.FontUtil;
import com.charles.funmusic.utils.HandlerUtil;
import com.charles.funmusic.utils.ScreenUtil;

import butterknife.ButterKnife;

/**
 * 基类
 */
public abstract class BaseFragment extends Fragment implements View.OnTouchListener, MusicStateListener {
    private static final String STATE_SAVE_IS_HIDDEN = "STATE_SAVE_IS_HIDDEN";
    protected Handler mHandler;

    public abstract int getLayoutId();

    public abstract void init(Bundle savedInstanceState);

    public View mView;

    public Context mContext;

    /**
     * 重写fragment的onAttach()方法，fragment第一次附属于activity时调用，
     * 在onCreate之前调用
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            boolean isSupportHidden = savedInstanceState.getBoolean(STATE_SAVE_IS_HIDDEN);
            if (getFragmentManager() != null) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                if (isSupportHidden) {
                    ft.hide(this);
                } else {
                    ft.show(this);
                }
                ft.commitAllowingStateLoss();
            }
        }
        mHandler = HandlerUtil.getInstance(mContext);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            ((BaseActivity) getActivity()).setMusicStateListener(this);
        }
        reloadAdapter();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getActivity() != null) {
            ((BaseActivity) getActivity()).removeMusicStateListener(this);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SAVE_IS_HIDDEN, isHidden());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(getLayoutId(), container, false);

            ButterKnife.bind(this, mView);
            init(savedInstanceState);
        }

        mView.setOnTouchListener(this); // 防止fragment被击穿
        return mView;
    }

    protected MusicService getMusicService() {
        MusicService musicService = AppCache.getMusicService();
        if (musicService == null) {
            throw new NullPointerException("music service is null");
        }
        return musicService;
    }

    protected boolean checkServiceAlive() {
        if (AppCache.getMusicService() == null) {
            startActivity(new Intent(getActivity(), SplashActivity.class));
            AppCache.clearStack();
            return false;
        }
        return true;
    }

    /**
     * 改变字体
     * @param textView TextView
     * @param isBold 是否加粗
     */
    protected void changeFont(TextView textView, boolean isBold) {
        FontUtil fontUtil = new FontUtil();
        fontUtil.changeFont(getActivity(), textView);
        if (isBold) {
            textView.getPaint().setFakeBoldText(true); // 设置为粗体
        } else {
            textView.getPaint().setFakeBoldText(false); // 不设置为粗体
        }
    }

    /**
     * 防止fragment被击穿
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mView != null) {
            ((ViewGroup) mView.getParent()).removeView(mView);
        }
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

    public void showSortDialog() {
        if (getFragmentManager() != null) {
            SortDialogFragment sortDialog = new SortDialogFragment();
            sortDialog.show(getFragmentManager(), "sort");
        }
    }

    /**
     * 沉浸式状态栏
     */
    public void initSystemBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int top = ScreenUtil.getStatusBarHeight();
            view.setPadding(0, top, 0, 0);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void updateTrackInfo() {
    }

    @Override
    public void updateTime() {
    }

    @Override
    public void changeTheme() {
    }

    @Override
    public void reloadAdapter() {
    }
}