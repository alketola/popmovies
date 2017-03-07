package com.mobilitio.popmovies;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.util.Log;

import com.mobilitio.popmovies.data.PopMoviesDbContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;


/**
 * Class for handling JSON results from TheMovieDB API
 *
 * Renamed "Database Access" as TmdbDigger, as it is actually doing digging
 * And there is to be a local database to be confused with.
 *
 * A lot of JSON processing is done here, and it's TMDB related.
 *
 * Created by antti on 25/01/17.
 */

public class TmdbDigger {
    private static final String TAG = TmdbDigger.class.getSimpleName();


    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    /**
     * org.json is used for handling results
     * <p>
     * Example of the results:
     * {
     * "original_language": "en",
     * "genre_ids": [12,16,35,10751],
     * "backdrop_path": "/lubzBMQLLmG88CLQ4F3TxZr2Q7N.jpg",
     * "overview": "The quiet life of a terrier named Max is upended when his owner takes in Duke, a stray whom Max instantly dislikes.",
     * "video": false,
     * "popularity": 308.180118,
     * "vote_count": 1854,
     * "vote_average": 5.8,
     * "release_date": "2016-06-18",
     * "id": 328111,
     * "poster_path": "/WLQN5aiQG8wc9SeKwixW7pAR8K.jpg",
     * "title": "The Secret Life of Pets",
     * "adult": false,
     * "original_title": "The Secret Life of Pets"
     * }
     */

    public static JSONArray extractJSONArray(Context context, String jason) {

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jason);
        } catch (JSONException e) {
            Log.w(TAG, "Jason had eaten something bad: " + jason);
            e.printStackTrace();
        }

        final String STATUS_CODE = context.getString(R.string.tmdb_res_out_status_code);
        final String STATUS_MESSAGE = context.getString(R.string.tmdb_res_out_status_message);
        final String RESULTS = context.getString(R.string.tmdb_res_out_results);
        int error_code = 0;
        String error_message = null;

        /* Is there an error? */
        if (jsonObject.has(STATUS_CODE)) {
            try {
                int errorCode = jsonObject.getInt(STATUS_CODE);
                switch (error_code) {
                    case 0:
                    case 1:
                        // Equals to OK! HTTP 200 see: https://www.themoviedb.org/documentation/api/status-codes?language=ch
                        break;
                    default: // some error code, say, any error
                        Log.i(TAG, "Received response with error code:" + error_code);
                        return null;
                }
            } catch (JSONException e) {
                Log.d(TAG, "Strange, received response with error code but no readable content, maybe! No errors, then?");
            }

            try {
                error_message = jsonObject.getString(STATUS_MESSAGE);
            } catch (JSONException e) {
                error_message = "BAD STATUS MESSAGE";
                Log.w(TAG, "Error in TMDB HTTP communication: Message=" + error_message + " code=" + error_code);
                return null;
            }
        } else {
            error_code = 0;
        }
        // Ok, Here with no error codes
        JSONArray jsonResult = null;
        if (jsonObject.has(RESULTS)) {
            try {
                jsonResult = (JSONArray) jsonObject.get(RESULTS);
            } catch (JSONException e) {
                Log.w(TAG, "TMDB response has result without result? json=" + jsonObject.toString());
                return null;
            }
        }
        // Last debug
        if (jsonResult != null) {
            //Log.d(TAG, "jsonResult=" + (jsonResult.toString()));
        } else {
            Log.e(TAG, "jsonResult is null.");
        }
        return jsonResult;

    }

    public static int getPageCount(Context context, JSONObject jsonObject, int defaultCount) {
        int pageCount = defaultCount;
        if (jsonObject.has(context.getString(R.string.tmdb_res_out_total_pages))) {
            try {
                pageCount = jsonObject.getInt(context.getString(R.string.tmdb_res_out_total_pages));
            } catch (JSONException exception) {
                Log.v(TAG, "No total_pages found in json:" + jsonObject.toString());
            }
        }
        return pageCount;
    }

    /*
     * @param defaultCount -the int that should be returned if the call fails, 0 or 1 to decide
     *
     */
    public static int getResultCount(Context context, JSONObject jsonObject, int defaultCount) {
        int resultCount = defaultCount;
        if (jsonObject.has(context.getString(R.string.tmdb_res_out_total_results))) {
            try {
                resultCount = jsonObject.getInt(context.getString(R.string.tmdb_res_out_total_results));
            } catch (JSONException exception) {
                Log.v(TAG, "No total_results found in json:" + jsonObject.toString());
            }
        }
        return resultCount;
    }



    public static String extractPosterName(int position, JSONArray array) {
        JSONObject oneMovieData = extractOneMovieData(position, array);
        String postername = new String();
        if (oneMovieData != null) {

            postername = extractPosterName(oneMovieData);
        }

        return postername;
    }

    public static String extractPosterName(JSONObject jsonObject) {
        String postername = new String();
        try {
            if (jsonObject != null) {
                postername = jsonObject.getString("poster_path");
            }
        } catch (JSONException e) {
            Log.d(TAG, "extractPosterName: could not get string from object, or something.");
            Log.d(TAG, "jsonObject=" + jsonObject);

            e.printStackTrace();
        }
        if (postername.length() > 0) {
            postername = postername.substring(1);
        }
        return postername;

    }

    public static JSONObject extractOneMovieData(int position, JSONArray array) {
        JSONObject oneMovieData = null;

        try {
            oneMovieData = (JSONObject) array.get(position);
        } catch (JSONException e) {
            Log.d(TAG, "extractOneMovieData: could not get object");
            Log.d(TAG, "array=" + array);
            Log.d(TAG, "oneMovieData=" + oneMovieData);

            e.printStackTrace();
        }
        return oneMovieData;
    }

    // This is a hack
    public static String extractShortMovieInfo(int position, JSONArray array) {
        JSONObject movieData = extractOneMovieData(position, array);
        String original_title = extractStringField("original_title", movieData);
        float vote_average = extractDecimalField("vote_average", movieData);
        //original_title = original_title.substring(0,10);
        return " " + original_title + "  " + String.valueOf(vote_average);
    }

    public static JSONObject oneMovieDataObjectFrom(String s) {
        JSONObject returned_object = null;
        try {
            returned_object = new JSONObject(s);
        } catch (JSONException e) {
            Log.e(TAG, "could not create one movie data object from string");
            e.printStackTrace();
        }
        return returned_object;
    }

    public static String extractStringField(String field_name, JSONObject jsonObject) {
        String string = new String();
//        Log.d(TAG, "extractStringField field_name=" + field_name);
//        Log.d(TAG, "jsonObject:" + jsonObject.toString());
        try {
            string = jsonObject.getString(field_name);
        } catch (JSONException e) {
            Log.d(TAG, "could not extractStringField:" + field_name);
            e.printStackTrace();
        }
        return string;
    }

    public static int extractIntField(String field_name, JSONObject jsonObject) {
        int i = 0;
        try {
            i = jsonObject.getInt(field_name);
        } catch (JSONException e) {
            Log.d(TAG, "could not extractIntField:" + field_name);
            e.printStackTrace();
        }
        return i;
    }

    public static float extractDecimalField(String field_name, JSONObject jsonObject) {
        float f = 0.0f;
        try {
            double d = jsonObject.getDouble(field_name);
//            Log.d(TAG, "Field=" + field_name + " double=" + d);
            f = (float) d;
        } catch (JSONException e) {
            Log.d(TAG, "could not extractDecimalField '" + field_name + "' to double");
            e.printStackTrace();
        }
        return f;
    }

    public static int getArrayLength(JSONArray array) {
        return array.length();
    }

    public static String extractKey(Context context, JSONArray array, int index) {

        JSONObject jsonObject = null;
        String key = "";
        try {
            jsonObject = array.getJSONObject(index);
        } catch (JSONException e) {
            Log.w(TAG, "JARRAY had eaten something bad: " + array.toString());
            e.printStackTrace();
        }
        if (jsonObject == null) return "";
        try {
            key = jsonObject.getString(context.getString(R.string.tmdb_res_key)); //"key"
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return key;
    }

    public static int extractMovieId(Context context, JSONObject jsonObject) {
        int id = 0;
        id = extractIntField(context.getString(R.string.tmdb_res_movie_id), jsonObject);
        return id;
    }
// Methods for dealing with TMDB data stored in our Content provider.
// I opted for simplicity so that working code of MainActivity and DetailActivity would
// be minimally affected, thus formatting data from database to JSON accepted by the Activities.
//
// Available data at cursor:
//        PopMoviesDbContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
//        PopMoviesDbContract.MovieEntry.COLUMN_POSTER_PATH, //Hmm. filename in cache?
//        PopMoviesDbContract.MovieEntry.COLUMN_OVERVIEW,
//        PopMoviesDbContract.MovieEntry.COLUMN_VOTE_AVERAGE,
//        PopMoviesDbContract.MovieEntry.COLUMN_RELEASE_DATE,
//        PopMoviesDbContract.MovieEntry.COLUMN_MOVIE_ID
// Data reception of DetailActivity
//        String string = intentIn.getStringExtra(getString(R.string.intent_x_jsonobject));
//        jsonObject = TmdbDigger.oneMovieDataObjectFrom(string);
//     Fields:
//        mMovieTitle = TmdbDigger.extractStringField(getString(R.string.tmdb_res_title), jsonObject);
//        imageUriString = TmdbDigger.extractPosterName(jsonObject); // ahould be
//        mSynopsisText = TmdbDigger.extractStringField(getString(R.string.tmdb_res_overview), jsonObject);
//        mRatingFloat = TmdbDigger.extractDecimalField(getString(R.string.tmdb_res_vote_average_decimal), jsonObject);
//        mReleaseDate = TmdbDigger.extractStringField(getString(R.string.tmdb_res_release_date_string_yyyy_mm_dd), jsonObject);
//        mMovieId = TmdbDigger.extractMovieId(context, jsonObject);
    // Methods for creating a JSON object that can be received by DetailActivity
    // Using the same format as JSON from TMDB stripped in MainActivity

    // Looking at the database row at cursor, the column indicated by the resource ID
    // is picked up and stored to JSONObject.
    // Note that there _IS_ the convention that the column headers and JSON object names
    // are the same!
    // @param context, to get access to Resources, resource a.k.a. R.type.something
    // cursor to read from, target JSONObject to yank the data to.
    public static void stringAtCursorToJSON(Context context, int resource,
                                            Cursor cursor, JSONObject target) {
        Resources r = context.getResources();
        String resourceName = r.getString(resource);
        int column = cursor.getColumnIndex(resourceName);
        String s = cursor.getString(column);

        try {
            target.put(resourceName, s);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return;
    }

    public static void intAtCursorToJSON(Context context, int resource, Cursor cursor, JSONObject target) {
        Resources r = context.getResources();
        String resourceName = r.getString(resource);
        int column = cursor.getColumnIndex(resourceName);
        int i = cursor.getInt(column);


        try {
            target.put(resourceName, i);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return;
    }

    public static void floatAtCursorToJSON(Context context, int resource, Cursor cursor, JSONObject target) {
        Resources r = context.getResources();
        String resourceName = r.getString(resource);
        int column = cursor.getColumnIndex(resourceName);
        float f = cursor.getInt(column);
        //  movieId = favDbCursor.getInt(
        //          favDbCursor.getColumnIndex(PopMoviesDbContract.MovieEntry.COLUMN_MOVIE_ID));

        try {
            target.put(resourceName, f);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return;
    }

    public static JSONObject movieIdAtCursorToJSON(Cursor cursor, JSONObject object) {
        int column = cursor.getColumnIndex(PopMoviesDbContract.MovieEntry.COLUMN_MOVIE_ID);
        int i = cursor.getInt(column);

        try {
            object.put("id", i);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    public static JSONObject extractOneMovieDataAtCursor(Context context, Cursor cursor) {
        JSONObject json = new JSONObject();
        TmdbDigger.movieIdAtCursorToJSON(cursor, json);

        TmdbDigger.stringAtCursorToJSON(context,
                R.string.tmdb_res_release_date_string_yyyy_mm_dd,
                cursor, json);

        TmdbDigger.floatAtCursorToJSON(context,
                R.string.tmdb_res_vote_average_decimal,
                cursor, json);

        TmdbDigger.stringAtCursorToJSON(context,
                R.string.tmdb_res_overview,
                cursor, json);

        TmdbDigger.stringAtCursorToJSON(context,
                R.string.tmdb_res_poster,
                cursor, json);

        TmdbDigger.stringAtCursorToJSON(context,
                R.string.tmdb_res_original_title,
                cursor, json);
        return json;

    }
}