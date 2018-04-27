package com.charles.funmusic.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.charles.funmusic.R;
import com.charles.funmusic.adapter.SearchAdapter;
import com.charles.funmusic.model.Music;
import com.charles.funmusic.provider.SearchHistory;
import com.charles.funmusic.utils.ScreenUtil;
import com.charles.funmusic.utils.SearchUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LocalSearchActivity extends BaseActivity implements SearchView.OnQueryTextListener, View.OnTouchListener {

    @BindView(R.id.activity_local_search_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.activity_local_search_toolbar)
    Toolbar mToolbar;

    private SearchView mSearchView;
    private String mQueryStr;
    private SearchAdapter mAdapter;

    private List<Music> mSearchResults = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_search);
        ButterKnife.bind(this);

        mToolbar.setPadding(0, ScreenUtil.getStatusHeight(this), 0, 0);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new SearchAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);

        mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_search));

        mSearchView.setOnQueryTextListener(this);
        mSearchView.setQueryHint(getResources().getString(R.string.search_local));

        mSearchView.setIconifiedByDefault(false);
        mSearchView.setIconified(false);

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.menu_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                finish();
                return false;
            }
        });

        menu.findItem(R.id.menu_search).expandActionView();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        onQueryTextChange(query);
        hideSoftInput();
        mSearchView.clearFocus();
        SearchHistory.getInstance(this).addSerachString(mQueryStr);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText.equals(mQueryStr)) {
            return true;
        }
        mQueryStr = newText;
        if (!mQueryStr.trim().equals("")) {
            mSearchResults = new ArrayList<>();
            List<Music> songs = SearchUtil.searchSongs(this, mQueryStr);

            mSearchResults.addAll(songs.size() < 100 ? songs : songs.subList(0, 100));
        } else {
            mSearchResults.clear();
            mAdapter.updateSearchResults(mSearchResults);
            mAdapter.notifyDataSetChanged();
        }

        mAdapter.updateSearchResults(mSearchResults);
        mAdapter.notifyDataSetChanged();

        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        hideSoftInput();
        mSearchView.clearFocus();
        SearchHistory.getInstance(this).addSerachString(mQueryStr);
        return false;
    }
}
