

package com.luke.android.travelogy;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.luke.android.travelogy.data.FlagContract;
import com.luke.android.travelogy.network.Flag;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FlagListAdapter
        extends RecyclerView.Adapter<FlagListAdapter.ViewHolder> {

    @SuppressWarnings("unused")
    private final static String LOG_TAG = FlagListAdapter.class.getSimpleName();

    private final ArrayList<Flag> mMovies;
    private final Callbacks mCallbacks;

    public interface Callbacks {
        void open(Flag movie, int position);
    }

    public FlagListAdapter(ArrayList<Flag> movies, Callbacks callbacks) {
        mMovies = movies;
        this.mCallbacks = callbacks;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.flag_list_content, parent, false);
        final Context context = view.getContext();

        int gridColsNumber = context.getResources()
                .getInteger(R.integer.grid_number_cols);

        view.getLayoutParams().height = (int) (parent.getWidth() / gridColsNumber *
                Flag.POSTER_ASPECT_RATIO);

        return new ViewHolder(view);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.cleanUp();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Flag movie = mMovies.get(position);
        final Context context = holder.mView.getContext();

        holder.mMovie = movie;
        holder.mTitleView.setText(movie.getTitle());

        String posterUrl = movie.getPosterUrl(context);
        // Warning: onError() will not be called, if url is null.
        // Empty url leads to app crash.
        if (posterUrl == null) {
            holder.mTitleView.setVisibility(View.VISIBLE);
        }

        Picasso.with(context)
                .load(movie.getPosterUrl(context))
                .config(Bitmap.Config.RGB_565)
                .into(holder.mThumbnailView,
                        new Callback() {
                            @Override
                            public void onSuccess() {
                                if (holder.mMovie.getId() != movie.getId()) {
                                    holder.cleanUp();
                                } else {
                                    holder.mThumbnailView.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onError() {
                                holder.mTitleView.setVisibility(View.VISIBLE);
                            }
                        }
                );

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.open(movie, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        @Bind(R.id.thumbnail)
        ImageView mThumbnailView;
        @Bind(R.id.title)
        TextView mTitleView;
        public Flag mMovie;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;
        }

        public void cleanUp() {
            final Context context = mView.getContext();
            Picasso.with(context).cancelRequest(mThumbnailView);
            mThumbnailView.setImageBitmap(null);
            mThumbnailView.setVisibility(View.INVISIBLE);
            mTitleView.setVisibility(View.GONE);
        }

    }

    public void add(List<Flag> movies) {
        mMovies.clear();
        mMovies.addAll(movies);
        notifyDataSetChanged();
    }

    public void add(Cursor cursor) {
        mMovies.clear();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(FlagContract.MovieEntry.COL_MOVIE_ID);
                String title = cursor.getString(FlagContract.MovieEntry.COL_MOVIE_TITLE);
                String posterPath = cursor.getString(FlagContract.MovieEntry.COL_MOVIE_POSTER_PATH);
                String overview = cursor.getString(FlagContract.MovieEntry.COL_MOVIE_OVERVIEW);
                String rating = cursor.getString(FlagContract.MovieEntry.COL_MOVIE_VOTE_AVERAGE);
                String releaseDate = cursor.getString(FlagContract.MovieEntry.COL_MOVIE_RELEASE_DATE);
                String backdropPath = cursor.getString(FlagContract.MovieEntry.COL_MOVIE_BACKDROP_PATH);
                Flag movie = new Flag(id, title, posterPath, overview, rating, releaseDate, backdropPath);
                mMovies.add(movie);
            } while (cursor.moveToNext());
        }
        notifyDataSetChanged();
    }

    public ArrayList<Flag> getMovies() {
        return mMovies;
    }
}
