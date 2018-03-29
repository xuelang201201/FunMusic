// IFunMusicService.aidl
package com.charles.funmusic;

// Declare any non-default types here with import statements
import com.charles.funmusic.helper.MusicPlaybackTrack;

// 不能设置同名不同参的方法，也不能重复设置方法，否则编译无法通过
interface IFunMusicService {
    void openFile(String path);
    void open(in Map infos, in long [] list, int position);
    void stop();
    void pause();
    void play();
    void prev(boolean forcePrevious);
    void next();
    void enqueue(in long [] list, in Map infos, int action);
    Map getPlayInfos();
    void setQueuePosition(int index);
    void setShuffleMode(int shuffleMode);
    void setRepeatMode(int repeatMode);
    void moveQueueItem(int from, int to);
    void refresh();
    void playlistChanged();
    boolean isPlaying();
    long [] getQueue();
    long getQueueItemAtPosition(int position);
    int getQueueSize();
    int getQueuePosition();
    int getQueueHistoryPosition(int position);
    int getQueueHistorySize();
    int [] getQueueHistoryList();
    long duration();
    long position();
    int secondPosition();
    long seek(long pos);
    void seekRelative(long deltaInMs);
    long getAudioId();
    MusicPlaybackTrack getCurrentTrack();
    MusicPlaybackTrack getTrack(int index);
    long getNextAudioId();
    long getPreviousAudioId();
    long getArtistId();
    long getAlbumId();
    String getArtistName();
    String getTrackName();
    boolean isTrackLocal();
    String getAlbumName();
    String getAlbumPath();
    String [] getAlbumPathAll();
    String getPath();
    int getShuffleMode();
    int removeTracks(int first, int last);
    int removeTrack(long id);
    boolean removeTrackAtPosition(long id, int position);
    int getRepeatMode();
    int getMediaMountedCount();
    int getAudioSessionId();
    void setLockScreenAlbumArt(boolean enabled);
    void exit();
    void timer(int time);
}