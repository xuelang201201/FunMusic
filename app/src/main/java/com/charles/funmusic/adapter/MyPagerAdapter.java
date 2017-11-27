package com.charles.funmusic.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Fragment适配器
 */
public class MyPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragments;
    private String[] mTitles;

    public MyPagerAdapter(FragmentManager fm, List<Fragment> fragments, String[] titles) {
        super(fm);
        mFragments = fragments;
        mTitles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    /**
     * 每一个pager的标题
     *
     * @param position pager位置
     * @return 每个pager的标题
     */
    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

    /**
     * fragment的数量
     */
    @Override
    public int getCount() {
        return mFragments.size();
    }
}
