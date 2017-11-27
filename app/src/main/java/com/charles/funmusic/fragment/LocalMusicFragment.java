package com.charles.funmusic.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.adapter.MyPagerAdapter;
import com.charles.funmusic.utils.ScreenUtil;
import com.flyco.tablayout.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Unbinder;

public class LocalMusicFragment extends BaseFragment {

    @BindView(R.id.header_view)
    View mHeaderView;
    @BindView(R.id.header_view_image_view)
    ImageView mBack;
    @BindView(R.id.header_view_title_text_view)
    TextView mTitle;
    @BindView(R.id.header_view_more)
    ImageView mMore;
    @BindView(R.id.fragment_local_music_tabs)
    SlidingTabLayout mTabs;
    @BindView(R.id.fragment_local_music_view_pager)
    ViewPager mViewPager;
    @BindView(R.id.header_view_search)
    ImageView mSearch;
    @BindView(R.id.header_view_edit_text)
    EditText mEditText;
    @BindView(R.id.header_view_clear)
    ImageView mClear;
    @BindView(R.id.header_view_cancel)
    TextView mCancel;
    Unbinder unbinder;

    private List<Fragment> mFragments = new ArrayList<>();
    private final String[] mTitles = {"单曲", "歌手", "专辑", "文件夹"};

    @Override
    public int getLayoutId() {
        return R.layout.fragment_local_music;
    }

    @Override
    public void initView(Bundle savedInstanceState) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                SingleFragment singleFragment = new SingleFragment();
                mFragments.add(singleFragment);
                ArtistFragment artistFragment = new ArtistFragment();
                mFragments.add(artistFragment);
                AlbumFragment albumFragment = new AlbumFragment();
                mFragments.add(albumFragment);
                FolderFragment folderFragment = new FolderFragment();
                mFragments.add(folderFragment);

                MyPagerAdapter adapter = new MyPagerAdapter(getFragmentManager(), mFragments, mTitles);
                mViewPager.setOffscreenPageLimit(4); // 设置ViewPager预加载数量为4
                mViewPager.setAdapter(adapter);
                mTabs.setViewPager(mViewPager);
                mViewPager.setCurrentItem(0);
            }
        });

        mMore.setVisibility(View.VISIBLE);
        mSearch.setVisibility(View.VISIBLE);
        mTitle.setText(R.string.local_musics);
        changeFont(mTitle, true);
        initSystemBar();

        setEditText();
    }

    private void setEditText() {
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    if (mClear != null) {
                        mClear.setVisibility(View.VISIBLE);
                        mClear.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mEditText != null) {
                                    mEditText.getText().clear();
                                }
                            }
                        });
                    }
                } else {
                    if (mClear != null) {
                        mClear.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * 沉浸式状态栏
     */
    private void initSystemBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int top = ScreenUtil.getStatusBarHeight();
            mHeaderView.setPadding(0, top, 0, 0);
        }
    }

    private void updateView() {
//        mRandomPlay.setVisibility(View.VISIBLE);
//        mSort.setVisibility(View.VISIBLE);
        mTitle.setVisibility(View.VISIBLE);
        mSearch.setVisibility(View.VISIBLE);
        mMore.setVisibility(View.VISIBLE);
        mEditText.setVisibility(View.GONE);
        mCancel.setVisibility(View.GONE);
        mClear.setVisibility(View.GONE);
        mEditText.getText().clear();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.header_view_image_view, R.id.header_view_more, R.id.header_view_search,
            R.id.header_view_edit_text, R.id.header_view_clear, R.id.header_view_cancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.header_view_image_view:
                onBackPressed();
                break;

            case R.id.header_view_more:
                break;

            case R.id.header_view_search:
                setSearchMode();
                break;

            case R.id.header_view_edit_text:
                break;

            case R.id.header_view_cancel:
                updateView();
                // 强制隐藏软键盘
                hideSoftInput(view);
                break;
        }
    }

    /**
     * 显示搜索框
     */
    private void setSearchMode() {
        updateView();
//        mRandomPlay.setVisibility(View.GONE);
        mSearch.setVisibility(View.GONE);
//        mSort.setVisibility(View.GONE);
        mCancel.setVisibility(View.VISIBLE);
        mMore.setVisibility(View.GONE);
        mTitle.setVisibility(View.GONE);
        mEditText.setVisibility(View.VISIBLE);
        mEditText.clearFocus();
        mEditText.requestFocus();
        // 显示软键盘
        showSoftInput();
    }

    public void onBackPressed() {
        if (getActivity() != null) {
            hideSoftInput(mView);
            updateView();
            getActivity().onBackPressed();
        }

        mBack.setEnabled(false);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBack.setEnabled(true);
            }
        }, 300);
    }
}
