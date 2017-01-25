package com.mobilitio.popmovies;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by antti on 25/01/17.
 */

public class DatabaseAccess {

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
}
