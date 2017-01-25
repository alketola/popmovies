package com.mobilitio.popmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

public class MainActivity extends AppCompatActivity implements PosterAdapter.PosterClickListener {
    private static final String TAG = PosterAdapter.class.getSimpleName();

    PosterAdapter mPosterAdapter;
    DetailActivity detailActivity;

    private GridLayoutManager mGridLayoutManager;

    private RecyclerView mMoviePosterGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        detailActivity = new DetailActivity();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        Log.v("****HELLO****", "screen width="+width);
        Context context = getApplicationContext();

        /* set up RecyclerView */
        mMoviePosterGrid = (RecyclerView) findViewById(R.id.rv_movie_posters);
        mMoviePosterGrid.setHasFixedSize(true);

        /* set up LayoutManager */
        int hor_or_ver = GridLayoutManager.VERTICAL;          // TODO Better if dynamic
        int spanCount = 4 ;//GridLayoutManager.DEFAULT_SPAN_COUNT; // TODO This must be calculated accordingly
        boolean reverseLayout = false;
        mGridLayoutManager = new GridLayoutManager(this, spanCount);//, hor_or_ver, reverseLayout);
        mMoviePosterGrid.setLayoutManager(mGridLayoutManager);

        /* set up Adapter */

        int posterWidth;
        mPosterAdapter = new PosterAdapter(7,width/ spanCount,this);
        mMoviePosterGrid.setAdapter(mPosterAdapter);

    }

    /* Pump up the menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.main_activity_menu,menu);
        return true;

    }
    @Override
    public void onPosterClick(int adapterPosition,String uristring)    {
        Class destActivity = DetailActivity.class;
        Intent intent = new Intent(this, destActivity);
        intent.putExtra(getString(R.string.intent_x_imageuri),uristring);
        Log.v(TAG,"Intenting DetailActivity with uristring="+uristring);
        startActivity(intent);
    }

}
