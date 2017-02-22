package com.mobilitio.popmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Antti on 2017-02-22.
 */

public class PopMoviesDbContract {
    public static final String AUTHORITY = "com.mobilitio.popmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String MOVIE_PATH = "movie";
    public static final String TRAILERS_PATH = "trailers";

    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE = "movies";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(MOVIE_PATH)
                .build();
        public static final String COLUMN_ORIGINAL_LANGUAGE = "original_language";
        public static final String COLUMN_GENRE_IDS = "genre_ids"; // INT ARRAY: [12,16,35,10751],
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path"; // "/lubzBMQLLmG88CLQ4F3TxZr2Q7N.jpg",
        public static final String COLUMN_OVERVIEW = "overview"; // "The quiet life of a terrier named Max is upended when his owner takes in Duke, a stray whom Max instantly dislikes.",
        public static final String COLUMN_VIDEO = "video"; // boolean: INT 0=false,
        public static final String COLUMN_POPULARITY = "popularity"; // FLOAT 308.180118,
        public static final String COLUMN_VOTE_COUNT = "vote_count";//INT 1854,
        public static final String COLUMN_VOTE_AVERAGE = "vote_average"; // FLOAT: 5.8
        public static final String COLUMN_RELEASE_DATE = "release_date"; // DATE FORMAT "2016-06-18"
        public static final String COLUMN_MOVIE_ID = "id"; // INT 328111, PRIMARY KEY
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_ADULT = "adult"; // boolean: INT 0=false,
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_FAVOURITE = "favourite"; // boolean: INT 0=false,
    }
}
