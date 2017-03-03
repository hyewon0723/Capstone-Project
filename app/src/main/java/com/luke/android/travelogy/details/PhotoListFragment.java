
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.luke.android.travelogy.PhotoListAdapter;
import com.luke.android.travelogy.R;
import com.luke.android.travelogy.TravelogyIntentService;
import com.luke.android.travelogy.data.TravelogyContract;
import com.luke.android.travelogy.network.Flag;
import com.luke.android.travelogy.network.Photo;
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
    private static final int PHOTO_LIST_LOADER = 1;
    private int PICK_IMAGE_REQUEST = 1;
    private Intent mServiceIntent;
    private Activity activity;

    private Flag mFlag;
    private final ArrayList<Location> mLocations;

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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.photo_detail, container, false);
        ButterKnife.bind(this, rootView);
        Log.v("Luke","PhotoListFragment ### onCreateView rootView.findViewById(R.id.empty_state_flag_container) "+rootView.findViewById(R.id.empty_state_flag_container));


        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        // To avoid "E/RecyclerView: No adapter attached; skipping layout"
        mAdapter = new PhotoListAdapter(new ArrayList<Photo>(), this);
        mRecyclerView.setAdapter(mAdapter);

        // For horizontal list of trailers
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        PhotoListFragment fragment = (PhotoListFragment) getFragmentManager().findFragmentByTag("fragment_tag_String");
        Log.v("Luke", "PhotoListFragment onCreateOptionsMenu~~~~~~~!!!!!!!!!!!!!!!!!!!  fragment? "+fragment);
        inflater.inflate(R.menu.flag_detail_fragment, menu);
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
                    try {
                        String filePath = getRealPathFromURI(uri);
                        mLocations.add(readGeoTagImage(filePath));
                        Log.v("Luke","PhotoListFragment ++++  onOptionsItemSelected realfilePath "+filePath);
                        if (mLocations.size() == 0) {
                            Toast.makeText(getContext(),R.string.error_location, Toast.LENGTH_LONG).show();
                        }
                        else {
                            mCallback.passData(mLocations);
                        }
                    }
                    catch(Exception ex) {
                        Toast.makeText(getContext(),R.string.error_permission, Toast.LENGTH_LONG).show();
                    }
                }

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


    public void open(Photo photo, int position) {
    }


    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.v("Luke", "PhotoList Fragment onLoadFinished~~~~~~~**");
        mAdapter = new PhotoListAdapter(new ArrayList<Photo>(), this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.add(cursor);
        updateEmptyState();
        Log.v("Luke", "PhotoList Fragment onLoadFinished~~~~~~***count: "+mAdapter.getItemCount());

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.v("Luke", "PhotoList Fragment onCreateLoader~~~~~~~");

        Uri photoByFlag = TravelogyContract.PhotoEntry.buildPhotoUriByFlag(mFlag.getTitle());
        Log.v("Luke", "PhotoList Fragment onCreateLoader~~~~~~~   "+photoByFlag);


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

    private void updateEmptyState() {
        Log.v("Luke","PhotoListFragment ### updateEmptyState activity.findViewById(R.id.empty_state_flag_container) "+activity.findViewById(R.id.empty_state_flag_container));
            if (activity.findViewById(R.id.empty_state_photo_container) != null) {
                if (mAdapter.getItemCount() == 0) {
                    activity.findViewById(R.id.empty_state_photo_container).setVisibility(View.VISIBLE);
                } else {
                    activity.findViewById(R.id.empty_state_photo_container).setVisibility(View.GONE);
                }
            }
    }


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
