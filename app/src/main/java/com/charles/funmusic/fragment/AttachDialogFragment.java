package com.charles.funmusic.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.utils.FontUtil;
import com.charles.funmusic.utils.ScreenUtil;

public abstract class AttachDialogFragment extends DialogFragment {

    public Activity mContext;

//    private View mView;
//
//    public abstract int getLayoutId();
//
//    public abstract void init(Bundle savedInstanceState);
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        if (mView == null) {
//            mView = inflater.inflate(getLayoutId(), container, false);
//
//            ButterKnife.bind(this, mView);
//            init(savedInstanceState);
//        }
//        return mView;
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = (Activity) context;
    }

    public void showSortDialog() {
        if (getFragmentManager() != null) {
            SortDialogFragment sortDialog = new SortDialogFragment();
            sortDialog.show(getFragmentManager(), "sort");
        }
    }

    /**
     * 沉浸式状态栏
     */
    public void initSystemBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int top = ScreenUtil.getStatusBarHeight();
            view.setPadding(0, top, 0, 0);
        }
    }

    /**
     * 改变字体
     * @param textView TextView
     * @param isBold 是否加粗
     */
    protected void changeFont(TextView textView, boolean isBold) {
        FontUtil fontUtil = new FontUtil();
        fontUtil.changeFont(getActivity(), textView);
        if (isBold) {
            textView.getPaint().setFakeBoldText(true); // 设置为粗体
        } else {
            textView.getPaint().setFakeBoldText(false); // 不设置为粗体
        }
    }

    /**
     * 显示软键盘
     */
    public void showSoftInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) AppCache
                .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 隐藏软键盘
     */
    public void hideSoftInput() {
        InputMethodManager manager = (InputMethodManager) AppCache
                .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }
}
