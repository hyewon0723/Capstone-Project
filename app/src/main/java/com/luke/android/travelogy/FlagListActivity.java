

package com.luke.android.travelogy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.luke.android.travelogy.data.TravelogyContract;
import com.luke.android.travelogy.details.PhotoListActivity;
import com.luke.android.travelogy.details.PhotoListFragment;
import com.luke.android.travelogy.network.Flag;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * An activity representing a grid of Flags. This activity
 * has different presentations for handset and tablet-size devices.
 */
public class FlagListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, FlagListAdapter.Callbacks {

    private static final int FLAG_LOADER = 0;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private FlagListAdapter mAdapter;
    private Intent mServiceIntent;
    private Context mContext;

    @Bind(R.id.flag_list)
    RecyclerView mRecyclerView;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    ConnectivityManager cm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(getApplicationContext(),R.string.error_notfound, Toast.LENGTH_LONG).show();

            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("myBroadcastIntent"));


        setContentView(R.layout.activity_flag_list);
        ButterKnife.bind(this);
        mContext = this;
        cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        mToolbar.setTitle(R.string.title_flag_list);
        setSupportActionBar(mToolbar);
        mServiceIntent = new Intent(this, TravelogyIntentService.class);


        mRecyclerView.setLayoutManager(new GridLayoutManager(this, getResources()
                .getInteger(R.integer.grid_number_cols)));
        // To avoid "E/RecyclerView: No adapter attached; skipping layout"
        mAdapter = new FlagListAdapter(new ArrayList<Flag>(), this);
        mRecyclerView.setAdapter(mAdapter);

        // For large-screen layouts (res/values-w900dp).
        mTwoPane = findViewById(R.id.photo_detail_container) != null;
        if (!mTwoPane) {
            //tag = PhotoListFragment.class.getName();
            PhotoListFragment fragment = (PhotoListFragment) getSupportFragmentManager().findFragmentByTag("fragment_tag_String");
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
        }

        Loader loader = getSupportLoaderManager().getLoader(FLAG_LOADER);
        if (loader != null) {
            getSupportLoaderManager().destroyLoader(FLAG_LOADER);
            getSupportLoaderManager().restartLoader(FLAG_LOADER, null, this);
        } else {
            getSupportLoaderManager().initLoader(FLAG_LOADER, null, this);
        }
        updateEmptyState();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (isConnected()){
                    new MaterialDialog.Builder(mContext).title(R.string.title_dialog)
                            .content(R.string.content_fab)
                            .inputType(InputType.TYPE_CLASS_TEXT)
                            .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                                @Override public void onInput(MaterialDialog dialog, CharSequence input) {


                                    // in the DB and proceed accordingly


                                    Cursor flagCursor = mContext.getContentResolver().query( TravelogyContract.FlagEntry.CONTENT_URI,new String[]{TravelogyContract.FlagEntry._ID},
                                    TravelogyContract.FlagEntry.COLUMN_FLAG_SETTING + " = ?", new String[]{input.toString()}, null);
                                    if (flagCursor.getCount() == 0) {
                                        mServiceIntent.putExtra("tag", "add");
                                        mServiceIntent.putExtra("name", input.toString());
                                        startService(mServiceIntent);
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(),R.string.error_existing, Toast.LENGTH_LONG).show();
                                    }


                                }
                            })
                            .show();
                } else {
                    networkToast();
                }

            }
        });
    }




    public boolean isConnected() {
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting());
    }

    public void networkToast(){
        Toast.makeText(mContext, getString(R.string.network_toast), Toast.LENGTH_SHORT).show();
    }


    @Override
    public void open(Flag flag, int position) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(PhotoListFragment.ARG_FLAG, flag);
            PhotoListFragment fragment = new PhotoListFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.photo_detail_container, fragment,"fragment_tag_String")
                    .commit();
        } else {
            Intent intent = new Intent(this, PhotoListActivity.class);
            intent.putExtra(PhotoListFragment.ARG_FLAG, flag);
            startActivity(intent);
        }
    }


    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.add(cursor);
        updateEmptyState();

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                TravelogyContract.FlagEntry.CONTENT_URI,
                TravelogyContract.FlagEntry.FLAG_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        // Not used
    }



    private void updateEmptyState() {
        if (mAdapter.getItemCount() == 0) {
            findViewById(R.id.empty_state_container).setVisibility(View.GONE);
            findViewById(R.id.empty_state_flag_container).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.empty_state_container).setVisibility(View.GONE);
            findViewById(R.id.empty_state_flag_container).setVisibility(View.GONE);
        }
    }
}
