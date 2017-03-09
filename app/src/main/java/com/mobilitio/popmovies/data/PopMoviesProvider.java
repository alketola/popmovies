package com.mobilitio.popmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Antti on 2017-02-22.
 */

public class PopMoviesProvider extends ContentProvider {

    // constants
    public static final int URI_SELECTOR_ONE_MOVIE = 100;
    public static final int URI_SELECTOR_ONE_MOVIE_WITH_ID = 101;
    public static final int URI_SELECTOR_ALL_MOVIES = 103;

    private static final UriMatcher mUriMatcher = buildUriMatcher();
    private PopMoviesDbHelper mDbHelper;

    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        //Ex: content://com.mobilitio.popmovies/movie/
        uriMatcher.addURI(PopMoviesDbContract.AUTHORITY, PopMoviesDbContract.MOVIE_PATH, URI_SELECTOR_ONE_MOVIE);
        //Ex: content://com.mobilitio.popmovies/movie/12345
        uriMatcher.addURI(PopMoviesDbContract.AUTHORITY, PopMoviesDbContract.MOVIE_PATH + "/#", URI_SELECTOR_ONE_MOVIE_WITH_ID);
        //Ex: content://com.mobilitio.popmovies/allmovies
        uriMatcher.addURI(PopMoviesDbContract.AUTHORITY, PopMoviesDbContract.ALL_MOVIES_PATH, URI_SELECTOR_ALL_MOVIES);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new PopMoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] p1, String s1, String[] strings1, String s2) {
        Cursor returnedCursor = null;
        String[] columns = new String[]{
                PopMoviesDbContract.MovieEntry.COLUMN_MOVIE_ID,
                PopMoviesDbContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
                PopMoviesDbContract.MovieEntry.COLUMN_POSTER_PATH,
                PopMoviesDbContract.MovieEntry.COLUMN_RELEASE_DATE,
                PopMoviesDbContract.MovieEntry.COLUMN_VOTE_AVERAGE,
                PopMoviesDbContract.MovieEntry.COLUMN_OVERVIEW,
                PopMoviesDbContract.MovieEntry.COLUMN_TITLE
        };

        String selection;
        String[] selectionArgs;
        String groupBy = null;
        String having = null;
        String orderBy = PopMoviesDbContract.MovieEntry.COLUMN_MOVIE_ID + " ASC";
        SQLiteDatabase db;
        switch (mUriMatcher.match(uri)) {
            case URI_SELECTOR_ONE_MOVIE_WITH_ID:

                String movie_id = uri.getLastPathSegment().toString();

                selection = PopMoviesDbContract.MovieEntry.COLUMN_MOVIE_ID + "=?";
                selectionArgs = new String[]{movie_id};
                db = mDbHelper.getReadableDatabase();
                returnedCursor = db.query(
                        PopMoviesDbContract.MovieEntry.TABLE,
                        columns,
                        selection,
                        selectionArgs,
                        groupBy, having, orderBy);

                break;
            case URI_SELECTOR_ALL_MOVIES: {
                Log.d("POPMOVIESPROVIDE", "case URI_SELECTOR_ALL_MOVIES");
                selection = null;
                selectionArgs = null;
                db = mDbHelper.getReadableDatabase();
                returnedCursor = db.query(
                        PopMoviesDbContract.MovieEntry.TABLE,
                        columns,
                        selection,
                        selectionArgs,
                        groupBy, having, orderBy);
            }
            break;
            default:
                Log.d("PopMoviesProvider", "Query, bad match for URI:" + mUriMatcher.match(uri));
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
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Uri retval = null;

        int matchInt = mUriMatcher.match(uri);
        switch (matchInt) {
            case URI_SELECTOR_ONE_MOVIE:
                long inserted = db.insert(PopMoviesDbContract.MovieEntry.TABLE, null, contentValues);

                if (inserted > 0) {
                    retval = ContentUris.withAppendedId(PopMoviesDbContract.MovieEntry.CONTENT_URI, inserted);
                } else {
                    throw new android.database.SQLException("DB did not accept insertion of a row, uri:" + uri);
                }
                db.close();
                break;

            default:
                try {
                    Log.d("PopMoviesProvider", "Insert, bad match for URI:" + uri.toString());
                } catch (Exception e) {
                    Log.w("PopMoviesProvider", e.toString());
                }
        }

        return retval;
    }

    @Override
    public int delete(Uri uri, String whereClause, String[] whereArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int deletedCount = 0;

        int matchInt = mUriMatcher.match(uri);
        switch (matchInt) {
            case URI_SELECTOR_ONE_MOVIE_WITH_ID:
                String movieId = uri.getPathSegments().get(1);
                String selection = PopMoviesDbContract.MovieEntry.COLUMN_MOVIE_ID + "=?";
                String selectionArgs[] = new String[]{movieId};
                deletedCount = db.delete(PopMoviesDbContract.MovieEntry.TABLE,
                        selection, selectionArgs);
                db.close();
                break;
            case URI_SELECTOR_ALL_MOVIES: // Warning, delete all
                String all = " * ";
                deletedCount = db.delete(PopMoviesDbContract.MovieEntry.TABLE,
                        all, null);
                db.close();
                break;
            default:
                Log.d("PopMoviesProvider", "Delete, bad match for URI:" + deletedCount);
        }

        return deletedCount;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String w, String[] wArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int updatedCount = 0;

        int matchInt = mUriMatcher.match(uri);
        switch (matchInt) {
            case URI_SELECTOR_ONE_MOVIE_WITH_ID:
                String movieId = uri.getPathSegments().get(1);
                String selection = PopMoviesDbContract.MovieEntry.COLUMN_MOVIE_ID + "=?";
                String selectionArgs[] = new String[]{movieId};
                updatedCount = db.update(PopMoviesDbContract.MovieEntry.TABLE,
                        contentValues, selection, selectionArgs);
                db.close();
                break;
            default:
                Log.d("PopMoviesProvider", "Update, bad match for URI:" + updatedCount);
        }

        return updatedCount;
    }
}
