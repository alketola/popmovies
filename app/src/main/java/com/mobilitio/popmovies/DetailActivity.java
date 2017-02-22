package com.mobilitio.popmovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

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

    ImageView mDetailIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_1);
        Context context = getApplicationContext();

        mDetailIv = (ImageView) findViewById(R.id.iv_detail);
        Intent intentIn = getIntent();

        JSONObject jsonObject = null;
        String imageUriString = new String();
        int movieId = 0;
        if (intentIn.hasExtra(getString(R.string.intent_x_imageuri))) {
            imageUriString = intentIn.getStringExtra(getString(R.string.intent_x_imageuri));
//            Log.d(TAG, "received intent with imageuri=" + imageUriString + "- not supported any more");
        } else if (intentIn.hasExtra(getString(R.string.intent_x_jsonobject))) {
//            Log.d(TAG, "received JSON intent");
            String string = intentIn.getStringExtra(getString(R.string.intent_x_jsonobject));
            jsonObject = TmdbDigger.oneMovieDataObjectFrom(string);
            imageUriString = TmdbDigger.extractPosterName(jsonObject);
            movieId = TmdbDigger.extractMovieId(context, jsonObject);
//            Log.d(TAG, "UriString from JSON:" + imageUriString);
        }

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
        // assuming square image
        int imageSize;

        if (smaller_dim < SMALL_SCREEN_DIM) {
            imageSize = getResources().getInteger(R.integer.tmdb_img_size_smallest_int);
        } else if (landscape) {
            imageSize = (int) (screenHeight * IMAGE_SIZE_FRACTION_LANDSCAPE);
        } else {
            imageSize = (int) (screenWidth * IMAGE_SIZE_FRACTION_PORTRAIT);
        }

        String movieTitle = extractStringField(getString(R.string.tmdb_res_title), jsonObject);

        TextView tv_movie_title = (TextView) findViewById(R.id.tv_movie_title);
        tv_movie_title.setText(movieTitle);
//        setTitle(movieTitle); I would prefer putting movie title to app title

        String sizePath = TmdbUriUtil.getImageSizePathString(imageSize);
        String imageuri = TmdbUriUtil.buildImageUri(this, imageUriString, sizePath).toString();
        Picasso.with(context)
                .load(imageuri)
                .placeholder(R.mipmap.ic_launcher)
                .resize(imageSize, imageSize) // square
                .into(mDetailIv);

        TextView tv_synopsis = (TextView) findViewById(R.id.tv_synopsis);
        String synopsistext = extractStringField(getString(R.string.tmdb_res_overview), jsonObject);

        tv_synopsis.setText(synopsistext);

        FetchMovieVideosTask videoLister = new FetchMovieVideosTask();
        videoLister.execute(movieId);

        float rating_float = extractDecimalField(getString(R.string.tmdb_res_vote_average_decimal), jsonObject);
        String rating_string = String.valueOf(rating_float);
        TextView tv_rating = (TextView) findViewById(R.id.tv_rating_decimal_number);
        tv_rating.setText(rating_string);

        String release_date = extractStringField(
                getString(R.string.tmdb_res_release_date_string_yyyy_mm_dd),
                jsonObject);
        TextView tv_release_date = (TextView) findViewById(R.id.tv_release_date);
        tv_release_date.setText(release_date);


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
