package com.charles.funmusic.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import com.charles.funmusic.R;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimePickerFragment extends PickerDialogFragment {

    private TimePicker mTimePicker;

    public static TimePickerFragment newInstance(Date date) {
        Bundle args = getArgs(date);
        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected View initLayout() {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_time, null);

        mTimePicker = v.findViewById(R.id.dialog_time_time_picker);
        mTimePicker.setIs24HourView(false);
        mTimePicker.setCurrentHour(mCalendar.get(Calendar.HOUR_OF_DAY));
        mTimePicker.setCurrentMinute(mCalendar.get(Calendar.MINUTE));

        return v;
    }

    @Override
    protected Date getDate() {
        // TimePicker只设置时间，日期保持不变
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);

        // 从timepicker获得时间
        int hour = mTimePicker.getCurrentHour();
        int minute = mTimePicker.getCurrentMinute();

        return new GregorianCalendar(year, month, day, hour, minute).getTime();
    }
}
