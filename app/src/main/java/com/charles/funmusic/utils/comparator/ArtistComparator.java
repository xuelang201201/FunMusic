package com.charles.funmusic.utils.comparator;

import com.charles.funmusic.model.Artist;

import java.util.Comparator;

public class ArtistComparator implements Comparator<Artist> {

    @Override
    public int compare(Artist a1, Artist a2) {
        String py1 = a1.getArtistSort();
        String py2 = a2.getArtistSort();
        // 判断是否为空""  
        if (isEmpty(py1) && isEmpty(py2)) {
            return 0;
        }
        if (isEmpty(py1)) {
            return -1;
        }
        if (isEmpty(py2)) {
            return 1;
        }
        return py1.compareTo(py2);
    }

    private boolean isEmpty(String str) {
        return "".equals(str.trim());
    }
}  