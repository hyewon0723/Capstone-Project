package com.luke.android.travelogy;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ArrayList<Location> myList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        myList = getIntent().getParcelableArrayListExtra("location");
        Log.v("Luke","MapsActivity ++++  getParcelableExtra myList"+myList);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        if (myList != null) {
            Marker[] allMarkers = new Marker[myList.size()];
            for (int i = 0; i < myList.size(); i++)
            {
                LatLng latLng = new LatLng((myList.get(i)).getLatitude(), (myList.get(i)).getLongitude());
                if (googleMap != null) {
                    allMarkers[i] = googleMap.addMarker(new MarkerOptions().position(latLng));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 4.0f));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 4));

                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v("Luke","MapsActivity ++++  onOptionsItemSelected item.getItemId()  "+item.getItemId() );
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
