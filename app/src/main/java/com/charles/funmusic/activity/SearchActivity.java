package com.charles.funmusic.activity;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
