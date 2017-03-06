package com.mobilitio.popmovies;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Utility Class for The Movie DB URI handling
 * Created by antti on 27/01/17.
 */

public class TmdbUriUtil {
    private static final String TAG = TmdbUriUtil.class.getSimpleName();

    public static Uri buildImageUri(Context context, String filename, String sizePathString) {
        Resources res = context.getResources();
        Uri.Builder ub = new Uri.Builder();
        Uri uri = ub.scheme(res.getString(R.string.tmdb_img_scheme))
                .authority(res.getString(R.string.tmdb_img_authority))
                .appendPath(res.getString(R.string.tmdb_img_path_1))
                .appendPath(res.getString(R.string.tmdb_img_path_2))
                .appendPath(sizePathString) // e.g. 'w185'
                .appendPath(filename)
                .build();
//        Log.v(TAG, "Image Uri:" + uri.toString());
        return uri;
    }

    public static Uri buildMovieListUri(Context context, String searchCriteriaR, String api_key, int page) {
        if (page < 1) page = 1;
        Resources res = context.getResources();
        Uri.Builder ub = new Uri.Builder();
        Uri uri = ub.scheme(res.getString(R.string.tmdb_api_scheme))
                .authority(res.getString(R.string.tmdb_api_authority))
                .appendPath(res.getString(R.string.tmdb_api_v3))
                .appendPath(res.getString(R.string.tmdb_api_movie))
                .appendPath(searchCriteriaR)
                .appendQueryParameter("api_key", api_key)
                .appendQueryParameter("page", Integer.toString(page))
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

    /* building Uri to Videos *
     * https://api.themoviedb.org/3/movie/{id int}/videos?api_key=
     */
    public static Uri buildVideoListUri(Context context, int movie_id, String api_key) {
        Resources res = context.getResources();
        Uri.Builder ub = new Uri.Builder();
        Uri uri = ub.scheme(res.getString(R.string.tmdb_api_scheme))
                .authority(res.getString(R.string.tmdb_api_authority))
                .appendPath(res.getString(R.string.tmdb_api_v3))
                .appendPath(res.getString(R.string.tmdb_api_movie))
                .appendPath(Integer.toString(movie_id))
                .appendPath(res.getString(R.string.tmdb_api_videos))
                .appendQueryParameter("api_key", api_key)
                .build();
//        Log.v(TAG, "Movie Video List Uri:" + uri.toString());
        return uri;
    }

    /* build Uri to reviews
     * https://api.themoviedb.org/3/movie/{id int}/reviews?api_key=
     */
    public static Uri buildReviewListUri(Context context, int movie_id, String api_key) {
        Resources res = context.getResources();
        Uri.Builder ub = new Uri.Builder();
        Uri uri = ub.scheme(res.getString(R.string.tmdb_api_scheme))
                .authority(res.getString(R.string.tmdb_api_authority))
                .appendPath(res.getString(R.string.tmdb_api_v3))
                .appendPath(res.getString(R.string.tmdb_api_movie))
                .appendPath(Integer.toString(movie_id))
                .appendPath(res.getString(R.string.tmdb_api_reviews))
                .appendQueryParameter("api_key", api_key)
                .build();
//        Log.v(TAG, "Movie Review List Uri:" + uri.toString());
        return uri;
    }

    public static URL buildYouTubeURL(Context context, String videoKey) {
        Resources res = context.getResources();
        Uri.Builder ub = new Uri.Builder();
        Uri uri = ub.scheme("https").authority("www.youtube.com")
                .appendPath("watch").appendQueryParameter("v", videoKey)
                .build();
        URL videoUrl = null;
        try {
            videoUrl = new URL(uri.toString());
        } catch (MalformedURLException e) {
            Log.w(TAG, "Malformed URL, videoKey:" + videoKey + " uri:" + uri.toString());
        } finally {
            return videoUrl;
        }
    }

    public static URL buildReviewURL(Context context, int movieId, String apiKey) {
        Resources res = context.getResources();
        Uri.Builder ub = new Uri.Builder();
        Uri uri = ub.scheme("https").authority("api.themoviedb.org")
                .appendPath(res.getString(R.string.tmdb_api_v3))
                .appendPath(res.getString(R.string.tmdb_api_movie))
                .appendPath(String.valueOf(movieId))
                .appendPath(res.getString(R.string.tmdb_api_reviews))
                .appendQueryParameter("api_key", apiKey)
                .build();
        URL reviewUrl = null;
        try {
            reviewUrl = new URL(uri.toString());
        } catch (MalformedURLException e) {
            Log.w(TAG, "Malformed URL for review, movieId:" + movieId + " uri:" + uri.toString());
        } finally {
            return reviewUrl;
        }
    }
}
