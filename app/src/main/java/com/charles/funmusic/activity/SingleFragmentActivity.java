package com.charles.funmusic.activity;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Window;

import com.charles.funmusic.R;

public abstract class SingleFragmentActivity extends FragmentActivity {

    protected abstract Fragment createFragment();

    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    @Override
    protected void onPause() {
        overridePendingTransition(0, 0);
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // setContentView(R.layout.activity_fragment);
        setContentView(getLayoutResId());

        FragmentManager fm = getSupportFragmentManager();
        // 使用容器视图资源ID，从FragmentManager中获取CrimeFragment
        // 使用容器视图资源ID去识别UI fragment是FragmentManager的内部实现机制
        // 如果要向activity中添加多个fragment，通常需要分别为每个fragment创建不同ID的容器
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            // SingleFragmentActivity的子类会实现该方法，来返回由activity托管的fragment实例
            fragment = createFragment();
            fm.beginTransaction() // 创建一个新的fragment事务队列
                    .add(R.id.fragment_container, fragment) // 加入一个添加操作，向容器中添加fragment
                    .commit(); // 提交该事务
        }
    }
}
