package com.charles.funmusic.fragment;

import android.os.Bundle;

import com.charles.funmusic.model.Music;
import com.charles.funmusic.provider.PlaylistInfo;
import com.charles.funmusic.provider.PlaylistManager;

import java.util.ArrayList;

public class AddNetPlaylistFragment extends AttachDialogFragment {
    private PlaylistInfo mPlaylistInfo;
    private PlaylistManager mPlaylistManager;

    public static AddNetPlaylistFragment newInstance(ArrayList<Music> musics, String author) {
        AddNetPlaylistFragment fragment = new AddNetPlaylistFragment();
        Bundle args = new Bundle();
        args.putString("author", author);
        args.putParcelableArrayList("songs", musics);
        fragment.setArguments(args);
        return fragment;
    }

    public static AddNetPlaylistFragment newInstance(ArrayList<Music> musics) {
        AddNetPlaylistFragment fragment = new AddNetPlaylistFragment();
        Bundle args = new Bundle();
        args.putString("author", "local");
        args.putParcelableArrayList("songs", musics);
        fragment.setArguments(args);
        return fragment;
    }
}
