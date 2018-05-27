package com.charles.funmusic.fragment;

import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.activity.PlayingActivity;
import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.service.MusicPlayer;
import com.charles.funmusic.utils.FileUtil;
import com.charles.funmusic.utils.HandlerUtil;
import com.charles.funmusic.utils.ToastUtil;
import com.charles.funmusic.widget.TintImageView;
import com.charles.funmusic.widget.TintProgressBar;
import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class QuickControlsFragment extends BaseFragment {

    @BindView(R.id.play_bar_cover)
    SimpleDraweeView mCover;
    @BindView(R.id.play_bar_progress_bar)
    TintProgressBar mProgress;
    @BindView(R.id.play_bar_play_or_pause)
    TintImageView mPlayPause;
    @BindView(R.id.play_bar_next)
    TintImageView mNext;
    @BindView(R.id.play_bar_artist)
    TextView mArtist;
    @BindView(R.id.play_bar_title)
    TextView mTitle;
    @BindView(R.id.play_bar_playlist)
    TintImageView mPlaylist;
    @BindView(R.id.play_bar)
    View mPlayBar;

    public Runnable mUpdateProgress = new Runnable() {
        @Override
        public void run() {
            long position = MusicPlayer.position();
            long duration = MusicPlayer.duration();
            if (duration > 0 && duration < 627080716) {
                mProgress.setProgress((int) (1000 * position / duration));
            }

            if (MusicPlayer.isPlaying()) {
                mProgress.postDelayed(mUpdateProgress, 50);
            } else {
                mProgress.removeCallbacks(mUpdateProgress);
            }
        }
    };

    public static QuickControlsFragment newInstance() {
        return new QuickControlsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.play_bar, container, false);
        ButterKnife.bind(this, view);

        init();

        return view;
    }

    private void init() {
//        mProgress.setProgressTintList(ThemeUtils.getThemeColorStateList(mContext, R.color.theme_color_primary));
        mProgress.postDelayed(mUpdateProgress, 0);

        changeFont(mTitle, false);
        changeFont(mArtist, false);
    }

    @OnClick({R.id.play_bar, R.id.play_bar_play_or_pause, R.id.play_bar_next, R.id.play_bar_playlist})
    public void doClick(View view) {
        switch (view.getId()) {
            case R.id.play_bar:
                Intent intent = new Intent(AppCache.getContext(), PlayingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                AppCache.getContext().startActivity(intent);
                break;

            case R.id.play_bar_play_or_pause:
                mPlayPause.setImageResource(MusicPlayer.isPlaying() ?
                        R.drawable.ic_play_bar_btn_pause_normal : R.drawable.ic_play_bar_btn_play_normal);
//                if (MusicPlayer.isPlaying()) {
//                    mPlayPause.setSelected(false);
//                } else {
//                    mPlayPause.setSelected(true);
//                }
//                mPlayPause.setImageTintList(R.color.theme_color_primary);

                if (MusicPlayer.getQueueSize() == 0) {
                    ToastUtil.show(getResources().getString(R.string.queue_is_empty));
                } else {
                    HandlerUtil.getInstance(AppCache.getContext()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MusicPlayer.playOrPause();
                        }
                    }, 60);
                }
                break;

            case R.id.play_bar_next:
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MusicPlayer.next();
                    }
                }, 60);
                break;

            case R.id.play_bar_playlist:
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        PlayQueueFragment playQueueFragment = new PlayQueueFragment();
                        playQueueFragment.show(getFragmentManager(), "play_queue_fragment");
                    }
                }, 60);
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mProgress.removeCallbacks(mUpdateProgress);
    }

    @Override
    public void onResume() {
        super.onResume();
        mProgress.setMax(1000);
        mProgress.removeCallbacks(mUpdateProgress);
        mProgress.postDelayed(mUpdateProgress, 0);
        updateNowPlayingCard();
    }

    private void updateNowPlayingCard() {
        if (MusicPlayer.getTrackName() != null) {
            mTitle.setText(MusicPlayer.getTrackName());
        } else {
            mTitle.setText(getString(R.string.app_name));
        }
        if (MusicPlayer.getArtist() != null) {
            mArtist.setText(FileUtil.getArtist(MusicPlayer.getArtist()));
        } else {
            mArtist.setText(getString(R.string.slogan));
        }
        ControllerListener<ImageInfo> controllerListener = new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                if (imageInfo == null) {
                    return;
                }
                QualityInfo qualityInfo = imageInfo.getQualityInfo();
                FLog.d("Final image received! " + "Size %d x %d",
                        "Quality level %d, good enough: %s, full quality: %s",
                        imageInfo.getWidth(), imageInfo.getHeight(), qualityInfo.getQuality(),
                        qualityInfo.isOfGoodEnoughQuality(), qualityInfo.isOfFullQuality());
            }

            @Override
            public void onIntermediateImageSet(String id, ImageInfo imageInfo) {
                // FLog.d("Intermediate image received");
            }

            @Override
            public void onFailure(String id, Throwable throwable) {
                mCover.setImageURI(Uri.parse("res:/" + R.drawable.ic_default_album_cover));
            }
        };
        Uri uri = null;
        try {
            uri = Uri.parse(MusicPlayer.getAlbumPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (uri != null) {
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri).build();

            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setOldController(mCover.getController())
                    .setImageRequest(request)
                    .setControllerListener(controllerListener)
                    .build();
            mCover.setController(controller);
        } else {
            mCover.setImageURI(Uri.parse("content://" + MusicPlayer.getAlbumPath()));
        }
    }

    @Override
    public void updateTrackInfo() {
        updateNowPlayingCard();
        updateState();
    }

    private void updateState() {
        if (MusicPlayer.isPlaying()) {
//            mPlayPause.setImageResource(R.drawable.ic_play_bar_btn_pause_normal);
//            mPlayPause.setImageTintList(R.color.theme_color_primary);
            mPlayPause.setSelected(false);
            mProgress.removeCallbacks(mUpdateProgress);
            mProgress.postDelayed(mUpdateProgress, 50);
        } else {
//            mPlayPause.setImageResource(R.drawable.ic_play_bar_btn_play_normal);
//            mPlayPause.setImageTintList(R.color.theme_color_primary);
            mPlayPause.setSelected(true);
            mProgress.removeCallbacks(mUpdateProgress);
        }
    }

    @Override
    public void changeTheme() {
        super.changeTheme();
//        mProgress.setProgressTintList(ThemeUtils.getThemeColorStateList(
//                mContext, R.color.theme_color_primary));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}