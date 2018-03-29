package com.charles.funmusic.net;

import android.util.Log;
import android.util.SparseArray;

import com.charles.funmusic.application.AppCache;
import com.charles.funmusic.json.MusicDetailInfo;
import com.google.gson.JsonObject;

public class MusicDetailInfoGet implements Runnable {
    private String id;
    private int p;
    private SparseArray<MusicDetailInfo> arrayList;

    public MusicDetailInfoGet(String id, int position, SparseArray<MusicDetailInfo> arrayList) {
        this.id = id;
        p = position;
        this.arrayList = arrayList;
    }

    @Override
    public void run() {
        try {
            MusicDetailInfo info;
            JsonObject jsonObject = HttpUtil.getResposeJsonObject(BMA.Song.songBaseInfo(id).trim()).get("result")
                    .getAsJsonObject().get("items").getAsJsonArray().get(0).getAsJsonObject();
            info = AppCache.gsonInstance().fromJson(jsonObject, MusicDetailInfo.class);
            synchronized (this) {
                Log.e("arraylist", "size" + arrayList.size());
                arrayList.put(p, info);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}