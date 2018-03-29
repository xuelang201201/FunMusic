package com.charles.funmusic.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.adapter.MyPagerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LocalMusicFragment extends AttachDialogFragment {

    @BindView(R.id.header_view)
    View mHeaderView;
    @BindView(R.id.header_view_image_view)
    ImageView mBack;
    @BindView(R.id.header_view_title_text_view)
    TextView mTitle;
    @BindView(R.id.header_view_more)
    ImageView mMore;
    @BindView(R.id.fragment_local_music_tabs)
    TabLayout mTabs;
    @BindView(R.id.fragment_local_music_view_pager)
    ViewPager mViewPager;
    @BindView(R.id.header_view_search)
    ImageView mSearch;
    @BindView(R.id.header_view_edit_text)
    EditText mEditText;
    @BindView(R.id.header_view_clear)
    ImageView mClear;
    @BindView(R.id.header_view_text_right)
    TextView mCancel;

    private SingleFragment mSingleFragment;
    private ArtistFragment mArtistFragment;
    private AlbumFragment mAlbumFragment;
    private FolderFragment mFolderFragment;

    private String[] mTitles;
    private PopupMenu mPopupMenu;
    private int mPage = 0;

//    private Preferences mPreferences;

    public static LocalMusicFragment newInstance(int page, String[] titles) {
        LocalMusicFragment fragment = new LocalMusicFragment();
        Bundle args = new Bundle(1);
        args.putInt("page_number", page);
        args.putStringArray("title", titles);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewPager.setCurrentItem(mPage);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPage = getArguments().getInt("page_number");
            mTitles = getArguments().getStringArray("title");
        }

//        mPreferences = Preferences.getInstance(mContext);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_music, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    public void init() {
        initView();
        initSystemBar(mHeaderView);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mTitles[0].equals("单曲")) {
            mTitle.setText(R.string.local_musics);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void initView() {

        MyPagerAdapter adapter = new MyPagerAdapter(getChildFragmentManager());
        mSingleFragment = new SingleFragment();
        adapter.addFragment(mSingleFragment, mTitles[0]);
        mArtistFragment = new ArtistFragment();
        adapter.addFragment(mArtistFragment, mTitles[1]);
        mAlbumFragment = new AlbumFragment();
        adapter.addFragment(mAlbumFragment, mTitles[2]);
        mFolderFragment = new FolderFragment();
        adapter.addFragment(mFolderFragment, mTitles[3]);
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(3); // 设置ViewPager预加载数量为3

        mTabs.setupWithViewPager(mViewPager);

        mMore.setVisibility(View.VISIBLE);
        mSearch.setVisibility(View.VISIBLE);
        mTitle.setText(R.string.local_musics);
        mCancel.setText(R.string.cancel);
        changeFont(mTitle, true);

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
                    mClear.setVisibility(View.VISIBLE);
                    mClear.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mEditText.getText().clear();
                        }
                    });
                } else {
                    mClear.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void updateView() {
        mTitle.setVisibility(View.VISIBLE);
        mSearch.setVisibility(View.VISIBLE);
        mMore.setVisibility(View.VISIBLE);
        mEditText.setVisibility(View.GONE);
        mCancel.setVisibility(View.GONE);
        mClear.setVisibility(View.GONE);
        mEditText.getText().clear();
    }

    @OnClick({R.id.header_view_image_view, R.id.header_view_more, R.id.header_view_search,
            R.id.header_view_edit_text, R.id.header_view_clear, R.id.header_view_text_right})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.header_view_image_view:
                onBackPressed();
                break;

            case R.id.header_view_more:
                showPopupMenu();
                break;

            case R.id.header_view_search:
                setSearchMode();
                break;

            case R.id.header_view_edit_text:
                break;

            case R.id.header_view_text_right:
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
//            mPopupMenu.inflate(R.menu.song_sort_by);
            mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_sort:
                            if (mViewPager.getCurrentItem() == 0) {
                                showSortDialog();
                                mSingleFragment.reloadAdapter();
                            }
                            if (mViewPager.getCurrentItem() == 1) {
//                                showSortDialog();
                                mArtistFragment.reloadAdapter();
                            }
                            if (mViewPager.getCurrentItem() == 2) {
//                                showSortDialog();
                                mAlbumFragment.reloadAdapter();
                            }
                            if (mViewPager.getCurrentItem() == 3) {
//                                showSortDialog();
                                mFolderFragment.reloadAdapter();
                            }
                            mPopupMenu.dismiss();
                            break;
                        case R.id.action_get_cover_lyric:
                            mPopupMenu.dismiss();
                            break;
                        case R.id.action_update_quality:
                            mPopupMenu.dismiss();
                            break;
//                        case R.id.menu_sort_by_az:
//                            mPreferences.saveSongSortOrder(SortOrder.SongSortOrder.SONG_A_Z);
//                            mSingleFragment.reloadAdapter();
//                            return true;
//                        case R.id.menu_sort_by_date:
//                            mPreferences.saveSongSortOrder(SortOrder.SongSortOrder.SONG_DATE);
//                            mSingleFragment.reloadAdapter();
//                            return true;
//                        case R.id.menu_sort_by_artist:
//                            mPreferences.saveSongSortOrder(SortOrder.SongSortOrder.SONG_ARTIST);
//                            mSingleFragment.reloadAdapter();
//                            return true;
//                        case R.id.menu_sort_by_album:
//                            mPreferences.saveSongSortOrder(SortOrder.SongSortOrder.SONG_ALBUM);
//                            mSingleFragment.reloadAdapter();
//                            return true;
                    }
                    return false;
                }
            });
            MenuPopupHelper helper = new MenuPopupHelper(getActivity(), (MenuBuilder) mPopupMenu.getMenu(), mMore);
            helper.setForceShowIcon(true);
            helper.show();
        }
    }

    /**
     * 显示搜索框
     */
    private void setSearchMode() {
        updateView();
        mSearch.setVisibility(View.GONE);
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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mBack.setEnabled(true);
            }
        }, 300);
    }
}
