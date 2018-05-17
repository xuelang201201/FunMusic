package com.charles.funmusic.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AlbumColumns;
import android.provider.MediaStore.Audio.Albums;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.Audio.Media;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.charles.funmusic.IFunMusicService;
import com.charles.funmusic.R;
import com.charles.funmusic.activity.LockActivity;
import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.download.Download;
import com.charles.funmusic.helper.MusicPlaybackTrack;
import com.charles.funmusic.json.MusicFileDownInfo;
import com.charles.funmusic.model.Music;
import com.charles.funmusic.net.BMA;
import com.charles.funmusic.net.HttpUtil;
import com.charles.funmusic.premission.Permission;
import com.charles.funmusic.provider.MusicPlaybackState;
import com.charles.funmusic.provider.RecentStore;
import com.charles.funmusic.provider.SongPlayCount;
import com.charles.funmusic.proxy.utils.MusicPlayerProxy;
import com.charles.funmusic.receiver.MediaButtonIntentReceiver;
import com.charles.funmusic.utils.ImageUtil;
import com.charles.funmusic.utils.Preferences;
import com.charles.funmusic.utils.SystemUtil;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;

@SuppressLint("NewApi")
public class MusicService extends Service {
    public static final String PLAY_STATE_CHANGED = "com.charles.funmusic.play_state_changed";
    public static final String POSITION_CHANGED = "com.charles.funmusic.position_changed";
    public static final String META_CHANGED = "com.charles.funmusic.meta_changed";
    public static final String QUEUE_CHANGED = "com.charles.funmusic.queue_changed";
    public static final String PLAYLIST_CHANGED = "com.charles.funmusic.playlist_changed";
    public static final String REPEAT_MODE_CHANGED = "com.charles.funmusic.repeat_mode_changed";
    public static final String SHUFFLE_MODE_CHANGED = "com.charles.funmusic.shuffle_mode_changed";
    public static final String TRACK_ERROR = "com.charles.funmusic.track_error";
    public static final String FUN_MUSIC_PACKAGE_NAME = "com.charles.funmusic";
    public static final String MUSIC_PACKAGE_NAME = "com.android.music";
    public static final String SERVICE_COMMAND = "com.charles.funmusic.music_service_command";
    public static final String ACTION_TOGGLE_PAUSE = "com.charles.funmusic.toggle_pause";
    public static final String ACTION_STOP = "com.charles.funmusic.stop";
    public static final String ACTION_NEXT = "com.charles.funmusic.next";
    public static final String ACTION_PAUSE = "com.charles.funmusic.pause";
    public static final String ACTION_PREVIOUS = "com.charles.funmusic.previous";
    public static final String ACTION_PREVIOUS_FORCE = "com.charles.funmusic.previous_force";
    public static final String ACTION_REPEAT = "com.charles.funmusic.repeat";
    public static final String ACTION_SHUFFLE = "com.charles.funmusic.shuffle";
    public static final String MUSIC_CHANGED = "com.charles.funmusic.music_changed";
    public static final String FROM_MEDIA_BUTTON = "com.charles.funmusic.from_media_button";
    public static final String REFRESH = "com.charles.funmusic.refresh";
    public static final String LRC_UPDATED = "com.charles.update_lrc";
    public static final String UPDATE_LOCKSCREEN = "com.charles.update_lock_screen";
    public static final String CMD_NAME = "command";
    public static final String CMD_TOGGLE_PAUSE = "toggle_pause";
    public static final String CMD_STOP = "mStop";
    public static final String CMD_PAUSE = "pause";
    public static final String CMD_PLAY = "play";
    public static final String CMD_PREVIOUS = "previous";
    public static final String CMD_NEXT = "next";
    public static final String CMD_NOTIF = "buttonId";
    public static final String TRACK_PREPARED = "com.charles.funmusic.prepared";
    public static final String TRY_GET_TRACK_INFO = "com.charles.funmusic.get_track_info";
    public static final String BUFFER_UP = "com.charles.funmusic.buffer_up";
    public static final String LOCK_SCREEN = "com.charles.funmusic.lock";
    public static final String MUSIC_LOADING = "com.charles.funmusic.loading";
    public static final String SHUTDOWN = "com.charles.funmusic.shutdown";
    public static final String SET_QUEUE = "com.charles.funmusic.set_queue";
    public static final String SEND_PROGRESS = "com.charles.funmusic.send_progress";

    public static final int NEXT = 2;
    public static final int LAST = 3;
    public static final int SHUFFLE_NONE = 0;
    public static final int SHUFFLE_NORMAL = 1;
    public static final int SHUFFLE_AUTO = 2;
    public static final int REPEAT_NONE = 2;
    public static final int REPEAT_CURRENT = 1;
    public static final int REPEAT_ALL = 2;
    public static final int MAX_HISTORY_SIZE = 1000;

    private static final int LRC_DOWNLOADED = -10;
    private static final int ID_COL_IDX = 0;
    private static final int TRACK_ENDED = 1;
    private static final int TRACK_WENT_TO_NEXT = 2;
    private static final int RELEASE_WAKELOCK = 3;
    private static final int SERVER_DIED = 4;
    private static final int FOCUS_CHANGE = 5;
    private static final int FADE_DOWN = 6;
    private static final int FADE_UP = 7;
    private static final int IDLE_DELAY = 5 * 60 * 1000;
    private static final int REWIND_INSTEAD_PREVIOUS_THRESHOLD = 3000;
    private static final boolean D = false;
    private static final String TAG = "MusicPlaybackService";

    private static LinkedList<Integer> mHistory = new LinkedList<>();
    private final IBinder mBinder = new ServiceStub(this);
    private MultiPlayer mPlayer;
    private String mFileToPlay;
    private PowerManager.WakeLock mWakeLock;
    private AlarmManager mAlarmManager;
    private PendingIntent mShutdownIntent;
    private boolean mShutdownScheduled;
    private NotificationManager mNotificationManager;
    private Cursor mCursor, mAlbumCursor;
    private AudioManager mAudioManager;
    private SharedPreferences mPreferences;
    private boolean mServiceInUse = false;
    private boolean mIsSupposedToBePlaying = false;
    private long mLastPlayedTime;
    private int mNotifyMode = NOTIFY_MODE_NONE;
    private long mNotificationPostTime = 0;
    private boolean mQueueIsSavable = true;
    private boolean mPausedByTransientLossOfFocus = false;

    private static final Shuffler mShuffler = new Shuffler();
    private static final int NOTIFY_MODE_NONE = 0;
    private static final int NOTIFY_MODE_FOREGROUND = 1;
    private static final int NOTIFY_MODE_BACKGROUND = 2;

    private int mPlayPos = -1;
    private int mNextPlayPos = -1;
    private int mOpenFailedCounter = 0;
    private int mMediaMountedCount = 0;
    private int mShuffleMode = SHUFFLE_NONE;
    private int mRepeatMode = REPEAT_NONE;
    private int mServiceStartId = -1;
    private MediaSession mSession;
    private int mCardId;

    private ArrayList<MusicPlaybackTrack> mPlaylist = new ArrayList<>(100);
    @SuppressLint("UseSparseArrays")
    private HashMap<Long, Music> mPlaylistInfo = new HashMap<>();
    private long[] mAutoShuffleList = null;
    private MusicPlayerHandler mPlayerHandler;
    private HandlerThread mHandlerThread;
    private BroadcastReceiver mUnMountReceiver = null;
    private MusicPlaybackState mPlaybackStateStore;
    private boolean mShowAlbumArtOnLockScreen;
    private SongPlayCount mSongPlayCount;
    private RecentStore mRecentStore;

    private int mNotificationId = hashCode();
    private ContentObserver mMediaStoreObserver;
    private static Handler mUrlHandler, mLrcHandler;
    private MusicPlayerProxy mProxy;
    public static final String LRC_PATH = "/funmusic/lrc/";
    private long mLastSeekPos = 0;
    private RequestPlayUrl mRequestUrl;
    private RequestLrc mRequestLrc;
    private boolean mIsSending = false;
    private boolean mIsLocked;
    private Bitmap mNoBit;

    private Notification mNotification;

    private Thread mLrcThread = new Thread(new Runnable() {
        @Override
        public void run() {
            Looper.prepare();
            mLrcHandler = new Handler();
            Looper.loop();
        }
    });

    private Thread mGetUrlThread = new Thread(new Runnable() {
        @Override
        public void run() {
            Looper.prepare();
            mUrlHandler = new Handler();
            Looper.loop();
        }
    });

    private static final String[] PROJECTION = new String[]{
            "audio._id AS _id", Media.ARTIST, Media.ALBUM, Media.TITLE,
            Media.DATA, Media.MIME_TYPE, Media.ALBUM_ID, Media.ARTIST_ID
    };

    private static final String[] ALBUM_PROJECTION = new String[]{
            Albums.ALBUM, Albums.ARTIST, Albums.LAST_YEAR
    };

    private static final String[] PROJECTION_MATRIX = new String[]{
            "_id", Media.ARTIST, Media.ALBUM, Media.TITLE, Media.DATA,
            Media.MIME_TYPE, Media.ALBUM_ID, Media.ARTIST_ID
    };

    private AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            mPlayerHandler.obtainMessage(FOCUS_CHANGE, focusChange, 0).sendToTarget();
        }
    };
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String command = intent.getStringExtra(CMD_NAME);

            Log.d(TAG, "onReceive " + command + " " + intent.toUri(0));
            handleCommandIntent(intent);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        if (D) {
            Log.d(TAG, "Service bound, intent = " + intent);
        }
        cancelShutdown();
        mServiceInUse = true;
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (D) {
            Log.d(TAG, "Service unbound");
        }
        mServiceInUse = false;
        saveQueue(true);

        if (mIsSupposedToBePlaying || mPausedByTransientLossOfFocus) {
            return true;
        } else if (mPlaylist.size() > 0 || mPlayerHandler.hasMessages(TRACK_ENDED)) {
            scheduleDelayedShutdown();
            return true;
        }
        stopSelf(mServiceStartId);

        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        cancelShutdown();
        mServiceInUse = true;
    }

    @Override
    public void onCreate() {
        if (D) {
            Log.d(TAG, "Creating service");
        }
        super.onCreate();
        mGetUrlThread.start();
        mLrcThread.start();
        mProxy = new MusicPlayerProxy(this);
        mProxy.init();
        mProxy.start();

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // gets a pointer to the playback state store
        mPlaybackStateStore = MusicPlaybackState.getInstance(this);
        mSongPlayCount = SongPlayCount.getInstance(this);
        mRecentStore = RecentStore.getInstance(this);

        mHandlerThread = new HandlerThread("MusicPlayerHandler",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();

        mPlayerHandler = new MusicPlayerHandler(this, mHandlerThread.getLooper());

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        ComponentName mediaButtonReceiverComponent = new ComponentName(getPackageName(),
                MediaButtonIntentReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(mediaButtonReceiverComponent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setUpMediaSession();
        }

        mPreferences = getSharedPreferences("Service", 0);
        mCardId = getCardId();

        registerExternalStorageListener();

        mPlayer = new MultiPlayer(this);
        mPlayer.setHandler(mPlayerHandler);

        // Initialize the intent filter and each action
        final IntentFilter filter = new IntentFilter();
        filter.addAction(SERVICE_COMMAND);
        filter.addAction(ACTION_TOGGLE_PAUSE);
        filter.addAction(ACTION_PAUSE);
        filter.addAction(ACTION_STOP);
        filter.addAction(ACTION_NEXT);
        filter.addAction(ACTION_PREVIOUS);
        filter.addAction(ACTION_PREVIOUS_FORCE);
        filter.addAction(ACTION_REPEAT);
        filter.addAction(ACTION_SHUFFLE);
        filter.addAction(TRY_GET_TRACK_INFO);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(LOCK_SCREEN);
        filter.addAction(SEND_PROGRESS);
        filter.addAction(SET_QUEUE);

        // Attach the broadcast listener
        registerReceiver(mIntentReceiver, filter);

        mMediaStoreObserver = new MediaStoreObserver(mPlayerHandler);
        getContentResolver().registerContentObserver(
                MediaStore.Audio.Media.INTERNAL_CONTENT_URI, true, mMediaStoreObserver);
        getContentResolver().registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, mMediaStoreObserver);

        // Initialize the wake lock
        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.setReferenceCounted(false);


        final Intent shutdownIntent = new Intent(this, MusicService.class);
        shutdownIntent.setAction(SHUTDOWN);

        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mShutdownIntent = PendingIntent.getService(this, 0, shutdownIntent, 0);

        scheduleDelayedShutdown();

        reloadQueueAfterPermissionCheck();
        notifyChange(QUEUE_CHANGED);
        notifyChange(META_CHANGED);
    }

    private void setUpMediaSession() {
        mSession = new MediaSession(this, "funmusic");
        mSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPause() {
                pause();
                mPausedByTransientLossOfFocus = false;
            }

            @Override
            public void onPlay() {
                play();
            }

            @Override
            public void onSeekTo(long pos) {
                seek(pos);
            }

            @Override
            public void onSkipToNext() {
                gotoNext(true);
            }

            @Override
            public void onSkipToPrevious() {
                prev(false);
            }

            @Override
            public void onStop() {
                pause();
                mPausedByTransientLossOfFocus = false;
                seek(0);
                releaseServiceUiAndStop();
            }
        });
        mSession.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
    }

    public void exit() {
        stop();
        QuitTimer.getInstance().stop();
        stopSelf();
    }

    @Override
    public void onDestroy() {
        if (D) {
            Log.d(TAG, "Destroying service");
        }
        super.onDestroy();
        // Remove any sound effects
        final Intent audioEffectsIntent = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(audioEffectsIntent);

        cancelNotification();

        mAlarmManager.cancel(mShutdownIntent);

        mPlayerHandler.removeCallbacksAndMessages(null);

        if (SystemUtil.isJellyBeanMR2()) {
            mHandlerThread.quitSafely();
        } else {
            mHandlerThread.quit();
        }

        mPlayer.release();
        mPlayer = null;

        mAudioManager.abandonAudioFocus(mAudioFocusListener);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSession.release();
        }

        getContentResolver().unregisterContentObserver(mMediaStoreObserver);

        closeCursor();

        unregisterReceiver(mIntentReceiver);
        if (mUnMountReceiver != null) {
            unregisterReceiver(mUnMountReceiver);
            mUnMountReceiver = null;
        }

        mWakeLock.release();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (D) {
            Log.d(TAG, "Got new intent " + intent + ", startId = " + startId);
        }
        mServiceStartId = startId;
        if (intent != null) {
            String action = intent.getAction();

            if (SHUTDOWN.equals(action)) {
                mShutdownScheduled = false;
                releaseServiceUiAndStop();
                return START_NOT_STICKY;
            }
            handleCommandIntent(intent);
        }

        scheduleDelayedShutdown();

        if (intent != null && intent.getBooleanExtra(FROM_MEDIA_BUTTON, false)) {
            MediaButtonIntentReceiver.completeWakefulIntent(intent);
        }
        return START_STICKY;
    }

    private void releaseServiceUiAndStop() {
        if (isPlaying() || mPausedByTransientLossOfFocus || mPlayerHandler.hasMessages(TRACK_ENDED)) {
            return;
        }

        if (D) {
            Log.d(TAG, "Nothing is playing anymore, releasing notification");
        }
        cancelNotification();
        mAudioManager.abandonAudioFocus(mAudioFocusListener);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSession.setActive(false);
        }

        if (!mServiceInUse) {
            saveQueue(true);
            stopSelf(mServiceStartId);
        }
    }

    private void handleCommandIntent(Intent intent) {
        final String action = intent.getAction();
        final String command = SERVICE_COMMAND.equals(action) ? intent.getStringExtra(CMD_NAME) : null;

        if (D) {
            Log.d(TAG, "handleCommandIntent: action = " + action + ", command = " + command);
        }
        if (CMD_NEXT.equals(command) || ACTION_NEXT.equals(action)) {
            gotoNext(true);
        } else if (CMD_PREVIOUS.equals(command) || ACTION_PREVIOUS.equals(action)
                || ACTION_PREVIOUS_FORCE.equals(action)) {
            prev(ACTION_PREVIOUS_FORCE.equals(action));
        } else if (CMD_TOGGLE_PAUSE.equals(command) || ACTION_TOGGLE_PAUSE.equals(action)) {
            if (isPlaying()) {
                pause();
                mPausedByTransientLossOfFocus = false;
            } else {
                play();
            }
        } else if (CMD_PAUSE.equals(command) || ACTION_PAUSE.equals(action)) {
            pause();
            mPausedByTransientLossOfFocus = false;
        } else if (CMD_PLAY.equals(command)) {
            play();
        } else if (CMD_STOP.equals(command) || ACTION_STOP.equals(action)) {
            pause();
            mPausedByTransientLossOfFocus = false;
            seek(0);
            releaseServiceUiAndStop();
        } else if (ACTION_REPEAT.equals(action)) {
            cycleRepeat();
        } else if (ACTION_SHUFFLE.equals(action)) {
            cycleShuffle();
        } else if (TRY_GET_TRACK_INFO.equals(action)) {
            getLrc(mPlaylist.get(mPlayPos).getId());
        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            if (isPlaying() && !mIsLocked) {
                Intent lockScreen = new Intent(this, LockActivity.class);
                lockScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(lockScreen);
            }
        } else if (LOCK_SCREEN.equals(action)) {
            mIsLocked = intent.getBooleanExtra("is_lock", true);
            if (D) {
                Log.d(TAG, "isLocked = " + mIsLocked);
            }
        } else if (SEND_PROGRESS.equals(action)) {
            if (isPlaying() && !mIsSending) {
                mPlayerHandler.post(mSendDuration);
                mIsSending = true;
            } else if (!isPlaying()) {
                mPlayerHandler.removeCallbacks(mSendDuration);
                mIsSending = true;
            }
        } else if (SET_QUEUE.equals(action)) {
            Log.e("play_ab", "action");
            setQueuePosition(intent.getIntExtra("position", 0));
        }
    }

    private Runnable mSendDuration = new Runnable() {
        @Override
        public void run() {
            notifyChange(SEND_PROGRESS);
            mPlayerHandler.postDelayed(mSendDuration, 1000);
        }
    };

    private void updateNotification() {
        int newNotifyMode;
        if (isPlaying()) {
            newNotifyMode = NOTIFY_MODE_FOREGROUND;
        } else if (recentlyPlayed()) {
            newNotifyMode = NOTIFY_MODE_BACKGROUND;
        } else {
            newNotifyMode = NOTIFY_MODE_NONE;
        }

        if (mNotifyMode != newNotifyMode) {
            if (mNotifyMode == NOTIFY_MODE_FOREGROUND) {
                if (SystemUtil.isLollipop()) {
                    stopForeground(newNotifyMode == NOTIFY_MODE_NONE);
                } else {
                    stopForeground(newNotifyMode == NOTIFY_MODE_NONE || newNotifyMode == NOTIFY_MODE_BACKGROUND);
                }
            } else if (newNotifyMode == NOTIFY_MODE_NONE) {
                mNotificationManager.cancel(mNotificationId);
                mNotificationPostTime = 0;
            }
        }

        if (newNotifyMode == NOTIFY_MODE_FOREGROUND) {
            startForeground(mNotificationId, buildNotification());
        } else if (newNotifyMode == NOTIFY_MODE_BACKGROUND) {
            mNotificationManager.notify(mNotificationId, buildNotification());
        }

        mNotifyMode = newNotifyMode;
    }

    private Notification buildNotification() {
        RemoteViews remoteViews;
        final int PAUSE_FLAG = 0x1;
        final int NEXT_FLAG = 0x2;
        final int STOP_FLAG = 0x3;
        final String albumName = getAlbumName();
        final String artistName = getArtistName();
        final boolean isPlaying = isPlaying();

        remoteViews = new RemoteViews(this.getPackageName(), R.layout.notification);
        String text = TextUtils.isEmpty(albumName) ? artistName : artistName + " - " + albumName;
        remoteViews.setTextViewText(R.id.notification_title, getTrackName());
        remoteViews.setTextViewText(R.id.notification_subtitle, text);

        //此处action不能是一样的 如果一样的 接受的flag参数只是第一个设置的值
        Intent pauseIntent = new Intent(ACTION_TOGGLE_PAUSE);
        pauseIntent.putExtra("FLAG", PAUSE_FLAG);
        PendingIntent pausePIntent = PendingIntent.getBroadcast(this, 0, pauseIntent, 0);
        remoteViews.setImageViewResource(R.id.notification_play_or_pause, isPlaying ? R.drawable.ic_status_bar_pause_dark : R.drawable.ic_status_bar_play_dark);
        remoteViews.setOnClickPendingIntent(R.id.notification_play_or_pause, pausePIntent);

        Intent nextIntent = new Intent(ACTION_NEXT);
        nextIntent.putExtra("FLAG", NEXT_FLAG);
        PendingIntent nextPIntent = PendingIntent.getBroadcast(this, 0, nextIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.notification_next, nextPIntent);

        Intent exitIntent = new Intent(ACTION_STOP);
        exitIntent.putExtra("FLAG", STOP_FLAG);
        PendingIntent exitPIntent = PendingIntent.getBroadcast(this, 0, exitIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.notification_exit, exitPIntent);

        final Intent nowPlayingIntent = new Intent();
        nowPlayingIntent.setComponent(new ComponentName("com.charles.funmusic", "com.charles.funmusic.activity.PlayingActivity"));
        nowPlayingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent clickIntent = PendingIntent.getActivity(this, 0, nowPlayingIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final Bitmap bitmap = ImageUtil.getArtworkQuick(this, getAlbumId(), 160, 160);
        if (bitmap != null) {
            remoteViews.setImageViewBitmap(R.id.notification_icon, bitmap);
            // remoteViews.setImageViewUri(R.id.image, MusicUtils.getAlbumUri(this, getAudioId()));
            mNoBit = null;
        } else if (!isTrackLocal()) {
            if (mNoBit != null) {
                remoteViews.setImageViewBitmap(R.id.notification_icon, mNoBit);
                mNoBit = null;
            } else {
                Uri uri = null;
                if (getAlbumPath() != null) {
                    try {
                        uri = Uri.parse(getAlbumPath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (getAlbumPath() == null || uri == null) {
                    mNoBit = BitmapFactory.decodeResource(getResources(), R.drawable.ic_default_album_cover);
                    updateNotification();
                } else {
                    ImageRequest imageRequest = ImageRequestBuilder
                            .newBuilderWithSource(uri)
                            .setProgressiveRenderingEnabled(true)
                            .build();
                    ImagePipeline imagePipeline = Fresco.getImagePipeline();
                    DataSource<CloseableReference<CloseableImage>> dataSource =
                            imagePipeline.fetchDecodedImage(
                                    imageRequest, MusicService.this);

                    dataSource.subscribe(
                            new BaseBitmapDataSubscriber() {

                                @Override
                                public void onNewResultImpl(@Nullable Bitmap bitmap) {
                                    // You can use the bitmap in only limited ways
                                    // No need to do any cleanup.
                                    if (bitmap != null) {
                                        mNoBit = bitmap;
                                    }
                                    updateNotification();
                                }

                                @Override
                                public void onFailureImpl(DataSource dataSource) {
                                    // No cleanup required here.
                                    mNoBit = BitmapFactory.decodeResource(getResources(), R.drawable.ic_default_album_cover);
                                    updateNotification();
                                }
                            },
                            CallerThreadExecutor.getInstance()
                    );
                }
            }
        } else {
            remoteViews.setImageViewResource(R.id.notification_icon, R.drawable.ic_default_album_cover);
        }

        if (mNotificationPostTime == 0) {
            mNotificationPostTime = System.currentTimeMillis();
        }
        if (mNotification == null) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "notifier")
                    .setContent(remoteViews)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(clickIntent)
                    .setOngoing(true)
                    .setWhen(mNotificationPostTime);
            if (SystemUtil.isJellyBeanMR1()) {
                builder.setShowWhen(false);
            }
            mNotification = builder.build();
        } else {
            mNotification.contentView = remoteViews;
        }

        return mNotification;
    }

    private void cancelNotification() {
        stopForeground(true);
        mNotificationManager.cancel(mNotificationId);
        mNotificationPostTime = 0;
        mNotifyMode = NOTIFY_MODE_NONE;
    }

    private int getCardId() {
        if (SystemUtil.isMarshmallow()) {
            if (Permission.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                return getMCardId();
            } else {
                return 0;
            }
        } else {
            return getMCardId();
        }
    }

    private int getMCardId() {
        final ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(Uri.parse("content://media/external/fs_id"),
                null, null, null, null);
        int cardId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            cardId = cursor.getInt(0);
            cursor.close();
        }
        return cardId;
    }

    public void closeExternalStorageFiles(String storagePath) {
        stop(true);
        notifyChange(QUEUE_CHANGED);
        notifyChange(META_CHANGED);
    }

    public void registerExternalStorageListener() {
        if (mUnMountReceiver == null) {
            mUnMountReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action != null) {
                        if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                            saveQueue(true);
                            mQueueIsSavable = false;
                            closeExternalStorageFiles(intent.getData().getPath());
                        } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                            mMediaMountedCount++;
                            mCardId = getCardId();
                            reloadQueueAfterPermissionCheck();
                            mQueueIsSavable = true;
                            notifyChange(QUEUE_CHANGED);
                            notifyChange(META_CHANGED);
                        }
                    }
                }
            };
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_MEDIA_EJECT);
            filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            filter.addDataScheme("file");
            registerReceiver(mUnMountReceiver, filter);
        }
    }

    private void cancelShutdown() {
        if (D) Log.d(TAG, "Cancelling delayed shutdown, scheduled = " + mShutdownScheduled);
        if (mShutdownScheduled) {
            mAlarmManager.cancel(mShutdownIntent);
            mShutdownScheduled = false;
        }
    }

    private void stop(boolean goToIdle) {
        if (D) {
            Log.d(TAG, "Stopping playback, goToIdle = " + goToIdle);
        }
        if (mPlayer.isInitialized()) {
            mPlayer.stop();
        }
        mFileToPlay = null;
        closeCursor();
        if (goToIdle) {
            setIsSupposedToBePlaying(false, false);
        }
    }

    private int removeTrackInternal(int first, int last) {
        synchronized (this) {
            if (last < first) {
                return 0;
            } else if (first < 0) {
                first = 0;
            } else if (last >= mPlaylist.size()) {
                last = mPlaylist.size() - 1;
            }

            boolean goToNext = false;
            if (first <= mPlayPos && mPlayPos <= last) {
                mPlayPos = first;
                goToNext = true;
            } else if (mPlayPos > last) {
                mPlayPos -= last - first + 1;
            }
            int numToRemove = last - first + 1;

            if (first == 0 && last == mPlaylist.size() - 1) {
                mPlayPos = -1;
                mNextPlayPos = -1;
                mPlaylist.clear();
                mHistory.clear();
            } else {
                for (int i = 0; i < numToRemove; i++) {
                    mPlaylistInfo.remove(mPlaylist.get(first).getId());
                    mPlaylist.remove(first);
                }

                ListIterator<Integer> positionIterator = mHistory.listIterator();
                while (positionIterator.hasNext()) {
                    int pos = positionIterator.next();
                    if (pos >= first && pos <= last) {
                        positionIterator.remove();
                    } else if (pos > last) {
                        positionIterator.set(pos - numToRemove);
                    }
                }
            }
            if (goToNext) {
                if (mPlaylist.size() == 0) {
                    stop(true);
                    mPlayPos = -1;
                    closeCursor();
                } else {
                    if (mShuffleMode != SHUFFLE_NONE) {
                        mPlayPos = getNextPosition(true);
                    } else if (mPlayPos >= mPlaylist.size()) {
                        mPlayPos = 0;
                    }
                    boolean wasPlaying = isPlaying();
                    stop(false);
                    openCurrentAndNext();
                    if (wasPlaying) {
                        play();
                    }
                }
                notifyChange(META_CHANGED);
            }
            return last - first + 1;
        }
    }

    private void addToPlaylist(long[] list, int position) {
        int addLen = list.length;
        if (position < 0) {
            mPlaylist.clear();
            position = 0;
        }

        mPlaylist.ensureCapacity(mPlaylist.size() + addLen);
        if (position > mPlaylist.size()) {
            position = mPlaylist.size();
        }

        ArrayList<MusicPlaybackTrack> arrayList = new ArrayList<>(addLen);
        for (int i = 0; i < list.length; i++) {
            arrayList.add(new MusicPlaybackTrack(list[i], i));
        }

        mPlaylist.addAll(position, arrayList);

        if (mPlaylist.size() == 0) {
            closeCursor();
            notifyChange(META_CHANGED);
        }
    }

    private void updateCursor(final long trackId) {
        Music music = mPlaylistInfo.get(trackId);
        if (mPlaylistInfo.get(trackId) != null) {
            MatrixCursor cursor = new MatrixCursor(PROJECTION);
            cursor.addRow(new Object[]{
                    music.getId(), music.getArtist(), music.getAlbum(), music.getTitle(),
                    music.getUrl(), music.getAlbumArt(), music.getAlbumId(), music.getArtistId()
            });
            cursor.moveToFirst();
            mCursor = cursor;
            cursor.close();
        }
    }

    private void updateCursor(String selection, String[] selectionArgs) {
        synchronized (this) {
            closeCursor();
            mCursor = openCursorAndGoToFirst(
                    Media.EXTERNAL_CONTENT_URI, PROJECTION, selection, selectionArgs);
        }
    }

    private void updateCursor(Uri uri) {
        synchronized (this) {
            closeCursor();
            mCursor = openCursorAndGoToFirst(uri, PROJECTION, null, null);
        }
    }

    private Cursor openCursorAndGoToFirst(Uri uri, String[] projection,
                                          String selection, String[] selectionArgs) {
        Cursor c = getContentResolver().query(
                uri, projection, selection, selectionArgs, null);
        if (c == null) {
            return null;
        }
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }
        return c;
    }

    private synchronized void closeCursor() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        if (mAlbumCursor != null) {
            mAlbumCursor.close();
            mAlbumCursor = null;
        }
    }

    class RequestPlayUrl implements Runnable {
        private long id;
        private boolean play;
        private boolean stop;

        RequestPlayUrl(long id, boolean play) {
            this.id = id;
            this.play = play;
        }

        public void stop() {
            stop = true;
        }

        @Override
        public void run() {
            try {
                String url = Preferences.getInstance(MusicService.this).getPlayLink(id);
                if (url == null) {
                    MusicFileDownInfo song = Download.getUrl(MusicService.this, id + "");
                    if (song != null && song.getShow_link() != null) {
                        url = song.getShow_link();
                        Preferences.getInstance(MusicService.this).setPlayLink(id, url);
                    }
                }
                if (url != null) {
                    if (D) {
                        Log.e(TAG, "current url = " + url);
                    }
                } else {
                    gotoNext(true);
                }

                if (!stop) {
                    startProxy();
                    // String urlEn = HttpUtil.urlEncode(url);
                    String urlEn = url;
                    urlEn = mProxy.getProxyURL(urlEn);
                    mPlayer.setDataSource(urlEn);
                }


                if (play && !stop) {
                    play();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startProxy() {
        if (mProxy == null) {
            mProxy = new MusicPlayerProxy(this);
            mProxy.init();
            mProxy.start();
        }
    }

    class RequestLrc implements Runnable {

        private Music mMusic;
        private boolean mStop;

        RequestLrc(Music music) {
            mMusic = music;
        }

        public void stop() {
            mStop = true;
        }

        @Override
        public void run() {
            if (D) {
                Log.e(TAG, "start to get lrc");
            }
            String url = null;
            if (mMusic != null && mMusic.getLrc() != null) {
                url = mMusic.getLrc();
            }
            try {
                JsonObject jsonObject = HttpUtil.getResposeJsonObject(BMA.Search.searchLrcPic(mMusic.getTitle(), mMusic.getArtist()));
                JsonArray array = jsonObject.get("song").getAsJsonArray();
                int len = array.size();
                url = null;
                for (int i = 0; i < len; i++) {
                    url = array.get(i).getAsJsonObject().get("lrclink").getAsString();
                    if (url != null) {
                        if (D) {
                            Log.d(TAG, "lrc link = " + url);
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!mStop) {
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + LRC_PATH + mMusic.getId());
                String lrc;
                try {
                    lrc = HttpUtil.getResposeString(url);
                    if (lrc != null && !lrc.isEmpty()) {
                        if (!file.exists())
                            file.createNewFile();
                        writeToFile(file, lrc);
                        mPlayerHandler.sendEmptyMessage(LRC_DOWNLOADED);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void getLrc(long id) {
        Music music = mPlaylistInfo.get(id);

        if (music == null) {
            Log.d(TAG, "get lrc err, music is null");
        }
        String lrc = Environment.getExternalStorageDirectory().getAbsolutePath() + LRC_PATH;
        File file = new File(lrc);
        Log.d(TAG, "file exists = " + file.exists());
        if (!file.exists()) {
            //不存在就建立此目录
            boolean r = file.mkdirs();
            Log.d(TAG, "file created = " + r);

        }
        file = new File(lrc + id);
        if (!file.exists()) {
            if (mRequestLrc != null) {
                mRequestLrc.stop();
                mLrcHandler.removeCallbacks(mRequestLrc);
            }
            mRequestLrc = new RequestLrc(mPlaylistInfo.get(id));
            mLrcHandler.postDelayed(mRequestLrc, 70);
        }
    }

    private synchronized void writeToFile(File file, String lrc) {
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(lrc.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openCurrentAndNextPlay(boolean isPlay) {
        openCurrentAndMaybeNext(isPlay, true);
    }

    private void openCurrentAndNext() {
        openCurrentAndMaybeNext(false, true);
    }

    private void openCurrentAndMaybeNext(final boolean isPlay, final boolean openNext) {
        synchronized (this) {
            if (D) {
                Log.d(TAG, "open current");
            }
            closeCursor();
            stop(false);
            boolean shutdown = false;

            if (mPlaylist.size() == 0 || mPlaylistInfo.size() == 0 && mPlayPos >= mPlaylist.size()) {
                clearPlayInfos();
                return;
            }
            long id = mPlaylist.get(mPlayPos).getId();
            updateCursor(id);
            getLrc(id);
            if (mPlaylistInfo.get(id) == null) {
                return;
            }
            if (!mPlaylistInfo.get(id).isLocal()) {
                if (mRequestUrl != null) {
                    mRequestUrl.stop();
                    mUrlHandler.removeCallbacks(mRequestUrl);
                }
                mRequestUrl = new RequestPlayUrl(id, isPlay);
                mUrlHandler.postDelayed(mRequestUrl, 70);
            } else {
                while (true) {
                    if (mCursor != null
                            && openFile(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/"
                            + mCursor.getLong(ID_COL_IDX))) {
                        break;
                    }

                    closeCursor();
                    if (mOpenFailedCounter++ < 10 && mPlaylist.size() > 1) {
                        final int pos = getNextPosition(false);
                        if (pos < 0) {
                            shutdown = true;
                            break;
                        }
                        mPlayPos = pos;
                        stop(false);
                        mPlayPos = pos;
                        updateCursor(mPlaylist.get(mPlayPos).getId());
                    } else {
                        mOpenFailedCounter = 0;
                        Log.w(TAG, "Failed to open file for playback");
                        shutdown = true;
                        break;
                    }
                }
            }

            if (shutdown) {
                scheduleDelayedShutdown();
                if (mIsSupposedToBePlaying) {
                    mIsSupposedToBePlaying = false;
                    notifyChange(PLAYLIST_CHANGED);
                }
            } else if (openNext) {
                setNextTrack();
            }
        }
    }

    private void sendErrorMessage(String trackName) {
        Intent i = new Intent(TRACK_ERROR);
        i.putExtra(TrackErrorExtra.TRACK_NAME, trackName);
        sendBroadcast(i);
    }

    private int getNextPosition(boolean force) {
        if (mPlaylist == null || mPlaylist.isEmpty()) {
            return -1;
        }
        if (!force && mRepeatMode == REPEAT_CURRENT) {
            if (mPlayPos < 0) {
                return 0;
            }
            return mPlayPos;
        } else if (mShuffleMode == SHUFFLE_NORMAL) {
            int numTracks = mPlaylist.size();
            int[] trackNumPlays = new int[numTracks];
            for (int i = 0; i < numTracks; i++) {
                trackNumPlays[i] = 0;
            }

            int numHistory = mHistory.size();
            for (int i = 0; i < numHistory; i++) {
                int idx = mHistory.get(i);
                if (idx >= 0 && idx < numTracks) {
                    trackNumPlays[idx]++;
                }
            }

            if (mPlayPos >= 0 && mPlayPos < numTracks) {
                trackNumPlays[mPlayPos]++;
            }

            int minNumPlays = Integer.MAX_VALUE;
            int numTracksWithMinNumPlays = 0;
            for (int trackNumPlay : trackNumPlays) {
                if (trackNumPlay < minNumPlays) {
                    minNumPlays = trackNumPlay;
                    numTracksWithMinNumPlays = 1;
                } else if (trackNumPlay == minNumPlays) {
                    numTracksWithMinNumPlays++;
                }
            }

            if (minNumPlays > 0 && numTracksWithMinNumPlays == numTracks
                    && mRepeatMode != REPEAT_ALL && !force) {
                return -1;
            }

            int skip = mShuffler.nextInt(numTracksWithMinNumPlays);
            for (int i = 0; i < trackNumPlays.length; i++) {
                if (trackNumPlays[i] == minNumPlays) {
                    if (skip == 0) {
                        return i;
                    } else {
                        skip--;
                    }
                }
            }

            if (D) {
                Log.e(TAG, "Getting the next position resulted did not get a result when it should have");
            }
            return -1;
        } else if (mShuffleMode == SHUFFLE_AUTO) {
            doAutoShuffleUpdate();
            return mPlayPos + 1;
        } else {
            if (mPlayPos >= mPlaylist.size() - 1) {
                if (mRepeatMode == REPEAT_NONE && !force) {
                    return -1;
                } else if (mRepeatMode == REPEAT_ALL || force) {
                    return 0;
                }
                return -1;
            } else {
                return mPlayPos + 1;
            }
        }
    }

    private void setNextTrack() {
        setNextTrack(getNextPosition(false));
    }

    private void setNextTrack(int position) {
        mNextPlayPos = position;
        if (D) {
            Log.d(TAG, "setNextTrack: next play position = " + mNextPlayPos);
        }
        if (mNextPlayPos >= 0 && mPlaylist != null && mNextPlayPos < mPlaylist.size()) {
            final long id = mPlaylist.get(mNextPlayPos).getId();
            if (mPlaylistInfo.get(id) != null) {
                if (mPlaylistInfo.get(id).isLocal()) {
                    mPlayer.setNextDataSource(Media.EXTERNAL_CONTENT_URI + "/" + id);
                } else {
                    mPlayer.setNextDataSource(null);
                }
            }
        } else {
            mPlayer.setNextDataSource(null);
        }
    }

    private boolean makeAutoShuffleList() {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(
                    Media.EXTERNAL_CONTENT_URI, new String[]{Media._ID},
                    Media.IS_MUSIC + "=1", null, null
            );
            if (cursor == null || cursor.getCount() == 0) {
                return false;
            }
            int len = cursor.getCount();
            long[] list = new long[len];
            for (int i = 0; i < len; i++) {
                cursor.moveToNext();
                list[i] = cursor.getLong(0);
            }
            mAutoShuffleList = list;
            return true;
        } catch (RuntimeException ignored) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    private void doAutoShuffleUpdate() {
        boolean notify = false;
        if (mPlayPos > 10) {
            removeTracks(0, mPlayPos - 9);
            notify = true;
        }
        final int toAdd = 7 - (mPlaylist.size() - (mPlayPos < 0 ? -1 : mPlayPos));
        for (int i = 0; i < toAdd; i++) {
            int lookBack = mHistory.size();
            int idx;
            while (true) {
                idx = mShuffler.nextInt(mAutoShuffleList.length);
                if (!wasRecentlyUsed(idx, lookBack)) {
                    break;
                }
                lookBack /= 2;
            }
            mHistory.add(idx);
            if (mHistory.size() > MAX_HISTORY_SIZE) {
                mHistory.remove(0);
            }
            mPlaylist.add(new MusicPlaybackTrack(mAutoShuffleList[idx], -1));
            notify = true;
        }
        if (notify) {
            notifyChange(QUEUE_CHANGED);
        }
    }

    private boolean wasRecentlyUsed(int idx, int lookBackSize) {
        if (lookBackSize == 0) {
            return false;
        }
        int listSize = mHistory.size();
        if (listSize < lookBackSize) {
            lookBackSize = listSize;
        }
        int maxIdx = listSize - 1;
        for (int i = 0; i < lookBackSize; i++) {
            long entry = mHistory.get(maxIdx - i);
            if (entry == idx) {
                return true;
            }
        }
        return false;
    }

    private void sendUpdateBuffer(int progress) {
        Intent intent = new Intent(BUFFER_UP);
        intent.putExtra("progress", progress);
        sendBroadcast(intent);
    }

    private void updateMediaSession(String what) {
        int playState = mIsSupposedToBePlaying ?
                PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED;

        if (what.equals(PLAY_STATE_CHANGED) || what.equals(POSITION_CHANGED)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mSession.setPlaybackState(new PlaybackState.Builder()
                        .setState(playState, position(), 1.0f)
                        .setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_PLAY_PAUSE
                                | PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS
                        ).build()
                );
            }
        } else if (what.equals(META_CHANGED) || what.equals(QUEUE_CHANGED)) {
//            Bitmap albumArt = ImageLoader.getInstance().loadImageSync(CommonUtils.getAlbumArtUri(getAlbumId()).toString());
            Bitmap albumArt = null;

            if (albumArt != null) {
                Bitmap.Config config = albumArt.getConfig();
                if (config == null) {
                    config = Bitmap.Config.ARGB_8888;
                }
                albumArt = albumArt.copy(config, false);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mSession.setMetadata(new MediaMetadata.Builder()
                        .putString(MediaMetadata.METADATA_KEY_ARTIST, getArtistName())
                        .putString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST, getAlbumArtistName())
                        .putString(MediaMetadata.METADATA_KEY_ALBUM, getAlbumName())
                        .putString(MediaMetadata.METADATA_KEY_TITLE, getTrackName())
                        .putLong(MediaMetadata.METADATA_KEY_DURATION, duration())
                        .putLong(MediaMetadata.METADATA_KEY_TRACK_NUMBER, getQueuePosition() + 1)
                        .putLong(MediaMetadata.METADATA_KEY_NUM_TRACKS, getQueue().length)
                        .putString(MediaMetadata.METADATA_KEY_GENRE, getGenreName())
                        .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART,
                                mShowAlbumArtOnLockScreen ? albumArt : null)
                        .build());

                mSession.setPlaybackState(new PlaybackState.Builder()
                        .setState(playState, position(), 1.0f)
                        .setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PAUSE | PlaybackState.ACTION_PLAY_PAUSE |
                                PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS)
                        .build());
            }
        }
    }

    private void saveQueue(boolean full) {
        if (!mQueueIsSavable) {
            return;
        }

        SharedPreferences.Editor editor = mPreferences.edit();
        if (full) {
            mPlaybackStateStore.saveState(mPlaylist, mShuffleMode != SHUFFLE_NONE ? mHistory : null);
            if (mPlaylistInfo.size() > 0) {
                String temp = AppCache.gsonInstance().toJson(mPlaylistInfo);
                try {
                    File file = new File(getCacheDir().getAbsolutePath() + "playlist");
                    RandomAccessFile ra = new RandomAccessFile(file, "rws");
                    ra.write(temp.getBytes());
                    ra.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            editor.putInt("cardId", mCardId);
        }
        editor.putInt("cur_pos", mPlayPos);
        if (mPlayer.isInitialized()) {
            editor.putLong("seek_pos", mPlayer.position());
        }
        editor.putInt("repeat_mode", mRepeatMode);
        editor.putInt("shuffle_mode", mShuffleMode);
        editor.apply();
    }

    private void reloadQueueAfterPermissionCheck() {
        if (SystemUtil.isMarshmallow()) {
            if (Permission.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                reloadQueue();
            }
        } else {
            reloadQueue();
        }
    }

    private String readTextFromSDCard(InputStream is) throws Exception {
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder buffer = new StringBuilder();
        String str;
        while ((str = bufferedReader.readLine()) != null) {
            buffer.append(str);
            buffer.append("\n");
        }
        return buffer.toString();
    }

    private void clearPlayInfos() {
        File file = new File(getCacheDir().getAbsolutePath() + "playlist");
        if (file.exists()) {
            file.delete();
        }
        MusicPlaybackState.getInstance(this).clearQueue();
    }

    private void reloadQueue() {
        int id = mCardId;
        if (mPreferences.contains("card_id")) {
            id = mPreferences.getInt("card_id", ~mCardId);
        }
        if (id == mCardId) {
            mPlaylist = mPlaybackStateStore.getQueue();
            try {
                FileInputStream in = new FileInputStream(new File(getCacheDir().getAbsolutePath() + "playlist"));
                String c = readTextFromSDCard(in);
                HashMap<Long, Music> play = AppCache.gsonInstance().fromJson(c,
                        new TypeToken<HashMap<Long, Music>>() {
                        }.getType());
                if (play != null && play.size() > 0) {
                    mPlaylistInfo = play;
                    if (D) {
                        Log.d(TAG, mPlaylistInfo.keySet().toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if ((mPlaylist.size() == mPlaylistInfo.size()) && mPlaylist.size() > 0) {
            int pos = mPreferences.getInt("cur_pos", 0);
            if (pos < 0 || pos > mPlaylist.size()) {
                mPlaylist.clear();
                return;
            }
            mPlayPos = pos;
            updateCursor(mPlaylist.get(mPlayPos).getId());
            if (mCursor == null) {
                SystemClock.sleep(3000);
                updateCursor(mPlaylist.get(mPlayPos).getId());
            }
            synchronized (this) {
                closeCursor();
                mOpenFailedCounter = 20;
                openCurrentAndNext();
            }

            final long seekPos = mPreferences.getLong("seek_pos", 0);
            mLastSeekPos = seekPos;
            seek(seekPos >= 0 && seekPos < duration() ? seekPos : 0);

            if (D) {
                Log.d(TAG, "restored queue, currently at position "
                        + position() + "/" + duration()
                        + " (requested " + seekPos + ")");
            }

            int repeatMode = mPreferences.getInt("repeat_mode", REPEAT_ALL);
            if (repeatMode != REPEAT_ALL && repeatMode != REPEAT_CURRENT) {
                repeatMode = REPEAT_NONE;
            }
            mRepeatMode = repeatMode;

            int shuffleMode = mPreferences.getInt("shuffle_mode", SHUFFLE_NONE);
            if (shuffleMode != SHUFFLE_AUTO && shuffleMode != SHUFFLE_NORMAL) {
                shuffleMode = SHUFFLE_NONE;
            }
            if (shuffleMode != SHUFFLE_NONE) {
                mHistory = mPlaybackStateStore.getHistory(mPlaylist.size());
            }
            if (shuffleMode == SHUFFLE_AUTO) {
                if (!makeAutoShuffleList()) {
                    shuffleMode = SHUFFLE_NONE;
                }
            }
            mShuffleMode = shuffleMode;
        } else {
            clearPlayInfos();
        }
        notifyChange(MUSIC_CHANGED);
    }

    public boolean openFile(String path) {
        if (D) {
            Log.d(TAG, "openFile: path = " + path);
        }
        synchronized (this) {
            if (path == null) {
                return false;
            }

            if (mCursor == null) {
                Uri uri = Uri.parse(path);
                boolean shouldAddToPlaylist = true;
                long id = -1;
                try {
                    id = Long.valueOf(uri.getLastPathSegment());
                } catch (NumberFormatException ignored) {
                }

                if (id != -1 && path.startsWith(Media.EXTERNAL_CONTENT_URI.toString())) {
                    updateCursor(uri);
                } else if (id != -1 && path.startsWith(MediaStore.Files
                        .getContentUri("external").toString())) {
                    updateCursor(id);
                } else if (path.startsWith("content://downloads/")) {
                    String mpUri = getValueForDownloadedFile(this, uri, "media_provider_uri");
                    if (D) {
                        Log.i(TAG, "Downloaded file's MP uri: " + mpUri);
                    }
                    if (!TextUtils.isEmpty(mpUri)) {
                        if (openFile(mpUri)) {
                            notifyChange(META_CHANGED);
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        updateCursorForDownloadedFile(this, uri);
                        shouldAddToPlaylist = false;
                    }
                } else {
                    String where = Media.DATA + "=?";
                    String[] selectionArgs = new String[]{path};
                    updateCursor(where, selectionArgs);
                }
                try {
                    if (mCursor != null && shouldAddToPlaylist) {
                        mPlaylist.clear();
                        mPlaylist.add(new MusicPlaybackTrack(mCursor.getLong(ID_COL_IDX), -1));
                        notifyChange(QUEUE_CHANGED);
                        mPlayPos = 0;
                        mHistory.clear();
                    }
                } catch (UnsupportedOperationException ignored) {
                }
            }

            mFileToPlay = path;
            mPlayer.setDataSource(mFileToPlay);
            if (mPlayer.isInitialized()) {
                mOpenFailedCounter = 0;
                return true;
            }

            String trackName = getTrackName();
            if (TextUtils.isEmpty(trackName)) {
                trackName = path;
            }
            sendErrorMessage(trackName);

            stop(true);
            return false;
        }
    }

    private void updateCursorForDownloadedFile(Context context, Uri uri) {
        synchronized (this) {
            closeCursor();
            MatrixCursor cursor = new MatrixCursor(PROJECTION_MATRIX);
            String title = getValueForDownloadedFile(this, uri, "title");
            cursor.addRow(new Object[]{
                    null,
                    null,
                    null,
                    title,
                    null,
                    null,
                    null,
                    null
            });
            mCursor = cursor;
            mCursor.moveToFirst();
        }
    }

    private String getValueForDownloadedFile(Context context, Uri uri, String column) {

        Cursor cursor = null;
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(
                    uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public int getAudioSessionId() {
        synchronized (this) {
            return mPlayer.getAudioSessionId();
        }
    }

    public int getMediaMountedCount() {
        return mMediaMountedCount;
    }

    public int getShuffleMode() {
        return mShuffleMode;
    }

    public void setShuffleMode(int shuffleMode) {
        synchronized (this) {
            if (mShuffleMode == shuffleMode && mPlaylist.size() > 0) {
                return;
            }

            mShuffleMode = shuffleMode;
            if (mShuffleMode == SHUFFLE_AUTO) {
                if (makeAutoShuffleList()) {
                    mPlaylist.clear();
                    doAutoShuffleUpdate();
                    mPlayPos = 0;
                    openCurrentAndNext();
                    play();
                    notifyChange(META_CHANGED);
                    return;
                } else {
                    mShuffleMode = SHUFFLE_NONE;
                }
            } else {
                setNextTrack();
            }
            saveQueue(false);
            notifyChange(SHUFFLE_MODE_CHANGED);
        }
    }

    public int getRepeatMode() {
        return mRepeatMode;
    }

    public void setRepeatMode(int repeatMode) {
        synchronized (this) {
            mRepeatMode = repeatMode;
            setNextTrack();
            saveQueue(false);
            notifyChange(REPEAT_MODE_CHANGED);
        }
    }

    public int removeTrack(long id) {
        int numRemoved = 0;
        synchronized (this) {
            for (int i = 0; i < mPlaylist.size(); i++) {
                if (mPlaylist.get(i).getId() == id) {
                    numRemoved += removeTrackInternal(i, i);
                    i--;
                }
            }
            mPlaylistInfo.remove(id);
        }
        if (numRemoved > 0) {
            notifyChange(QUEUE_CHANGED);
        }
        return numRemoved;
    }

    public boolean removeTrackAtPosition(long id, int position) {
        synchronized (this) {
            if (position >= 0 &&
                    position < mPlaylist.size() &&
                    mPlaylist.get(position).getId() == id) {
                mPlaylistInfo.remove(id);
                return removeTracks(position, position) > 0;
            }

        }
        return false;
    }

    private int removeTracks(int first, int last) {
        int numRemoved = removeTrackInternal(first, last);
        if (numRemoved > 0) {
            notifyChange(QUEUE_CHANGED);
        }
        return numRemoved;
    }

    public int getQueuePosition() {
        synchronized (this) {
            return mPlayPos;
        }
    }

    public void setQueuePosition(int index) {
        synchronized (this) {
            stop(false);
            mPlayPos = index;
            openCurrentAndNext();
            play();
            notifyChange(META_CHANGED);
            if (mShuffleMode == SHUFFLE_AUTO) {
                doAutoShuffleUpdate();
            }
        }
    }

    public int getQueueHistorySize() {
        synchronized (this) {
            return mHistory.size();
        }
    }

    public int getQueueHistoryPosition(int position) {
        synchronized (this) {
            if (position >= 0 && position < mHistory.size()) {
                return mHistory.get(position);
            }
        }

        return -1;
    }

    public int[] getQueueHistoryList() {
        synchronized (this) {
            int[] history = new int[mHistory.size()];
            for (int i = 0; i < mHistory.size(); i++) {
                history[i] = mHistory.get(i);
            }
            return history;
        }
    }

    public void setAndRecordPlayPos(int nextPos) {
        synchronized (this) {
            if (mShuffleMode != SHUFFLE_NONE) {
                mHistory.add(mPlayPos);
                if (mHistory.size() > MAX_HISTORY_SIZE) {
                    mHistory.remove(0);
                }
            }
            mPlayPos = nextPos;
        }
    }

    public void prev(boolean forcePrevious) {
        synchronized (this) {
            boolean goPrevious = getRepeatMode() != REPEAT_CURRENT &&
                    (position() < REWIND_INSTEAD_PREVIOUS_THRESHOLD || forcePrevious);

            if (goPrevious) {
                if (D) {
                    Log.d(TAG, "Going to previous track");
                }
                int pos = getPreviousPlayPosition(true);

                if (pos < 0) {
                    return;
                }
                mNextPlayPos = mPlayPos;
                mPlayPos = pos;
                stop(false);
                openCurrent();
                play(false);
                notifyChange(META_CHANGED);
                notifyChange(MUSIC_CHANGED);
            } else {
                if (D) {
                    Log.d(TAG, "Going to beginning of track");
                }
                seek(0);
                play(false);
            }
        }
    }

    private void scheduleDelayedShutdown() {
        if (D) {
            Log.v(TAG, "Scheduling shutdown in " + IDLE_DELAY + " ms");
        }
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + IDLE_DELAY, mShutdownIntent);
        mShutdownScheduled = true;
    }

    public long seek(long position) {
        if (mPlayer.isInitialized()) {
            if (position < 0) {
                position = 0;
            } else if (position > mPlayer.duration()) {
                position = mPlayer.duration();
            }
            long result = mPlayer.seek(position);
            notifyChange(POSITION_CHANGED);
            return result;
        }
        return -1;
    }

    public void seekRelative(long deltaInMs) {
        synchronized (this) {
            if (mPlayer.isInitialized()) {
                final long newPos = position() + deltaInMs;
                final long duration = duration();
                if (newPos < 0) {
                    prev(true);
                    // seek to the new duration + the leftover position
                    seek(duration() + newPos);
                } else if (newPos >= duration) {
                    gotoNext(true);
                    // seek to the leftover duration
                    seek(newPos - duration);
                } else {
                    seek(newPos);
                }
            }
        }
    }

    public long position() {
        if (mPlayer.isInitialized()) {
            return mPlayer.position();
        }
        return -1;
    }

    public int getSecondPosition() {
        if (mPlayer.isInitialized()) {
            return mPlayer.mSecondaryPosition;
        }
        return -1;
    }

    public long duration() {
        if (mPlayer.isInitialized()) {
            return mPlayer.duration();
        }
        return -1;
    }

    public HashMap<Long, Music> getPlayInfos() {
        synchronized (this) {
            return mPlaylistInfo;
        }
    }

    public long[] getQueue() {
        synchronized (this) {
            final int len = mPlaylist.size();
            final long[] list = new long[len];
            for (int i = 0; i < len; i++) {
                list[i] = mPlaylist.get(i).getId();
            }
            return list;
        }
    }

    public long getQueueItemAtPosition(int position) {
        synchronized (this) {
            if (position >= 0 && position < mPlaylist.size()) {
                return mPlaylist.get(position).getId();
            }
        }
        return -1;
    }

    public int getQueueSize() {
        synchronized (this) {
            return mPlaylist.size();
        }
    }

    public void play() {
        play(true);
    }

    public void play(boolean createNewNextTrack) {
        int status = mAudioManager.requestAudioFocus(mAudioFocusListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (D) {
            Log.d(TAG, "Starting playback: audio focus request status = " + status);
        }

        if (status != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return;
        }

        Intent intent = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(intent);

        mAudioManager.registerMediaButtonEventReceiver(new ComponentName(getPackageName(),
                MediaButtonIntentReceiver.class.getName()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSession.setActive(true);
        }
        if (createNewNextTrack) {
            setNextTrack();
        } else {
            setNextTrack(mNextPlayPos);
        }
        if (mPlayer.isTrackPrepared()) {
            long duration = mPlayer.duration();
            if (mRepeatMode != REPEAT_CURRENT && duration > 2000
                    && mPlayer.position() >= duration - 2000) {
                gotoNext(true);
            }
        }
        mPlayer.start();
        mPlayerHandler.removeMessages(FADE_DOWN);
        mPlayerHandler.sendEmptyMessage(FADE_UP);
        setIsSupposedToBePlaying(true, true);
        cancelShutdown();
        updateNotification();
        notifyChange(META_CHANGED);
    }

    public void pause() {
        if (D) {
            Log.d(TAG, "Pausing playback");
        }
        synchronized (this) {
            mPlayerHandler.removeMessages(FADE_UP);
            if (mIsSupposedToBePlaying) {
                Intent intent = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
                intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
                intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
                sendBroadcast(intent);

                mPlayer.pause();

                setIsSupposedToBePlaying(false, false);
                notifyChange(META_CHANGED);
            }
        }
    }

    public void gotoNext(final boolean force) {
        if (D) Log.d(TAG, "Going to next track");
        synchronized (this) {
            if (mPlaylist.size() <= 0) {
                if (D) Log.d(TAG, "No play queue");
                scheduleDelayedShutdown();
                return;
            }

            int pos = mNextPlayPos;
            if (pos < 0) {
                pos = getNextPosition(force);
            }

            if (pos < 0) {
                setIsSupposedToBePlaying(false, true);
                return;
            }

            stop(false);
            setAndRecordPlayPos(pos);
            openCurrentAndNext();
            play();
            notifyChange(META_CHANGED);
            notifyChange(MUSIC_CHANGED);
        }
    }

    public String getPath() {
        synchronized (this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(AudioColumns.DATA));
        }
    }

    public String getAlbumName() {
        synchronized (this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(AudioColumns.ALBUM));
        }
    }

    public String getAlbumPath() {
        synchronized (this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(AudioColumns.MIME_TYPE));
        }
    }

    public String[] getAlbumPathAll() {
        synchronized (this) {
            try {
                int len = mPlaylistInfo.size();
                String[] albums = new String[len];
                long[] queue = getQueue();
                for (int i = 0; i < len; i++) {
                    albums[i] = mPlaylistInfo.get(queue[i]).getAlbumArt();
                }
                return albums;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new String[]{};
        }
    }

    public String getTrackName() {
        synchronized (this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(AudioColumns.TITLE));
        }
    }

    public boolean isTrackLocal() {
        synchronized (this) {
            Music music = mPlaylistInfo.get(getAudioId());
            if (music == null) {
                return true;
            }
            return music.isLocal();
        }
    }

    public String getAlbumPath(long id) {
        synchronized (this) {
            try {
                return mPlaylistInfo.get(id).getAlbumArt();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public String getGenreName() {
        synchronized (this) {
            if (mCursor == null || mPlayPos < 0 || mPlayPos >= mPlaylist.size()) {
                return null;
            }
            String[] genreProjection = {MediaStore.Audio.Genres.NAME};
            Uri genreUri = MediaStore.Audio.Genres.getContentUriForAudioId("external",
                    (int) mPlaylist.get(mPlayPos).getId());
            Cursor genreCursor = getContentResolver().query(genreUri, genreProjection,
                    null, null, null);
            if (genreCursor != null) {
                try {
                    if (genreCursor.moveToFirst()) {
                        return genreCursor.getString(
                                genreCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME));
                    }
                } finally {
                    genreCursor.close();
                }
            }
            return null;
        }
    }

    public String getArtistName() {
        synchronized (this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(AudioColumns.ARTIST));
        }
    }

    public String getAlbumArtistName() {
        synchronized (this) {
            if (mAlbumCursor == null) {
                return null;
            }
            return mAlbumCursor.getString(mAlbumCursor.getColumnIndexOrThrow(AlbumColumns.ARTIST));
        }
    }

    public long getAlbumId() {
        synchronized (this) {
            if (mCursor == null) {
                return -1;
            }
            return mCursor.getLong(mCursor.getColumnIndexOrThrow(AudioColumns.ALBUM_ID));
        }
    }

    public long getArtistId() {
        synchronized (this) {
            if (mCursor == null) {
                return -1;
            }
            return mCursor.getLong(mCursor.getColumnIndexOrThrow(AudioColumns.ARTIST_ID));
        }
    }

    public long getAudioId() {
        MusicPlaybackTrack track = getCurrentTrack();
        if (track != null) {
            return track.getId();
        }
        return -1;
    }

    public MusicPlaybackTrack getCurrentTrack() {
        return getTrack(mPlayPos);
    }

    public synchronized MusicPlaybackTrack getTrack(int index) {
        if (index >= 0 && index < mPlaylist.size() && mPlayer.isInitialized()) {
            return mPlaylist.get(index);
        }
        return null;
    }

    public long getNextAudioId() {
        synchronized (this) {
            if (mNextPlayPos >= 0 && mNextPlayPos < mPlaylist.size() && mPlayer.isInitialized()) {
                return mPlaylist.get(mNextPlayPos).getId();
            }
        }
        return -1;
    }

    public long getPreviousAudioId() {
        synchronized (this) {
            if (mPlayer.isInitialized()) {
                int pos = getPreviousPlayPosition(false);
                if (pos >= 0 && pos < mPlaylist.size()) {
                    return mPlaylist.get(pos).getId();
                }
            }
        }
        return -1;
    }

    public int getPreviousPlayPosition(boolean removeFromHistory) {
        synchronized (this) {
            if (mShuffleMode == SHUFFLE_NORMAL) {
                int listSize = mHistory.size();
                if (listSize == 0) {
                    return -1;
                }
                Integer pos = mHistory.get(listSize - 1);
                if (removeFromHistory) {
                    mHistory.remove(listSize - 1);
                }
                return pos;
            } else {
                if (mPlayPos > 0) {
                    return mPlayPos - 1;
                } else {
                    return mPlaylist.size() - 1;
                }
            }
        }
    }

    private void openCurrent() {
        openCurrentAndMaybeNext(false, false);
    }

    public void moveQueueItem(int index1, int index2) {
        synchronized (this) {
            if (index1 >= mPlaylist.size()) {
                index1 = mPlaylist.size() - 1;
            }
            if (index2 >= mPlaylist.size()) {
                index2 = mPlaylist.size() - 1;
            }

            if (index1 == index2) {
                return;
            }
            mPlaylistInfo.remove(mPlaylist.get(index1).getId());
            MusicPlaybackTrack track = mPlaylist.remove(index1);
            if (index1 < index2) {
                mPlaylist.add(index2, track);
                if (mPlayPos == index1) {
                    mPlayPos = index2;
                } else if (mPlayPos >= index1 && mPlayPos <= index2) {
                    mPlayPos--;
                }
            } else if (index2 < index1) {
                mPlaylist.add(index2, track);
                if (mPlayPos == index1) {
                    mPlayPos = index2;
                } else if (mPlayPos >= index2 && mPlayPos <= index1) {
                    mPlayPos++;
                }
            }
            notifyChange(QUEUE_CHANGED);
        }
    }

    public void enqueue(long[] list, HashMap<Long, Music> map, int action) {
        synchronized (this) {
            mPlaylistInfo.putAll(map);
            if (action == NEXT && mPlayPos + 1 < mPlaylist.size()) {
                addToPlaylist(list, mPlayPos + 1);
                mNextPlayPos = mPlayPos + 1;
                notifyChange(QUEUE_CHANGED);
            } else {
                addToPlaylist(list, Integer.MAX_VALUE);
                notifyChange(QUEUE_CHANGED);
            }

            if (mPlayPos < 0) {
                mPlayPos = 0;
                openCurrentAndNext();
                play();
                notifyChange(META_CHANGED);
            }
        }
    }

    private void cycleRepeat() {
        if (mRepeatMode == REPEAT_NONE) {
            setRepeatMode(REPEAT_CURRENT);
            if (mShuffleMode != SHUFFLE_NONE) {
                setShuffleMode(SHUFFLE_NONE);
            }
        } else {
            setRepeatMode(REPEAT_NONE);
        }
    }

    private void cycleShuffle() {
        if (mShuffleMode == SHUFFLE_NONE) {
            setShuffleMode(SHUFFLE_NORMAL);
            if (mRepeatMode == REPEAT_CURRENT) {
                setRepeatMode(REPEAT_ALL);
            }
        } else if (mShuffleMode == SHUFFLE_NORMAL || mShuffleMode == SHUFFLE_AUTO) {
            setShuffleMode(SHUFFLE_NONE);
        }
    }

    public void refresh() {
        notifyChange(REFRESH);
    }

    public void playlistChanged() {
        notifyChange(PLAYLIST_CHANGED);
    }

    public void loading(boolean l) {
        Intent intent = new Intent(MUSIC_LOADING);
        intent.putExtra("isLoading", l);
        sendBroadcast(intent);
    }

    public void setLockScreenAlbumArt(boolean enabled) {
        mShowAlbumArtOnLockScreen = enabled;
        notifyChange(META_CHANGED);
    }

    public void timer(int time) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                new Intent(ACTION_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (am != null) {
            am.set(AlarmManager.RTC, System.currentTimeMillis() + time, pendingIntent);
        }
    }

    public boolean isPlaying() {
        return mIsSupposedToBePlaying;
    }

    private void setIsSupposedToBePlaying(boolean value, boolean notify) {
        if (mIsSupposedToBePlaying != value) {
            mIsSupposedToBePlaying = value;

            if (!mIsSupposedToBePlaying) {
                scheduleDelayedShutdown();
                mLastPlayedTime = System.currentTimeMillis();
            }

            if (notify) {
                notifyChange(PLAY_STATE_CHANGED);
            }
        }
    }

    private boolean recentlyPlayed() {
        return isPlaying() || System.currentTimeMillis() - mLastPlayedTime < IDLE_DELAY;
    }

    public void open(final HashMap<Long, Music> musics, final long[] list, final int position) {
        synchronized (this) {

            mPlaylistInfo = musics;
            Log.d(TAG, mPlaylistInfo.toString());
            if (mShuffleMode == SHUFFLE_AUTO) {
                mShuffleMode = SHUFFLE_NORMAL;
            }
            final long oldId = getAudioId();
            final int listLength = list.length;
            boolean newList = true;
            if (mPlaylist.size() == listLength) {
                newList = false;
                for (int i = 0; i < listLength; i++) {
                    if (list[i] != mPlaylist.get(i).getId()) {
                        newList = true;
                        break;
                    }
                }
            }
            if (newList) {
                addToPlaylist(list, -1);
                notifyChange(QUEUE_CHANGED);
            }
            if (position >= 0) {
                mPlayPos = position;
            } else {
                mPlayPos = mShuffler.nextInt(mPlaylist.size());
            }

            mHistory.clear();
            openCurrentAndNextPlay(true);
            if (oldId != getAudioId()) {
                notifyChange(META_CHANGED);
            }
        }
    }

    public void stop() {
        stop(true);
    }

    private void notifyChange(String what) {
        if (D) {
            Log.d(TAG, "notifyChange: what = " + what);
        }
        if (SEND_PROGRESS.equals(what)) {
            Intent intent = new Intent(what);
            intent.putExtra("position", position());
            intent.putExtra("duration", duration());
            sendStickyBroadcast(intent);
            return;
        }

        // Update the lock screen controls
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            updateMediaSession(what);
        }

        if (what.equals(POSITION_CHANGED)) {
            return;
        }

        Intent intent = new Intent(what);
        intent.putExtra("id", getAudioId());
        intent.putExtra("artist", getArtistName());
        intent.putExtra("album", getAlbumName());
        intent.putExtra("track", getTrackName());
        intent.putExtra("playing", isPlaying());
        intent.putExtra("album_uri", getAlbumPath());
        intent.putExtra("is_local", isTrackLocal());

        sendStickyBroadcast(intent);
        Intent musicIntent = new Intent(intent);
        musicIntent.setAction(what.replace(FUN_MUSIC_PACKAGE_NAME, MUSIC_PACKAGE_NAME));
        sendStickyBroadcast(musicIntent);

        switch (what) {
            case META_CHANGED:
                mRecentStore.addSongId(getAudioId());
                mSongPlayCount.bumpSongCount(getAudioId());
                break;

            case QUEUE_CHANGED:
                Intent intent1 = new Intent("com.charles.funmusic.empty_playlist");
                intent.putExtra("show_or_hide", "show");
                sendBroadcast(intent1);
                saveQueue(true);
                if (isPlaying()) {

                    if (mNextPlayPos >= 0 && mNextPlayPos < mPlaylist.size()
                            && getShuffleMode() != SHUFFLE_NONE) {
                        setNextTrack(mNextPlayPos);
                    } else {
                        setNextTrack();
                    }
                }
                break;

            default:
                saveQueue(false);
                break;
        }

        if (what.equals(PLAY_STATE_CHANGED)) {
            updateNotification();
        }

    }

    private static final class MusicPlayerHandler extends Handler {
        private final WeakReference<MusicService> mService;
        private float mCurrentVolume = 1.0f;

        MusicPlayerHandler(MusicService service, Looper looper) {
            super(looper);
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            final MusicService service = mService.get();
            if (service == null) {
                return;
            }

            synchronized (service) {
                switch (msg.what) {
                    case FADE_DOWN:
                        mCurrentVolume -= .05f;
                        if (mCurrentVolume > .2f) {
                            sendEmptyMessageDelayed(FADE_DOWN, 10);
                        } else {
                            mCurrentVolume = .2f;
                        }
                        service.mPlayer.setVolume(mCurrentVolume);
                        break;

                    case FADE_UP:
                        mCurrentVolume += .01f;
                        if (mCurrentVolume < 1.0f) {
                            sendEmptyMessageDelayed(FADE_UP, 10);
                        } else {
                            mCurrentVolume = 1.0f;
                        }
                        service.mPlayer.setVolume(mCurrentVolume);
                        break;

                    case SERVER_DIED:
                        if (service.isPlaying()) {
                            final TrackErrorInfo info = (TrackErrorInfo) msg.obj;
                            service.sendErrorMessage(info.mTrackName);


                            service.removeTrack(info.mId);
                        } else {
                            service.openCurrentAndNext();
                        }
                        break;

                    case TRACK_WENT_TO_NEXT:
                        service.setAndRecordPlayPos(service.mNextPlayPos);
                        service.setNextTrack();
                        if (service.mCursor != null) {
                            service.mCursor.close();
                            service.mCursor = null;
                        }

                        service.updateCursor(service.mPlaylist.get(service.mPlayPos).getId());
                        service.notifyChange(META_CHANGED);
                        service.notifyChange(MUSIC_CHANGED);
                        service.updateNotification();
                        break;

                    case TRACK_ENDED:
                        if (service.mRepeatMode == REPEAT_CURRENT) {
                            service.seek(0);
                            service.play();
                        } else {
                            if (D) Log.d(TAG, "Going to  of track");
                            service.gotoNext(false);
                        }
                        break;

                    case RELEASE_WAKELOCK:
                        service.mWakeLock.release();
                        break;

                    case FOCUS_CHANGE:
                        if (D) {
                            Log.d(TAG, "Received audio focus change event " + msg.arg1);
                        }
                        switch (msg.arg1) {
                            case AudioManager.AUDIOFOCUS_LOSS:
                            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                                if (service.isPlaying()) {
                                    service.mPausedByTransientLossOfFocus =
                                            msg.arg1 == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
                                }
                                service.pause();
                                break;
                            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                                removeMessages(FADE_UP);
                                sendEmptyMessage(FADE_DOWN);
                                break;
                            case AudioManager.AUDIOFOCUS_GAIN:
                                if (!service.isPlaying()
                                        && service.mPausedByTransientLossOfFocus) {
                                    service.mPausedByTransientLossOfFocus = false;
                                    mCurrentVolume = 0f;
                                    service.mPlayer.setVolume(mCurrentVolume);
                                    service.play();
                                } else {
                                    removeMessages(FADE_DOWN);
                                    sendEmptyMessage(FADE_UP);
                                }
                                break;
                            default:
                        }
                        break;

                    case LRC_DOWNLOADED:
                        service.notifyChange(LRC_UPDATED);

                    default:
                        break;
                }
            }
        }
    }

    private static final class Shuffler {

        private final LinkedList<Integer> mHistoryOfNumbers = new LinkedList<>();
        private final TreeSet<Integer> mPreviousNumbers = new TreeSet<>();
        private final Random mRandom = new Random();
        private int mPrevious;

        Shuffler() {
            super();
        }

        int nextInt(final int interval) {
            int next;
            do {
                next = mRandom.nextInt(interval);
            } while (next == mPrevious && interval > 1
                    && !mPreviousNumbers.contains(next));
            mPrevious = next;
            mHistoryOfNumbers.add(mPrevious);
            mPreviousNumbers.add(mPrevious);
            cleanUpHistory();
            return next;
        }

        private void cleanUpHistory() {
            if (!mHistoryOfNumbers.isEmpty() && mHistoryOfNumbers.size() >= MAX_HISTORY_SIZE) {
                for (int i = 0; i < Math.max(1, MAX_HISTORY_SIZE / 2); i++) {
                    mPreviousNumbers.remove(mHistoryOfNumbers.removeFirst());
                }
            }
        }
    }

    private static final class TrackErrorInfo {
        private long mId;
        private String mTrackName;

        TrackErrorInfo(long id, String trackName) {
            mId = id;
            mTrackName = trackName;
        }
    }

    public interface TrackErrorExtra {
        String TRACK_NAME = "track_name";
    }

    private static final class MultiPlayer implements MediaPlayer.OnErrorListener,
            MediaPlayer.OnCompletionListener {

        private final WeakReference<MusicService> mService;
        private MediaPlayer mCurrentMediaPlayer = new MediaPlayer();
        private MediaPlayer mNextMediaPlayer;
        private Handler mHandler;
        private boolean mIsInitialized = false;
        private String mNextMediaPath;
        private boolean isFirstLoad = true;
        private int mSecondaryPosition = 0;
        private boolean mIsTrackPrepared = false;
        private boolean mIsTrackNet = false;
        private boolean mIsNextTrackPrepared = false;
        private boolean mIsNextInitialized = false;
        private boolean mIllegalState = false;

        MultiPlayer(MusicService service) {
            mService = new WeakReference<>(service);
            mCurrentMediaPlayer.setWakeMode(mService.get(), PowerManager.PARTIAL_WAKE_LOCK);
        }

        void setDataSource(String path) {
            mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, path);
            if (mIsInitialized) {
                setNextDataSource(null);
            }
        }

        void setNextDataSource(String path) {
            mNextMediaPath = null;
            mIsNextInitialized = false;
            try {
                mCurrentMediaPlayer.setNextMediaPlayer(null);
            } catch (IllegalArgumentException e) {
                Log.i(TAG, "Next media player is current one, continuing");
            } catch (IllegalStateException e) {
                Log.e(TAG, "Media player not initialized!");
                return;
            }
            if (mNextMediaPlayer != null) {
                mNextMediaPlayer.release();
                mNextMediaPlayer = null;
            }
            if (path == null) {
                return;
            }
            mNextMediaPlayer = new MediaPlayer();
            mNextMediaPlayer.setWakeMode(mService.get(), PowerManager.PARTIAL_WAKE_LOCK);
            mNextMediaPlayer.setAudioSessionId(getAudioSessionId());

            if (setNextDataSourceImpl(mNextMediaPlayer, path)) {
                mNextMediaPath = path;
                mCurrentMediaPlayer.setNextMediaPlayer(mNextMediaPlayer);
//                mHandler.post(mSetNextMediaPlayerIfPrepared);
            } else {
                if (mNextMediaPlayer != null) {
                    mNextMediaPlayer.release();
                    mNextMediaPlayer = null;
                }
            }
        }

        private boolean setDataSourceImpl(MediaPlayer player, String path) {
            mIsTrackNet = false;
            mIsTrackPrepared = false;
            try {
                player.reset();
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                if (path.startsWith("content://")) {
                    player.setOnPreparedListener(null);
                    player.setDataSource(AppCache.getContext(), Uri.parse(path));
                    player.prepare();
                    mIsTrackPrepared = true;
                    player.setOnCompletionListener(this);

                } else {
                    player.setDataSource(path);
                    player.setOnPreparedListener(mPreparedListener);
                    player.prepareAsync();
                    mIsTrackNet = true;
                }
                if (mIllegalState) {
                    mIllegalState = false;
                }

            } catch (final IOException todo) {

                return false;
            } catch (final IllegalArgumentException todo) {

                return false;
            } catch (final IllegalStateException todo) {
                todo.printStackTrace();
                if (!mIllegalState) {
                    Log.e(TAG, "CurrentMediaPlayer invoke IllegalState");
                    mCurrentMediaPlayer = null;
                    mCurrentMediaPlayer = new MediaPlayer();
                    mCurrentMediaPlayer.setWakeMode(mService.get(), PowerManager.PARTIAL_WAKE_LOCK);
                    mCurrentMediaPlayer.setAudioSessionId(getAudioSessionId());
                    setDataSourceImpl(mCurrentMediaPlayer, path);
                    mIllegalState = true;
                } else {
                    Log.e(TAG, "CurrentMediaPlayer invoke IllegalState ,and try set again failed ,setnext");
                    mIllegalState = false;
                    return false;
                }
            }

            player.setOnErrorListener(this);
            player.setOnBufferingUpdateListener(mBufferingUpdateListener);
            return true;
        }

        private boolean setNextDataSourceImpl(MediaPlayer player, String path) {

            mIsNextTrackPrepared = false;
            try {
                player.reset();
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                if (path.startsWith("content://")) {
                    player.setOnPreparedListener(mPreparedNextListener);
                    player.setDataSource(AppCache.getContext(), Uri.parse(path));
                    player.prepare();

                } else {
                    player.setDataSource(path);
                    player.setOnPreparedListener(mPreparedNextListener);
                    player.prepare();
                    mIsNextTrackPrepared = false;
                }

            } catch (final IOException todo) {

                return false;
            } catch (final IllegalArgumentException todo) {

                return false;
            }
            player.setOnCompletionListener(this);
            player.setOnErrorListener(this);
            //  player.setOnBufferingUpdateListener(this);
            return true;
        }

        MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (isFirstLoad) {
                    long seekPos = mService.get().mLastSeekPos;
                    Log.e(TAG, "seek_pos = " + seekPos);
                    seek(seekPos >= 0 ? seekPos : 0);
                    isFirstLoad = false;
                }
                // mService.get().notifyChange(TRACK_PREPARED);
                mService.get().notifyChange(META_CHANGED);
                mp.setOnCompletionListener(MultiPlayer.this);
                mIsTrackPrepared = true;
            }
        };

        MediaPlayer.OnPreparedListener mPreparedNextListener = new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mIsNextTrackPrepared = true;
            }
        };

        MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {

            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                if (mSecondaryPosition != 100)
                    mService.get().sendUpdateBuffer(percent);
                mSecondaryPosition = percent;
            }
        };

        Runnable mSetNextMediaPlayerIfPrepared = new Runnable() {
            int count = 0;

            @Override
            public void run() {
                if (mIsNextTrackPrepared && mIsInitialized) {
//                    mCurrentMediaPlayer.setNextMediaPlayer(mNextMediaPlayer);
                } else if (count < 60) {
                    mHandler.postDelayed(mSetNextMediaPlayerIfPrepared, 100);
                }
                count++;
            }
        };

        Runnable mStartMediaPlayerIfPrepared = new Runnable() {

            @Override
            public void run() {
                if (D) Log.d(TAG, "mIsTrackPrepared, " + mIsTrackPrepared);
                if (mIsTrackPrepared) {
                    mCurrentMediaPlayer.start();
                    final long duration = duration();
                    if (mService.get().mRepeatMode != REPEAT_CURRENT && duration > 2000
                            && position() >= duration - 2000) {
                        mService.get().gotoNext(true);
                        Log.e("play to go", "");
                    }
                    mService.get().loading(false);
                } else {
                    mHandler.postDelayed(mStartMediaPlayerIfPrepared, 700);
                }
            }
        };

        public void setHandler(Handler handler) {
            mHandler = handler;
        }

        public boolean isInitialized() {
            return mIsInitialized;
        }

        public boolean isTrackPrepared() {
            return mIsTrackPrepared;
        }

        public void start() {
            if (D) {
                Log.d(TAG, "mIsTrackNet, " + mIsTrackNet);
            }
            if (!mIsTrackNet) {
                mService.get().sendUpdateBuffer(100);
                mSecondaryPosition = 100;
                mCurrentMediaPlayer.start();
            } else {
                mSecondaryPosition = 0;
                mService.get().loading(true);
                mHandler.postDelayed(mStartMediaPlayerIfPrepared, 50);
            }
            mService.get().notifyChange(MUSIC_CHANGED);
        }

        public void stop() {
            mHandler.removeCallbacks(mSetNextMediaPlayerIfPrepared);
            mHandler.removeCallbacks(mStartMediaPlayerIfPrepared);
            mCurrentMediaPlayer.reset();
            mIsInitialized = false;
            mIsTrackPrepared = false;
        }

        public void release() {
            mCurrentMediaPlayer.release();
        }

        public void pause() {
            mHandler.removeCallbacks(mStartMediaPlayerIfPrepared);
            mCurrentMediaPlayer.pause();
        }

        public long position() {
            if (mIsTrackPrepared) {
                try {
                    return mCurrentMediaPlayer.getCurrentPosition();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return -1;
        }

        public long secondPosition() {
            if (mIsTrackPrepared) {
                return mSecondaryPosition;
            }
            return -1;
        }

        public long duration() {
            if (mIsTrackPrepared) {
                return mCurrentMediaPlayer.getDuration();
            }
            return -1;
        }

        public long seek(long whereTo) {
            mCurrentMediaPlayer.seekTo((int) whereTo);
            return whereTo;
        }

        public void setVolume(float volume) {
            try {
                mCurrentMediaPlayer.setVolume(volume, volume);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        public int getAudioSessionId() {
            return mCurrentMediaPlayer.getAudioSessionId();
        }

        public void setAudioSessionId(int sessionId) {
            mCurrentMediaPlayer.setAudioSessionId(sessionId);
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            Log.w(TAG, "completion");
            if (mp == mCurrentMediaPlayer && mNextMediaPlayer != null) {
                mCurrentMediaPlayer.release();
                mCurrentMediaPlayer = mNextMediaPlayer;
                mNextMediaPath = null;
                mNextMediaPlayer = null;
                mHandler.sendEmptyMessage(TRACK_WENT_TO_NEXT);
            } else {
                mService.get().mWakeLock.acquire(30000);
                mHandler.sendEmptyMessage(TRACK_ENDED);
                mHandler.sendEmptyMessage(RELEASE_WAKELOCK);
            }
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.w(TAG, "Music Server Error what: " + what + " extra: " + extra);
            switch (what) {
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    final MusicService service = mService.get();
                    final TrackErrorInfo errorInfo = new TrackErrorInfo(service.getAudioId(),
                            service.getTrackName());

                    mIsInitialized = false;
                    mIsTrackPrepared = false;
                    mCurrentMediaPlayer.release();
                    mCurrentMediaPlayer = new MediaPlayer();
                    mCurrentMediaPlayer.setWakeMode(service, PowerManager.PARTIAL_WAKE_LOCK);
                    Message msg = mHandler.obtainMessage(SERVER_DIED, errorInfo);
                    mHandler.sendMessageDelayed(msg, 2000);
                    return true;

                default:
                    break;
            }
            return false;
        }
    }

    private static final class ServiceStub extends IFunMusicService.Stub {

        private final WeakReference<MusicService> mService;

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
            try {
                super.onTransact(code, data, reply, flags);
            } catch (final RuntimeException e) {
                if (D) {
                    Log.e(TAG, "onTransact error");
                }
                e.printStackTrace();
                File file = new File(mService.get().getCacheDir().getAbsolutePath() + "/err/");
                if (!file.exists()) {
                    file.mkdirs();
                }
                try {
                    PrintWriter writer = new PrintWriter(mService.get().getCacheDir()
                            .getAbsolutePath() + "/err/" + System.currentTimeMillis() + "_aidl.log");
                    e.printStackTrace(writer);
                    writer.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

                throw e;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return true;
        }

        private ServiceStub(MusicService musicService) {
            mService = new WeakReference<>(musicService);
        }

        @Override
        public void openFile(String path) {
            mService.get().openFile(path);
        }

        @Override
        public void open(final Map infos, final long[] list, final int position) {
            mService.get().open((HashMap<Long, Music>) infos, list, position);
        }

        @Override
        public void stop() {
            mService.get().stop();
        }

        @Override
        public void pause() {
            mService.get().pause();
        }

        @Override
        public void play() {
            mService.get().play();
        }

        @Override
        public void prev(boolean forcePrevious) {
            mService.get().prev(forcePrevious);
        }

        @Override
        public void next() {
            mService.get().gotoNext(true);
        }

        @Override
        public void enqueue(long[] list, Map infos, int action) {
            mService.get().enqueue(list, (HashMap<Long, Music>) infos, action);
        }

        @Override
        public Map getPlayInfos() {
            return mService.get().getPlayInfos();
        }

        @Override
        public void setQueuePosition(int index) {
            mService.get().setQueuePosition(index);
        }

        @Override
        public void setShuffleMode(int shuffleMode) {
            mService.get().setShuffleMode(shuffleMode);
        }

        @Override
        public void setRepeatMode(int repeatMode) {
            mService.get().setRepeatMode(repeatMode);
        }

        @Override
        public void moveQueueItem(int from, int to) {
            mService.get().moveQueueItem(from, to);
        }

        @Override
        public void refresh() {
            mService.get().refresh();
        }

        @Override
        public void playlistChanged() {
            mService.get().playlistChanged();
        }

        @Override
        public boolean isPlaying() {
            return mService.get().isPlaying();
        }

        @Override
        public long[] getQueue() {
            return mService.get().getQueue();
        }

        @Override
        public long getQueueItemAtPosition(int position) {
            return mService.get().getQueueItemAtPosition(position);
        }

        @Override
        public int getQueueSize() {
            return mService.get().getQueueSize();
        }

        @Override
        public int getQueuePosition() {
            return mService.get().getQueuePosition();
        }

        @Override
        public int getQueueHistoryPosition(int position) {
            return mService.get().getQueueHistoryPosition(position);
        }

        @Override
        public int getQueueHistorySize() {
            return mService.get().getQueueHistorySize();
        }

        @Override
        public int[] getQueueHistoryList() {
            return mService.get().getQueueHistoryList();
        }

        @Override
        public long duration() {
            return mService.get().duration();
        }

        @Override
        public long position() {
            return mService.get().position();
        }

        @Override
        public int secondPosition() {
            return mService.get().getSecondPosition();
        }

        @Override
        public long seek(long position) {
            return mService.get().seek(position);
        }

        @Override
        public void seekRelative(long deltaInMs) {
            mService.get().seekRelative(deltaInMs);
        }

        @Override
        public long getAudioId() {
            return mService.get().getAudioId();
        }

        @Override
        public MusicPlaybackTrack getCurrentTrack() {
            return mService.get().getCurrentTrack();
        }

        @Override
        public MusicPlaybackTrack getTrack(int index) {
            return mService.get().getTrack(index);
        }

        @Override
        public long getNextAudioId() {
            return mService.get().getNextAudioId();
        }

        @Override
        public long getPreviousAudioId() {
            return mService.get().getPreviousAudioId();
        }

        @Override
        public long getArtistId() {
            return mService.get().getArtistId();
        }

        @Override
        public long getAlbumId() {
            return mService.get().getAlbumId();
        }

        @Override
        public String getArtistName() {
            return mService.get().getArtistName();
        }

        @Override
        public String getTrackName() {
            return mService.get().getTrackName();
        }

        @Override
        public boolean isTrackLocal() {
            return mService.get().isTrackLocal();
        }

        @Override
        public String getAlbumName() {
            return mService.get().getAlbumName();
        }

        @Override
        public String getAlbumPath() {
            return mService.get().getAlbumPath();
        }

        @Override
        public String[] getAlbumPathAll() {
            return mService.get().getAlbumPathAll();
        }

        @Override
        public String getPath() {
            return mService.get().getPath();
        }

        @Override
        public int getShuffleMode() {
            return mService.get().getShuffleMode();
        }

        @Override
        public int removeTracks(final int first, final int last) {
            return mService.get().removeTracks(first, last);
        }

        @Override
        public int removeTrack(final long id) {
            return mService.get().removeTrack(id);
        }

        @Override
        public boolean removeTrackAtPosition(final long id, final int position) {
            return mService.get().removeTrackAtPosition(id, position);
        }

        @Override
        public int getRepeatMode() {
            return mService.get().getRepeatMode();
        }

        @Override
        public int getMediaMountedCount() {
            return mService.get().getMediaMountedCount();
        }

        @Override
        public int getAudioSessionId() {
            return mService.get().getAudioSessionId();
        }

        @Override
        public void setLockScreenAlbumArt(boolean enabled) {
            mService.get().setLockScreenAlbumArt(enabled);
        }

        @Override
        public void exit() {
            mService.get().exit();
        }

        @Override
        public void timer(int time) {
            mService.get().timer(time);
        }
    }

    private class MediaStoreObserver extends ContentObserver implements Runnable {

        private static final long REFRESH_DELAY = 500;
        private Handler mHandler;

        MediaStoreObserver(Handler handler) {
            super(handler);
            mHandler = handler;
        }

        @Override
        public void onChange(boolean selfChange) {
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, REFRESH_DELAY);
        }

        @Override
        public void run() {
            Log.e("ELEVEN", "calling refresh!");
            refresh();
        }
    }
}