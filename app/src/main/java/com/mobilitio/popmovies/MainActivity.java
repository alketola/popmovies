package com.mobilitio.popmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mobilitio.popmovies.data.PopMoviesDbContract;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/****************************************************
 * This is the MainActivity of PopMovies
 * Copyright 2017, Antti Ketola
 * <p>
 * MIT Licence
 */
public class MainActivity extends AppCompatActivity implements PosterAdapter.PosterClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    // Settings
    public static String mApiKey = ""; // API key set in Settings by the user
    private int mNumColumns = 2;

    // View Items
    PosterAdapter mPosterAdapter;
    ProgressBar mLoadingIndicator;
    TextView mErrorView;
    private GridLayoutManager mGridLayoutManager;
    private RecyclerView mMoviePosterGrid;
    private JSONArray mMovieData;
    private int mScreenWidth;

    // Reference to connected activities
    DetailActivity detailActivity;

    // internal mode
    private String mSearchMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mScreenWidth = measureScreenWidth();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        detailActivity = new DetailActivity();

        /* set up loading indicator */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_tmdb_loading);
        mErrorView = (TextView) findViewById(R.id.tv_error);

        /* set up RecyclerView */
        mMoviePosterGrid = (RecyclerView) findViewById(R.id.rv_movie_posters);
        mMoviePosterGrid.setHasFixedSize(false);

        /* set up LayoutManager */
        int hor_or_ver = GridLayoutManager.VERTICAL;
        int spanCount = mNumColumns;  //width / 92 ;// TODO Maybe should be calculated, 4 is OK too
        boolean reverseLayout = false;
        mGridLayoutManager = new GridLayoutManager(this, spanCount);//, hor_or_ver, reverseLayout);
        mMoviePosterGrid.setLayoutManager(mGridLayoutManager);

        /* set up Adapter */

        int posterWidth;
        mPosterAdapter = new PosterAdapter(20, mScreenWidth / spanCount, this);
        mMoviePosterGrid.setAdapter(mPosterAdapter);
        readPreferences();

        /* The search mode must be set at start */
        setSearchModePopular();

        /* load movie data from tmdb */
        loadMovieData(1);
        showMainPosters();
    }

    int measureScreenWidth() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        int screenWidth = displaymetrics.widthPixels;

        return screenWidth;
    }

    private void readPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mApiKey = sharedPreferences.getString(getString(R.string.pref_api_key_key), "NO_API_KEY");
        String columnsString = sharedPreferences.getString(getString(R.string.pref_num_columns_key), getString(R.string.pref_num_columns_2));
        mNumColumns = Integer.valueOf(columnsString);
        mGridLayoutManager.setSpanCount(mNumColumns);
        int width = measureScreenWidth();
        mPosterAdapter.setPosterWidth(width / mNumColumns);
        mPosterAdapter.setOverlay(sharedPreferences.getBoolean(getString(R.string.pref_poster_overlay_key), false));
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        readPreferences();

        Log.d(TAG, "onRestart(), mApiKey=>" + mApiKey);
        loadMovieData(1);
    }


    /* Pump up the menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi = getMenuInflater();
        Log.d(TAG, "onCreateOptionsMenu");
        mi.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int item_rid = item.getItemId();

        switch (item_rid) {
            case R.id.mi_most_popular:
                Log.v(TAG, "Most Popular Search Mode Selected");
                setSearchModePopular();
                loadMovieData(1);
                return true;

            case R.id.mi_top_rated:
                Log.v(TAG, "Top Rated Search Mode Selected");
                setSearchModeTopRated();
                loadMovieData(1);
                return true;

            case R.id.mi_favourites:
                Log.v(TAG, "Favourites");
                setSearchModeFavourites();
                loadMovieData(1);
                return true;

            case R.id.mi_settings:
                Log.v(TAG, "SETTINGS selected");
                Intent goToSettings = new Intent(this, SettingsActivity.class);
                startActivity(goToSettings);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPosterClick(int adapterPosition, JSONObject dataToDetailActivity) {
        Class destActivity = DetailActivity.class;
        Intent intent = new Intent(this, destActivity);
        String string_to_pass_the_object_to_detail_activity = dataToDetailActivity.toString();
        intent.putExtra(getString(R.string.intent_x_jsonobject), string_to_pass_the_object_to_detail_activity);
        Log.d(TAG, "Intent to DetailActivity with JSONObject=" + dataToDetailActivity.toString().substring(0, 50));
        startActivity(intent);
    }

    private void setSearchModeTopRated() {
        mSearchMode = getString(R.string.tmdb_api_top);
        setTitle(getString(R.string.main_title_top_rated));
    }

    private void setSearchModePopular() {
        mSearchMode = getString(R.string.tmdb_api_popular);
        setTitle(getString(R.string.main_title_most_popular));
    }

    private void setSearchModeFavourites() {
        mSearchMode = getString(R.string.search_my_favourites);
        setTitle(getString(R.string.search_my_favourites_title));
    }

    public void loadMovieData(int page) {
        new FetchMovieDataTask().execute(mSearchMode, Integer.toString(page));
    }

    private void showMainPosters() {
        mMoviePosterGrid.setVisibility(View.VISIBLE);
        mErrorView.setVisibility(View.INVISIBLE);
    }

    private void showMainError() {
        mMoviePosterGrid.setVisibility(View.INVISIBLE);
        mErrorView.setVisibility(View.VISIBLE);

    }

    // Side effects:
    // mLoadingIndicator
    // mPosterAdapter.setMovieData(movieData)
    public class FetchMovieDataTask extends AsyncTask<String, Void, JSONArray> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONArray doInBackground(String... params) {

            JSONArray tmdbData = null;
            URL movieRequestURL = null;
            if (params.length == 0) {
                return null;
            }

            String movieSearchMode = params[0];//Not pretty but works

            String pageString = "1";
            int pageNr = 1;
            if (params[1] != null) {
                pageString = params[1];
                try {
                    pageNr = Integer.valueOf(pageString);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Something wrong with the task parameter[1]:" + params[1]);
                    pageNr = 1;
                }
            }

            // Instead of any net query, query our own content provider
            if (movieSearchMode.equals(getString(R.string.search_my_favourites))) {

                Uri uri = PopMoviesDbContract.BASE_CONTENT_URI.buildUpon()
                        .appendPath(PopMoviesDbContract.ALL_MOVIES_PATH).build();
                Cursor favDbCursor = null;

                try {
                    favDbCursor = getContentResolver().query(uri, null, null, null, null);
                } catch (Exception e) {

                }
                if (favDbCursor != null) {
                    tmdbData = readTmdbDataFromFavDB(getApplicationContext(), favDbCursor);
                }

                return tmdbData; // To make things later easier a JSONArray must be returned

            }

            // Here we go with the 'ordinary' TMDB query
            Uri movieRequestUri = TmdbUriUtil.buildMovieListUri(getApplicationContext(),
                    movieSearchMode, mApiKey, pageNr);

            try {
                movieRequestURL = new URL(movieRequestUri.toString());
            } catch (MalformedURLException e) {
                Log.e(TAG, "The Uri had eaten something bad" + movieRequestUri.toString());
                e.printStackTrace();
            }

            try {
                String jsonTMDBResponse = TmdbDigger
                        .getResponseFromHttpUrl(movieRequestURL);
                Log.v(TAG, "jsonTMDBResponse=" + jsonTMDBResponse.substring(0, 100));
                tmdbData = TmdbDigger
                        .extractJSONArray(MainActivity.this, jsonTMDBResponse);
            } catch (MalformedURLException mfue) {
                Log.w(TAG, "MalformedURLException, url=" + movieRequestURL.toString());
            } catch (IOException ioex) {
                Log.w(TAG, "IOException:" + ioex.toString() + " url=" + movieRequestURL.toString());
            } catch (Exception e) {
                Log.e(TAG, "Exeptional Exception.");
                e.printStackTrace();
            }
            return tmdbData;
        }

        @Override
        protected void onPostExecute(JSONArray movieData) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (movieData != null) {
//                Log.v(TAG,"onPostExecute movieData="+movieData.toString().substring(0,100));
                mPosterAdapter.setMovieData(movieData);
                showMainPosters();
            } else {
                Log.i(TAG, "Movie data not available");
                showMainError();
            }
        }
    }

    private static JSONArray readTmdbDataFromFavDB(Context context, Cursor favDbCursor) {
        Resources r = context.getResources();
        if (favDbCursor == null) {
            return null; // What else?
        }

        JSONArray moviesArray = new JSONArray();

        int favCount = favDbCursor.getCount();
        if (favCount < 1) {
            return moviesArray;
        }

        while (favDbCursor.moveToNext()) {
            JSONObject oneMovieData = TmdbDigger.extractOneMovieDataAtCursor(context, favDbCursor);

            moviesArray.put(oneMovieData);
        }

        return moviesArray;
    }
}
