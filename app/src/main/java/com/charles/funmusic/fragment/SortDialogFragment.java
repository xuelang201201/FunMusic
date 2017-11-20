package com.charles.funmusic.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.utils.FontUtil;
import com.charles.funmusic.utils.Preferences;

public class SortDialogFragment extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_sort, null);

        TextView text = view.findViewById(R.id.dialog_sort_text);

        View titleView = view.findViewById(R.id.dialog_sort_by_title);
        ImageView titleImage = view.findViewById(R.id.dialog_sort_by_title_image);
        TextView titleText = view.findViewById(R.id.dialog_sort_by_title_text);

        View addTimeView = view.findViewById(R.id.dialog_sort_by_add_time);
        ImageView addTimeImage = view.findViewById(R.id.dialog_sort_by_add_time_image);
        TextView addTimeText = view.findViewById(R.id.dialog_sort_by_add_time_text);

        View playTimeView = view.findViewById(R.id.dialog_sort_by_play_time);
        ImageView playTimeImage = view.findViewById(R.id.dialog_sort_by_play_time_image);
        TextView playTimeText = view.findViewById(R.id.dialog_sort_by_play_time_text);

        int sortWay = Preferences.getSortWay();

        switch (sortWay) {
            case 1:
                addTimeImage.setVisibility(View.VISIBLE);
                addTimeText.setTextColor(ContextCompat.getColor(AppCache.getContext(), R.color.blue));
                break;

            case 2:
                playTimeImage.setVisibility(View.VISIBLE);
                playTimeText.setTextColor(ContextCompat.getColor(AppCache.getContext(), R.color.blue));
                break;

            case 0:
            default:
                titleImage.setVisibility(View.VISIBLE);
                titleText.setTextColor(ContextCompat.getColor(AppCache.getContext(), R.color.blue));
                break;
        }

        titleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Preferences.saveSortWay(0);
                dismiss();
            }
        });

        addTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Preferences.saveSortWay(1);
                dismiss();
            }
        });

        playTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Preferences.saveSortWay(2);
                dismiss();
            }
        });

        FontUtil fontUtil = new FontUtil();
        fontUtil.changeFont(getActivity(), text);
        text.getPaint().setFakeBoldText(true);
        fontUtil.changeFont(getActivity(), titleText);
        fontUtil.changeFont(getActivity(), addTimeText);
        fontUtil.changeFont(getActivity(), playTimeText);

        builder.setView(view);
        return builder.create();
    }
}