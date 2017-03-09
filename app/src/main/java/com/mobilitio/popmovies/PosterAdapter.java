package com.mobilitio.popmovies;

import android.content.Context;
import android.net.Uri;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import static android.support.v7.widget.RecyclerView.NO_POSITION;
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
    private SimpleArrayMap<Integer, JSONArray> mMovieDataPages;

    private boolean mOverlayOn = false;

    public PosterAdapter(int numberOfDisplayedMovies,
                         int posterWidth,
                         PosterClickListener posterOnClickListener) {
        mHowManyMovies = numberOfDisplayedMovies;
        setPosterWidth(posterWidth);
        mPosterOnclickListener = posterOnClickListener;
        mMovieDataPages = new SimpleArrayMap<Integer, JSONArray>(3);
    }

    private static String getPathBySetImageSize() {
        return getImageSizePathString(mPosterWidthPx);
    }

    public void setOverlay(boolean on) {
        mOverlayOn = on;
    }

    public void setMovieData(JSONArray movieData) {
        mMovieData = movieData;
        mHowManyMovies = movieData.length();
        notifyDataSetChanged();
        // JUST TOO MUCH PRINT Log.d(TAG, "setMovieData: mMovieData=" + mMovieData.toString());//.substring(0, 100)ad
    }

    public void setPosterWidth(int px) {
        mPosterWidthPx = px;
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
        TextView overlayView;
        Context pvContext;
        private final static int TEXT_SIZE_FRACTION_OF_POSTER = 10;

        public PosterViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.iv_a_poster);
            imageView.setOnClickListener(this);
            pvContext = view.getContext();
            overlayView = (TextView) view.findViewById(R.id.tv_poster_overlay);
        }

        @Override
        public void onClick(View v) {
            Log.v(TAG, "onClick");
            Context context = v.getContext();
            int adapterPosition = getAdapterPosition();
            if (adapterPosition == (NO_POSITION)) {
                Log.d(TAG, "ViewHolder at NO_POSITION; not ready yet");
                return;
            }
            if (mMovieData == null) return;
            JSONObject dataToDetailActivity = extractOneMovieData(adapterPosition, mMovieData);
            if (dataToDetailActivity == null) {
                Log.w(TAG, "Data not ready for details (null)");
                return;
            }
            mPosterOnclickListener.onPosterClick(adapterPosition, dataToDetailActivity);
        }

        public void bind(int position) {
            //Log.d(TAG, "bind: position=" + position);
            Context context = pvContext;
            // if an android is needed, do imageView.setImageResource(R.mipmap.ic_launcher);
            JSONObject movie = null;
            String imagefilename = null;
            if (mMovieData != null) {
                imagefilename = TmdbDigger.extractPosterName(position, mMovieData);
            } else {
                //Log.d(TAG, "mMovieData = null");
                return;
            }

            if (imagefilename != null) {
                Uri uri = buildImageUri(context, imagefilename, getImageSizePathString(mPosterWidthPx));
                Picasso.with(pvContext)
                        .load(uri.toString())
                        .placeholder(R.drawable.squarepivot92)
                        .error(R.drawable.squarepivot92red)
                        .resize(mPosterWidthPx, mPosterWidthPx) // square
                        .into(imageView);
                if (mOverlayOn) {
                    String movieInfo = TmdbDigger.extractShortMovieInfo(position, mMovieData);
                    overlayView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mPosterWidthPx / TEXT_SIZE_FRACTION_OF_POSTER);
                    overlayView.setText(String.valueOf(position + 1) + ". " + movieInfo);
                    overlayView.setVisibility(View.VISIBLE);
                } else {
                    overlayView.setVisibility(View.INVISIBLE);
                }
            } else {
                Log.e(TAG, "imagefilename = null");
            }
        }
    }
}
