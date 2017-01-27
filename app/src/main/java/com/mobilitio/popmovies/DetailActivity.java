package com.mobilitio.popmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RatingBar;
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
        setContentView(R.layout.activity_details);
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
        int imageWidth = 200; // TODO something obtained or calculated

        TextView tv_movie_title = (TextView) findViewById(R.id.tv_movie_title);
        String movieTitle = extractStringField(getString(R.string.tmdb_res_title), jsonObject);
        tv_movie_title.setText(movieTitle);

        String sizePath = Util.getImageSizePathString(imageWidth);
        String imageuri = Util.buildImageUri(this, imageUriString, sizePath).toString();
        Picasso.with(context)
                .load(imageuri)
                .resize(imageWidth, imageWidth) // square
                .into(mDetailIv);

        TextView tv_synopsis = (TextView) findViewById(R.id.tv_synopsis);
        String synopsistext = extractStringField(getString(R.string.tmdb_res_overview), jsonObject);

        tv_synopsis.setText(synopsistext);

        float rating_float = extractDecimalField(getString(R.string.tmdb_res_vote_average_decimal), jsonObject);
        String rating_string = String.valueOf(rating_float);

        RatingBar rb_rating = (RatingBar) findViewById(R.id.rb_ratingBar);
        rb_rating.setMax(10);
        rb_rating.setNumStars(10);
        rb_rating.setStepSize(1.0f);
        rb_rating.setRating(rating_float);

        TextView tv_rating = (TextView) findViewById(R.id.tv_ratingDecimalNumber);
        tv_rating.setText(rating_string);
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
