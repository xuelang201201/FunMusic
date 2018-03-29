package com.charles.funmusic.net;

import com.charles.funmusic.json.MusicDetailInfo;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaylistPlayInfoGet {
    private static ArrayList<MusicDetailInfo> arrayList;

    public PlaylistPlayInfoGet(ArrayList<MusicDetailInfo> arrayList) {
        PlaylistPlayInfoGet.arrayList = arrayList;
    }

    private static ExecutorService pool = Executors.newFixedThreadPool(10);

    public static void get(MusicDetailInfoGet musicDetailInfoGet) {
        pool.execute(musicDetailInfoGet);
    }
}
