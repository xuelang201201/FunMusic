package com.charles.funmusic.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.charles.funmusic.R;
import com.charles.funmusic.adapter.MenuItemAdapter;
import com.charles.funmusic.adapter.MyPagerAdapter;
import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.constant.Keys;
import com.charles.funmusic.fragment.MyFragment;
import com.charles.funmusic.fragment.OnlineFragment;
import com.charles.funmusic.fragment.TimerFragment;
import com.charles.funmusic.service.MusicPlayer;
import com.charles.funmusic.service.MusicService;
import com.charles.funmusic.utils.ToastUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class MusicActivity extends BaseActivity {

    @BindView(R.id.activity_music_drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.activity_music_navigation_view)
    ListView mNavigationView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.toolbar_menu)
    ImageView mToolbarMenu;
    @BindView(R.id.toolbar_search)
    ImageView mToolbarSearch;
    @BindView(R.id.activity_music_viewpager)
    ViewPager mViewPager;
    @BindView(R.id.toolbar_tabs)
    TabLayout mToolbarTabs;
    @BindView(R.id.play_bar)
    FrameLayout mPlayBar;

    private TimerFragment mTimerFragment;
    private MyFragment mMyFragment;
    private OnlineFragment mOnlineFragment;

    private String[] mTitles = {"我的", "乐库"};
    /**
     * 定时界面是否显示
     */
    private boolean isTimerFragmentShow = false;

    /**
     * 上一次按下返回键的时间
     */
    private long lastClickBackTimeMillis;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        getWindow().setBackgroundDrawableResource(R.color.background_material_light_1);

        initView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    private void initView() {

        setSupportActionBar(mToolbar);

        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());
        mMyFragment = new MyFragment();
        adapter.addFragment(mMyFragment, mTitles[0]);
        mOnlineFragment = new OnlineFragment();
        adapter.addFragment(mOnlineFragment, mTitles[1]);
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(2);

        mToolbarTabs.setupWithViewPager(mViewPager);

        mViewPager.setCurrentItem(0);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

//        mNavigationView.setNavigationItemSelectedListener(this);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        mDrawerLayout.addDrawerListener(toggle);
//        toggle.syncState();
//
//        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
//                    onBackPressed();
//                }
//            }
//        });
//        disableNavigationViewScrolls(mNavigationView);

        setUpDrawer();
    }

    private void setUpDrawer() {
        LayoutInflater inflater = LayoutInflater.from(this);
        mNavigationView.addHeaderView(inflater.inflate(R.layout.navigation_header_view, mNavigationView, false));
        mNavigationView.setAdapter(new MenuItemAdapter(this));
        mNavigationView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        startActivity(MusicActivity.this, ThemePickerActivity.class);
                        break;
                    case 2:
                        showTimerFragment();
                        break;
                    case 3:
                        break;
                    case 4:
                        if (MusicPlayer.isPlaying()) {
                            MusicPlayer.playOrPause();
                        }
                        unbindService();
                        onDestroy();
                        finish();
                        break;
                }
            }
        });
    }

//    /**
//     * 去除掉NavigationView的滚动条
//     *
//     * @param navigationView 侧边导航栏
//     */
//    private void disableNavigationViewScrolls(NavigationView navigationView) {
//        if (navigationView != null) {
//            NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);
//            if (navigationMenuView != null) {
//                navigationMenuView.setVerticalScrollBarEnabled(false);
//            }
//        }
//    }

    /**
     * 让返回键和home键功能一样
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (mTimerFragment != null && isTimerFragmentShow) {
                hideFragments(mTimerFragment);
                isTimerFragmentShow = false;
                return false;
            }

            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawers();
                return false;
            }

            // 判断是不是连续按下
            long currentClickBackTimeMillis = System.currentTimeMillis();
            if (currentClickBackTimeMillis - lastClickBackTimeMillis > 2000) {
                ToastUtil.show(getString(R.string.click_again_to_return_to_desktop));
                lastClickBackTimeMillis = currentClickBackTimeMillis;
                return true;
            }
            moveTaskToBack(false);
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 让 PlayFragment 页面上的返回键 起作用
     */
    @Override
    public void onBackPressed() {

        if (mTimerFragment != null && isTimerFragmentShow) {
            hideFragments(mTimerFragment);
            isTimerFragmentShow = false;
            return;
        }
        super.onBackPressed();
    }

    @OnClick({R.id.toolbar_menu, R.id.toolbar_search, R.id.play_bar})
    public void doClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_menu:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;

            case R.id.toolbar_search:
                showSearchActivity();
                break;

            case R.id.play_bar:
                break;
        }
    }

    private void showSearchActivity() {
        final Intent intent = new Intent(MusicActivity.this, SearchActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        MusicActivity.this.startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null) {
                    fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Keys.VIEW_PAGER_INDEX, mViewPager.getCurrentItem());
        outState.putStringArray("title", mTitles);
        mMyFragment.onSaveInstanceState(outState);
        mOnlineFragment.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        mViewPager.post(new Runnable() {
            @Override
            public void run() {
                mViewPager.setCurrentItem(savedInstanceState.getInt(Keys.VIEW_PAGER_INDEX), false);
//                mLocalMusicFragment.onRestoreInstanceState(savedInstanceState);
//                mOnlineFragment.onRestoreInstanceState(savedInstanceState);
            }
        });
        mTitles = savedInstanceState.getStringArray("title");
    }

    @Override
    protected void onDestroy() {
        MusicService service = AppCache.getMusicService();
        service.exit();
        finish();
        super.onDestroy();
    }

//    @Override
//    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
//        mDrawerLayout.closeDrawers();
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                item.setChecked(false);
//            }
//        }, 500);
//
//        switch (item.getItemId()) {
//            case R.id.action_timer:
//                showTimerFragment();
//                return true;
//
//            case R.id.action_setting:
//                startActivity(MusicActivity.this, SettingActivity.class);
//                return true;
//
//            case R.id.action_exit:
//                exit();
//                return true;
//        }
//        return false;
//    }

    /**
     * 显示定时界面
     */
    private void showTimerFragment() {
        if (isTimerFragmentShow) {
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in, 0);
        if (getSupportFragmentManager().findFragmentByTag("timer") == null) {
            mTimerFragment = new TimerFragment();
            ft.add(android.R.id.content, mTimerFragment, "timer");
        } else {
            ft.show(mTimerFragment);
        }
        ft.commitAllowingStateLoss();

        isTimerFragmentShow = true;
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

//    /**
//     * 退出程序
//     */
//    private void exit() {
//        finish();
//        MusicService service = AppCache.getMusicService();
//        if (service != null) {
//            service.exit();
//        }
//    }

    /**
     * 隐藏界面
     *
     * @param fragment 需要隐藏的fragment
     */
    private void hideFragments(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(0, R.anim.slide_out);
        ft.hide(fragment);
        ft.commitAllowingStateLoss();
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }
}