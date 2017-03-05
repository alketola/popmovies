package com.mobilitio.popmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Antti on 2017-02-22.
 */

public class PopMoviesDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "popmovies.db";
    public static final int DB_VERSION = 3;
    private static SQLiteDatabase.CursorFactory factory = null;

    public PopMoviesDbHelper(Context context) {
        super(context, DB_NAME, factory, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) { // TODO GET FROM DB CONTRACT
        final String DB_CREATION_SQL = "CREATE TABLE " +
                PopMoviesDbContract.MovieEntry.TABLE + " (" +
                PopMoviesDbContract.MovieEntry.COLUMN_MOVIE_ID + " INTEGER UNIQUE PRIMARY KEY NOT NULL," +
                PopMoviesDbContract.MovieEntry.COLUMN_FAVOURITE + " BOOLEAN," +
                PopMoviesDbContract.MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL," +
                PopMoviesDbContract.MovieEntry.COLUMN_OVERVIEW + " TEXT," +
                PopMoviesDbContract.MovieEntry.COLUMN_VOTE_AVERAGE + " REAL," +
                PopMoviesDbContract.MovieEntry.COLUMN_POSTER_PATH + " TEXT," +
                PopMoviesDbContract.MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL," +
                PopMoviesDbContract.MovieEntry.COLUMN_TITLE + " TEXT" +
                ");";
        Log.d("PopMDbHelperCrea", DB_CREATION_SQL);
        sqLiteDatabase.execSQL(DB_CREATION_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        String DB_DELETION_SQL = "DROP TABLE IF EXISTS " + PopMoviesDbContract.MovieEntry.TABLE + ";";
        if (newVersion > oldVersion) { // perform upgrade
            sqLiteDatabase.execSQL(DB_DELETION_SQL); //DEFAULT: drop it and create new!
            onCreate(sqLiteDatabase);
        }
    }
}
