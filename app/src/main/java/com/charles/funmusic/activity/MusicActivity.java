package com.charles.funmusic.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.constant.Extra;
import com.charles.funmusic.constant.Keys;
import com.charles.funmusic.fragment.MyFragment;
import com.charles.funmusic.fragment.OnlineFragment;
import com.charles.funmusic.fragment.PlayFragment;
import com.charles.funmusic.fragment.SearchFragment;
import com.charles.funmusic.fragment.TimerFragment;
import com.charles.funmusic.model.Music;
import com.charles.funmusic.service.OnPlayerEventListener;
import com.charles.funmusic.service.PlayService;
import com.charles.funmusic.utils.CoverLoader;
import com.charles.funmusic.utils.SystemUtil;
import com.charles.funmusic.utils.ToastUtil;
import com.flyco.tablayout.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class MusicActivity extends BaseActivity implements OnPlayerEventListener, NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.activity_music_drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.activity_music_navigation_view)
    NavigationView mNavigationView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.toolbar_menu)
    ImageView mToolbarMenu;
    @BindView(R.id.toolbar_search)
    ImageView mToolbarSearch;
    @BindView(R.id.activity_music_viewpager)
    ViewPager mViewPager;
    @BindView(R.id.play_bar_cover)
    ImageView mPlayBarCover;
    @BindView(R.id.play_bar_progress_bar)
    ProgressBar mPlayBarProgressBar;
    @BindView(R.id.play_bar_play_or_pause)
    ImageView mPlayBarPlayOrPause;
    @BindView(R.id.play_bar_next)
    ImageView mPlayBarNext;
    @BindView(R.id.play_bar_artist)
    TextView mPlayBarArtist;
    @BindView(R.id.play_bar_title)
    TextView mPlayBarTitle;
    @BindView(R.id.activity_music_play_bar)
    View mPlayBar;
    @BindView(R.id.toolbar_tabs)
    SlidingTabLayout mToolbarTabs;

    private PlayFragment mPlayFragment;
    private TimerFragment mTimerFragment;
    private SearchFragment mSearchFragment;
    private MyFragment mMyFragment;
    private OnlineFragment mOnlineFragment;

    private List<Fragment> mFragments = new ArrayList<>();
    private final String[] mTitles = {"我的", "在线"};
    /**
     * 是否显示播放界面
     */
    private boolean isPlayFragmentShow = false;
    /**
     * 是否显示定时界面
     */
    private boolean isTimerFragmentShow = false;
    /**
     * 是否显示搜索界面
     */
    private boolean isSearchFragmentShow = false;
    /**
     * 上一次按下返回键的时间
     */
    private long lastClickBackTimeMillis;

    private MenuItem mTimerItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        if (!checkServiceAlive()) {
            return;
        }

        getPlayService().setOnPlayEventListener(this);

        initView();
        onChangeImpl(getPlayService().getPlayingMusic());
        parseIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        parseIntent();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(Extra.EXTRA_NOTIFICATION)) {
            showPlayFragment();
            setIntent(new Intent());
        }
    }

    private void initView() {
        mMyFragment = new MyFragment();
        mFragments.add(mMyFragment);

        mOnlineFragment = new OnlineFragment();
        mFragments.add(mOnlineFragment);

        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mToolbarTabs.setViewPager(mViewPager, mTitles);

        mViewPager.setCurrentItem(1);
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

        mNavigationView.setNavigationItemSelectedListener(this);
        setSupportActionBar(mToolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    onBackPressed();
                }
            }
        });

        disableNavigationViewScrolls(mNavigationView);
    }

    /**
     * 去除掉NavigationView的滚动条
     *
     * @param navigationView 侧边导航栏
     */
    private void disableNavigationViewScrolls(NavigationView navigationView) {
        if (navigationView != null) {
            NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);
            if (navigationMenuView != null) {
                navigationMenuView.setVerticalScrollBarEnabled(false);
            }
        }
    }

    @Override
    public void onChange(final Music music) {
        onChangeImpl(music);
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            mHandler.post(new Thread(new Runnable() {
                @Override
                public void run() {
                    mPlayFragment.onChange(music);
                }
            }));
        }
    }

    @Override
    public void onPlayerStart() {
        mPlayBarPlayOrPause.setSelected(true);
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            mHandler.post(new Thread(new Runnable() {
                @Override
                public void run() {
                    mPlayFragment.onPlayerStart();
                }
            }));
        }
    }

    @Override
    public void onPlayerPause() {
        mPlayBarPlayOrPause.setSelected(false);
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            mPlayFragment.onPlayerPause();
        }
    }

    /**
     * 更新播放进度
     *
     * @param progress 进度
     */
    @Override
    public void onPublish(final int progress) {
        mPlayBarProgressBar.setProgress(progress);
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            mHandler.post(new Thread(new Runnable() {
                @Override
                public void run() {
                    mPlayFragment.onPublish(progress);
                }
            }));
        }
    }

    @Override
    public void onBufferingUpdate(final int percent) {
        if (mPlayFragment != null && mPlayFragment.isAdded()) {
            mHandler.post(new Thread(new Runnable() {
                @Override
                public void run() {
                    mPlayFragment.onBufferingUpdate(percent);
                }
            }));
        }
    }

    @Override
    public void onTimer(long remain) {
        if (mTimerItem == null) {
            mTimerItem = mNavigationView.getMenu().findItem(R.id.action_timer);
        }
        String title = getString(R.string.menu_timer);
        mTimerItem.setTitle(remain == 0 ? title : SystemUtil.formatTime(title + "                  mm:ss", remain));
    }

    @Override
    public void onMusicListUpdate() {
        if (mMyFragment != null && mMyFragment.isAdded()) {
            mMyFragment.onMusicListUpdate();
        }
    }

    /**
     * 让返回键和home键功能一样
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (mPlayFragment != null && isPlayFragmentShow) {
                hideFragments(mPlayFragment, R.anim.fragment_slide_down);
                isPlayFragmentShow = false;
                return false;
            }

            if (mTimerFragment != null && isTimerFragmentShow) {
                hideFragments(mTimerFragment, R.anim.fragment_left_out);
                isTimerFragmentShow = false;
                return false;
            }

            if (mSearchFragment != null && isSearchFragmentShow) {
                hideFragments(mSearchFragment, R.anim.fragment_left_out);
                isSearchFragmentShow = false;
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

//            Intent intent = new Intent(Intent.ACTION_MAIN);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.addCategory(Intent.CATEGORY_HOME);
//            this.startActivity(intent);
            moveTaskToBack(false);
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 让 PlayFragment 页面上的返回键 起作用
     */
    @Override
    public void onBackPressed() {
        if (mPlayFragment != null && isPlayFragmentShow) {
            hideFragments(mPlayFragment, R.anim.fragment_slide_down);
            isPlayFragmentShow = false;
            return;
        }

        if (mTimerFragment != null && isTimerFragmentShow) {
            hideFragments(mTimerFragment, R.anim.fragment_left_out);
            isTimerFragmentShow = false;
            return;
        }

        if (mSearchFragment != null && isSearchFragmentShow) {
            hideFragments(mSearchFragment, R.anim.fragment_left_out);
            isSearchFragmentShow = false;
            return;
        }

//        moveTaskToBack(false);
        super.onBackPressed();
    }

    /**
     * 显示正在播放界面
     */
    private void showPlayFragment() {
        if (isPlayFragmentShow) {
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_slide_up, 0);
        if (mPlayFragment == null) {
            mPlayFragment = new PlayFragment();
            ft.replace(android.R.id.content, mPlayFragment);
        } else {
            ft.show(mPlayFragment);
        }
        ft.commitAllowingStateLoss();

        isPlayFragmentShow = true;
    }

    /**
     * 显示定时界面
     */
    private void showTimerFragment() {
        if (isTimerFragmentShow) {
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_right_in, 0);
        if (mTimerFragment == null) {
            mTimerFragment = new TimerFragment();
            ft.replace(R.id.activity_music_timer_container, mTimerFragment);
        } else {
            ft.show(mTimerFragment);
        }
        ft.commitAllowingStateLoss();

        isTimerFragmentShow = true;

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    /**
     * 显示搜索界面
     */
    private void showSearchFragment() {
        if (isSearchFragmentShow) {
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fragment_right_in, 0);
        if (mSearchFragment == null) {
            mSearchFragment = new SearchFragment();
            ft.replace(R.id.activity_music_search_container, mSearchFragment);
        } else {
            ft.show(mSearchFragment);
        }
        ft.commitAllowingStateLoss();
        isSearchFragmentShow = true;
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    /**
     * 隐藏界面
     * @param fragment 需要隐藏的fragment
     * @param anim 退出动画
     */
    private void hideFragments(Fragment fragment, int anim) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(0, anim);
        ft.hide(fragment);
        ft.commitAllowingStateLoss();
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @OnClick({R.id.toolbar_menu, R.id.toolbar_search, R.id.activity_music_play_bar,
            R.id.play_bar_next, R.id.play_bar_play_or_pause})
    public void doClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_menu:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;

            case R.id.toolbar_search:
                showSearchFragment();
                break;

            case R.id.activity_music_play_bar:
                showPlayFragment();
                break;

            case R.id.play_bar_play_or_pause:
                playPause();
                break;

            case R.id.play_bar_next:
                next();
                break;
        }
    }

    /**
     * 播放或者暂停
     */
    private void playPause() {
        getPlayService().playPause();
    }

    /**
     * 播放下一首
     */
    private void next() {
        getPlayService().next();
    }

    private void onChangeImpl(Music music) {
        if (music == null) {
            return;
        }

        Bitmap cover = CoverLoader.getInstance().loadRound(music);
        mPlayBarCover.setImageBitmap(cover);

        // 向控件内填充数据
        String artist;

        if ("<unknown>".equals(music.getArtist())) {
            artist = getString(R.string.unknown_artist);
        } else {
            artist = music.getArtist();
        }

        changeFont(mPlayBarTitle, false);
        changeFont(mPlayBarArtist, false);

        mPlayBarTitle.setText(music.getTitle());
        mPlayBarArtist.setText(artist);
        mPlayBarPlayOrPause.setSelected(getPlayService().isPlaying() || getPlayService().isPreparing());
        mPlayBarProgressBar.setMax((int) music.getDuration());
        mPlayBarProgressBar.setProgress((int) getPlayService().getCurrentPosition());

        if (mMyFragment != null && mMyFragment.isAdded()) {
            mMyFragment.onItemPlay();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Keys.VIEW_PAGER_INDEX, mViewPager.getCurrentItem());
        mMyFragment.onSaveInstanceState(outState);
        mOnlineFragment.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        mViewPager.post(new Runnable() {
            @Override
            public void run() {
                mViewPager.setCurrentItem(savedInstanceState.getInt(Keys.VIEW_PAGER_INDEX), false);
                mMyFragment.onRestoreInstanceState(savedInstanceState);
//                mOnlineFragment.onRestoreInstanceState(savedInstanceState);
            }
        });
    }

    @Override
    protected void onDestroy() {
        PlayService service = AppCache.getPlayService();
        if (service != null) {
            service.setOnPlayEventListener(null);
        }
        super.onDestroy();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (mMyFragment != null) {
            mMyFragment.onWindowFocusChanged(hasFocus);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        mDrawerLayout.closeDrawers();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                item.setChecked(false);
            }
        }, 500);
//        return NaviMenuExecutor.onNavigationItemSelected(item, this);

        switch (item.getItemId()) {
            case R.id.action_timer:
                showTimerFragment();
                return true;

            case R.id.action_setting:
                startActivity(MusicActivity.this, SettingActivity.class);
                return true;

            case R.id.action_exit:
                exit();
                return true;
        }
        return false;
    }

    /**
     * 退出程序
     */
    private void exit() {
        finish();
        PlayService service = AppCache.getPlayService();
        if (service != null) {
            service.quit();
        }
    }

    /**
     * Fragment适配器
     */
    private class MyPagerAdapter extends FragmentPagerAdapter {

        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }
    }
}