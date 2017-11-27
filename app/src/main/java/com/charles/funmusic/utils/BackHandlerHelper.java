package com.charles.funmusic.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.charles.funmusic.fragment.IFragmentBackHander;

import java.util.List;

/**
 * 用于实现分发back事件，Fragment和Activity的外理
 * 逻辑是一样的，所以两者都需要调用该类的方法
 */
public class BackHandlerHelper {

    /**
     * 将back事件分发给FragmentManager中管理的子Fragment，如果该FragmentManager
     * 中的所有Fragment都没有处理back事件，则尝试FragmentManager.popBackStack()
     *
     * @return 如果处理了back键则返回 <b>true</b>
     */
    public static boolean handleBackPress(FragmentManager fragmentManager) {
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments == null) {
            return false;
        }

        for (int i = fragments.size() - 1; i >= 0; i--) {
            Fragment child = fragments.get(i);

            if (isFragmentBackHandled(child)) {
                return true;
            }
        }

        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            return true;
        }
        return false;
    }

    public static boolean handleBackPress(Fragment fragment) {
        return handleBackPress(fragment.getChildFragmentManager());
    }

    public static boolean handleBackPress(FragmentActivity fragmentActivity) {
        return handleBackPress(fragmentActivity.getSupportFragmentManager());
    }

    /**
     * 判断Fragment是否处理了Back键
     *
     * @return 如果处理了back键，则返回 <b>true</b>
     */
    public static boolean isFragmentBackHandled(Fragment fragment) {
        return fragment != null
                && fragment.isVisible()
                && fragment.getUserVisibleHint() // for ViewPager
                && fragment instanceof IFragmentBackHander
                && ((IFragmentBackHander) fragment).onBackPressed();
    }
}