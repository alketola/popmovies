package com.mobilitio.popmovies;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by antti on 24/01/17.
 */

public class PosterAdapter extends RecyclerView.Adapter<PosterAdapter.PosterViewHolder> {
    //
    private static final String TAG = PosterAdapter.class.getSimpleName();
    private int mHowManyMovies;
    //    private ArrayList<String> mPosterImageNames; TODO REMOVE
    private JSONArray mMovieData;
    private static int mPosterWidthPx;

    final private PosterClickListener mPosterOnclickListener;


    public PosterAdapter(int numberOfDisplayedMovies,
                         int posterWidth,
                         PosterClickListener posterOnClickListener) {
        mHowManyMovies = numberOfDisplayedMovies;
        mPosterWidthPx = posterWidth;
        mPosterOnclickListener = posterOnClickListener;
//        if (mPosterImageNames == null) {// TODO REMOVE
//            mPosterImageNames = new ArrayList<String>(numberOfDisplayedMovies);
//        }

    }

    public void setMovieData(JSONArray movieData) {
        mMovieData = movieData;
        notifyDataSetChanged();
        Log.d(TAG, "setMovieData: mMovieData=" + mMovieData.toString().substring(0, 100));
    }

    public interface PosterClickListener {
        public void onPosterClick(int itemIndex, String imageUrl);
    }


    @Override
    public PosterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.main_poster_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        PosterViewHolder pvh = new PosterViewHolder(view);


        return pvh;
    }

    @Override
    public void onBindViewHolder(PosterViewHolder holder, int position) {
        Log.v(TAG, "bind:" + Integer.toString(position));
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mHowManyMovies;
    }

    private static String getImageSizePathString(int width) {
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
        //Log.v(TAG, "px=" + width + " sizestring=" + widthString);
        return widthString;

    }

    private static String getPathBySetImageSize() {
        return getImageSizePathString(mPosterWidthPx);
    }

    /****
     * ViewHolder class
     ****/
    public class PosterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;
        Context pvContext;


        public PosterViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.iv_a_poster);
            imageView.setOnClickListener(this);
            pvContext = view.getContext();
        }

        @Override
        public void onClick(View v) {
            Log.v(TAG, "onClick");
            Context context = v.getContext();
            int adapterPosition = getAdapterPosition();

            String imagefile = new String();
            imagefile = DatabaseAccess.extractPosterName(adapterPosition, mMovieData);
            //DatabaseAccess.extractOneMovieData(); TODO the way to go later for all data
            String imageurlstring = Util.buildImageUri(context, imagefile, getImageSizePathString(mPosterWidthPx))
                    .toString();
            mPosterOnclickListener.onPosterClick(adapterPosition, imageurlstring);
        }

        public void bind(int position) {
            Log.v(TAG, "bind: position=" + position);
            Context context = pvContext;
//            imageView.setImageResource(R.mipmap.ic_launcher);
            JSONObject movie = null;
            String imagefilename = null;
            if (mMovieData != null) {
                imagefilename = DatabaseAccess.extractPosterName(position, mMovieData);
            } else {
                Log.e(TAG, "mMovieData = null");
            }

            if (imagefilename != null) {
                Log.v(TAG, "imagefilename=" + imagefilename);
                Uri uri = Util.buildImageUri(context, imagefilename, getImageSizePathString(mPosterWidthPx));
                Log.v(TAG, "image uri=" + uri.toString());
                Picasso.with(pvContext)
                        .load(uri.toString())
                        .resize(mPosterWidthPx, mPosterWidthPx) // square
                        .into(imageView);
            } else {
                Log.e(TAG, "imagefilename = null");
            }

        }

    }
}
