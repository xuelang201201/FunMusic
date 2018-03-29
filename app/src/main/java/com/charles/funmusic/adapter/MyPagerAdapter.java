package com.charles.funmusic.adapter;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment适配器
 */
public class MyPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragments = new ArrayList<>();
    private List<String> mTitles = new ArrayList<>();

    public MyPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(Fragment fragment, String title) {
        mFragments.add(fragment);
        mTitles.add(title);
    }

    @Override
    public Fragment getItem(int position) {
        if (mFragments.size() > position) {
            return mFragments.get(position);
        }
        return null;
    }

    /**
     * 每一个pager的标题
     *
     * @param position pager位置
     * @return 每个pager的标题
     */
    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }

    /**
     * fragment的数量
     */
    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }
}
