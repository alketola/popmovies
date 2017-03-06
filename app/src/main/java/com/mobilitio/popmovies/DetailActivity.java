package com.mobilitio.popmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobilitio.popmovies.data.PopMoviesDbContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.mobilitio.popmovies.R.id.review_list;
import static com.mobilitio.popmovies.TmdbDigger.extractDecimalField;
import static com.mobilitio.popmovies.TmdbDigger.extractStringField;

/**
 * Created by antti on 26/01/17.
 */

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = DetailActivity.class.getSimpleName();

    private class VideoData {
        public String name;
        public URL url;

        public VideoData(String name, URL url) {
            this.name = name;
            this.url = url;
        }
    }

    private class ReviewData {
        public String author;
        public String content;

        public ReviewData(String author, String content) {
            this.author = author;
            this.content = content;
        }
    }

    private class VideoButtonAdapter extends ArrayAdapter<VideoData> {
        Context context;
        int resource;
        List<VideoData> videoDatas;

        public VideoButtonAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<VideoData> objects) {
            super(context, resource, objects);
            this.context = context;
            this.resource = resource;
            videoDatas = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            Button b = (Button) getLayoutInflater().inflate(resource, null);
            b.setText(videoDatas.get(position).name);
            final URL url = videoDatas.get(position).url;
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()));
                    startActivity(intent);
                }
            });
            return b;
            //return super.getView(position, convertView, parent);
        }

        @Override
        public int getCount() {
            return videoDatas.size();
        }
    }

    private class ReviewListAdapter extends ArrayAdapter<ReviewData> {
        Context context;
        int resource;
        List<ReviewData> reviewDatas;

        public ReviewListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<ReviewData> objects) {
            super(context, resource, objects);
            this.context = context;
            this.resource = resource;
            reviewDatas = objects;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            String reviewAuthor = reviewDatas.get(position).author;
            String content = reviewDatas.get(position).content;
            LinearLayout lin = (LinearLayout) getLayoutInflater().inflate(resource, null);

            TextView authorTv = new TextView(getApplicationContext());

            authorTv.setText(reviewAuthor);

            TextView reviewTv = new TextView(getApplicationContext());

            reviewTv.setText(content);

            lin.addView(authorTv);
            lin.addView(reviewTv);
            return (View) lin;
        }

        @Override
        public int getCount() {
            return reviewDatas.size();
        }
    }

    // module variables shown in the activity UI and put to database
    String mMovieTitle;
    int mMovieId = 0;
    String mImageURIString;
    String mSynopsisText;
    float mRatingFloat;
    String mReleaseDate;
    boolean mFavoriteOn;
    VideoButtonAdapter mVideoListAdapter;
    ReviewListAdapter mReviewListAdapter;
    String mShortImageUriString = new String();

    ImageView mDetailIv;
    AppCompatCheckBox favouriteButton;
    private View.OnClickListener mFavouriteOnClickListener;

    // Instantiate
    private ArrayList<VideoData> mVideoURLs = new ArrayList<>();
    private ArrayList<ReviewData> mReviews = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_1);
        Context context = getApplicationContext();

        mDetailIv = (ImageView) findViewById(R.id.iv_detail);
        Intent intentIn = getIntent();

        JSONObject jsonObject = null;

        // Extract received data
        if (intentIn.hasExtra(getString(R.string.intent_x_imageuri))) {
            mShortImageUriString = intentIn.getStringExtra(getString(R.string.intent_x_imageuri));
//            Log.d(TAG, "received intent with imageuri=" + imageUriString + "- not supported any more");
        } else if (intentIn.hasExtra(getString(R.string.intent_x_jsonobject))) {
//            Log.d(TAG, "received JSON intent");
            String string = intentIn.getStringExtra(getString(R.string.intent_x_jsonobject));
            jsonObject = TmdbDigger.oneMovieDataObjectFrom(string);
            mShortImageUriString = TmdbDigger.extractPosterName(jsonObject);
            mMovieId = TmdbDigger.extractMovieId(context, jsonObject);
            mFavoriteOn = movieIsInDB(mMovieId);
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
        mMovieTitle = TmdbDigger.extractStringField(getString(R.string.tmdb_res_original_title), jsonObject);

        TextView tv_movie_title = (TextView) findViewById(R.id.tv_movie_title);
        tv_movie_title.setText(mMovieTitle);
//        setTitle(mMovieTitle); I would prefer putting movie title to app title

        String sizePath = TmdbUriUtil.getImageSizePathString(imageSize);
        mImageURIString = TmdbUriUtil.buildImageUri(this, mShortImageUriString, sizePath).toString();
        Picasso.with(context)
                .load(mImageURIString)
                .placeholder(R.mipmap.ic_launcher)
                .resize(imageSize, imageSize) // square
                .into(mDetailIv);

        TextView tv_synopsis = (TextView) findViewById(R.id.tv_synopsis);
        mSynopsisText = extractStringField(getString(R.string.tmdb_res_overview), jsonObject);

        tv_synopsis.setText(mSynopsisText);

        // There's the favourite checkbox which can be clicked to set the movie as favourite
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
        favouriteButton = (AppCompatCheckBox) findViewById(R.id.tv_favourite_title);
        favouriteButton.setOnClickListener(mFavouriteOnClickListener);
        favouriteButton.setChecked(movieIsInDB(mMovieId));

        mRatingFloat = extractDecimalField(getString(R.string.tmdb_res_vote_average_decimal), jsonObject);
        String ratingString = String.valueOf(mRatingFloat);
        TextView tv_rating = (TextView) findViewById(R.id.tv_rating_decimal_number);
        tv_rating.setText(ratingString);

        mReleaseDate = extractStringField(getString(R.string.tmdb_res_release_date_string_yyyy_mm_dd), jsonObject);
        TextView tv_release_date = (TextView) findViewById(R.id.tv_release_date);
        tv_release_date.setText(mReleaseDate);

        mVideoListAdapter = new VideoButtonAdapter(this,
                R.layout.video_list_item, mVideoURLs);

        FetchMovieVideosTask videoLister = new FetchMovieVideosTask();
        LinearLayout lv_videoList = (LinearLayout) findViewById(R.id.video_list);
        ///lv_videoList.setAdapter(mVideoListAdapter);
        videoLister.execute(mMovieId);


        mReviewListAdapter = new ReviewListAdapter(this,
                R.layout.review_item, mReviews);

        FetchReviewsTask reviewLister = new FetchReviewsTask();
        reviewLister.execute(mMovieId);

    } // end onCreate

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    private boolean movieIsInDB(int movieId) {
        boolean exists = false;
        String movieIdString = String.valueOf(mMovieId);
        Uri qUri = Uri.parse(PopMoviesDbContract.MovieEntry.CONTENT_URI + "/" + mMovieId);
        Cursor cursor = getContentResolver().query(qUri, null, null, null, null);
        if (cursor.getCount() > 0) {
            exists = true;
        }

        return exists;
    }

    private void addMovieToDb(Context context) {
        ContentValues movieDataCV = new ContentValues();
        movieDataCV.put(PopMoviesDbContract.MovieEntry.COLUMN_ORIGINAL_TITLE, mMovieTitle);
        // PLAIN path not the complete URI mImageURIString, but it must be preceded by a slash
        movieDataCV.put(PopMoviesDbContract.MovieEntry.COLUMN_POSTER_PATH, "/" + mShortImageUriString);
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
            LinearLayout videoList = (LinearLayout) findViewById(R.id.video_list);
            for (int i = 0; i < length; i++) {
                String videoKey = TmdbDigger.extractKey(context, jsonArray, i);
                URL url = TmdbUriUtil.buildYouTubeURL(context, videoKey);
                JSONObject jsonObject = TmdbDigger.extractOneMovieData(i, jsonArray);
                String videoName = TmdbDigger.extractStringField("name", jsonObject);
                boolean result = mVideoURLs.add(new VideoData(videoName, url));
                videoList.addView(mVideoListAdapter.getView(i, null, null));
                Log.d(TAG, "Added video name=" + videoName + " URL=" + i + ": " + url.toString() + " result = " + result);
            }

//            videoList.invalidateViews();
        }
    } // end FetchMovieVideos Task

    /*
     * Returns JSONObject that contains JSONArray in "result" tuple.
     */
    private class FetchReviewsTask extends AsyncTask<Integer, Void, String> {
        int movieId;

        /* @param ints[0] tmdb movie id number */
        @Override
        protected String doInBackground(Integer... ints) {
            String jsonTMDBResponse = null;
            movieId = ints[0];

            Uri reviewListUri = TmdbUriUtil.buildReviewListUri(getApplicationContext(), movieId,
                    MainActivity.mApiKey);

            URL reviewListURL = null;
            try {
                reviewListURL = new URL(reviewListUri.toString());
            } catch (MalformedURLException e) {
                Log.e(TAG, "The reviewUri had eaten something bad" + reviewListUri.toString());
                e.printStackTrace();
            }

            try {
                jsonTMDBResponse = TmdbDigger.getResponseFromHttpUrl(reviewListURL);
                Log.v(TAG, "Fetch reviews, Response=" + jsonTMDBResponse.substring(0, 100));

            } catch (Exception e) {
                Log.e(TAG, "Problems with response from URL:" + reviewListURL);

                e.printStackTrace();
            }

            return jsonTMDBResponse;
        }

        @Override
        protected void onPostExecute(String jsonString) {
            super.onPostExecute(jsonString);
            if (jsonString.length() == 0) return;
            Context context = getApplicationContext();
            JSONArray jsonArray = TmdbDigger.extractJSONArray(context, jsonString);
            if (jsonArray == null) return;
            int length = TmdbDigger.getArrayLength(jsonArray);

            //debug info, url not used
            URL url = TmdbUriUtil.buildReviewURL(context, movieId, MainActivity.mApiKey);
            Log.d(TAG, "ReviewURL: " + url.toString());
            //end debug info
            LinearLayout reviewList = (LinearLayout) findViewById(R.id.video_list);

            for (int i = 0; i < length; i++) {

                JSONObject reviewObject = TmdbDigger.extractOneMovieData(i, jsonArray);
                String reviewAuthor = TmdbDigger.extractStringField("author", reviewObject);
                String reviewContent = TmdbDigger.extractStringField("content", reviewObject);

                boolean result = mReviews.add(new ReviewData(reviewAuthor, reviewContent));
                TextView reviewAuthorView = new TextView(getBaseContext());
                reviewAuthorView.setText(reviewAuthor);
                reviewAuthorView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.detail_h2_size));
                reviewAuthorView.setTypeface(null, Typeface.BOLD);
                int padding = getResources().getDimensionPixelSize(R.dimen.margin_minimal);
                reviewAuthorView.setPadding(padding, padding, padding, padding);
                reviewAuthorView.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                reviewList.addView(reviewAuthorView);

                TextView reviewContentView = new TextView(getBaseContext());
                reviewContentView.setText(reviewContent);
                padding = getResources().getDimensionPixelSize(R.dimen.margin_a);
                reviewContentView.setPadding(padding, padding, padding, padding);
                reviewContentView.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                reviewList.addView(reviewContentView);

            }
        }
    } // end FetchReviewsTask Task
}
