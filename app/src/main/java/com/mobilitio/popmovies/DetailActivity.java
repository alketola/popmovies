package com.mobilitio.popmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilitio.popmovies.data.PopMoviesDbContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

import static com.mobilitio.popmovies.TmdbDigger.extractDecimalField;
import static com.mobilitio.popmovies.TmdbDigger.extractStringField;

/**
 * Created by antti on 26/01/17.
 */

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = DetailActivity.class.getSimpleName();

    // module variables shown in the activity UI and put to database
    String mMovieTitle;
    int mMovieId = 0;
    String mImageURIString;
    String mSynopsisText;
    float mRatingFloat;
    String mReleaseDate;
    boolean mFavoriteOn = false;


    ImageView mDetailIv;
    View favouriteButton;
    private View.OnClickListener mFavouriteOnClickListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_1);
        Context context = getApplicationContext();

        mDetailIv = (ImageView) findViewById(R.id.iv_detail);
        Intent intentIn = getIntent();

        JSONObject jsonObject = null;
        String imageUriString = new String();

        if (intentIn.hasExtra(getString(R.string.intent_x_imageuri))) {
            imageUriString = intentIn.getStringExtra(getString(R.string.intent_x_imageuri));
//            Log.d(TAG, "received intent with imageuri=" + imageUriString + "- not supported any more");
        } else if (intentIn.hasExtra(getString(R.string.intent_x_jsonobject))) {
//            Log.d(TAG, "received JSON intent");
            String string = intentIn.getStringExtra(getString(R.string.intent_x_jsonobject));
            jsonObject = TmdbDigger.oneMovieDataObjectFrom(string);
            imageUriString = TmdbDigger.extractPosterName(jsonObject);
            mMovieId = TmdbDigger.extractMovieId(context, jsonObject);
//            Log.d(TAG, "UriString from JSON:" + imageUriString);
        }
        // Measure display for semi-automatic layout tuning
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int screenHeight = displaymetrics.heightPixels;
        int screenWidth = displaymetrics.widthPixels;
        boolean landscape = (screenWidth > screenHeight);
        Log.i(TAG, "screenHeight:" + screenHeight + " screenWidth:" + screenWidth + " landscape:" + landscape);
        int smaller_dim = landscape ? screenHeight : screenWidth;
        int dpi = displaymetrics.densityDpi;

        // These constants are hand-tuned
        // assuming square image
        final float IMAGE_SIZE_FRACTION_LANDSCAPE = 0.5f;
        final float IMAGE_SIZE_FRACTION_PORTRAIT = 0.9f;
        final int SMALL_SCREEN_DIM = 240;

        int imageSize;

        if (smaller_dim < SMALL_SCREEN_DIM) {
            imageSize = getResources().getInteger(R.integer.tmdb_img_size_smallest_int);
        } else if (landscape) {
            imageSize = (int) (screenHeight * IMAGE_SIZE_FRACTION_LANDSCAPE);
        } else {
            imageSize = (int) (screenWidth * IMAGE_SIZE_FRACTION_PORTRAIT);
        }

        // Now, extract elements to UI, storing them to mOdule variables
        // and putting them to UI views
        mMovieTitle = TmdbDigger.extractStringField(getString(R.string.tmdb_res_title), jsonObject);

        TextView tv_movie_title = (TextView) findViewById(R.id.tv_movie_title);
        tv_movie_title.setText(mMovieTitle);
//        setTitle(mMovieTitle); I would prefer putting movie title to app title

        String sizePath = TmdbUriUtil.getImageSizePathString(imageSize);
        mImageURIString = TmdbUriUtil.buildImageUri(this, imageUriString, sizePath).toString();
        Picasso.with(context)
                .load(mImageURIString)
                .placeholder(R.mipmap.ic_launcher)
                .resize(imageSize, imageSize) // square
                .into(mDetailIv);

        TextView tv_synopsis = (TextView) findViewById(R.id.tv_synopsis);
        mSynopsisText = extractStringField(getString(R.string.tmdb_res_overview), jsonObject);

        tv_synopsis.setText(mSynopsisText);
        // here we have a star button
//        final AppCompatImageButton ib_star = (AppCompatImageButton) findViewById(R.id.favourite_star_button);
//        ib_star.setImageResource(R.drawable.ic_star1_empty);

        // There's favourite checkbox which can be clicked to set the movie as favourite
        mFavouriteOnClickListener = new View.OnClickListener() {
            final AppCompatCheckBox clickableFavTitle = (AppCompatCheckBox) findViewById(R.id.tv_favourite_title);

            void toggle() {
                if (!mFavoriteOn) mFavoriteOn = true;
                else mFavoriteOn = false;
                Log.d(TAG, "toggle()=" + mFavoriteOn);
            }

            @Override
            public void onClick(View view) {
                toggle();
                if (mFavoriteOn) {
                    clickableFavTitle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorFavouriteOn));
                    // here I would set a movie a favourite in ContentProvider
                    addMovieToDb(getApplicationContext());
                } else {
                    clickableFavTitle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorFavouriteOff));
                    deleteMovieFromDb(getApplicationContext());
                }
                Log.d(TAG, "Clicked Favourite:" + mFavoriteOn);
            }
        };
        favouriteButton = findViewById(R.id.tv_favourite_title);
        favouriteButton.setOnClickListener(mFavouriteOnClickListener);


        FetchMovieVideosTask videoLister = new FetchMovieVideosTask();
        videoLister.execute(mMovieId);

        mRatingFloat = extractDecimalField(getString(R.string.tmdb_res_vote_average_decimal), jsonObject);
        String ratingString = String.valueOf(mRatingFloat);
        TextView tv_rating = (TextView) findViewById(R.id.tv_rating_decimal_number);
        tv_rating.setText(ratingString);

        mReleaseDate = extractStringField(
                getString(R.string.tmdb_res_release_date_string_yyyy_mm_dd),
                jsonObject);
        TextView tv_release_date = (TextView) findViewById(R.id.tv_release_date);
        tv_release_date.setText(mReleaseDate);
    }

    private void addMovieToDb(Context context) {
        ContentValues movieDataCV = new ContentValues();
        movieDataCV.put(PopMoviesDbContract.MovieEntry.COLUMN_ORIGINAL_TITLE, mMovieTitle);
        movieDataCV.put(PopMoviesDbContract.MovieEntry.COLUMN_POSTER_PATH, mImageURIString);
        movieDataCV.put(PopMoviesDbContract.MovieEntry.COLUMN_OVERVIEW, mSynopsisText);
        movieDataCV.put(PopMoviesDbContract.MovieEntry.COLUMN_VOTE_AVERAGE, mRatingFloat);
        movieDataCV.put(PopMoviesDbContract.MovieEntry.COLUMN_RELEASE_DATE, mReleaseDate);
        movieDataCV.put(PopMoviesDbContract.MovieEntry.COLUMN_MOVIE_ID, mMovieId);

        Uri uri = getContentResolver().insert(PopMoviesDbContract.MovieEntry.CONTENT_URI, movieDataCV);
        if (uri != null) {
            Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void deleteMovieFromDb(Context context) {
        String movieIdString = String.valueOf(mMovieId);
        Uri delUri = Uri.parse(PopMoviesDbContract.MovieEntry.CONTENT_URI + "/" + mMovieId);
        int deletedCount =
                getContentResolver().delete(delUri,
                        PopMoviesDbContract.MovieEntry.COLUMN_MOVIE_ID + "=?",
                        new String[]{movieIdString});
        Log.d(TAG, "deleted movie id=" + mMovieId + " deleted count=" + deletedCount);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    /*
     * Returns JSONObject that contains JSONArray in "result" tuple.
     */
    private class FetchMovieVideosTask extends AsyncTask<Integer, Void, String> {

        /* @param ints[0] tmdb movie id number */
        @Override
        protected String doInBackground(Integer... ints) {
            String jsonTMDBResponse = null;
            int movieId = ints[0];

            Uri videoListUri = TmdbUriUtil.buildVideoListUri(getApplicationContext(), movieId,
                    MainActivity.mApiKey);

            URL videoListURL = null;
            try {
                videoListURL = new URL(videoListUri.toString());
            } catch (MalformedURLException e) {
                Log.e(TAG, "The VideoUri had eaten something bad" + videoListUri.toString());
                e.printStackTrace();
            }

            try {
                jsonTMDBResponse = TmdbDigger.getResponseFromHttpUrl(videoListURL);
                Log.v(TAG, "Fetch video data Response=" + jsonTMDBResponse.substring(0, 100));

            } catch (Exception e) {
                Log.e(TAG, "Problems with response from URL:" + videoListURL);

                e.printStackTrace();
            }

            return jsonTMDBResponse;
        }

        @Override
        protected void onPostExecute(String jsonString) {
            super.onPostExecute(jsonString);
            Context context = getApplicationContext();
            JSONArray jsonArray = TmdbDigger.extractJSONArray(context, jsonString);
            int length = TmdbDigger.getArrayLength(jsonArray);
            for (int i = 0; i < length; i++) {
                String videoKey = TmdbDigger.extractVideoKey(context, jsonArray, i);
                URL url = TmdbUriUtil.buildYouTubeURL(context, videoKey);
                Log.d(TAG, "VideoURL " + i + ": " + url.toString());
            }
        }
    }
}
