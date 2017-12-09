package com.charles.funmusic.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.service.QuitTimer;
import com.charles.funmusic.utils.ScreenUtil;
import com.charles.funmusic.utils.ToastUtil;

import butterknife.BindView;
import butterknife.OnClick;

public class TimerFragment extends BaseFragment {

    @BindView(R.id.header_view)
    View mHeaderView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.header_view_image_view)
    ImageView mBack;
    @BindView(R.id.header_view_title_text_view)
    TextView mTitle;
    //    @BindView(R.id.fragment_timer_set)
//    TextView mTimerSet;
    @BindView(R.id.fragment_timer_no_text)
    TextView mTimerNoText;
    @BindView(R.id.fragment_timer_no_image)
    ImageView mTimerNoImage;
    @BindView(R.id.fragment_timer_no)
    LinearLayout mTimerNo;
    @BindView(R.id.fragment_timer_ten_text)
    TextView mTimerTenText;
    @BindView(R.id.fragment_timer_ten_image)
    ImageView mTimerTenImage;
    @BindView(R.id.fragment_timer_ten)
    LinearLayout mTimerTen;
    @BindView(R.id.fragment_timer_twenty_text)
    TextView mTimerTwentyText;
    @BindView(R.id.fragment_timer_twenty_image)
    ImageView mTimerTwentyImage;
    @BindView(R.id.fragment_timer_twenty)
    LinearLayout mTimerTwenty;
    @BindView(R.id.fragment_timer_thirty_text)
    TextView mTimerThirtyText;
    @BindView(R.id.fragment_timer_thirty_image)
    ImageView mTimerThirtyImage;
    @BindView(R.id.fragment_timer_thirty)
    LinearLayout mTimerThirty;
    @BindView(R.id.fragment_timer_an_hour_text)
    TextView mTimerAnHourText;
    @BindView(R.id.fragment_timer_an_hour_image)
    ImageView mTimerAnHourImage;
    @BindView(R.id.fragment_timer_an_hour)
    LinearLayout mTimerAnHour;
    @BindView(R.id.fragment_timer_custom_text)
    TextView mTimerCustomText;
    @BindView(R.id.fragment_timer_custom_image)
    ImageView mTimerCustomImage;
    @BindView(R.id.fragment_timer_custom)
    LinearLayout mTimerCustom;

//    private CountDownTimer mTimer;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_timer;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        mTitle.setText(getString(R.string.menu_timer));

        setSelectedTimer(mTimerNoText, mTimerNoImage);
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

//    private void startCountDownTimer(final int minute) {
//        mTimer = new CountDownTimer(minute * 60 * 1000, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                mTimerSet.setText(SystemUtil.formatTime("mm:ss" + "后，将停止播放歌曲", millisUntilFinished));
//            }
//
//            @Override
//            public void onFinish() {
//                mTimer.cancel();
//                mTimerSet.setText(getString(R.string.timer_not_set));
//            }
//        }.start();
//    }

    @OnClick({R.id.header_view_image_view, R.id.fragment_timer_no, R.id.fragment_timer_ten, R.id.fragment_timer_twenty, R.id.fragment_timer_thirty, R.id.fragment_timer_an_hour, R.id.fragment_timer_custom})
    public void setTimer(View v) {

        switch (v.getId()) {
            case R.id.header_view_image_view:
                onBackPressed();
                break;

            case R.id.fragment_timer_no:
                resetTimer();
                setSelectedTimer(mTimerNoText, mTimerNoImage);
                startTimer(0);
//                startCountDownTimer(0);
                break;

            case R.id.fragment_timer_ten:
                resetTimer();
                setSelectedTimer(mTimerTenText, mTimerTenImage);
                startTimer(10);
//                startCountDownTimer(10);
                break;

            case R.id.fragment_timer_twenty:
                resetTimer();
                setSelectedTimer(mTimerTwentyText, mTimerTwentyImage);
                startTimer(20);
//                startCountDownTimer(20);
                break;

            case R.id.fragment_timer_thirty:
                resetTimer();
                setSelectedTimer(mTimerThirtyText, mTimerThirtyImage);
                startTimer(30);
//                startCountDownTimer(30);
                break;

            case R.id.fragment_timer_an_hour:
                resetTimer();
                setSelectedTimer(mTimerAnHourText, mTimerAnHourImage);
                startTimer(60);
//                startCountDownTimer(60);
                break;

            case R.id.fragment_timer_custom:
                resetTimer();
                setSelectedTimer(mTimerCustomText, mTimerCustomImage);
//                startCountDownTimer(time);
//                startTimer(time);
                break;
        }
    }

    private void startTimer(int minute) {
        QuitTimer.getInstance().start(minute * 60 * 1000);
        if (minute > 0) {
            ToastUtil.show(getString(R.string.timer_set, String.valueOf(minute)));
        }
//        if (minute > 0) {
//            mTimerSet.setText(context.getString(R.string.timer_set, String.valueOf(minute)));
//        } else {
//            mTimerSet.setText(context.getString(R.string.timer_not_set));
//        }
    }

    private void resetTimer() {
        if (getActivity() != null) {
            mTimerNoText.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
            mTimerNoImage.setVisibility(View.INVISIBLE);
            mTimerTenText.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
            mTimerTenImage.setVisibility(View.INVISIBLE);
            mTimerTwentyText.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
            mTimerTwentyImage.setVisibility(View.INVISIBLE);
            mTimerThirtyText.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
            mTimerThirtyImage.setVisibility(View.INVISIBLE);
            mTimerAnHourText.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
            mTimerAnHourImage.setVisibility(View.INVISIBLE);
            mTimerCustomText.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
            mTimerCustomImage.setVisibility(View.INVISIBLE);
        }
    }

    private void setSelectedTimer(TextView text, ImageView image) {
        if (getActivity() != null) {
            text.setTextColor(ContextCompat.getColor(getActivity(), R.color.red));
            image.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 返回activity
     */
    public void onBackPressed() {
        if (getActivity() != null) {
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