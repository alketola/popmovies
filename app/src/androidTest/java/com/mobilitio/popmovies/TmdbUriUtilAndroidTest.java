package com.mobilitio.popmovies;

import android.content.Context;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Antti on 2017-02-22.
 */
public class TmdbUriUtilAndroidTest {
    private static Context mAppContext;

    @Before
    public void setUp() throws Exception {
        Context mAppContext = InstrumentationRegistry.getTargetContext();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void buildImageUri() throws Exception {
        Context context = mAppContext;
        String testFileName = "TestFileName";
        String sizeString = "w185";
        String expectedResult = "http://image.tmdb.org/image/t/p/" + sizeString + "/" + testFileName;
        Uri uri = TmdbUriUtil.buildImageUri(context, testFileName, sizeString);
        assertEquals(expectedResult, uri.toString())

    }

    @Test
    public void buildMovieListUri() throws Exception {

    }

    @Test
    public void getImageSizePathString() throws Exception {

    }

    @Test
    public void buildVideoListUri() throws Exception {
        Context context = mAppContext;
        String testApiKey = "abc987654";
        int testMovieId = 98765;
        String expectedResult = "https://api.themoviedb.org/3/movie/" + testMovieId + "/videos?api_key=" + testApiKey;
        Uri uri = TmdbUriUtil.buildVideoListUri(context, testMovieId, testApiKey);
        assertEquals(expectedResult, uri.toString());

    }

    @Test
    public void buildReviewListUri() throws Exception {

    }

}