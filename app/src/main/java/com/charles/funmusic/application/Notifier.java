package com.charles.funmusic.application;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.charles.funmusic.R;
import com.charles.funmusic.activity.SplashActivity;
import com.charles.funmusic.constant.Extra;
import com.charles.funmusic.model.Music;
import com.charles.funmusic.receiver.StatusBarReceiver;
import com.charles.funmusic.service.PlayService;
import com.charles.funmusic.utils.CoverLoader;
import com.charles.funmusic.utils.FileUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 通知栏
 */
public class Notifier {
    private static final int NOTIFICATION_ID = 0x111;
    private static PlayService sPlayService;
    private static NotificationManager sNotificationManager;

    public static void init(PlayService playService) {
        Notifier.sPlayService = playService;
        sNotificationManager = (NotificationManager) playService.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void showPlay(Music music) {
        sPlayService.startForeground(NOTIFICATION_ID, buildNotification(sPlayService, music, true));
    }

    public static void showPause(Music music) {
        sPlayService.stopForeground(false);
        sNotificationManager.notify(NOTIFICATION_ID, buildNotification(sPlayService, music, false));
    }

    public static void cancelAll() {
        sNotificationManager.cancelAll();
    }

    private static Notification buildNotification(Context context, Music music, boolean isPlaying) {
        Intent intent = new Intent(context, SplashActivity.class);
        intent.putExtra(Extra.EXTRA_NOTIFICATION, true);
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "notifier")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_notification)
                .setCustomContentView(getRemoteViews(context, music, isPlaying));
        return builder.build();
    }

    private static RemoteViews getRemoteViews(Context context, Music music, boolean isPlaying) {

        String artist;
        String album;

        if ("<unknown>".equals(music.getArtist())) {
            artist = context.getString(R.string.unknown_artist);
        } else {
            artist = music.getArtist();
        }
        if ("Music".equals(music.getAlbum()) || "0".equals(music.getAlbum())) {
            album = context.getString(R.string.unknown_album);
        } else {
            album = music.getAlbum();
        }
        String title = music.getTitle();
        String subtitle = FileUtil.getArtistAndAlbum(artist, album);
        Bitmap cover = CoverLoader.getInstance().loadThumbnail(music);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification);
        if (cover != null) {
            remoteViews.setImageViewBitmap(R.id.notification_icon, cover);
        } else {
            remoteViews.setImageViewResource(R.id.notification_icon, R.drawable.ic_launcher);
        }
        remoteViews.setTextViewText(R.id.notification_title, title);
        remoteViews.setTextViewText(R.id.notification_subtitle, subtitle);

        boolean isLightNotificationTheme = isLightNotificationTheme(sPlayService);

        Intent playIntent = new Intent(StatusBarReceiver.ACTION_STATUS_BAR);
        playIntent.putExtra(StatusBarReceiver.EXTRA, StatusBarReceiver.EXTRA_PLAY_PAUSE);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setImageViewResource(R.id.notification_play_or_pause, getPlayIconRes(isLightNotificationTheme ,isPlaying));
        remoteViews.setOnClickPendingIntent(R.id.notification_play_or_pause, playPendingIntent);

        Intent nextIntent = new Intent(StatusBarReceiver.ACTION_STATUS_BAR);
        nextIntent.putExtra(StatusBarReceiver.EXTRA, StatusBarReceiver.EXTRA_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, 1, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setImageViewResource(R.id.notification_next, getNextIconRes(isLightNotificationTheme));
        remoteViews.setOnClickPendingIntent(R.id.notification_next, nextPendingIntent);

        Intent exitIntent = new Intent(StatusBarReceiver.ACTION_STATUS_BAR);
        exitIntent.putExtra(StatusBarReceiver.EXTRA, StatusBarReceiver.EXTRA_EXIT);
        PendingIntent exitPendingIntent = PendingIntent.getBroadcast(context, 2, exitIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setImageViewResource(R.id.notification_exit, R.drawable.ic_close);
        remoteViews.setOnClickPendingIntent(R.id.notification_exit, exitPendingIntent);

        return remoteViews;
    }

    private static int getPlayIconRes(boolean isLightNotificationTheme, boolean isPlaying) {
        if (isPlaying) {
            return isLightNotificationTheme
                    ? R.drawable.selector_status_bar_pause_dark
                    : R.drawable.selector_status_bar_pause_light;
        } else {
            return isLightNotificationTheme
                    ? R.drawable.selector_status_bar_play_dark
                    : R.drawable.selector_status_bar_play_light;
        }
    }

    private static int getNextIconRes(boolean isLightNotificationTheme) {
        return isLightNotificationTheme
                ? R.drawable.selector_status_bar_next_dark
                : R.drawable.selector_status_bar_next_light;
    }

    private static boolean isLightNotificationTheme(Context context) {
        int notificationTextColor = getNotificationTextColor(context);
        return isSimilarColor(Color.BLACK, notificationTextColor);
    }

    private static int getNotificationTextColor(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "notifier");
        Notification notification = builder.build();
        RemoteViews remoteViews = notification.contentView;
        if (remoteViews == null) {
            return Color.BLACK;
        }
        int layoutId = remoteViews.getLayoutId();
        ViewGroup notificationLayout = (ViewGroup) LayoutInflater.from(context).inflate(layoutId, null);
        TextView title = notificationLayout.findViewById(android.R.id.title);
        if (title != null) {
            return title.getCurrentTextColor();
        } else {
            return findTextColor(notificationLayout);
        }
    }

    /**
     * 如果通过 android.R.id.title 无法获得 title ，
     * 则通过遍历 notification 布局找到 textSize 最大的 TextView ，应该就是 title 了。
     */
    private static int findTextColor(ViewGroup notificationLayout) {
        List<TextView> textViewList = new ArrayList<>();
        findTextView(notificationLayout, textViewList);

        float maxTextSize = -1;
        TextView maxTextView = null;
        for (TextView textView : textViewList) {
            if (textView.getTextSize() > maxTextSize) {
                maxTextView = textView;
            }
        }

        if (maxTextView != null) {
            return maxTextView.getCurrentTextColor();
        }

        return Color.BLACK;
    }

    private static void findTextView(View view, List<TextView> textViewList) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                findTextView(viewGroup.getChildAt(i), textViewList);
            }
        } else if (view instanceof TextView) {
            textViewList.add((TextView) view);
        }
    }

    private static boolean isSimilarColor(int baseColor, int color) {
        int simpleBaseColor = baseColor | 0xff000000;
        int simpleColor = color | 0xff000000;
        int baseRed = Color.red(simpleBaseColor) - Color.red(simpleColor);
        int baseGreen = Color.green(simpleBaseColor) - Color.green(simpleColor);
        int baseBlue = Color.blue(simpleBaseColor) - Color.blue(simpleColor);
        double value = Math.sqrt(baseRed * baseRed + baseGreen * baseGreen + baseBlue * baseBlue);
        return value < 180.0;
    }
}
