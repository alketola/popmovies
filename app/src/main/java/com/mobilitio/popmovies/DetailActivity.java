package com.mobilitio.popmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by antti on 26/01/17.
 */

public class DetailActivity  extends AppCompatActivity {
    private static final String TAG = PosterAdapter.class.getSimpleName();

    ImageView mDetailIv;
    public DetailActivity() {

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mDetailIv = (ImageView) findViewById(R.id.iv_detail);
        Intent intentIn = getIntent();

        if (intentIn.hasExtra(getString(R.string.intent_x_imageuri))) {
            String imageuri = intentIn.getStringExtra(getString(R.string.intent_x_imageuri));
            Log.v(TAG,"received intentwith imageuri="+imageuri);
            Context context = getApplicationContext();
            Picasso.with(context)
                    .load(imageuri)
                    .resize(200,200) // square
                    .into(mDetailIv);

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
