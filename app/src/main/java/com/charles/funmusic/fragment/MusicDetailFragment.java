package com.charles.funmusic.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.model.Music;
import com.charles.funmusic.utils.FileUtil;
import com.charles.funmusic.utils.MusicUtil;

import java.io.File;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MusicDetailFragment extends AttachDialogFragment {

    @BindView(R.id.fragment_music_detail_title)
    TextView mTitle;
    @BindView(R.id.fragment_music_detail_file_name)
    TextView mFileName;
    @BindView(R.id.fragment_music_detail_file_duration)
    TextView mFileDuration;
    @BindView(R.id.fragment_music_detail_file_size)
    TextView mFileSize;
    @BindView(R.id.fragment_music_detail_file_url)
    TextView mFileUrl;

    private Music mMusic;

    public static MusicDetailFragment newInstance(Music music) {

        MusicDetailFragment fragment = new MusicDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable("music", music);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置样式
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomDatePickerDialog);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_detail, container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    public void init() {
        // 设置无标题
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 设置从底部弹出
        if (getDialog().getWindow() != null) {
            WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
            params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setAttributes(params);
        }
        if (getArguments() != null) {
            mMusic = getArguments().getParcelable("music");
        }

        mTitle.setText(mMusic.getTitle());
        String[] str = mMusic.getUrl().split("/");
        mFileName.setText(str[str.length - 1]);
        mFileDuration.setText(MusicUtil.makeShortTimeString(mContext, mMusic.getDuration() / 1000));
        String fileSize = FileUtil.b2mb(mMusic.getFileSize()) + "M";
        mFileSize.setText(fileSize);

        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < str.length - 1; i++) {
            builder.append("/").append(str[i]);
        }
        mFileUrl.setText(builder);
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置fragment高度、宽度
        int dialogHeight = (int) (mContext.getResources().getDisplayMetrics().heightPixels * 0.35);
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            getDialog().setCanceledOnTouchOutside(true);
        }
    }
}