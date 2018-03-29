package com.charles.funmusic.fragment;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import com.charles.funmusic.R;
import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RoundFragment extends Fragment {

    @BindView(R.id.fragment_round_simple_drawee_view)
    SimpleDraweeView mSimpleDraweeView;

    private ObjectAnimator mAnimator;
    private String mAlbumPath;

    public static RoundFragment newInstance(String albumPath) {
        RoundFragment fragment = new RoundFragment();
        Bundle bundle = new Bundle();
        bundle.putString("album", albumPath);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_round, container, false);

        ButterKnife.bind(this, view);

        ((ViewGroup) view).setAnimationCacheEnabled(false);
        if (getArguments() != null) {
            mAlbumPath = getArguments().getString("album");
        }

        // 初始化圆角圆形参数对象
        RoundingParams rp = new RoundingParams();
        // 设置图像是否为圆形
        rp.setRoundAsCircle(true);
        // 设置圆角半径
//        rp.setCornersRadius(20);
        // 分别设置左上角、右上角、左下角、右下角的圆角半径
//        rp.setCornersRadii(20, 25, 30, 35);
        // 分别设置（前2个）左上角、(3、4)右上角、(5、6)左下角、(7、8)右下角的圆角半径
//        rp.setCornersRadii(new float[]{20, 25, 30, 35, 40, 45, 50, 55});
        // 设置边框颜色及其宽度
        rp.setBorder(Color.BLACK, 6);

        // 获取GenericDraweeHierarchy对象
        GenericDraweeHierarchy hierarchy = GenericDraweeHierarchyBuilder.newInstance(getResources())
                // 设置圆形圆角参数
                .setRoundingParams(rp)
                // 设置圆角半径
//                .setRoundingParams(RoundingParams.fromCornersRadius(20))
                // 分别设置左上角、右上角、左下角、右下角的圆角半径
//                .setRoundingParams(RoundingParams.fromCornersRadii(20, 25, 30, 35))
                // 分别设置（前2个）左上角、(3、4)右上角、(5、6)左下角、(7、8)右下角的圆角半径
//                .setRoundingParams(RoundingParams.fromCornersRadii(new float[]{20, 25, 30, 35, 40, 45, 50, 55}))
                // 设置圆形圆角参数；RoundingParams.asCircle()是将图像设置成圆形
//                .setRoundingParams(RoundingParams.asCircle())
                // 设置淡入淡出动画持续时间(单位：毫秒ms)
                .setFadeDuration(300)
                // 构建
                .build();

        // 设置Hierarchy
        mSimpleDraweeView.setHierarchy(hierarchy);

        ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                if (imageInfo == null) {
                    return;
                }
                QualityInfo qualityInfo = imageInfo.getQualityInfo();
                FLog.d("Final image received! " +
                                "Size %d x %d",
                        "Quality level %d, good enough: %s, full quality: %s",
                        imageInfo.getWidth(),
                        imageInfo.getHeight(),
                        qualityInfo.isOfGoodEnoughQuality(),
                        qualityInfo.isOfFullQuality());
            }

            @Override
            public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
                // FLog.d("Intermediate image received");
            }

            @Override
            public void onFailure(String id, Throwable throwable) {
                mSimpleDraweeView.setImageURI(Uri.parse("res:/" + R.drawable.play_page_default_cover));
            }
        };

        if (mAlbumPath == null) {
            mSimpleDraweeView.setImageURI(Uri.parse("res:/" + R.drawable.play_page_default_cover));
        } else {
            try {
                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(
                        Uri.parse(mAlbumPath)).build();

                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setOldController(mSimpleDraweeView.getController())
                        .setImageRequest(request)
                        .setControllerListener(controllerListener)
                        .build();

                mSimpleDraweeView.setController(controller);
            } catch (Exception ignored) {
            }
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        WeakReference<ObjectAnimator> animatorWeakReference = new WeakReference<>(ObjectAnimator.ofFloat(getView(), "rotation", 0.0F, 360.0F));
        mAnimator = animatorWeakReference.get();
        mAnimator.setRepeatCount(Integer.MAX_VALUE);
        mAnimator.setDuration(25000L);
        mAnimator.setInterpolator(new LinearInterpolator());

        if (getView() != null) {
            getView().setTag(R.id.tag_animator, mAnimator);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            // 相当于Fragment的onResume

        } else {
            // 相当于Fragment的onPause

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("RoundFragment", " id = " + hashCode());
        if (mAnimator != null) {
            mAnimator = null;
            Log.e("RoundFragment", " id = " + hashCode() + "  , animator destroy");
        }
    }
}
