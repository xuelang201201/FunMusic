package com.charles.funmusic.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.utils.FontUtil;
import com.charles.funmusic.utils.Preferences;
import com.charles.funmusic.utils.SortOrder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SortDialogFragment extends DialogFragment {

    public static final int SORT_BY_ARTIST = 0;
    public static final int SORT_BY_SINGLE = 1;
    public static final int SORT_BY_ADD_TIME = 2;
    public static final int SORT_BY_ALBUM = 3;
    public static final int SORT_BY_PLAY_TIME = 4;

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

    Preferences mPreferences;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = Preferences.getInstance(AppCache.getContext());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Dialog_Full_Screen);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_sort, null);
        ButterKnife.bind(this, view);

        int sortWay = Preferences.getSortWay();

        switch (sortWay) {
            case SORT_BY_SINGLE:
                mSingleImage.setVisibility(View.VISIBLE);
                mSingleText.setTextColor(ContextCompat.getColor(AppCache.getContext(), R.color.theme_color_primary));
                break;

            case SORT_BY_ADD_TIME:
                mAddTimeImage.setVisibility(View.VISIBLE);
                mAddTimeText.setTextColor(ContextCompat.getColor(AppCache.getContext(), R.color.theme_color_primary));
                break;

            case SORT_BY_ALBUM:
                mAlbumImage.setVisibility(View.VISIBLE);
                mAlbumText.setTextColor(ContextCompat.getColor(AppCache.getContext(), R.color.theme_color_primary));
                break;

            case SORT_BY_PLAY_TIME:
                mPlayTimeImage.setVisibility(View.VISIBLE);
                mPlayTimeText.setTextColor(ContextCompat.getColor(AppCache.getContext(), R.color.theme_color_primary));
                break;

            case SORT_BY_ARTIST:
            default:
                mArtistImage.setVisibility(View.VISIBLE);
                mArtistText.setTextColor(ContextCompat.getColor(AppCache.getContext(), R.color.theme_color_primary));
                break;
        }

        FontUtil fontUtil = new FontUtil();
        fontUtil.changeFont(getActivity(), mText);
        mText.getPaint().setFakeBoldText(true);
        fontUtil.changeFont(getActivity(), mArtistText);
        mArtistText.getPaint().setFakeBoldText(true);
        fontUtil.changeFont(getActivity(), mSingleText);
        mSingleText.getPaint().setFakeBoldText(true);
        fontUtil.changeFont(getActivity(), mAddTimeText);
        mAddTimeText.getPaint().setFakeBoldText(true);
        fontUtil.changeFont(getActivity(), mAlbumText);
        mAlbumText.getPaint().setFakeBoldText(true);
        fontUtil.changeFont(getActivity(), mPlayTimeText);
        mPlayTimeText.getPaint().setFakeBoldText(true);

        AlertDialog dialog = builder.setView(view).create();
        Window window = dialog.getWindow();

        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(params);

            window.setGravity(Gravity.BOTTOM);
            window.setWindowAnimations(R.style.Dialog_Bottom);
        }

        return dialog;
    }

    @OnClick({R.id.dialog_sort_by_artist, R.id.dialog_sort_by_single,
            R.id.dialog_sort_by_add_time, R.id.dialog_sort_by_album,
            R.id.dialog_sort_by_play_time, R.id.dialog_sort_cancel})
    public void setSortWay(View view) {
        switch (view.getId()) {
            case R.id.dialog_sort_by_artist:
                Preferences.saveSortWay(SORT_BY_ARTIST);
                mPreferences.saveArtistSortOrder(SortOrder.SongSortOrder.SONG_ARTIST);

                dismiss();
                break;

            case R.id.dialog_sort_by_single:
                Preferences.saveSortWay(SORT_BY_SINGLE);
                mPreferences.saveSongSortOrder(SortOrder.SongSortOrder.SONG_A_Z);
                dismiss();
                break;

            case R.id.dialog_sort_by_add_time:
                Preferences.saveSortWay(SORT_BY_ADD_TIME);
                mPreferences.saveAlbumSortOrder(SortOrder.SongSortOrder.SONG_DATE);
                dismiss();
                break;

            case R.id.dialog_sort_by_album:
                Preferences.saveSortWay(SORT_BY_ALBUM);
                mPreferences.saveAlbumSortOrder(SortOrder.SongSortOrder.SONG_ALBUM);
                dismiss();
                break;

            case R.id.dialog_sort_by_play_time:
                Preferences.saveSortWay(SORT_BY_PLAY_TIME);
                dismiss();
                break;

            case R.id.dialog_sort_cancel:
                dismiss();
                break;
        }
    }
}