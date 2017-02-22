package com.mobilitio.popmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by Antti on 2017-02-22.
 */

public class PopMoviesProvider extends ContentProvider {

    // constants
    public static final int URI_SELECTOR_ONE_MOVIE = 100;

    private static final UriMatcher mUriMatcher = buildUriMatcher();
    private PopMoviesDbHelper mDbHelper;

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        //Ex: content://com.mobilitio.popmovies/movie/12345
        uriMatcher.addURI(PopMoviesDbContract.AUTHORITY, PopMoviesDbContract.MOVIE_PATH + "/#", URI_SELECTOR_ONE_MOVIE);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        return false;
//        mDbHelper = new PopMoviesDbHelper(getContext());
//        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] strings1, String s1) {
        Cursor returnedCursor = null;

        switch (mUriMatcher.match(uri)) {
            case URI_SELECTOR_ONE_MOVIE:

                String movie_id = uri.getLastPathSegment().toString();
                String[] columns = projection;
                String[] selectionArgs = projection;
                String groupBy = null;
                String having = null;
                String orderBy = PopMoviesDbContract.MovieEntry.COLUMN_MOVIE_ID + " ASC";
                returnedCursor = mDbHelper.getReadableDatabase().query(
                        PopMoviesDbContract.MovieEntry.TABLE,
                        columns,
                        selection,
                        selectionArgs,
                        groupBy, having, orderBy);
                break;
            default:
                ;
        }

        return returnedCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
