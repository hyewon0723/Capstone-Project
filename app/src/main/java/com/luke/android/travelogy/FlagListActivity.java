

package com.luke.android.travelogy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.luke.android.travelogy.data.FlagContract;
import com.luke.android.travelogy.details.PhotoListActivity;
import com.luke.android.travelogy.details.PhotoListFragment;
import com.luke.android.travelogy.network.Flag;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * An activity representing a grid of Flags. This activity
 * has different presentations for handset and tablet-size devices.
 */
public class FlagListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        FetchMoviesTask.Listener, FlagListAdapter.Callbacks {

    private static final String EXTRA_MOVIES = "EXTRA_MOVIES";
    private static final String EXTRA_SORT_BY = "EXTRA_SORT_BY";
    private static final int FAVORITE_MOVIES_LOADER = 0;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private RetainedFragment mRetainedFragment;
    private FlagListAdapter mAdapter;
    private String mSortBy = FetchMoviesTask.MOST_POPULAR;
    private Intent mServiceIntent;
    private Context mContext;

    @Bind(R.id.movie_list)
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
                //Handle wrong symbol.
                //Toast.makeText(getApplicationContext(),"test", Toast.LENGTH_LONG).show();
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("myBroadcastIntent"));


        setContentView(R.layout.activity_flag_list);
        ButterKnife.bind(this);
        mContext = this;
        cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        mToolbar.setTitle(R.string.title_flag_list);
        setSupportActionBar(mToolbar);
        mServiceIntent = new Intent(this, FlagIntentService.class);
        String tag = RetainedFragment.class.getName();
        this.mRetainedFragment = (RetainedFragment) getSupportFragmentManager().findFragmentByTag(tag);
        if (this.mRetainedFragment == null) {
            this.mRetainedFragment = new RetainedFragment();
            getSupportFragmentManager().beginTransaction().add(this.mRetainedFragment, tag).commit();
        }

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, getResources()
                .getInteger(R.integer.grid_number_cols)));
        // To avoid "E/RecyclerView: No adapter attached; skipping layout"
        mAdapter = new FlagListAdapter(new ArrayList<Flag>(), this);
        mRecyclerView.setAdapter(mAdapter);

        // For large-screen layouts (res/values-w900dp).
        mTwoPane = findViewById(R.id.movie_detail_container) != null;

        if (savedInstanceState != null) {
            mSortBy = savedInstanceState.getString(EXTRA_SORT_BY);
            if (savedInstanceState.containsKey(EXTRA_MOVIES)) {
                List<Flag> movies = savedInstanceState.getParcelableArrayList(EXTRA_MOVIES);
                mAdapter.add(movies);
                findViewById(R.id.progress).setVisibility(View.GONE);

                // For listening content updates for tow pane mode
                if (mSortBy.equals(FetchMoviesTask.FAVORITES)) {
                    getSupportLoaderManager().initLoader(FAVORITE_MOVIES_LOADER, null, this);
                }
            }
            updateEmptyState();
        } else {
            // Fetch Flags only if savedInstanceState == null
            fetchMovies(mSortBy);
        }

        updateEmptyState();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (isConnected()){
                    new MaterialDialog.Builder(mContext).title(R.string.title_dialog)
                            .content(R.string.content_test)
                            .inputType(InputType.TYPE_CLASS_TEXT)
                            .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
                                @Override public void onInput(MaterialDialog dialog, CharSequence input) {
                                    // On FAB click, receive user input. Make sure the stock doesn't already exist
                                    // in the DB and proceed accordingly
//                                    Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
//                                            new String[] { QuoteColumns.SYMBOL }, QuoteColumns.SYMBOL + "= ?",
//                                            new String[] { input.toString() }, null);
//
//                                    if (c.getCount() != 0) {
//                                        Toast toast =
//                                                Toast.makeText(MyStocksActivity.this, R.string.saved_msg,
//                                                        Toast.LENGTH_LONG);
//                                        toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
//                                        toast.show();
//                                        return;
//                                    } else {
//                                        // Add the stock to DB
//                                        mServiceIntent.putExtra("tag", "add");
//                                        mServiceIntent.putExtra("symbol", input.toString());
//                                        startService(mServiceIntent);
//                                    }
                                    Log.v("Luke","FlagListActivity fab!!!! country name: "+input.toString());
                                    mServiceIntent.putExtra("tag", "add");
                                    mServiceIntent.putExtra("name", input.toString());
                                    startService(mServiceIntent);
                                    Log.v("Luke","FlagListActivity fab!!!! ending country name: "+input.toString());

                                }
                            })
                            .show();
                } else {
                    networkToast();
                }

            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<Flag> movies = mAdapter.getMovies();
        if (movies != null && !movies.isEmpty()) {
            outState.putParcelableArrayList(EXTRA_MOVIES, movies);
        }
        outState.putString(EXTRA_SORT_BY, mSortBy);

        // Needed to avoid confusion, when we back from detail screen (i. e. top rated selected but
        // favorite movies are shown and onCreate was not called in this case).
        if (!mSortBy.equals(FetchMoviesTask.FAVORITES)) {
            getSupportLoaderManager().destroyLoader(FAVORITE_MOVIES_LOADER);
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.movie_list_activity, menu);

        switch (mSortBy) {
            case FetchMoviesTask.MOST_POPULAR:
                menu.findItem(R.id.sort_by_most_popular).setChecked(true);
                break;
            case FetchMoviesTask.TOP_RATED:
                menu.findItem(R.id.sort_by_top_rated).setChecked(true);
                break;
            case FetchMoviesTask.FAVORITES:
                menu.findItem(R.id.sort_by_favorites).setChecked(true);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_by_top_rated:
                if (mSortBy.equals(FetchMoviesTask.FAVORITES)) {
                    getSupportLoaderManager().destroyLoader(FAVORITE_MOVIES_LOADER);
                }
                mSortBy = FetchMoviesTask.TOP_RATED;
                fetchMovies(mSortBy);
                item.setChecked(true);
                break;
            case R.id.sort_by_most_popular:
                if (mSortBy.equals(FetchMoviesTask.FAVORITES)) {
                    getSupportLoaderManager().destroyLoader(FAVORITE_MOVIES_LOADER);
                }
                mSortBy = FetchMoviesTask.MOST_POPULAR;
                fetchMovies(mSortBy);
                item.setChecked(true);
                break;
            case R.id.sort_by_favorites:
                mSortBy = FetchMoviesTask.FAVORITES;
                item.setChecked(true);
                fetchMovies(mSortBy);
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void open(Flag movie, int position) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(PhotoListFragment.ARG_MOVIE, movie);
            PhotoListFragment fragment = new PhotoListFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, PhotoListActivity.class);
            intent.putExtra(PhotoListFragment.ARG_MOVIE, movie);
            startActivity(intent);
        }
    }

    @Override
    public void onFetchFinished(Command command) {
        if (command instanceof FetchMoviesTask.NotifyAboutTaskCompletionCommand) {
            Log.v("Luke", "onFetchFinished~~~~~~~");
            mAdapter.add(((FetchMoviesTask.NotifyAboutTaskCompletionCommand) command).getMovies());
            updateEmptyState();
            findViewById(R.id.progress).setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.v("Luke", "onLoadFinished~~~~~~~");
        mAdapter.add(cursor);
        updateEmptyState();
        findViewById(R.id.progress).setVisibility(View.GONE);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        return new CursorLoader(this,
                FlagContract.FlagEntry.CONTENT_URI,
                FlagContract.FlagEntry.MOVIE_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        // Not used
    }

    private void fetchMovies(String sortBy) {
        if (!sortBy.equals(FetchMoviesTask.FAVORITES)) {
            findViewById(R.id.progress).setVisibility(View.VISIBLE);
            FetchMoviesTask.NotifyAboutTaskCompletionCommand command =
                    new FetchMoviesTask.NotifyAboutTaskCompletionCommand(this.mRetainedFragment);
            new FetchMoviesTask(sortBy, command).execute();
        } else {
            Log.v("Luke", "fetchMovies~~~~~~~ initLoader");
            getSupportLoaderManager().initLoader(FAVORITE_MOVIES_LOADER, null, this);
        }
    }

    private void updateEmptyState() {
        if (mAdapter.getItemCount() == 0) {
            if (mSortBy.equals(FetchMoviesTask.FAVORITES)) {
                findViewById(R.id.empty_state_container).setVisibility(View.GONE);
                findViewById(R.id.empty_state_favorites_container).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.empty_state_container).setVisibility(View.VISIBLE);
                findViewById(R.id.empty_state_favorites_container).setVisibility(View.GONE);
            }
        } else {
            findViewById(R.id.empty_state_container).setVisibility(View.GONE);
            findViewById(R.id.empty_state_favorites_container).setVisibility(View.GONE);
        }
    }

    /**
     * RetainedFragment with saving state mechanism.
     * The saving state mechanism helps to not lose user's progress even when app is in the
     * background state or user rotate device and also to avoid performing code which
     * will lead to "java.lang.IllegalStateException: Can not perform some actions after
     * onSaveInstanceState". As the result we have commands which we cannot execute now,
     * but we have to store it and execute later.
     *
     * @see com.luke.android.travelogy.FetchMoviesTask.NotifyAboutTaskCompletionCommand
     */
    public static class RetainedFragment extends Fragment implements FetchMoviesTask.Listener {

        private boolean mPaused = false;
        // Currently allow to wait one command, because more is not needed. In future it can be
        // extended to list etc. Using "MacroCommand" which contain includes other commands as waiting command.
        private Command mWaitingCommand = null;

        public RetainedFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        @Override
        public void onPause() {
            super.onPause();
            mPaused = true;
        }

        @Override
        public void onResume() {
            super.onResume();
            mPaused = false;
            if (mWaitingCommand != null) {
                onFetchFinished(mWaitingCommand);
            }
        }

        @Override
        public void onFetchFinished(Command command) {
            if (getActivity() instanceof FetchMoviesTask.Listener && !mPaused) {
                FetchMoviesTask.Listener listener = (FetchMoviesTask.Listener) getActivity();
                listener.onFetchFinished(command);
                mWaitingCommand = null;
            } else {
                // Save the command for later.
                mWaitingCommand = command;
            }
        }
    }
}
