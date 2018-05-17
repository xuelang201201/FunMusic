package com.charles.funmusic.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.charles.funmusic.R;
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

public class SplashActivity extends BaseActivity {

//    private static final String TAG = "SplashActivity";
    private static final String SPLASH_FILE_NAME = "splash";

//    private PermissionHelper mPermissionHelper;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @BindView(R.id.activity_splash_image_view)
    ImageView mImageView;
    @BindView(R.id.activity_splash_copyright)
    TextView mCopyright;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        start();
    }

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
//        // 当系统为6.0以上时，需要申请权限
//        mPermissionHelper = new PermissionHelper(this);
//        mPermissionHelper.setOnApplyPermissionListener(new PermissionHelper.OnApplyPermissionListener() {
//            @Override
//            public void onAfterApplyAllPermission() {
//                Log.i(TAG, "All of requested permissions has been granted, so run app logic.");
//                start();
//            }
//        });
//        if (Build.VERSION.SDK_INT < 23) {
//            // 如果系统版本低于23，直接跑应用的逻辑
//            Log.d(TAG, "The api level of system is lower than 23, so run app logic directly.");
//            start();
//        } else {
//            // 如果权限全部申请了，那就直接跑应用逻辑
//            if (mPermissionHelper.isAllRequestedPermissionGranted()) {
//                Log.d(TAG, "All of requested permissions has been granted, so run app logic directly.");
//                start();
//            } else {
//                // 如果还有权限为申请，而且系统版本大于23，执行申请权限逻辑
//                Log.i(TAG, "Some of requested permissions hasn't been granted, so apply permissions first.");
//                mPermissionHelper.applyPermissions();
//            }
//        }
//    }

    private void start() {
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
    }

    private void showSplash() {
        File splashImg = new File(FileUtil.getSplashDir(this), SPLASH_FILE_NAME);
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
        intent.setClass(SplashActivity.this, MusicActivity.class);
        intent.putExtras(getIntent());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        mPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        mPermissionHelper.onActivityResult(requestCode, resultCode, data);
//    }

    @Override
    public void onBackPressed() {
    }
}
