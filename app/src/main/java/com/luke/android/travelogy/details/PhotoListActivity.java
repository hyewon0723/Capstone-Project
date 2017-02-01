

package com.luke.android.travelogy.details;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.luke.android.travelogy.FlagListActivity;
import com.luke.android.travelogy.MapsActivity;
import com.luke.android.travelogy.R;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * An activity representing a single Flag detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link FlagListActivity}.
 */
public class PhotoListActivity extends AppCompatActivity implements PhotoListFragment.DataPassListener {

    @Bind(R.id.detail_toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(PhotoListFragment.ARG_MOVIE,
                    getIntent().getParcelableExtra(PhotoListFragment.ARG_MOVIE));
            PhotoListFragment fragment = new PhotoListFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v("Luke","PhotoListActivity ++++  onOptionsItemSelected item.getItemId()  "+item.getItemId() );
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void passData(ArrayList<Location> list) {
        Log.v("Luke","PhotoListFragment ++++  passData list "+list.size());
        Intent intent=new Intent(this, MapsActivity.class);
//        intent.putParcelableArrayListExtra("location", list);
        intent.putParcelableArrayListExtra("location", list);
        startActivity(intent);
    }
}
