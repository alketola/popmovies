package com.mobilitio.popmovies;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;

/**
 * Created by antti on 27/01/17.
 */

public class Util {
    private static final String TAG = Util.class.getSimpleName();

    public static Uri buildImageUri(Context context, String filename, String sizePathString) {
        Resources mRes = context.getResources();
        Uri.Builder ub = new Uri.Builder();
        Uri uri = ub.scheme(mRes.getString(R.string.tmdb_img_scheme))
                .authority(mRes.getString(R.string.tmdb_img_authority))
                .appendPath(mRes.getString(R.string.tmdb_img_path_1))
                .appendPath(mRes.getString(R.string.tmdb_img_path_2))
                .appendPath(sizePathString) // e.g. 'w185'
                .appendPath(filename)
                .build();
//        Log.v(TAG, "Image Uri:" + uri.toString());
        return uri;
    }

    public static Uri buildMovieListUri(Context context, String searchCriteriaR, String api_key) {
        Resources mRes = context.getResources();
        Uri.Builder ub = new Uri.Builder();
        Uri uri = ub.scheme(mRes.getString(R.string.tmdb_api_scheme))
                .authority(mRes.getString(R.string.tmdb_api_authority))
                .appendPath(mRes.getString(R.string.tmdb_api_v3))
                .appendPath(mRes.getString(R.string.tmdb_api_movie))
                .appendPath(searchCriteriaR)
                .appendQueryParameter("api_key", api_key)
                .build();
//        Log.v(TAG, "Movie List Uri:" + uri.toString());
        return uri;
    }

    public static String getImageSizePathString(int width) {
        /* tmdb has a few supported image sizes */
        int[] tmdbImageSizes = {92, 154, 185, 342, 500, 780}; // must be in ascending order
        int greatestLessOrEqualSize = 0;
        for (int s : tmdbImageSizes) {
            if (width <= s) {
                greatestLessOrEqualSize = s;
                break;
            }
            greatestLessOrEqualSize = s;
        }

        String jstring = Integer.toString(greatestLessOrEqualSize);
        String widthString = "w" + jstring;
//        Log.v(TAG, "px=" + width + " sizestring=" + widthString);
        return widthString;

    }
}
