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

import static com.mobilitio.popmovies.TmdbDigger.extractOneMovieData;
import static com.mobilitio.popmovies.TmdbUriUtil.buildImageUri;
import static com.mobilitio.popmovies.TmdbUriUtil.getImageSizePathString;

/**
 * Created by antti on 24/01/17.
 */

public class PosterAdapter extends RecyclerView.Adapter<PosterAdapter.PosterViewHolder> {
    //
    private static final String TAG = PosterAdapter.class.getSimpleName();
    private static int mPosterWidthPx;
    final private PosterClickListener mPosterOnclickListener;
    private int mHowManyMovies;
    //    private ArrayList<String> mPosterImageNames; TODO REMOVE
    private JSONArray mMovieData;


    public PosterAdapter(int numberOfDisplayedMovies,
                         int posterWidth,
                         PosterClickListener posterOnClickListener) {
        mHowManyMovies = numberOfDisplayedMovies;
        mPosterWidthPx = posterWidth;
        mPosterOnclickListener = posterOnClickListener;
    }

    private static String getPathBySetImageSize() {
        return getImageSizePathString(mPosterWidthPx);
    }

    public void setMovieData(JSONArray movieData) {
        mMovieData = movieData;
        notifyDataSetChanged();
        Log.d(TAG, "setMovieData: mMovieData=" + mMovieData.toString().substring(0, 100));
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
        if ((position >= getItemCount() - 1)) {
            Log.v(TAG, "Bind in the end, position:" + position);
        }
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mHowManyMovies;
    }


    public interface PosterClickListener {
        public void onPosterClick(int itemIndex, JSONObject jsonObject);
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

            JSONObject dataToDetailActivity = extractOneMovieData(adapterPosition, mMovieData);
            //String imageurlstring = TmdbUriUtil.buildImageUri(context, imagefile, getImageSizePathString(mPosterWidthPx))
            //        .toString();
            //mPosterOnclickListener.onPosterClick(adapterPosition, imageurlstring); //TODO remove
            String dataString;
            dataString = dataToDetailActivity.toString();
            mPosterOnclickListener.onPosterClick(adapterPosition, dataToDetailActivity);
        }

        public void bind(int position) {
            Log.d(TAG, "bind: position=" + position);
            Context context = pvContext;
            // if an android is needed, do imageView.setImageResource(R.mipmap.ic_launcher);
            JSONObject movie = null;
            String imagefilename = null;
            if (mMovieData != null) {
                imagefilename = TmdbDigger.extractPosterName(position, mMovieData);
            } else {
                Log.d(TAG, "mMovieData = null");
            }

            if (imagefilename != null) {
                Uri uri = buildImageUri(context, imagefilename, getImageSizePathString(mPosterWidthPx));
                Picasso.with(pvContext)
                        .load(uri.toString())
                        .placeholder(R.mipmap.ic_launcher)
                        .resize(mPosterWidthPx, mPosterWidthPx) // square
                        .into(imageView);
            } else {
                Log.e(TAG, "imagefilename = null");
            }


        }

    }
}
