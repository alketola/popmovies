package com.mobilitio.popmovies;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;


/**
 * Created by antti on 25/01/17.
 */

public class DatabaseAccess {
    private static final String TAG = PosterAdapter.class.getSimpleName();


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

        final String STATUS_CODE = "status_code";
        final String STATUS_MESSAGE = "status_message";
        final String RESULTS = "results";
        int error_code = 0;
        String error_message = null;

        /* Is there an error? */
        if (jsonObject.has(STATUS_CODE)) {
            try {
                int errorCode = jsonObject.getInt(STATUS_CODE);
            } catch (JSONException e) {
                error_code = 0;
            }
            try {
                error_message = jsonObject.getString(STATUS_MESSAGE);
            } catch (JSONException e) {
                error_message = "";
            }
            Log.w(TAG, "Error in TMDB HTTP communication: Message=" + error_message + " code=" + error_code);
            return null;

        }
        JSONArray jsonResult = null;
        if (jsonObject.has(RESULTS)) {
            try {
                jsonResult = (JSONArray) jsonObject.get(RESULTS);
            } catch (JSONException e) {
                Log.w(TAG, "TMDB response has result without result? json=" + jsonObject.toString());
            }
        }
        if (jsonResult != null) {
            //Log.d(TAG, "jsonResult=" + (jsonResult.toString()));
        } else {
            Log.e(TAG, "jsonResult is null.");
        }
        return jsonResult;

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
        postername = postername.substring(1);
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
        Log.d(TAG, "extractStringField field_name=" + field_name);
        Log.d(TAG, "jsonObject:" + jsonObject.toString());
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
            Log.d(TAG, "Field=" + field_name + " double=" + d);
            f = (float) d;
        } catch (JSONException e) {
            Log.d(TAG, "could not extractDecimalField '" + field_name + "' to double");
            e.printStackTrace();
        }
        return f;
    }

}