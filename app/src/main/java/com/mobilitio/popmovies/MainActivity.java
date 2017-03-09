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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

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
    private static String mSearchModeString;
    private int mNumColumns = 2;

    // View Items
    PosterAdapter mPosterAdapter;
    ProgressBar mLoadingIndicator;
    TextView mErrorView;
    private GridLayoutManager mGridLayoutManager;
    private RecyclerView mMoviePosterGrid;

    private int mScreenWidth;
    public final int ADAPTER_IMAGE_COUNT = 100;
    // Reference to connected activities
    DetailActivity detailActivity;

    // internal mode
    private final String SAVED_SEARCH_MODE = "saved_search_mode";
    private int mSearchMode;
    // persisted movie data
    private final String SAVED_MOVIE_DATA = "saved_movie_data";
    JSONArray mTmdbData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            int searchMode = savedInstanceState.getInt(SAVED_SEARCH_MODE, R.id.mi_most_popular);
            setSearchMode(searchMode);
            String jsonString = (String) savedInstanceState.get(SAVED_MOVIE_DATA);
            if (jsonString != null) {
                try {
                    mTmdbData = new JSONArray(jsonString);
                } catch (JSONException e) {
                    mTmdbData = null;
                }
            }
        } else {
            setSearchMode(R.id.mi_most_popular);
            mTmdbData = null;
        }

        mScreenWidth = measureScreenWidth();
        setContentView(R.layout.activity_main);
        detailActivity = new DetailActivity();

        /* set up loading indicator */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_tmdb_loading);
        mErrorView = (TextView) findViewById(R.id.tv_error);
        mErrorView.setFocusable(false);

        mErrorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadMovieData();
            }
        });

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

        mPosterAdapter = new PosterAdapter(ADAPTER_IMAGE_COUNT, mScreenWidth / spanCount, this);
        mMoviePosterGrid.setAdapter(mPosterAdapter);
        readPreferences();

        /* load movie data from tmdb */
        loadMovieData();
        showMainPosters();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        readPreferences();

        loadMovieData();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SAVED_SEARCH_MODE, getSearchMode());
        Log.d(TAG, "onSaveInstanceState saved search mode:" + getSearchMode());
        outState.putString(SAVED_MOVIE_DATA, mTmdbData.toString());
        super.onSaveInstanceState(outState);
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
                setSearchMode(item_rid);
                loadMovieData();
                return true;

            case R.id.mi_top_rated:
                Log.v(TAG, "Top Rated Search Mode Selected");
                setSearchMode(item_rid);
                loadMovieData();
                return true;

            case R.id.mi_favourites:
                Log.v(TAG, "Favourites");
                setSearchMode(item_rid);
                loadMovieData();
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

    private void setSearchMode(int searchModeResourceId) {
        mSearchMode = searchModeResourceId;
        switch (searchModeResourceId) {
            case R.id.mi_most_popular:
                mSearchModeString = getString(R.string.tmdb_api_popular);
                setTitle(getString(R.string.main_title_most_popular));
                break;
            case R.id.mi_top_rated:
                mSearchModeString = getString(R.string.tmdb_api_top);
                setTitle(getString(R.string.main_title_top_rated));
                break;
            case R.id.mi_favourites:
                mSearchModeString = getString(R.string.search_my_favourites);
                setTitle(getString(R.string.search_my_favourites_title));
                break;
            default:
                mSearchModeString = getString(R.string.tmdb_api_popular);
                mSearchMode = R.id.mi_most_popular;
                setTitle(getString(R.string.main_title_most_popular));
        }
    }

    private static String getSearchModeString(int mSearchMode) {
        return mSearchModeString;
    }

    private int getSearchMode() {
        return mSearchMode;
    }

    public void loadMovieData(int numberOfMovies) {

        new FetchMovieDataTask().execute(getSearchModeString(mSearchMode), Integer.toString(numberOfMovies));
    }

    public void loadMovieData() {
        loadMovieData(ADAPTER_IMAGE_COUNT);
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

            //PARAM[0] movie search mode e.g. most popular / top rated /favourites
            //PARAM[1] count of movies to load
            // - Does not observe TMDB rate limit at the moment, so don't request too many movies
            //REQUIRES the 2 parameters
            if (params[0] == null || params[1] == null) return null;

            String movieSearchMode = params[0];
            String requestedMovieCountString = params[1];
            int requestedMovieCount;

            try {
                requestedMovieCount = Integer.valueOf(requestedMovieCountString);
            } catch (NumberFormatException e) {
                Log.w(TAG, "Something wrong with the task parameter[1]:" + params[1]);
                requestedMovieCount = 1; //anti-crash
            }

            // FAVOURITES. Instead of any net query, query our own content provider
            if (movieSearchMode.equals(getString(R.string.search_my_favourites))) {

                Uri uri = PopMoviesDbContract.BASE_CONTENT_URI.buildUpon()
                        .appendPath(PopMoviesDbContract.ALL_MOVIES_PATH).build();
                Cursor favDbCursor = null;

                try {
                    favDbCursor = getContentResolver().query(uri, null, null, null, null);
                } catch (Exception e) {
                    Log.e(TAG, "Could not make query to movie fav DB:" + uri.toString());
                    return null;
                }

                if (favDbCursor != null) {
                    tmdbData = readTmdbDataFromFavDB(getApplicationContext(), favDbCursor);
                } else {
                    Log.w(TAG, "Movie fav DB cursor from query is null");
                    return null;
                }

                // To make things easier in onPostExecute a JSONArray must be returned
                return tmdbData;
            }
            int movieCount = 0;
            int tmdbRequestPageNr = 1;
            if (tmdbData == null) {
                tmdbData = new JSONArray();
            }
            while (movieCount < requestedMovieCount) {
                // Here we go with the 'ordinary' TMDB query
                Uri movieRequestUri = TmdbUriUtil.buildMovieListUri(getApplicationContext(),
                        movieSearchMode, mApiKey, tmdbRequestPageNr);

                try {
                    movieRequestURL = new URL(movieRequestUri.toString());
                } catch (MalformedURLException e) {
                    Log.e(TAG, "The Uri had eaten something bad" + movieRequestUri.toString());
                    e.printStackTrace();
                }

                JSONArray jsonResultsArray = null; // here we'll get a batch of movie data
                try {
                    String jsonTMDBResponse = TmdbDigger
                            .getResponseFromHttpUrl(movieRequestURL);
                    //Log.v(TAG, "jsonTMDBResponse=" + jsonTMDBResponse.substring(0, 100));

                    jsonResultsArray = TmdbDigger.extractJSONArray(MainActivity.this, jsonTMDBResponse);
                } catch (MalformedURLException mfue) {
                    Log.w(TAG, "MalformedURLException, url=" + movieRequestURL.toString());

                    return null;
                } catch (UnknownHostException uhostex) {
                    Log.w(TAG, "UnknownHostException url=" + movieRequestURL.toString());
                    return null;
                } catch (IOException ioex) {
                    Log.w(TAG, "IOException:" + ioex.toString() + " url=" + movieRequestURL.toString());
                    return tmdbData;
                } catch (Exception e) {
                    Log.e(TAG, "Exeptional Exception.");
                    e.printStackTrace();

                    return null;
                }

                tmdbRequestPageNr++;

                int lastResultLength = 0;
                if (jsonResultsArray != null) {
                    lastResultLength = jsonResultsArray.length();
                }

                for (int i = 0; i < lastResultLength; i++) {
                    JSONObject jsonObject = jsonResultsArray.optJSONObject(i);
                    if (jsonObject != null) {
                        tmdbData.put(jsonObject);
                        movieCount++;
                    }
                    if (movieCount >= requestedMovieCount) {
                        break;
                    }
                }
            }
            return tmdbData;
        }

        @Override
        protected void onPostExecute(JSONArray movieData) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (movieData != null) {
                mTmdbData = movieData;
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
