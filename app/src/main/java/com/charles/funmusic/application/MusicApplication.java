package com.charles.funmusic.application;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;

import com.charles.funmusic.R;
import com.charles.funmusic.http.HttpInterceptor;
import com.charles.funmusic.premission.Permission;
import com.charles.funmusic.utils.ThemeHelper;
import com.charles.funmusic.utils.ThemeUtils;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class MusicApplication extends Application
//        implements ThemeUtils.switchColor
{

    private static int MAX_MEM = (int) Runtime.getRuntime().maxMemory() / 4;

    @SuppressLint("NewApi")
    @Override
    public void onCreate() {
        frescoInit();
        super.onCreate();

        AppCache.init(this);
        initOkHttpUtils();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Permission.init(this);
        }
//        ThemeUtils.setSwitchColor(this);
    }

    private void frescoInit() {
        Fresco.initialize(this, getConfigureCaches(this));
    }


    private ImagePipelineConfig getConfigureCaches(Context context) {
        final MemoryCacheParams bitmapCacheParams = new MemoryCacheParams(
                MAX_MEM,// 内存缓存中总图片的最大大小,以字节为单位。
                Integer.MAX_VALUE,// 内存缓存中图片的最大数量。
                MAX_MEM,// 内存缓存中准备清除但尚未被删除的总图片的最大大小,以字节为单位。
                Integer.MAX_VALUE,// 内存缓存中准备清除的总图片的最大数量。
                Integer.MAX_VALUE / 10);// 内存缓存中单个图片的最大大小。

        Supplier<MemoryCacheParams> mSupplierMemoryCacheParams = new Supplier<MemoryCacheParams>() {
            @Override
            public MemoryCacheParams get() {
                return bitmapCacheParams;
            }
        };
        ImagePipelineConfig.Builder builder = ImagePipelineConfig.newBuilder(context)
                .setDownsampleEnabled(true);
        builder.setBitmapMemoryCacheParamsSupplier(mSupplierMemoryCacheParams);


        //小图片的磁盘配置
        DiskCacheConfig diskSmallCacheConfig = DiskCacheConfig.newBuilder(context)
                .setBaseDirectoryPath(context.getApplicationContext().getCacheDir())//缓存图片基路径
//                .setBaseDirectoryName(IMAGE_PIPELINE_SMALL_CACHE_DIR)//文件夹名
//            .setCacheErrorLogger(cacheErrorLogger)//日志记录器用于日志错误的缓存。
//            .setCacheEventListener(cacheEventListener)//缓存事件侦听器。
//            .setDiskTrimmableRegistry(diskTrimmableRegistry)//类将包含一个注册表的缓存减少磁盘空间的环境。
//                .setMaxCacheSize(ConfigConstants.MAX_DISK_CACHE_SIZE)//默认缓存的最大大小。
//                .setMaxCacheSizeOnLowDiskSpace(MAX_SMALL_DISK_LOW_CACHE_SIZE)//缓存的最大大小,使用设备时低磁盘空间。
//                .setMaxCacheSizeOnVeryLowDiskSpace(MAX_SMALL_DISK_VERYLOW_CACHE_SIZE)//缓存的最大大小,当设备极低磁盘空间
//            .setVersion(version)
                .build();

        //默认图片的磁盘配置
        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(context)
                .setBaseDirectoryPath(Environment.getExternalStorageDirectory().getAbsoluteFile())//缓存图片基路径
//                .setBaseDirectoryName(IMAGE_PIPELINE_CACHE_DIR)//文件夹名
//            .setCacheErrorLogger(cacheErrorLogger)//日志记录器用于日志错误的缓存。
//            .setCacheEventListener(cacheEventListener)//缓存事件侦听器。
//            .setDiskTrimmableRegistry(diskTrimmableRegistry)//类将包含一个注册表的缓存减少磁盘空间的环境。
//                .setMaxCacheSize(ConfigConstants.MAX_DISK_CACHE_SIZE)//默认缓存的最大大小。
//                .setMaxCacheSizeOnLowDiskSpace(MAX_DISK_CACHE_LOW_SIZE)//缓存的最大大小,使用设备时低磁盘空间。
//                .setMaxCacheSizeOnVeryLowDiskSpace(MAX_DISK_CACHE_VERYLOW_SIZE)//缓存的最大大小,当设备极低磁盘空间
//            .setVersion(version)
                .build();

        //缓存图片配置
        ImagePipelineConfig.Builder configBuilder = ImagePipelineConfig.newBuilder(context)
//            .setAnimatedImageFactory(AnimatedImageFactory animatedImageFactory)//图片加载动画
                .setBitmapMemoryCacheParamsSupplier(mSupplierMemoryCacheParams)//内存缓存配置（一级缓存，已解码的图片）
//            .setCacheKeyFactory(cacheKeyFactory)//缓存Key工厂
//            .setEncodedMemoryCacheParamsSupplier(encodedCacheParamsSupplier)//内存缓存和未解码的内存缓存的配置（二级缓存）
//            .setExecutorSupplier(executorSupplier)//线程池配置
//            .setImageCacheStatsTracker(imageCacheStatsTracker)//统计缓存的命中率
//            .setImageDecoder(ImageDecoder imageDecoder) //图片解码器配置
//            .setIsPrefetchEnabledSupplier(Supplier<Boolean> isPrefetchEnabledSupplier)//图片预览（缩略图，预加载图等）预加载到文件缓存
                .setMainDiskCacheConfig(diskCacheConfig)//磁盘缓存配置（总，三级缓存）
//            .setMemoryTrimmableRegistry(memoryTrimmableRegistry) //内存用量的缩减,有时我们可能会想缩小内存用量。比如应用中有其他数据需要占用内存，不得不把图片缓存清除或者减小 或者我们想检查看看手机是否已经内存不够了。
//            .setNetworkFetchProducer(networkFetchProducer)//自定的网络层配置：如OkHttp，Volley
//            .setPoolFactory(poolFactory)//线程池工厂配置
//            .setProgressiveJpegConfig(progressiveJpegConfig)//渐进式JPEG图
//            .setRequestListeners(requestListeners)//图片请求监听
//            .setResizeAndRotateEnabledForNetwork(boolean resizeAndRotateEnabledForNetwork)//调整和旋转是否支持网络图片
                ;
        return builder.build();
    }

    private void initOkHttpUtils() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(new HttpInterceptor())
                .build();
        OkHttpUtils.initClient(okHttpClient);
    }

//    @Override
//    public int replaceColorById(Context context, @ColorRes int colorId) {
//        if (ThemeHelper.isDefaultTheme(context)) {
//            return context.getResources().getColor(colorId);
//        }
//        String theme = getTheme(context);
//        if (theme != null) {
//            colorId = getThemeColorId(context, colorId, theme);
//        }
//        return context.getResources().getColor(colorId);
//    }
//
//    @Override
//    public int replaceColor(Context context, @ColorInt int originColor) {
//        if (ThemeHelper.isDefaultTheme(context)) {
//            return originColor;
//        }
//        String theme = getTheme(context);
//        int colorId = -1;
//        if (theme != null) {
//            colorId = getThemeColor(context, originColor, theme);
//        }
//        return colorId != -1 ? getResources().getColor(colorId) : originColor;
//    }
//
//    private String getTheme(Context context) {
//        if (ThemeHelper.getTheme(context) == ThemeHelper.CARD_STORM) {
//            return "blue";
//        } else if (ThemeHelper.getTheme(context) == ThemeHelper.CARD_HOPE) {
//            return "purple";
//        } else if (ThemeHelper.getTheme(context) == ThemeHelper.CARD_WOOD) {
//            return "green";
//        } else if (ThemeHelper.getTheme(context) == ThemeHelper.CARD_SAND) {
//            return "orange";
//        } else if (ThemeHelper.getTheme(context) == ThemeHelper.CARD_FIREY) {
//            return "red";
//        }
//        return null;
//    }
//
//    private
//    @ColorRes
//    int getThemeColorId(Context context, int colorId, String theme) {
//        switch (colorId) {
//            case R.color.theme_color_primary:
//                return context.getResources().getIdentifier(theme, "color", getPackageName());
//            case R.color.theme_color_primary_dark:
//                return context.getResources().getIdentifier(theme + "_dark", "color", getPackageName());
//            case R.color.play_bar_progress_color:
//                return context.getResources().getIdentifier(theme + "_trans", "color", getPackageName());
//        }
//        return colorId;
//    }
//
//    private
//    @ColorRes
//    int getThemeColor(Context context, int color, String theme) {
//        switch (color) {
//            case 0xd20000:
//                return context.getResources().getIdentifier(theme, "color", getPackageName());
//        }
//        return -1;
//    }

}
