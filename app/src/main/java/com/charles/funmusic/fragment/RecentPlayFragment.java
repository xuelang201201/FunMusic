package com.charles.funmusic.fragment;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.utils.ScreenUtil;

import butterknife.BindView;
import butterknife.OnClick;

public class RecentPlayFragment extends BaseFragment {
    @BindView(R.id.header_view)
    View mHeaderView;
    @BindView(R.id.header_view_image_view)
    ImageView mBack;
    @BindView(R.id.header_view_title_text_view)
    TextView mTitle;
    @BindView(R.id.header_view_more)
    ImageView mMore;
    @BindView(R.id.header_view_search)
    ImageView mSearch;
    @BindView(R.id.header_view_edit_text)
    EditText mEditText;
    @BindView(R.id.header_view_clear)
    ImageView mClear;
    @BindView(R.id.header_view_text_right)
    TextView mClearAll;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_recent_play;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        mTitle.setText(R.string.recent_play);
        mMore.setVisibility(View.GONE);
        mSearch.setVisibility(View.GONE);
        mEditText.setVisibility(View.GONE);
        mClear.setVisibility(View.GONE);
        mClearAll.setText(R.string.clear_all);
        mClearAll.setVisibility(View.VISIBLE);
        mClearAll.setTextSize(16);
        initSystemBar();
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

    @OnClick({R.id.header_view_image_view, R.id.header_view_text_right})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.header_view_image_view:
                onBackPressed();
                break;

            case R.id.header_view_text_right:

                break;
        }
    }

    private void onBackPressed() {
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }
}
