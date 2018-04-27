package com.charles.funmusic.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class SearchHistory {
    private static final int MAX_ITEMS_IN_DB = 25;
    private static SearchHistory sInstance = null;
    private DatabaseHelper mDatabaseHelper;

    private SearchHistory(final Context context) {
        mDatabaseHelper = DatabaseHelper.getInstance(context);
    }

    public static synchronized SearchHistory getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new SearchHistory(context.getApplicationContext());
        }
        return sInstance;
    }

    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + SearchHistoryColumns.NAME + "("
                + SearchHistoryColumns.SEARCH_STRING + " TEXT NOT NULL,"
                + SearchHistoryColumns.TIME_SEARCHED + " LONG NOT NULL);");
    }

    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SearchHistoryColumns.NAME);
        onCreate(db);
    }

    public void addSerachString(final String searchString) {
        if (searchString == null) {
            return;
        }

        String trimmedString = searchString.trim();

        if (trimmedString.isEmpty()) {
            return;
        }

        final SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        database.beginTransaction();

        try {
            database.delete(SearchHistoryColumns.NAME,
                    SearchHistoryColumns.SEARCH_STRING + " = ? COLLATE NOCASE",
                    new String[]{trimmedString});

            final ContentValues values = new ContentValues(2);
            values.put(SearchHistoryColumns.SEARCH_STRING, trimmedString);
            values.put(SearchHistoryColumns.TIME_SEARCHED, System.currentTimeMillis());
            database.insert(SearchHistoryColumns.NAME, null, values);

            Cursor oldest = null;
            try {
                database.query(SearchHistoryColumns.NAME, new String[]{SearchHistoryColumns.TIME_SEARCHED},
                        null, null, null, null,
                        SearchHistoryColumns.TIME_SEARCHED + " ASC");

                if (oldest != null && oldest.getCount() > MAX_ITEMS_IN_DB) {
                    oldest.moveToPosition(oldest.getCount() - MAX_ITEMS_IN_DB);
                    long timeOfRecordToKeep = oldest.getLong(0);

                    database.delete(SearchHistoryColumns.NAME,
                            SearchHistoryColumns.TIME_SEARCHED + " < ?",
                            new String[]{String.valueOf(timeOfRecordToKeep)});
                }
            } finally {
                if (oldest != null) {
                    oldest.close();
                }
            }
        } finally {
            database.setTransactionSuccessful();
            database.endTransaction();
        }
    }

    public void deleteRecentSearches(String name) {
        final SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        database.delete(SearchHistoryColumns.NAME,
                SearchHistoryColumns.SEARCH_STRING + " = ?",
                new String[]{name});
    }

    public Cursor queryRecentSearches(final String limit) {
        final SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        return database.query(SearchHistoryColumns.NAME,
                new String[]{SearchHistoryColumns.SEARCH_STRING},
                null, null, null, null,
                SearchHistoryColumns.TIME_SEARCHED + " DESC", limit);
    }

    public ArrayList<String> getRecentSearches() {
        Cursor searches = queryRecentSearches(String.valueOf(MAX_ITEMS_IN_DB));

        ArrayList<String> results = new ArrayList<>(MAX_ITEMS_IN_DB);

        try {
            if (searches != null && searches.moveToFirst()) {
                int colIdx = searches.getColumnIndex(SearchHistoryColumns.SEARCH_STRING);
                do {
                    results.add(searches.getString(colIdx));
                } while (searches.moveToNext());
            }
        } finally {
            if (searches != null) {
                searches.close();
            }
        }
        return results;
    }

    public interface SearchHistoryColumns {
        /* Table name */
        String NAME = "search_history";

        /* What was searched */
        String SEARCH_STRING = "search_string";

        /* Time of search */
        String TIME_SEARCHED = "time_searched";
    }


}
