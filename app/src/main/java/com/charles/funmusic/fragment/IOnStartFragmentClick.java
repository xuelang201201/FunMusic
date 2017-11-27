package com.charles.funmusic.fragment;

/**
 * 用于fragment和activity之间的数据传递
 */
public interface IOnStartFragmentClick {
    /**
     * 同一个activity启动多个fragment
     * @param index 要启动的fragment的索引
     */
    void startFragment(int index);
}
