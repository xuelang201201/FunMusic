package com.charles.funmusic.fragment;

import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.charles.funmusic.utils.FileUtil;
import com.charles.funmusic.utils.FontUtil;
import com.charles.funmusic.utils.Preferences;

import java.io.File;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashFragment extends Fragment {
    private static final String SPLASH_FILE_NAME = "splash";
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @BindView(R.id.fragment_splash_image_view)
    ImageView mImageView;
    @BindView(R.id.fragment_splash_copyright)
    TextView mCopyright;
//    private ServiceConnection mPlayServiceConnection;
    private View mView;

    public static SplashFragment newInstance() {

        Bundle args = new Bundle();

        SplashFragment fragment = new SplashFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getActivity() != null) {
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_splash, container, false);
            ButterKnife.bind(this, mView);
        }
        int year = Calendar.getInstance().get(Calendar.YEAR);
        mCopyright.setText(getString(R.string.copyright, year));
        FontUtil fontUtil = new FontUtil();
        fontUtil.changeFont(AppCache.getContext(), mCopyright);
//        checkService();
        showSplash();
        updateSplash();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startMusicActivity();
            }
        }, 3000);
        return mView;
    }

//    private void checkService() {
//        if (AppCache.getMusicService() == null) {
//            startService();
//            showSplash();
//            updateSplash();
//
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    bindService();
//                }
//            }, 1000);
//        } else {
//            startMusicActivity();
//            if (getActivity() != null) {
//                getActivity().finish();
//            }
//        }
//    }
//
//    private void startService() {
//        Intent intent = new Intent(getActivity(), PlayService.class);
//        AppCache.getContext().startService(intent);
//    }
//
//    private void bindService() {
//        Intent intent = new Intent();
//        if (getActivity() != null) {
//            intent.setClass(getActivity(), PlayService.class);
//        }
//        mPlayServiceConnection = new PlayServiceConnection();
//        getActivity().bindService(intent, mPlayServiceConnection, Context.BIND_AUTO_CREATE);
//    }
//
//    private class PlayServiceConnection implements ServiceConnection {
//
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            final MusicService musicService = ((PlayService.PlayBinder) service).getService();
//            AppCache.setMusicService(playService);
//            PermissionReq.with(SplashFragment.this)
//                    .permissions(Manifest.permission.READ_EXTERNAL_STORAGE,
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    .result(new PermissionReq.Result() {
//                        @Override
//                        public void onGranted() {
//                            scanMusic(playService);
//                        }
//
//                        @Override
//                        public void onDenied() {
//                            ToastUtil.show(R.string.no_permission_storage);
//                            if (getActivity()!= null) {
//                                getActivity().finish();
//                            }
//                            playService.quit();
//                        }
//                    }).request();
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//        }
//    }
//
//    private void scanMusic(PlayService playService) {
//        playService.updateMusicList(new EventCallback<Void>() {
//            @Override
//            public void onEvent(Void aVoid) {
//                startMusicActivity();
//                if (getActivity() != null) {
//                    getActivity().finish();
//                }
//            }
//        });
//    }

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
        if (getActivity() != null) {
            intent.setClass(getActivity(), MusicActivity.class);
        }
        intent.putExtras(getActivity().getIntent());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        getActivity().startActivity(intent);
        getActivity().overridePendingTransition(0, 0);
    }

    @Override
    public void onDestroy() {
//        if (mPlayServiceConnection != null && getActivity() != null) {
//            getActivity().unbindService(mPlayServiceConnection);
//        }
        super.onDestroy();
    }
}