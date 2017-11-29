package com.charles.funmusic.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.adapter.MyPagerAdapter;
import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.service.EventCallback;
import com.charles.funmusic.utils.ScreenUtil;
import com.flyco.tablayout.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class LocalMusicFragment extends BaseFragment implements EventCallback {

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

    private List<Fragment> mFragments = new ArrayList<>();
    private final String[] mTitles = {"单曲", "歌手", "专辑", "文件夹"};

    private SingleFragment mSingleFragment;
    private ArtistFragment mArtistFragment;
    private AlbumFragment mAlbumFragment;
    private FolderFragment mFolderFragment;

    private PopupWindow mPopupWindow;
    private PopupMenu mPopupMenu;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_local_music;
    }

    @Override
    public void initView(Bundle savedInstanceState) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mSingleFragment == null) {
                    mSingleFragment = new SingleFragment();
                }
                mFragments.add(mSingleFragment);
                mArtistFragment = new ArtistFragment();
                mFragments.add(mArtistFragment);
                mAlbumFragment = new AlbumFragment();
                mFragments.add(mAlbumFragment);
                mFolderFragment = new FolderFragment();
                mFragments.add(mFolderFragment);

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

    public void updateView() {
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

    @OnClick({R.id.header_view_image_view, R.id.header_view_more, R.id.header_view_search,
            R.id.header_view_edit_text, R.id.header_view_clear, R.id.header_view_cancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.header_view_image_view:
                onBackPressed();
                break;

            case R.id.header_view_more:
//                showPopupWindow();
//                showMoreDialog();
                showPopupMenu();
                break;

            case R.id.header_view_search:
                setSearchMode();
                break;

            case R.id.header_view_edit_text:
                break;

            case R.id.header_view_cancel:
                updateView();
                // 强制隐藏软键盘
                hideSoftInput();
                break;
        }
    }

    @SuppressLint("RestrictedApi")
    private void showPopupMenu() {
        if (getActivity() != null) {
            mPopupMenu = new PopupMenu(getActivity(), mMore);
            mPopupMenu.inflate(R.menu.popup_menu);
            mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_sort:
                            showSortDialog();
                            mPopupMenu.dismiss();
                            break;
                        case R.id.action_get_cover_lyric:
                            mPopupMenu.dismiss();
                            break;
                        case R.id.action_update_quality:
                            mPopupMenu.dismiss();
                            break;
                    }
                    return false;
                }
            });
            MenuPopupHelper helper = new MenuPopupHelper(getActivity(), (MenuBuilder) mPopupMenu.getMenu(), mMore);
            helper.setForceShowIcon(true);
            helper.show();
        }
    }

    private void showPopupWindow() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.popup_layout, null);
        mPopupWindow = new PopupWindow(getActivity());
        mPopupWindow.setContentView(view);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());

        LinearLayout sort = view.findViewById(R.id.popup_layout_sort);
        LinearLayout get = view.findViewById(R.id.popup_layout_get_lyric_cover);
        LinearLayout update = view.findViewById(R.id.popup_layout_update_quality);

        sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortDialog();
                mPopupWindow.dismiss();
            }
        });

        get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
            }
        });

        int x = ScreenUtil.getScreenWidth();
        int y = ScreenUtil.getStatusBarHeight();

        mPopupWindow.showAtLocation(view, Gravity.TOP, x, y);
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
            hideSoftInput();
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

    /**
     * 隐藏软键盘
     */
    private void hideSoftInput() {
        InputMethodManager manager = (InputMethodManager) AppCache
                .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(mView.getWindowToken(), 0);
        }
    }

    @Override
    public void onEvent(Object o) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mSingleFragment.onItemPlay();
            }
        });
    }
}
