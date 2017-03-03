

package com.luke.android.travelogy;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.luke.android.travelogy.data.TravelogyContract;
import com.luke.android.travelogy.network.Flag;
import com.luke.android.travelogy.network.Photo;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PhotoListAdapter
        extends RecyclerView.Adapter<PhotoListAdapter.ViewHolder> {

    @SuppressWarnings("unused")
    private final static String LOG_TAG = PhotoListAdapter.class.getSimpleName();

    private final ArrayList<Photo> mPhotos;
    private final ArrayList<Location> mLocations;
    private final Callbacks mCallbacks;

    public interface Callbacks {
        void open(Photo photo, int position);
    }

    public PhotoListAdapter(ArrayList<Photo> photos, Callbacks callbacks) {
        mPhotos = photos;
        mLocations = new ArrayList();
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
        final Photo photo = mPhotos.get(position);
        final Context context = holder.mView.getContext();

        holder.mPhoto = photo;
        holder.mTitleView.setText(photo.getTitle());

        String posterUrl = photo.getPosterUrl(context);
        // Warning: onError() will not be called, if url is null.
        // Empty url leads to app crash.
        if (posterUrl == null) {
            holder.mTitleView.setVisibility(View.VISIBLE);
        }

        Picasso.with(context)
                .load(photo.getPosterUrl(context))
                .config(Bitmap.Config.RGB_565)
                .into(holder.mThumbnailView,
                        new Callback() {
                            @Override
                            public void onSuccess() {
                                if (holder.mPhoto.getId() != photo.getId()) {
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
                mCallbacks.open(photo, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPhotos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        @Bind(R.id.thumbnail)
        ImageView mThumbnailView;
        @Bind(R.id.title)
        TextView mTitleView;
        public Photo mPhoto;

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

    public void add(List<Photo> photo) {
        mPhotos.clear();
        mPhotos.addAll(photo);
        notifyDataSetChanged();
    }

    public void add(Cursor cursor) {
        mPhotos.clear();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(TravelogyContract.PhotoEntry.COL_PHOTO_ID);
                String title = cursor.getString(TravelogyContract.PhotoEntry.COL_PHOTO_TITLE);
                String path = cursor.getString(TravelogyContract.PhotoEntry.COL_PHOTO_PATH);
                String flag_key = cursor.getString(TravelogyContract.PhotoEntry.COL_PHOTO_FLAG_KEY);
                Photo photo = new Photo(id, title, path, flag_key);
                mPhotos.add(photo);
            } while (cursor.moveToNext());
        }
        notifyDataSetChanged();
    }

    public ArrayList<Photo> getPhotos() {
        return mPhotos;
    }

}
