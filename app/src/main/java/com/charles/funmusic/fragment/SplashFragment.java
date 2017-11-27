package com.charles.funmusic.fragment;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.charles.funmusic.R;
import com.charles.funmusic.activity.MusicActivity;
import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.constant.Splash;
import com.charles.funmusic.http.HttpCallback;
import com.charles.funmusic.http.HttpClient;
import com.charles.funmusic.service.EventCallback;
import com.charles.funmusic.service.PlayService;
import com.charles.funmusic.utils.FileUtil;
import com.charles.funmusic.utils.PermissionReq;
import com.charles.funmusic.utils.Preferences;
import com.charles.funmusic.utils.ToastUtil;

import java.io.File;
import java.util.Calendar;

import butterknife.BindView;

public class SplashFragment extends BaseFragment {
    private static final String SPLASH_FILE_NAME = "splash";

    @BindView(R.id.fragment_splash_image_view)
    ImageView mImageView;
    @BindView(R.id.fragment_splash_copyright)
    TextView mCopyright;
    private ServiceConnection mPlayServiceConnection;

    public static SplashFragment newInstance() {

        Bundle args = new Bundle();

        SplashFragment fragment = new SplashFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_splash;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        int year = Calendar.getInstance().get(Calendar.YEAR);
        mCopyright.setText(getString(R.string.copyright, year));
        changeFont(mCopyright, false);

        checkService();
    }

    private void checkService() {
        if (AppCache.getPlayService() == null) {
            startService();
            showSplash();
            updateSplash();

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bindService();
                }
            }, 1000);
        } else {
            startMusicActivity();
            getActivity().finish();
        }
    }

    private void startService() {
        Intent intent = new Intent(getActivity(), PlayService.class);
        AppCache.getContext().startService(intent);
    }

    private void bindService() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), PlayService.class);
        mPlayServiceConnection = new PlayServiceConnection();
        getActivity().bindService(intent, mPlayServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private class PlayServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            final PlayService playService = ((PlayService.PlayBinder) service).getService();
            AppCache.setPlayService(playService);
            PermissionReq.with(SplashFragment.this)
                    .permissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .result(new PermissionReq.Result() {
                        @Override
                        public void onGranted() {
                            scanMusic(playService);
                        }

                        @Override
                        public void onDenied() {
                            ToastUtil.show(R.string.no_permission_storage);
                            getActivity().finish();
                            playService.quit();
                        }
                    }).request();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }

    private void scanMusic(PlayService playService) {
        playService.updateMusicList(new EventCallback<Void>() {
            @Override
            public void onEvent(Void aVoid) {
                startMusicActivity();
                getActivity().finish();
            }
        });
    }

    private void showSplash() {
        File splashImg = new File(FileUtil.getSplashDir(getActivity()), SPLASH_FILE_NAME);
        if (splashImg.exists()) {
            Glide.with(AppCache.getContext()).load(splashImg.getPath()).into(mImageView);
        }
    }

    private void updateSplash() {
        HttpClient.getSplash(new HttpCallback<Splash>() {
            @Override
            public void onSuccess(Splash response) {
                if (response == null || TextUtils.isEmpty(response.getUrl())) {
                    return;
                }

                final String url = response.getUrl();
                String lastImgUrl = Preferences.getSplashUrl();
                if (TextUtils.equals(lastImgUrl, url)) {
                    return;
                }

                HttpClient.downloadFile(url, FileUtil.getSplashDir(AppCache.getContext()), SPLASH_FILE_NAME,
                        new HttpCallback<File>() {
                            @Override
                            public void onSuccess(File file) {
                                Preferences.saveSplashUrl(url);
                            }

                            @Override
                            public void onFail(Exception e) {
                            }
                        });
            }

            @Override
            public void onFail(Exception e) {
            }
        });
    }

    private void startMusicActivity() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), MusicActivity.class);
        intent.putExtras(getActivity().getIntent());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        getActivity().startActivity(intent);
    }

    @Override
    public void onDestroy() {
        if (mPlayServiceConnection != null) {
            getActivity().unbindService(mPlayServiceConnection);
        }
        super.onDestroy();
    }
}