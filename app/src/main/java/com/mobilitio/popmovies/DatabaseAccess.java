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

    public DatabaseAccess (){

    }

    public void onCreate (){


    }

//    public Uri queryUriAssemble(Context context, String filename) {
//        Resources res = context.getResources();
//        Uri.Builder ub = new Uri.Builder();
//        Uri uri = ub.scheme(res.getString(R.string.tmdb_api_scheme))
//                .authority(res.getString(R.string.tmdb_api_authority))
//                .appendPath(res.getString(R.string.tmdb_img_path_1))
//                .appendPath(res.getString(R.string.tmdb_img_path_2))
//                .appendPath(getPathBySetImageSize())
//                .appendPath(filename)
//                .build();
//        Log.v(TAG, "Image Uri:" + uri.toString());
//        return uri;
//    }
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
        try {
            if (oneMovieData != null) {
                postername = oneMovieData.getString("poster_path");
            }
        } catch (JSONException e) {
            Log.d(TAG, "extractPosterName: could not get string from object, or something.");
            Log.d(TAG, "position:" + position);
            Log.d(TAG, "array=" + array);
            Log.d(TAG, "oneMovieData=" + oneMovieData);

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
}
