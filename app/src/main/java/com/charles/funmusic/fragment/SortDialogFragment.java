package com.charles.funmusic.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.utils.FontUtil;
import com.charles.funmusic.utils.Preferences;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SortDialogFragment extends DialogFragment {

    private static final int SORT_BY_ARTIST = 0;
    private static final int SORT_BY_SINGLE = 1;
    private static final int SORT_BY_ADD_TIME = 2;
    private static final int SORT_BY_ALBUM = 3;
    private static final int SORT_BY_PLAY_TIME = 4;

    @BindView(R.id.dialog_sort_text)
    TextView mText;
    @BindView(R.id.dialog_sort_by_artist_text)
    TextView mArtistText;
    @BindView(R.id.dialog_sort_by_artist_image)
    ImageView mArtistImage;
    @BindView(R.id.dialog_sort_by_artist)
    LinearLayout mArtistView;
    @BindView(R.id.dialog_sort_by_single_text)
    TextView mSingleText;
    @BindView(R.id.dialog_sort_by_single_image)
    ImageView mSingleImage;
    @BindView(R.id.dialog_sort_by_single)
    LinearLayout mSingleView;
    @BindView(R.id.dialog_sort_by_add_time_text)
    TextView mAddTimeText;
    @BindView(R.id.dialog_sort_by_add_time_image)
    ImageView mAddTimeImage;
    @BindView(R.id.dialog_sort_by_add_time)
    LinearLayout mAddTimeView;
    @BindView(R.id.dialog_sort_by_album_text)
    TextView mAlbumText;
    @BindView(R.id.dialog_sort_by_album_image)
    ImageView mAlbumImage;
    @BindView(R.id.dialog_sort_by_album)
    LinearLayout mAlbumView;
    @BindView(R.id.dialog_sort_by_play_time_text)
    TextView mPlayTimeText;
    @BindView(R.id.dialog_sort_by_play_time_image)
    ImageView mPlayTimeImage;
    @BindView(R.id.dialog_sort_by_play_time)
    LinearLayout mPlayTimeView;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_sort, null);
        ButterKnife.bind(this, view);

        int sortWay = Preferences.getSortWay();

        switch (sortWay) {
            case SORT_BY_SINGLE:
                mSingleImage.setVisibility(View.VISIBLE);
                mSingleText.setTextColor(ContextCompat.getColor(AppCache.getContext(), R.color.red));
                break;

            case SORT_BY_ADD_TIME:
                mAddTimeImage.setVisibility(View.VISIBLE);
                mAddTimeText.setTextColor(ContextCompat.getColor(AppCache.getContext(), R.color.red));
                break;

            case SORT_BY_ALBUM:
                mAlbumImage.setVisibility(View.VISIBLE);
                mAlbumText.setTextColor(ContextCompat.getColor(AppCache.getContext(), R.color.red));
                break;

            case SORT_BY_PLAY_TIME:
                mPlayTimeImage.setVisibility(View.VISIBLE);
                mPlayTimeText.setTextColor(ContextCompat.getColor(AppCache.getContext(), R.color.red));
                break;

            case SORT_BY_ARTIST:
            default:
                mArtistImage.setVisibility(View.VISIBLE);
                mArtistText.setTextColor(ContextCompat.getColor(AppCache.getContext(), R.color.red));
                break;
        }

        FontUtil fontUtil = new FontUtil();
        fontUtil.changeFont(getActivity(), mText);
        mText.getPaint().setFakeBoldText(true);
        fontUtil.changeFont(getActivity(), mArtistText);
        fontUtil.changeFont(getActivity(), mSingleText);
        fontUtil.changeFont(getActivity(), mAddTimeText);
        fontUtil.changeFont(getActivity(), mAlbumText);
        fontUtil.changeFont(getActivity(), mPlayTimeText);

        builder.setView(view);
        return builder.create();
    }

    @OnClick({R.id.dialog_sort_by_artist, R.id.dialog_sort_by_single, R.id.dialog_sort_by_add_time, R.id.dialog_sort_by_album, R.id.dialog_sort_by_play_time})
    public void setSortWay(View view) {
        switch (view.getId()) {
            case R.id.dialog_sort_by_artist:
                Preferences.saveSortWay(SORT_BY_ARTIST);
                dismiss();
                break;

            case R.id.dialog_sort_by_single:
                Preferences.saveSortWay(SORT_BY_SINGLE);
                dismiss();
                break;

            case R.id.dialog_sort_by_add_time:
                Preferences.saveSortWay(SORT_BY_ADD_TIME);
                dismiss();
                break;

            case R.id.dialog_sort_by_album:
                Preferences.saveSortWay(SORT_BY_ALBUM);
                dismiss();
                break;

            case R.id.dialog_sort_by_play_time:
                Preferences.saveSortWay(SORT_BY_PLAY_TIME);
                dismiss();
                break;
        }
    }
}