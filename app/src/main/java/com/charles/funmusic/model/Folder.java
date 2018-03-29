package com.charles.funmusic.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class Folder implements Parcelable {

    private static final String KEY_FOLDER_NAME = "folder_name";
    private static final String KEY_FOLDER_PATH = "folder_path";
    private static final String KEY_FOLDER_SORT = "folder_sort";
    private static final String KEY_FOLDER_FILE_COUNT = "file_count";

    private String mFolderName;
    private String mFolderPath;
    private String mFolderSort;
    private int mFolderCount;

    public String getFolderName() {
        return mFolderName;
    }

    public void setFolderName(String folderName) {
        mFolderName = folderName;
    }

    public String getFolderPath() {
        return mFolderPath;
    }

    public void setFolderPath(String folderPath) {
        mFolderPath = folderPath;
    }

    public String getFolderSort() {
        return mFolderSort;
    }

    public void setFolderSort(String folderSort) {
        mFolderSort = folderSort;
    }

    public int getFolderCount() {
        return mFolderCount;
    }

    public void setFolderCount(int folderCount) {
        mFolderCount = folderCount;
    }

    // 用来创建自定义的Parcelable的对象
    public static Creator<Folder> CREATOR = new Creator<Folder>() {

        @Override
        public Folder createFromParcel(Parcel source) {
            Folder folder = new Folder();
            Bundle bundle = source.readBundle(getClass().getClassLoader());
            folder.mFolderName = bundle.getString(KEY_FOLDER_NAME);
            folder.mFolderPath = bundle.getString(KEY_FOLDER_PATH);
            folder.mFolderSort = bundle.getString(KEY_FOLDER_SORT);
            folder.mFolderCount = bundle.getInt(KEY_FOLDER_FILE_COUNT);
            return folder;
        }

        @Override
        public Folder[] newArray(int size) {
            return new Folder[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_FOLDER_NAME, mFolderName);
        bundle.putString(KEY_FOLDER_PATH, mFolderPath);
        bundle.putString(KEY_FOLDER_SORT, mFolderSort);
        bundle.putInt(KEY_FOLDER_FILE_COUNT, mFolderCount);
        dest.writeBundle(bundle);
    }

}
