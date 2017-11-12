package com.team3s.lostpropertyse.Maps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.team3s.lostpropertyse.R;

public class PropMaps extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;

    private DatabaseReference databaseMarker;
    private DatabaseReference mDatabaseUserLoc;
    private DatabaseReference mDatabaseLike;
    private DatabaseReference mDatabaseShowMap;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;


    Button addDuk;

    Marker marker;

    double latMarkers;
    double lngMarkers;
    private String post_key;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prop_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        post_key = getIntent().getExtras().getString("post_id");
        mDatabaseShowMap = FirebaseDatabase.getInstance().getReference().child("Icerik").child(post_key).child("latlng");



        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        databaseMarker = FirebaseDatabase.getInstance().getReference().child("Icerik");
        mDatabaseUserLoc = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        if(!post_key.equals("")) {
            mDatabaseShowMap.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    latMarkers = (double) snapshot.child("latitude").getValue();
                    lngMarkers = (double) snapshot.child("longitude").getValue();
                    LatLng item = new LatLng(latMarkers, lngMarkers);
                   // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                    //mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(item,18));
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

        }else{
            mDatabaseUserLoc.child("latLng").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    latMarkers = (double) snapshot.child("latitude").getValue();
                    lngMarkers = (double) snapshot.child("longitude").getValue();
                    LatLng user = new LatLng(latMarkers, lngMarkers);
                    // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                    //mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user,12));
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }


        markers();

    }
    public void markers(){
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance()
                .getReference();
        DatabaseReference node = mDatabaseReference.child("Icerik");

        node.orderByChild("latlng").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    double latUserL = (double) data.child("latlng").child("latitude").getValue();
                    double lngUserL = (double) data.child("latlng").child("longitude").getValue();



                    marker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(latUserL, lngUserL))
                            .title((String) data.child("questions").getValue())
                            //.icon(bitmapDescriptorFromVector(PropMaps.this, ids))
                            .snippet((String) data.child("desc").getValue()));
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(Marker arg0) {
                if(arg0 != null && arg0.getTitle().equals(marker.getTitle().toString())); // if marker  source is clicked
                arg0.showInfoWindow();
                return true;
            }

        });

    }
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (mGoogleApiClient == null) {
                        buildGoogleApiClient();
                    }
                    mMap.setMyLocationEnabled(true);


                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }
}