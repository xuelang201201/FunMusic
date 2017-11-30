package com.charles.funmusic.model;

public class Music {

    /**
     * 歌曲类型：本地/网络
     */
    private Type mType;
    /**
     * [本地歌曲]歌曲id
     */
    private long mId;
    /**
     * 音乐标题
     */
    private String mTitle;
    /**
     * 音乐标题的拼音
     */
    private String mTitlePinyin;
    /**
     * 艺术家
     */
    private String mArtist;
    /**
     * 艺术家的拼音
     */
    private String mArtistPinyin;
    /**
     * 专辑
     */
    private String mAlbum;
    /**
     * 专辑的拼音
     */
    private String mAlbumPinyin;
    /**
     * [本地歌曲]专辑id
     */
    private long mAlbumId;
    /**
     * [在线歌曲]专辑封面路径
     */
    private String mCoverUrl;
    /**
     * 持续时间
     */
    private long mDuration;
    /**
     * 音乐路径
     */
    private String mUrl;
    /**
     * 文件名
     */
    private String mFileName;
    /**
     * 文件大小
     */
    private long mFileSize;

    private String mSortLetter;

    public enum Type {
        LOCAL,
        ONLINE
    }

    public Type getType() {
        return mType;
    }

    public void setType(Type type) {
        mType = type;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTitlePinyin() {
        return mTitlePinyin;
    }

    public void setTitlePinyin(String titlePinyin) {
        mTitlePinyin = titlePinyin;
    }

    public String getArtist() {
        return mArtist;
    }

    public void setArtist(String artist) {
        mArtist = artist;
    }

    public String getArtistPinyin() {
        return mArtistPinyin;
    }

    public void setArtistPinyin(String artistPinyin) {
        mArtistPinyin = artistPinyin;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public void setAlbum(String album) {
        mAlbum = album;
    }

    public String getAlbumPinyin() {
        return mAlbumPinyin;
    }

    public void setAlbumPinyin(String albumPinyin) {
        mAlbumPinyin = albumPinyin;
    }

    public long getAlbumId() {
        return mAlbumId;
    }

    public void setAlbumId(long albumId) {
        mAlbumId = albumId;
    }

    public String getCoverUrl() {
        return mCoverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        mCoverUrl = coverUrl;
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        mDuration = duration;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    public long getFileSize() {
        return mFileSize;
    }

    public void setFileSize(long fileSize) {
        mFileSize = fileSize;
    }

    public String getSortLetter() {
        return mSortLetter;
    }

    public void setSortLetter(String sortLetter) {
        mSortLetter = sortLetter;
    }

    /**
     * 对比本地歌曲是否相同
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Music && this.getId() == ((Music) obj).getId();
    }
}