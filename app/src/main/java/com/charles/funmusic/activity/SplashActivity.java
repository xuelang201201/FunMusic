package com.charles.funmusic.activity;

import android.support.v4.app.Fragment;

import com.charles.funmusic.fragment.SplashFragment;

public class SplashActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return SplashFragment.newInstance();
    }

    @Override
    public void onBackPressed() {
    }
}
