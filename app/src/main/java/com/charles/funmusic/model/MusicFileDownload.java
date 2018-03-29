package com.charles.funmusic.model;

public class MusicFileDownload {

    private String mShowLink;
    private int mDownloadType;
    private int mOriginal;
    private int mFree;
    private String mReplayGain;
    private int mSongFileId;
    private int mFileSize;
    private String mFileExtension;
    private int mFileDuration;
    private int mCanSee;
    private boolean isCanLoad;
    private int mPreload;
    private int mFileBitrate;
    private String mFileLink;
    private int isUditionUrl;
    private String mHash;

    public String getShowLink() {
        return mShowLink;
    }

    public void setShowLink(String showLink) {
        mShowLink = showLink;
    }

    public int getDownloadType() {
        return mDownloadType;
    }

    public void setDownloadType(int downloadType) {
        mDownloadType = downloadType;
    }

    public int getOriginal() {
        return mOriginal;
    }

    public void setOriginal(int original) {
        mOriginal = original;
    }

    public int getFree() {
        return mFree;
    }

    public void setFree(int free) {
        mFree = free;
    }

    public String getReplayGain() {
        return mReplayGain;
    }

    public void setReplayGain(String replayGain) {
        mReplayGain = replayGain;
    }

    public int getSongFileId() {
        return mSongFileId;
    }

    public void setSongFileId(int songFileId) {
        mSongFileId = songFileId;
    }

    public int getFileSize() {
        return mFileSize;
    }

    public void setFileSize(int fileSize) {
        mFileSize = fileSize;
    }

    public String getFileExtension() {
        return mFileExtension;
    }

    public void setFileExtension(String fileExtension) {
        mFileExtension = fileExtension;
    }

    public int getFileDuration() {
        return mFileDuration;
    }

    public void setFileDuration(int fileDuration) {
        mFileDuration = fileDuration;
    }

    public int getCanSee() {
        return mCanSee;
    }

    public void setCanSee(int canSee) {
        mCanSee = canSee;
    }

    public boolean isCanLoad() {
        return isCanLoad;
    }

    public void setCanLoad(boolean canLoad) {
        isCanLoad = canLoad;
    }

    public int getPreload() {
        return mPreload;
    }

    public void setPreload(int preload) {
        mPreload = preload;
    }

    public int getFileBitrate() {
        return mFileBitrate;
    }

    public void setFileBitrate(int fileBitrate) {
        mFileBitrate = fileBitrate;
    }

    public String getFileLink() {
        return mFileLink;
    }

    public void setFileLink(String fileLink) {
        mFileLink = fileLink;
    }

    public int getIsUditionUrl() {
        return isUditionUrl;
    }

    public void setIsUditionUrl(int isUditionUrl) {
        this.isUditionUrl = isUditionUrl;
    }

    public String getHash() {
        return mHash;
    }

    public void setHash(String hash) {
        mHash = hash;
    }
}
