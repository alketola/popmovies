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
    public static final String ALL_MOVIES_PATH = "allmovies";
    public static final String TRAILERS_PATH = "trailers";

    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE = "movies";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(MOVIE_PATH)
                .build();
        // CONVENTION: DB column names are the same as TMDB json object names
        public static final String COLUMN_ORIGINAL_LANGUAGE = "original_language"; //tmdb_res_original_language
        public static final String COLUMN_GENRE_IDS = "genre_ids"; // INT ARRAY: [12,16,35,10751], // tmdb_res_genre_ids_int_array
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path"; // "/lubzBMQLLmG88CLQ4F3TxZr2Q7N.jpg", //tmdb_res_backdrop
        public static final String COLUMN_OVERVIEW = "overview"; // "The lorem.", // tmdb_res_overview
        public static final String COLUMN_VIDEO = "video"; // boolean: INT 0=false, //tmdb_res_video_boolean
        public static final String COLUMN_POPULARITY = "popularity"; // FLOAT 308.180118, // tmdb_res_popularity_decimal
        public static final String COLUMN_VOTE_COUNT = "vote_count";//INT 1854, //
        public static final String COLUMN_VOTE_AVERAGE = "vote_average"; // FLOAT: 5.8
        public static final String COLUMN_RELEASE_DATE = "release_date"; // DATE FORMAT "2016-06-18"
        public static final String COLUMN_MOVIE_ID = "movie_id"; // INT 328111, PRIMARY KEY // "tmdb_res_id_int">id< !!!!
        public static final String COLUMN_POSTER_PATH = "poster_path"; // tmdb_res_poster
        public static final String COLUMN_TITLE = "title"; // tmdb_res_title
        public static final String COLUMN_ADULT = "adult"; // boolean: INT 0=false,
        public static final String COLUMN_ORIGINAL_TITLE = "original_title"; // tmdb_res_original_title
        public static final String COLUMN_FAVOURITE = "favourite"; // boolean: INT 0=false,
    }
}
