package com.mobilitio.popmovies;

import android.content.Context;
import android.content.Intent;
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
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements PosterAdapter.PosterClickListener {
    private static final String TAG = PosterAdapter.class.getSimpleName();

    PosterAdapter mPosterAdapter;
    ProgressBar mLoadingIndicator;
    TextView mErrorView;
    DetailActivity detailActivity;

    private GridLayoutManager mGridLayoutManager;

    private RecyclerView mMoviePosterGrid;
    private String mApiKey = ""; // TODO MAKE EMPTY BEFORE COMMIT
    private JSONArray mMovieData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        detailActivity = new DetailActivity();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        int dpi = displaymetrics.densityDpi;
        Log.v(TAG, "screen width=" + width);
        Context context = getApplicationContext();

        /* set up loading indicator */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_tmdb_loading);
        mErrorView = (TextView) findViewById(R.id.tv_error);

        /* set up RecyclerView */
        mMoviePosterGrid = (RecyclerView) findViewById(R.id.rv_movie_posters);
        mMoviePosterGrid.setHasFixedSize(true);

        /* set up LayoutManager */
        int hor_or_ver = GridLayoutManager.VERTICAL;          // TODO Better if dynamic
        int spanCount = 4;  //width / 92 ;// TODO This must be calculated accordingly
        boolean reverseLayout = false;
        mGridLayoutManager = new GridLayoutManager(this, spanCount);//, hor_or_ver, reverseLayout);
        mMoviePosterGrid.setLayoutManager(mGridLayoutManager);

        /* set up Adapter */

        int posterWidth;
        mPosterAdapter = new PosterAdapter(20, width / spanCount, this);
        mMoviePosterGrid.setAdapter(mPosterAdapter);

        /* load movie data from tmdb */
        loadMovieData();

    }

    /* Pump up the menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.main_activity_menu,menu);
        return true;

    }
    @Override
    public void onPosterClick(int adapterPosition,String uristring)    {
        Class destActivity = DetailActivity.class;
        Intent intent = new Intent(this, destActivity);
        intent.putExtra(getString(R.string.intent_x_imageuri),uristring);
        Log.v(TAG,"Intenting DetailActivity with uristring="+uristring);
        startActivity(intent);
    }

    public void loadMovieData() {
        new FetchMovieDataTask().execute(getString(R.string.tmdb_api_popular));
    }

    public class FetchMovieDataTask extends AsyncTask<String, Void, JSONArray> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONArray doInBackground(String... params) {

            JSONArray tmdbData;
            URL movieRequestURL = null;
            if (params.length == 0) {
                return null;
            }

            String movieSearchMode = params[0];//TODO
            Uri movieRequestUri = Util.buildMovieListUri(getApplicationContext(),
                    movieSearchMode, mApiKey);

            try {
                movieRequestURL = new URL(movieRequestUri.toString());
            } catch (MalformedURLException e) {
                Log.e(TAG, "The Uri had eaten something bad" + movieRequestUri.toString());
                e.printStackTrace();
            }

            try {
                String jsonTMDBResponse = DatabaseAccess
                        .getResponseFromHttpUrl(movieRequestURL);
                Log.v(TAG, "jsonTMDBResponse=" + jsonTMDBResponse.substring(0, 100));
                tmdbData = DatabaseAccess
                        .extractJSONArray(MainActivity.this, jsonTMDBResponse);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return tmdbData;
        }

        @Override
        protected void onPostExecute(JSONArray movieData) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (movieData != null) {
//                Log.v(TAG,"onPostExecute movieData="+movieData.toString().substring(0,100));
                showMainPosters();
                mPosterAdapter.setMovieData(movieData);
            } else {
                showMainError();
            }
        }
    }

    private void showMainPosters() {
        mMoviePosterGrid.setVisibility(View.VISIBLE);
        mErrorView.setVisibility(View.INVISIBLE);
    }

    private void showMainError() {
        mMoviePosterGrid.setVisibility(View.INVISIBLE);
        mErrorView.setVisibility(View.VISIBLE);

    }



}
