
package com.luke.android.travelogy.details;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.luke.android.travelogy.PhotoListAdapter;
import com.luke.android.travelogy.R;
import com.luke.android.travelogy.TravelogyIntentService;
import com.luke.android.travelogy.data.TravelogyContract;
import com.luke.android.travelogy.network.Flag;
import com.luke.android.travelogy.network.Photo;
import com.luke.android.travelogy.network.Trailer;
import com.melnykov.fab.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

public class PhotoListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, PhotoListAdapter.Callbacks {

    public static final String LOG_TAG = PhotoListFragment.class.getSimpleName();
    public static final String ARG_FLAG = "ARG_FLAG";
    public static final String EXTRA_TRAILERS = "EXTRA_TRAILERS";
    public static final String EXTRA_REVIEWS = "EXTRA_REVIEWS";
    private static final int PHOTO_LIST_LOADER = 1;
    private int PICK_IMAGE_REQUEST = 1;
    private Intent mServiceIntent;
    private Activity activity;

    private Flag mFlag;
    private final ArrayList<Location> mLocations;
    private TrailerListAdapter mTrailerListAdapter;
    private ReviewListAdapter mReviewListAdapter;
    private ShareActionProvider mShareActionProvider;

    @Bind(R.id.photo_list)
    RecyclerView mRecyclerView;
    private PhotoListAdapter mAdapter;


    DataPassListener mCallback;
    public interface DataPassListener{
        public void passData(ArrayList<Location> list);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Make sure that container activity implement the callback interface
        try {
            if (context instanceof DataPassListener) {
                mCallback = (DataPassListener)context;
            }

        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement DataPassListener");
        }
    }

    public PhotoListFragment() {
        mLocations = new ArrayList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
        setHasOptionsMenu(true);

        if (getArguments().containsKey(ARG_FLAG)) {
            mFlag = getArguments().getParcelable(ARG_FLAG);
        }


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = getActivity();
        boolean mTwoPane = activity.findViewById(R.id.flag_list) != null;
        mServiceIntent = new Intent(activity, TravelogyIntentService.class);
        if (!mTwoPane) {
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout)
                    activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null && activity instanceof PhotoListActivity) {
                appBarLayout.setTitle(mFlag.getTitle());
            }
            appBarLayout.setExpandedTitleTextAppearance(R.style.ShadowTextStyle);

        }

        ImageView movieBackdrop = ((ImageView) activity.findViewById(R.id.movie_backdrop));
        if (movieBackdrop != null) {
            Picasso.with(activity)
                    .load(mFlag.getBackdropUrl(getContext()))
                    .config(Bitmap.Config.RGB_565)
                    .into(movieBackdrop);
        }
        if (!mTwoPane) {
            FloatingActionButton fab = (FloatingActionButton) activity.findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    Log.v("Luke", "PhotoListFragment fab!!!!!! ");
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

                }
            });
        }




    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Log.v("Luke", "PhotoListFragment onActivityResult!!!!!! ");
            Uri uri = data.getData();

            Log.v("Luke","PhotoListFragment onActivityResult!!!! country name: "+uri.toString());
            mServiceIntent.putExtra("tag", "addPhoto");
            mServiceIntent.putExtra("name", uri.toString());
            mServiceIntent.putExtra("flagName", mFlag.getTitle());
            activity.startService(mServiceIntent);

//            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
//                // Log.d(TAG, String.valueOf(bitmap));
//
//                ImageView imageView = (ImageView) findViewById(R.id.imageView);
//                imageView.setImageBitmap(bitmap);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.photo_detail, container, false);
        ButterKnife.bind(this, rootView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        // To avoid "E/RecyclerView: No adapter attached; skipping layout"
        mAdapter = new PhotoListAdapter(new ArrayList<Photo>(), this);
        mRecyclerView.setAdapter(mAdapter);



//        mMovieTitleView.setText(mMovie.getTitle());
//        mMovieOverviewView.setText(mMovie.getOverview());
//        mMovieReleaseDateView.setText(mMovie.getReleaseDate(getContext()));

//        Picasso.with(getContext())
//                .load(mMovie.getPosterUrl(getContext()))
//                .config(Bitmap.Config.RGB_565)
//                .into(mMoviePosterView);

//        updateRatingBar();
//        updateFavoriteButtons();

        // For horizontal list of trailers
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
//        mRecyclerViewForTrailers.setLayoutManager(layoutManager);
//        mTrailerListAdapter = new TrailerListAdapter(new ArrayList<Trailer>(), this);
//        mRecyclerViewForTrailers.setAdapter(mTrailerListAdapter);
//        mRecyclerViewForTrailers.setNestedScrollingEnabled(false);
//
//        // For vertical list of reviews
//        mReviewListAdapter = new ReviewListAdapter(new ArrayList<Review>(), this);
//        mRecyclerViewForReviews.setAdapter(mReviewListAdapter);

//        // Fetch trailers only if savedInstanceState == null
//        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_TRAILERS)) {
//            List<Trailer> trailers = savedInstanceState.getParcelableArrayList(EXTRA_TRAILERS);
//            mTrailerListAdapter.add(trailers);
//            mButtonWatchTrailer.setEnabled(true);
//        } else {
//            fetchTrailers();
//        }
//
//        // Fetch reviews only if savedInstanceState == null
//        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_REVIEWS)) {
//            List<Review> reviews = savedInstanceState.getParcelableArrayList(EXTRA_REVIEWS);
//            mReviewListAdapter.add(reviews);
//        } else {
//            fetchReviews();
//        }

        Loader loader = getLoaderManager().getLoader(PHOTO_LIST_LOADER);
        Log.v("Luke","PhogoListFragment.onActivityCreated loader: "+loader);
        if (loader != null) {
            getLoaderManager().destroyLoader(PHOTO_LIST_LOADER);
            getLoaderManager().restartLoader(PHOTO_LIST_LOADER, null, this);
        } else {
            getLoaderManager().initLoader(PHOTO_LIST_LOADER, null, this);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.v("Luke", "PhotoListFragment DestryLoader~~~~~~~!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//        getLoaderManager().destroyLoader(PHOTO_LIST_LOADER);
//        ArrayList<Trailer> trailers = mTrailerListAdapter.getTrailers();
//        if (trailers != null && !trailers.isEmpty()) {
//            outState.putParcelableArrayList(EXTRA_TRAILERS, trailers);
//        }
//
//        ArrayList<Review> reviews = mReviewListAdapter.getReviews();
//        if (reviews != null && !reviews.isEmpty()) {
//            outState.putParcelableArrayList(EXTRA_REVIEWS, reviews);
//        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        String tag = PhotoListFragment.class.getName();
        PhotoListFragment fragment = (PhotoListFragment) getFragmentManager().findFragmentByTag("fragment_tag_String");
        Log.v("Luke", "PhotoListFragment onCreateOptionsMenu~~~~~~~!!!!!!!!!!!!!!!!!!!  fragment? "+fragment);
        inflater.inflate(R.menu.movie_detail_fragment, menu);

//        MenuItem shareTrailerMenuItem = menu.findItem(R.id.share_trailer);
//        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareTrailerMenuItem);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v("Luke","PhotoListFragment ++++  onOptionsItemSelected item.getItemId()  "+item.getItemId() );
        Log.v("Luke","PhotoListFragment ++++  onOptionsItemSelected calling passData ");
        switch(item.getItemId()) {
            case R.id.map_portrait:
            case R.id.map_landscape:
                ArrayList<Photo> photoList = new ArrayList();
                photoList = mAdapter.getPhotos();

                for (int i = 0 ; i < photoList.size(); ++i) {

                    Uri uri = Uri.parse(photoList.get(i).getPoster());
                    Log.v("Luke","PhotoListFragment ++++  onOptionsItemSelected calling passData uri.getPath() "+uri.getPath());
                    String filePath = getRealPathFromURI(uri);
                    mLocations.add(readGeoTagImage(filePath));
                    Log.v("Luke","PhotoListFragment ++++  onOptionsItemSelected realfilePath "+filePath);
                }
                mCallback.passData(mLocations);
                break;

            case R.id.photo_landscape:
                Log.v("Luke", "PhotoListFragment optionmenuselected!!!!!! ");
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void watch(Trailer trailer, int position) {
//        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailer.getTrailerUrl())));
//    }
//
//    @Override
//    public void read(Review review, int position) {
//        startActivity(new Intent(Intent.ACTION_VIEW,
//                Uri.parse(review.getUrl())));
//    }

//    @Override
//    public void onFetchFinished(List<Trailer> trailers) {
//        mTrailerListAdapter.add(trailers);
//        mButtonWatchTrailer.setEnabled(!trailers.isEmpty());

//        if (mTrailerListAdapter.getItemCount() > 0) {
//            Trailer trailer = mTrailerListAdapter.getTrailers().get(0);
//            updateShareActionProvider(trailer);
//        }
//    }

//    @Override
//    public void onReviewsFetchFinished(List<Review> reviews) {
//        mReviewListAdapter.add(reviews);
//    }

//    private void fetchTrailers() {
//        FetchTrailersTask task = new FetchTrailersTask(this);
//        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mMovie.getId());
//    }
//
//    private void fetchReviews() {
//        FetchReviewsTask task = new FetchReviewsTask(this);
//        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mMovie.getId());
//    }

//    public void markAsFavorite() {
//
//        new AsyncTask<Void, Void, Void>() {
//
//            @Override
//            protected Void doInBackground(Void... params) {
//                if (!isFavorite()) {
//                    ContentValues movieValues = new ContentValues();
//                    movieValues.put(TravelogyContract.FlagEntry.COLUMN_FLAG_ID,
//                            mMovie.getId());
//                    movieValues.put(TravelogyContract.FlagEntry.COLUMN_FLAG_TITLE,
//                            mMovie.getTitle());
//                    movieValues.put(TravelogyContract.FlagEntry.COLUMN_FLAG_POSTER_PATH,
//                            mMovie.getPoster());
//                    movieValues.put(TravelogyContract.FlagEntry.COLUMN_FLAG_BACKDROP_PATH,
//                            mMovie.getBackdrop());
//                    getContext().getContentResolver().insert(
//                            TravelogyContract.FlagEntry.CONTENT_URI,
//                            movieValues
//                    );
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                updateFavoriteButtons();
//            }
//        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//    }
//
//    public void removeFromFavorites() {
//        new AsyncTask<Void, Void, Void>() {
//
//            @Override
//            protected Void doInBackground(Void... params) {
//                if (isFavorite()) {
//                    getContext().getContentResolver().delete(TravelogyContract.FlagEntry.CONTENT_URI,
//                            TravelogyContract.FlagEntry.COLUMN_FLAG_ID + " = " + mMovie.getId(), null);
//
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                updateFavoriteButtons();
//            }
//        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//    }

    private void updateRatingBar() {
//        if (mMovie.getUserRating() != null && !mMovie.getUserRating().isEmpty()) {
//            String userRatingStr = getResources().getString(R.string.user_rating_movie,
//                    mMovie.getUserRating());
//            mMovieRatingView.setText(userRatingStr);
//
//            float userRating = Float.valueOf(mMovie.getUserRating()) / 2;
//            int integerPart = (int) userRating;
//
//            // Fill stars
//            for (int i = 0; i < integerPart; i++) {
//                ratingStarViews.get(i).setImageResource(R.drawable.ic_star_black_24dp);
//            }
//
//            // Fill half star
//            if (Math.round(userRating) > integerPart) {
//                ratingStarViews.get(integerPart).setImageResource(
//                        R.drawable.ic_star_half_black_24dp);
//            }
//
//        } else {
//            mMovieRatingView.setVisibility(View.GONE);
//        }
    }

//    private void updateFavoriteButtons() {
//        // Needed to avoid "skip frames".
//        new AsyncTask<Void, Void, Boolean>() {
//
//            @Override
//            protected Boolean doInBackground(Void... params) {
//                return isFavorite();
//            }
//
//            @Override
//            protected void onPostExecute(Boolean isFavorite) {
//                if (isFavorite) {
//                    mButtonRemoveFromFavorites.setVisibility(View.VISIBLE);
//                    mButtonMarkAsFavorite.setVisibility(View.GONE);
//                } else {
//                    mButtonMarkAsFavorite.setVisibility(View.VISIBLE);
//                    mButtonRemoveFromFavorites.setVisibility(View.GONE);
//                }
//            }
//        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//
//        mButtonMarkAsFavorite.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        markAsFavorite();
//                    }
//                });
//
//        mButtonWatchTrailer.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (mTrailerListAdapter.getItemCount() > 0) {
//                            watch(mTrailerListAdapter.getTrailers().get(0), 0);
//                        }
//                    }
//                });
//
//        mButtonRemoveFromFavorites.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        removeFromFavorites();
//                    }
//                });
//    }
//
//    private boolean isFavorite() {
//        Cursor movieCursor = getContext().getContentResolver().query(
//                TravelogyContract.FlagEntry.CONTENT_URI,
//                new String[]{TravelogyContract.FlagEntry.COLUMN_FLAG_ID},
//                TravelogyContract.FlagEntry.COLUMN_FLAG_ID + " = " + mMovie.getId(),
//                null,
//                null);
//
//        if (movieCursor != null && movieCursor.moveToFirst()) {
//            movieCursor.close();
//            return true;
//        } else {
//            return false;
//        }
//    }

    public void open(Photo photo, int position) {
//        if (mTwoPane) {
//            Bundle arguments = new Bundle();
//            arguments.putParcelable(PhotoListFragment.ARG_MOVIE, movie);
//            PhotoListFragment fragment = new PhotoListFragment();
//            fragment.setArguments(arguments);
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.movie_detail_container, fragment)
//                    .commit();
//        } else {
//            Intent intent = new Intent(this, PhotoListActivity.class);
//            intent.putExtra(PhotoListFragment.ARG_MOVIE, movie);
//            startActivity(intent);
//        }
    }

    private void updateShareActionProvider(Trailer trailer) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mFlag.getTitle());
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, trailer.getName() + ": "
                + trailer.getTrailerUrl());
        mShareActionProvider.setShareIntent(sharingIntent);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.v("Luke", "PhotoList Fragment onLoadFinished~~~~~~~**");
        mAdapter = new PhotoListAdapter(new ArrayList<Photo>(), this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.add(cursor);
        Log.v("Luke", "PhotoList Fragment onLoadFinished~~~~~~***count: "+mAdapter.getItemCount());

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.v("Luke", "PhotoList Fragment onCreateLoader~~~~~~~");



        Uri photoByFlag = TravelogyContract.PhotoEntry.buildPhotoUriByFlag(mFlag.getTitle());
        Log.v("Luke", "PhotoList Fragment onCreateLoader~~~~~~~   "+photoByFlag);

//        return new CursorLoader(getContext(),
//                TravelogyContract.PhotoEntry.CONTENT_URI,
//                TravelogyContract.PhotoEntry.PHOTO_COLUMNS,
//                null,
//                null,
//                null);

        return new CursorLoader(getContext(),
                photoByFlag,
                TravelogyContract.PhotoEntry.PHOTO_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        // Not used
    }



//    public Location readGeoTagImage(String imagePath)
//    {
//        Log.v("Luke", "Photo readGeoTagImage #### imagePath "+imagePath);
//        Location loc = new Location("");
//        try {
//            ExifInterface exif = new ExifInterface(imagePath);
//            String exifLatitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
//            String exifLongitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
//
//            Log.v("Luke", "Photo readGeoTagImage #### mLat "+exifLatitude + " mLong "+exifLongitude);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return loc;
//    }

    public Location readGeoTagImage(String imagePath)
    {
        Location loc = new Location("");
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            float [] latlong = new float[2] ;
            if(exif.getLatLong(latlong)){
                loc.setLatitude(latlong[0]);
                loc.setLongitude(latlong[1]);
                Log.v("Luke","PhotoListFragment ++++  readGeoTagImage latlong[0] "+latlong[0] + " latlong[1] "+latlong[1]);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return loc;
    }


    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContext().getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }


}
