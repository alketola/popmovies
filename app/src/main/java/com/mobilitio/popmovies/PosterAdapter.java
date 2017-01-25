package com.mobilitio.popmovies;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by antti on 24/01/17.
 */

public class PosterAdapter extends RecyclerView.Adapter<PosterAdapter.PosterViewHolder> {
    //
    private static final String TAG = PosterAdapter.class.getSimpleName();
    private int mHowManyMovies;
    private ArrayList<String> mPosterImageNames;
    private static int mPosterWidthPx;
    final private PosterClickListener mPosterOnclickListener;


    public PosterAdapter(int numberOfDisplayedMovies,
                         int posterWidth,
                         PosterClickListener posterOnClickListener) {
        mHowManyMovies = numberOfDisplayedMovies;
        mPosterWidthPx = posterWidth;
        mPosterOnclickListener = posterOnClickListener;
        if (mPosterImageNames == null) {
            mPosterImageNames = new ArrayList<String>(numberOfDisplayedMovies);
        }
        clearPosters();
        prepareMoviePosterImageArrayList(numberOfDisplayedMovies);
    }

    public interface PosterClickListener {
        public void onPosterClick(int itemIndex, String imageUrl);
    }

    private void addMoviePoster(String posterName) {
        mPosterImageNames.add(posterName);
        mHowManyMovies++;
    }

    private void clearPosters() {
        mPosterImageNames.clear();
        mHowManyMovies = 0;
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

    private void prepareMoviePosterImageArrayList(int initialSize) {
        addMoviePoster("WLQN5aiQG8wc9SeKwixW7pAR8K.jpg");
        addMoviePoster("z4x0Bp48ar3Mda8KiPD1vwSY3D8.jpg");
        addMoviePoster("z09QAf8WbZncbitewNk6lKYMZsh.jpg");
        addMoviePoster("tIKFBxBZhSXpIITiiB5Ws8VGXjt.jpg");
        addMoviePoster("ylXCdC106IKiarftHkcacasaAcb.jpg");
        addMoviePoster("jjBgi2r5cRt36xF6iNUEhzscEcb.jpg");
        addMoviePoster("5gJkVIVU7FDp7AfRAbPSvvdbre2.jpg");

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

    private static Uri buildImageUri(Context context, String filename) {
        Resources res = context.getResources();
        Uri.Builder ub = new Uri.Builder();
        Uri uri = ub.scheme(res.getString(R.string.tmdb_img_scheme))
                .authority(res.getString(R.string.tmdb_img_authority))
                .appendPath(res.getString(R.string.tmdb_img_path_1))
                .appendPath(res.getString(R.string.tmdb_img_path_2))
                .appendPath(getPathBySetImageSize())
                .appendPath(filename)
                .build();
        Log.v(TAG, "Image Uri:" + uri.toString());
        return uri;
    }
    /**** ViewHolder class ****/
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
            Log.v(TAG,"onClick");
            Context context = v.getContext();
            int adapterPosition = getAdapterPosition();
            String imagefile = mPosterImageNames.get(adapterPosition);
            String imageurlstring = PosterAdapter.buildImageUri(context,imagefile).toString();
            mPosterOnclickListener.onPosterClick(adapterPosition,imageurlstring);
        }

        public void bind(int position) {
            Log.v(TAG, "bind: position=" + position);
            Context context = pvContext;
//            imageView.setImageResource(R.mipmap.ic_launcher);
            if (mPosterImageNames.size() >= position) {
                String imagefilename = mPosterImageNames.get(position);
                Uri uri = buildImageUri(context, imagefilename);

                Picasso.with(pvContext)
                        .load(uri.toString())
                        .resize(mPosterWidthPx, mPosterWidthPx) // square
                        .into(imageView);
            }

        }

    }
}
