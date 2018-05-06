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
import android.view.ViewGroup;
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

public class AlbumSortFragment extends DialogFragment {

    private static final int SORT_BY_ALBUM = 0;
    private static final int SORT_BY_SONG_COUNT = 1;

    @BindView(R.id.dialog_sort_text)
    TextView mText;
    @BindView(R.id.dialog_sort_by_album_text)
    TextView mAlbumText;
    @BindView(R.id.dialog_sort_by_album_image)
    ImageView mAlbumImage;
    @BindView(R.id.dialog_sort_by_album)
    LinearLayout mAlbumView;
    @BindView(R.id.dialog_sort_by_song_count_text)
    TextView mSongCountText;
    @BindView(R.id.dialog_sort_by_song_count_image)
    ImageView mSongCountImage;
    @BindView(R.id.dialog_sort_by_song_count)
    LinearLayout mSongCountView;

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
        @SuppressLint("InflateParams") View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_album_sort, null);
        ButterKnife.bind(this, view);

        int sortWay = Preferences.getAlbumSortWay();

        switch (sortWay) {
            case SORT_BY_ALBUM:
                mAlbumImage.setVisibility(View.VISIBLE);
                mAlbumText.setTextColor(ContextCompat.getColor(AppCache.getContext(), R.color.theme_color_primary));
                break;

            case SORT_BY_SONG_COUNT:
                mSongCountImage.setVisibility(View.VISIBLE);
                mSongCountText.setTextColor(ContextCompat.getColor(AppCache.getContext(), R.color.theme_color_primary));
                break;
        }

        FontUtil fontUtil = new FontUtil();
        fontUtil.changeFont(getActivity(), mText);
        mText.getPaint().setFakeBoldText(false);
        fontUtil.changeFont(getActivity(), mAlbumText);
        mAlbumText.getPaint().setFakeBoldText(false);
        fontUtil.changeFont(getActivity(), mSongCountText);
        mSongCountText.getPaint().setFakeBoldText(false);

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @OnClick({R.id.dialog_sort_by_album, R.id.dialog_sort_by_song_count, R.id.dialog_sort_cancel})
    public void setSortWay(View view) {
        switch (view.getId()) {
            case R.id.dialog_sort_by_album:
                Preferences.saveAlbumSortWay(SORT_BY_ALBUM);
                mPreferences.saveAlbumSortOrder(SortOrder.AlbumSortOrder.ALBUM_A_Z);
                dismiss();
                break;

            case R.id.dialog_sort_by_song_count:
                Preferences.saveAlbumSortWay(SORT_BY_SONG_COUNT);
                mPreferences.saveAlbumSortOrder(SortOrder.AlbumSortOrder.ALBUM_NUMBER_OF_SONGS);
                dismiss();
                break;

            case R.id.dialog_sort_cancel:
                dismiss();
                break;
        }
    }
}
