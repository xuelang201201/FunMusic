package com.charles.funmusic.activity;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.charles.funmusic.R;

import butterknife.BindView;
import butterknife.OnClick;

public class ThemePickerActivity extends BaseActivity {

    @BindView(R.id.header_view)
    View mHeaderView;
    @BindView(R.id.header_view_image_view)
    ImageView mBack;
    @BindView(R.id.header_view_title_text_view)
    TextView mTitle;
    @BindView(R.id.activity_theme_picker_night_mode_button)
    Button mNightModeButton;
    @BindView(R.id.activity_theme_picker_night_mode)
    LinearLayout mNightModeLayout;
    @BindView(R.id.activity_theme_picker_pink_button)
    Button mPinkButton;
    @BindView(R.id.activity_theme_picker_pink)
    LinearLayout mPinkLayout;
    @BindView(R.id.activity_theme_picker_red_button)
    Button mRedButton;
    @BindView(R.id.activity_theme_picker_red)
    LinearLayout mRedLayout;
    @BindView(R.id.activity_theme_picker_yellow_button)
    Button mYellowButton;
    @BindView(R.id.activity_theme_picker_yellow)
    LinearLayout mYellowLayout;
    @BindView(R.id.activity_theme_picker_green_button)
    Button mGreenButton;
    @BindView(R.id.activity_theme_picker_green)
    LinearLayout mGreenLayout;
    @BindView(R.id.activity_theme_picker_blue_button)
    Button mBlueButton;
    @BindView(R.id.activity_theme_picker_blue)
    LinearLayout mBlueLayout;
    @BindView(R.id.activity_theme_picker_purple_button)
    Button mPurpleButton;
    @BindView(R.id.activity_theme_picker_purple)
    LinearLayout mPurpleLayout;


    public static final String TAG = "ThemePickerActivity";
    ImageView[] mCards = new ImageView[8];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_picker);

        mTitle.setText(getString(R.string.skin));

        initSystemBar(mHeaderView);
    }

    @OnClick({R.id.header_view_image_view, R.id.activity_theme_picker_night_mode_button, R.id.activity_theme_picker_night_mode, R.id.activity_theme_picker_pink_button, R.id.activity_theme_picker_pink, R.id.activity_theme_picker_red_button, R.id.activity_theme_picker_red, R.id.activity_theme_picker_yellow_button, R.id.activity_theme_picker_yellow, R.id.activity_theme_picker_green_button, R.id.activity_theme_picker_green, R.id.activity_theme_picker_blue_button, R.id.activity_theme_picker_blue, R.id.activity_theme_picker_purple_button, R.id.activity_theme_picker_purple})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.header_view_image_view:
                finish();
                break;

            case R.id.activity_theme_picker_night_mode_button:
            case R.id.activity_theme_picker_night_mode:
                initView();
                mNightModeButton.setText(getString(R.string.in_use));
                mNightModeButton.setTextColor(ContextCompat.getColor(this, R.color.black));
                break;

            case R.id.activity_theme_picker_pink_button:
            case R.id.activity_theme_picker_pink:
                initView();
                mPinkButton.setText(getString(R.string.in_use));
                mPinkButton.setTextColor(ContextCompat.getColor(this, R.color.theme_color_primary));
                break;

            case R.id.activity_theme_picker_red_button:
            case R.id.activity_theme_picker_red:
                initView();
                mRedButton.setText(getString(R.string.in_use));
                mRedButton.setTextColor(ContextCompat.getColor(this, R.color.firey));
                break;

            case R.id.activity_theme_picker_yellow_button:
            case R.id.activity_theme_picker_yellow:
                initView();
                mYellowButton.setText(getString(R.string.in_use));
                mYellowButton.setTextColor(ContextCompat.getColor(this, R.color.sand));
                break;

            case R.id.activity_theme_picker_green_button:
            case R.id.activity_theme_picker_green:
                initView();
                mGreenButton.setText(getString(R.string.in_use));
                mGreenButton.setTextColor(ContextCompat.getColor(this, R.color.wood));
                break;

            case R.id.activity_theme_picker_blue_button:
            case R.id.activity_theme_picker_blue:
                initView();
                mBlueButton.setText(getString(R.string.in_use));
                mBlueButton.setTextColor(ContextCompat.getColor(this, R.color.storm));
                break;

            case R.id.activity_theme_picker_purple_button:
            case R.id.activity_theme_picker_purple:
                initView();
                mPurpleButton.setText(getString(R.string.in_use));
                mPurpleButton.setTextColor(ContextCompat.getColor(this, R.color.hope));
                break;
        }
    }

    private void initView() {
        mNightModeButton.setText(getString(R.string.use));
        mNightModeButton.setTextColor(ContextCompat.getColor(this, R.color.theme_picker_color));
        mPinkButton.setText(getString(R.string.use));
        mPinkButton.setTextColor(ContextCompat.getColor(this, R.color.theme_picker_color));
        mRedButton.setText(getString(R.string.use));
        mRedButton.setTextColor(ContextCompat.getColor(this, R.color.theme_picker_color));
        mYellowButton.setText(getString(R.string.use));
        mYellowButton.setTextColor(ContextCompat.getColor(this, R.color.theme_picker_color));
        mGreenButton.setText(getString(R.string.use));
        mGreenButton.setTextColor(ContextCompat.getColor(this, R.color.theme_picker_color));
        mBlueButton.setText(getString(R.string.use));
        mBlueButton.setTextColor(ContextCompat.getColor(this, R.color.theme_picker_color));
        mPurpleButton.setText(getString(R.string.use));
        mPurpleButton.setTextColor(ContextCompat.getColor(this, R.color.theme_picker_color));
    }
}
