package com.charles.funmusic.utils.comparator;

import com.charles.funmusic.model.Folder;

import java.util.Comparator;

public class FolderCountComparator implements Comparator<Folder> {

    @Override
    public int compare(Folder a1, Folder a2) {
        Integer py1 = a1.getFolderCount();
        Integer py2 = a2.getFolderCount();
        return py2.compareTo(py1);
    }

    private boolean isEmpty(String str) {
        return "".equals(str.trim());
    }
}  