package com.charles.funmusic.proxy;

import android.content.Context;
import android.util.Log;

import com.charles.funmusic.constant.Keys;
import com.charles.funmusic.proxy.db.CacheFileInfoDao;
import com.charles.funmusic.proxy.utils.Constants;
import com.charles.funmusic.proxy.utils.HttpUtils;
import com.charles.funmusic.proxy.utils.ProxyFileUtils;
import com.charles.funmusic.proxy.utils.RequestDealThread;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class PreLoad extends Thread {

    private static final String LOG_TAG = RequestDealThread.class.getSimpleName();

    private CacheFileInfoDao mCacheFileInfoDao = CacheFileInfoDao.getInstance();
    private ProxyFileUtils mProxyFileUtils;
    private URL mUrl;

    public PreLoad(Context context, String url) {
        try {
            this.mUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        URI uri = URI.create(url);
        mProxyFileUtils = ProxyFileUtils.getInstance(context, uri, false);
    }

    public boolean download(int size) {
        try {
            Log.i(LOG_TAG, "缓存开始");
            //
            if (!mProxyFileUtils.isEnable()) {
                return false;
            }
            // 得到文件长度，如果超过缓冲给定长度，则返回
            int fileLength = mProxyFileUtils.getLength();
            if (fileLength >= size) {
                return true;
            }
            // 如果已经下载完成，返回
            System.out.println(mProxyFileUtils.getLength() + " " + mCacheFileInfoDao.getFileSize(mProxyFileUtils.getFileName()));
            if (mProxyFileUtils.getLength() == mCacheFileInfoDao.getFileSize(mProxyFileUtils.getFileName())) {
                return true;
            }
            // 从之前的位置缓存
            HttpURLConnection response = HttpUtils.send(mUrl.openConnection());
            if (response == null) {
                return false;
            }
            int contentLength = Integer.valueOf(response.getHeaderField(Constants.CONTENT_LENGTH));
            mCacheFileInfoDao.insertOrUpdate(mProxyFileUtils.getFileName(), contentLength);
            //
            InputStream data = response.getInputStream();
            byte[] buff = new byte[1024 * 40];
            int readBytes = 0;
            int fileSize = 0;
            while (mProxyFileUtils.isEnable() && (readBytes = data.read(buff, 0, buff.length)) != -1) {
                mProxyFileUtils.write(buff, readBytes);
                fileSize += readBytes;
                if (fileSize >= size) {
                    break;
                }
            }
            if (mProxyFileUtils.isEnable()) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "缓存异常", e);
            return false;
        } finally {
            Log.i(LOG_TAG, "缓存结束");
            ProxyFileUtils.close(mProxyFileUtils, false);
        }
    }
}
