package com.mobilitio.popmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import static com.mobilitio.popmovies.DatabaseAccess.extractDecimalField;
import static com.mobilitio.popmovies.DatabaseAccess.extractPosterName;
import static com.mobilitio.popmovies.DatabaseAccess.extractStringField;
import static com.mobilitio.popmovies.DatabaseAccess.oneMovieDataObjectFrom;

/**
 * Created by antti on 26/01/17.
 */

public class DetailActivity  extends AppCompatActivity {
    private static final String TAG = DetailActivity.class.getSimpleName();

    ImageView mDetailIv;
    public DetailActivity() {

    }
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
            Log.d(TAG, "received intent with imageuri=" + imageUriString + "- not supported any more");
        } else if (intentIn.hasExtra(getString(R.string.intent_x_jsonobject))) {
            Log.d(TAG, "received JSON intent");
            String string = intentIn.getStringExtra(getString(R.string.intent_x_jsonobject));
            jsonObject = oneMovieDataObjectFrom(string);
            imageUriString = extractPosterName(jsonObject);
            Log.d(TAG, "UriString from JSON:" + imageUriString);
        }

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int screenHeight = displaymetrics.heightPixels;
        int screenWidth = displaymetrics.widthPixels;
        Log.d(TAG, "screenHeight:" + screenHeight + " screenWidth:" + screenWidth);
        boolean landscape = (screenWidth>screenHeight);
        int dpi = displaymetrics.densityDpi;

        // These constants are hand-tuned
        // assuming square image
        final float IMAGE_SIZE_FRACTION_LANDSCAPE = 0.5f;
        final float IMAGE_SIZE_FRACTION_PORTRAIT = 0.95f;
        final int SMALL_SCREEN_HEIGHT = 240;
        // assuming square image
        int imageSize;
        if (landscape) {
            imageSize = (int) (screenHeight * IMAGE_SIZE_FRACTION_LANDSCAPE);
        } else {
            imageSize = (int) (screenWidth * IMAGE_SIZE_FRACTION_PORTRAIT);
        }

        if (screenHeight < SMALL_SCREEN_HEIGHT) {
            imageSize = getResources().getInteger(R.integer.tmdb_img_size_smallest_int);
        }

//        TextView tv_movie_title = (TextView) findViewById(R.id.tv_movie_title);
        String movieTitle = extractStringField(getString(R.string.tmdb_res_title), jsonObject);
//        tv_movie_title.setText(movieTitle);
        setTitle(movieTitle);

        String sizePath = Util.getImageSizePathString(imageSize);
        String imageuri = Util.buildImageUri(this, imageUriString, sizePath).toString();
        Picasso.with(context)
                .load(imageuri)
                .placeholder(R.mipmap.ic_launcher)
                .resize(imageSize, imageSize) // square
                .into(mDetailIv);

        TextView tv_synopsis = (TextView) findViewById(R.id.tv_synopsis);
        String synopsistext = extractStringField(getString(R.string.tmdb_res_overview), jsonObject);

        tv_synopsis.setText(synopsistext);

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
}
