package com.charles.funmusic.fragment;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.charles.funmusic.R;
import com.charles.funmusic.service.PlayService;

public class SettingFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    private Preference mFilterSize;
    private Preference mFilterTime;

    private PlayService mPlayService;

    public static SettingFragment newInstance() {

        Bundle args = new Bundle();

        SettingFragment fragment = new SettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void setPlayService(PlayService playService) {
        mPlayService = playService;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_setting);
        mFilterSize = findPreference(getString(R.string.setting_key_filter_size));
        mFilterTime = findPreference(getString(R.string.setting_key_filter_time));
        mFilterSize.setOnPreferenceClickListener(this);
        mFilterTime.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }
}
