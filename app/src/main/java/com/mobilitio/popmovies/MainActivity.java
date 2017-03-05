package com.mobilitio.popmovies;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import org.json.JSONException;
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
    // TODO INSERT API KEY TO THE STRING INITIALIZER BELOW
    public static String mApiKey = "";
    // TODO REMOVE API KEY BEFORE COMMIT
    PosterAdapter mPosterAdapter;
    ProgressBar mLoadingIndicator;
    TextView mErrorView;
    DetailActivity detailActivity;
    String mSearchMode;
    private GridLayoutManager mGridLayoutManager;
    private RecyclerView mMoviePosterGrid;
    // TODO Make API KEY EMPTY BEFORE COMMIT
    private JSONArray mMovieData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        int dpi = displaymetrics.densityDpi;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        detailActivity = new DetailActivity();

        Context context = getApplicationContext();



        /* set up loading indicator */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_tmdb_loading);
        mErrorView = (TextView) findViewById(R.id.tv_error);

        /* set up RecyclerView */
        mMoviePosterGrid = (RecyclerView) findViewById(R.id.rv_movie_posters);
        mMoviePosterGrid.setHasFixedSize(false);

        /* set up LayoutManager */
        int hor_or_ver = GridLayoutManager.VERTICAL;
        int spanCount = 2;  //width / 92 ;// TODO Maybe should be calculated, 4 is OK too
        boolean reverseLayout = false;
        mGridLayoutManager = new GridLayoutManager(this, spanCount);//, hor_or_ver, reverseLayout);
        mMoviePosterGrid.setLayoutManager(mGridLayoutManager);

        /* set up Adapter */

        int posterWidth;
        mPosterAdapter = new PosterAdapter(20, width / spanCount, this);
        mMoviePosterGrid.setAdapter(mPosterAdapter);

        /* The search mode must be set at start */
        setSearchModePopular();

        /* load movie data from tmdb */
        loadMovieData(1);
        showMainPosters();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart()");
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
                break;
            case R.id.mi_top_rated:
                Log.v(TAG, "Top Rated Search Mode Selected");
                setSearchModeTopRated();
                loadMovieData(1);
                break;
            case R.id.mi_favourites:
                Log.v(TAG, "Favourites");
                setSearchModeFavourites();
                loadMovieData(1);
                break;
//            case R.id.mi_settings:
//                Log.v(TAG,"SETTINGS selected");
//                break;
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
                Log.i(TAG,"Movie data not available");
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
