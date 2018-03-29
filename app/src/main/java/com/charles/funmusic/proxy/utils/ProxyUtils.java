package com.charles.funmusic.proxy.utils;

import android.os.StatFs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProxyUtils {
    private static final String LOG_TAG = ProxyUtils.class.getSimpleName();

    /**
     * 删除多余的缓存文件
     *
     * @param max 缓存文件的最大数量
     */
    static void asyncRemoveBufferFile(final int max) {
        new Thread() {
            public void run() {
                List<File> lstBufferFile = getFilesSortByDate(Constants.DOWNLOAD_PATH);
                while (lstBufferFile.size() > max) {
                    lstBufferFile.get(0).delete();
                    lstBufferFile.remove(0);
                }
            }
        }.start();
    }

    /**
     * 获取外部存储器可用的空间
     */
    static long getAvailableSize(String dir) {
        StatFs stat = new StatFs(dir);// path.getPath());
        long totalBlocks = stat.getBlockCount();// 获取block数量
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize; // 获取可用大小
    }

    /**
     * 获取文件夹内的文件，按日期排序，从旧到新
     */
    static private List<File> getFilesSortByDate(String dirPath) {
        List<File> result = new ArrayList<>();
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0)
            return result;

        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            }
        });

        Collections.addAll(result, files);
        return result;
    }

    public static String getExceptionMessage(Exception ex) {
        StringBuilder result = new StringBuilder();
        StackTraceElement[] stackTraceElements = ex.getStackTrace();
        for (StackTraceElement ste : stackTraceElements) {
            result.append(ste.getClassName()).append(".").append(ste.getMethodName()).append("  ").append(ste.getLineNumber()).append("line").append("\r\n");
        }
        return result.toString();
    }

}
