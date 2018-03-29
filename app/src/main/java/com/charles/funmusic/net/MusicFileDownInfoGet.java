package com.charles.funmusic.net;

import android.util.SparseArray;

import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.json.MusicFileDownInfo;
import com.google.gson.JsonArray;

public class MusicFileDownInfoGet implements Runnable {
    private String id;
    private int p;
    private SparseArray<MusicFileDownInfo> arrayList;
    private int downloadBit;

    public MusicFileDownInfoGet(String id, int position, SparseArray<MusicFileDownInfo> arrayList, int bit) {
        this.id = id;
        p = position;
        this.arrayList = arrayList;
        downloadBit = bit;
    }

    @Override
    public void run() {
        try {
            JsonArray jsonArray = HttpUtil.getResposeJsonObject(BMA.Song.songInfo(id)).get("songurl")
                    .getAsJsonObject().get("url").getAsJsonArray();
            int len = jsonArray.size();

            MusicFileDownInfo musicFileDownInfo = null;
            for (int i = len - 1; i > -1; i--) {
                int bit = Integer.parseInt(jsonArray.get(i).getAsJsonObject().get("file_bitrate").toString());
                if (bit == downloadBit) {
                    musicFileDownInfo = AppCache.gsonInstance().fromJson(jsonArray.get(i), MusicFileDownInfo.class);
                } else if (bit < downloadBit && bit >= 64) {
                    musicFileDownInfo = AppCache.gsonInstance().fromJson(jsonArray.get(i), MusicFileDownInfo.class);
                }
            }

            synchronized (this) {
                if (musicFileDownInfo != null) {
                    arrayList.put(p, musicFileDownInfo);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}