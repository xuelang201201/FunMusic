package com.charles.funmusic.activity;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.service.PlayService;

import butterknife.BindView;
import butterknife.OnClick;

public class SettingActivity extends BaseActivity {

    @BindView(R.id.header_view)
    View mHeaderView;
    @BindView(R.id.header_view_image_view)
    ImageView mHeaderImage;
    @BindView(R.id.header_view_title_text_view)
    TextView mHeaderTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        if (!checkServiceAlive()) {
            return;
        }

        SettingFragment settingFragment = new SettingFragment();
        settingFragment.setPlayService(getPlayService());
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_setting_container, settingFragment)
                .commit();

        mHeaderTitle.setText(getString(R.string.setting));
    }

    @OnClick(R.id.header_view_image_view)
    public void onClick() {
        finish();
    }

    public static class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
        private Preference mFilterSize;
        private Preference mFilterTime;

        private PlayService mPlayService;

        public void setPlayService(PlayService playService) {
            mPlayService = playService;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_setting);

//            mFilterSize = findPreference(getString(R.string.setting_key_filter_size));
//            mFilterSize = findPreference(getString(R.string.setting_key_filter_time));
//            mFilterSize.setOnPreferenceClickListener(this);
//            mFilterTime.setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            return false;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            return false;
        }
    }
}




























