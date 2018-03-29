package com.charles.funmusic.utils.comparator;

import com.charles.funmusic.model.Folder;

import java.util.Comparator;

public class FolderComparator implements Comparator<Folder> {

    @Override
    public int compare(Folder a1, Folder a2) {
        String py1 = a1.getFolderSort();
        String py2 = a2.getFolderSort();
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