package com.charles.funmusic.utils.comparator;

import com.charles.funmusic.model.Music;

import java.util.Comparator;

public class MusicComparator implements Comparator<Music> {

    @Override
    public int compare(Music m1, Music m2) {
        String py1 = m1.getSort();
        String py2 = m2.getSort();
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