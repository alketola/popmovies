package com.mobilitio.popmovies;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by antti on 27/01/17.
 */

public class Util {

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
        Log.v(TAG, "Image Uri:" + uri.toString());
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
        Log.v(TAG, "Movie List Uri:" + uri.toString());
        return uri;
    }


//    private static ArrayList<String> prepareMoviePosterImageArrayList(int initialSize) {
//        ArrayList<String> a = new ArrayList<String>(initialSize);
//        a.add("WLQN5aiQG8wc9SeKwixW7pAR8K.jpg");
//        a.add("z4x0Bp48ar3Mda8KiPD1vwSY3D8.jpg");
//        a.add("z09QAf8WbZncbitewNk6lKYMZsh.jpg");
//        a.add("tIKFBxBZhSXpIITiiB5Ws8VGXjt.jpg");
//        a.add("ylXCdC106IKiarftHkcacasaAcb.jpg");
//        a.add("jjBgi2r5cRt36xF6iNUEhzscEcb.jpg");
//        a.add("5gJkVIVU7FDp7AfRAbPSvvdbre2.jpg");
//
//        return a;
//    }
}
