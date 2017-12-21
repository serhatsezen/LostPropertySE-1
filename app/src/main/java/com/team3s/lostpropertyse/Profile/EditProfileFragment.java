package com.team3s.lostpropertyse.Profile;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.team3s.lostpropertyse.R;
import com.team3s.lostpropertyse.Utils.CircleTransform;

import org.w3c.dom.Text;

import java.io.IOException;

public class EditProfileFragment extends AppCompatActivity {

    private EditText adSoyad;
    private TextView sehir;
    private String Uid,themeStr;
    private ImageView profileİmg;
    private MapView mapView;
    private GoogleMap myGoogleMap;
    public ImageButton kaydetButton,iptalButon,themaNightBtn,themaDayBtn;
    public Uri newImageUri;
    private boolean mLocationPermissionGranted;
    private StorageReference imgStorageRef;
    private FirebaseUser currentUser;
    private DatabaseReference curentUserRef;
    private Query mQueryUserLost;
    private Query mQueryUserFind;
    double latMarkers;
    double lngMarkers;
    public String imgUrl;
    public LatLng user;
    private static final int IMAGE_PICK_REQUEST = 888;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 999;
    private SharedPreferences sharedpreferences;
    public static final String PREFS = "MyPrefs" ;
    public SharedPreferences.Editor editor;
    double latMarkerss;
    double lngMarkerss;
    public LocationManager locationManager;
    public LocationListener locationListener;
    public Handler handler;
    public Runnable runnable;
    public FrameLayout frameLayout;
    int PLACE_PICKER_REQUEST = 3;
    private String fullAddress;
    private String addressName;
    private LatLng addressLatLng;
    private boolean write = false;
    public FrameLayout backgroundedtprof;
    public TextView editAdSoyad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_edit_profile);
        locationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Uid = currentUser.getUid();
        profileİmg = (ImageView) findViewById(R.id.EditPP);
        curentUserRef = FirebaseDatabase.getInstance().getReference("Users").child(Uid /* şuanki kullanıcının idsi */);
        imgStorageRef = FirebaseStorage.getInstance().getReference("Profile_images");
        mQueryUserLost = FirebaseDatabase.getInstance().getReference("Icerik").child("Kayiplar").orderByChild("uid").equalTo(Uid);
        mQueryUserFind = FirebaseDatabase.getInstance().getReference("Icerik").child("Bulunanlar").orderByChild("uid").equalTo(Uid);
        adSoyad = (EditText) findViewById(R.id.editTextAdSoyad);
        sehir = (TextView) findViewById(R.id.textSehir);
        kaydetButton = (ImageButton) findViewById(R.id.kaydetButon);
        iptalButon = (ImageButton) findViewById(R.id.iptalButon);
        themaNightBtn = (ImageButton) findViewById(R.id.themeNightBtn);
        themaDayBtn = (ImageButton) findViewById(R.id.themeDayBtn);
        mapView = (MapView) findViewById(R.id.mapView);
        kaydetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new UpdateDB().execute();

            }
        });
        iptalButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });

        mapView.onCreate(savedInstanceState);

        backgroundedtprof = (FrameLayout) findViewById(R.id.backgroundedtprof);
        editAdSoyad = (TextView) findViewById(R.id.editAdSoyad);

        mapView.onResume(); // needed to get the map to display immediately
        sharedpreferences = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        themeStr = sharedpreferences.getString("theme", "DayTheme");          //eğer null ise DayTheme

        frameLayout = (FrameLayout) findViewById(R.id.activity_bottom_bar);

        setProfileİmg();
        getLocation();

        if(themeStr.equals("DayTheme")){
            themaNightBtn.setVisibility(View.VISIBLE);
            themaDayBtn.setVisibility(View.GONE);
            backgroundedtprof.setBackgroundColor(Color.parseColor("#FFFFFF"));
            editAdSoyad.setTextColor(Color.BLACK);
            adSoyad.setHintTextColor(Color.BLACK);
            sehir.setTextColor(Color.BLACK);
        }else if(themeStr.equals("NightTheme")){
            themaNightBtn.setVisibility(View.GONE);
            themaDayBtn.setVisibility(View.VISIBLE);
            backgroundedtprof.setBackgroundColor(Color.parseColor("#142634"));
            editAdSoyad.setTextColor(Color.WHITE);
            adSoyad.setHintTextColor(Color.WHITE);
            sehir.setTextColor(Color.WHITE);
        }
        themaNightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                themaNightBtn.setVisibility(View.GONE);
                themaDayBtn.setVisibility(View.VISIBLE);

                NightThemeMode();

            }
        });
        themaDayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                themaNightBtn.setVisibility(View.VISIBLE);
                themaDayBtn.setVisibility(View.GONE);
                DayThemeMode();

            }
        });


        profileİmg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });
        curentUserRef.child("namesurname").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adSoyad.setHint(String.valueOf(dataSnapshot.getValue()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        curentUserRef.child("cityName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sehir.setText(String.valueOf(dataSnapshot.getValue()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        try {
            MapsInitializer.initialize(this.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                myGoogleMap = mMap;

                // For showing a move to my location button
                if (ActivityCompat.checkSelfPermission(EditProfileFragment.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(EditProfileFragment.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                myGoogleMap.setMyLocationEnabled(true);

                // For dropping a marker at a point on the Map
                if(user==null){         //latlng boş ise
                    getLocationFromShared();    //sharedprefence de kayıtlı olan latlng değerlerini çek
                    myGoogleMap.addMarker(new MarkerOptions().position(user).title("Bulundugunuz yer").snippet(""));
                    // For zooming automatically to the location of the marker
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(user).zoom(12).build();
                    myGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                }else {
                    myGoogleMap.addMarker(new MarkerOptions().position(user).title("Bulundugunuz yer").snippet(""));
                    // For zooming automatically to the location of the marker
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(user).zoom(12).build();
                    myGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }



            }
        });



        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                myGoogleMap = googleMap;
                myGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                myGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {

                        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                            methodGetLocation();
                            return true;
                        } else {
                            methodGetLocation();
                            return  true;
                        }

                    }
                });
            }
        });




    }

    private void methodGetLocation(){
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        Intent intentroadpic;

        try {
            intentroadpic = builder.build(EditProfileFragment.this);
            startActivityForResult(intentroadpic,PLACE_PICKER_REQUEST );
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }



    public void DayThemeMode(){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("theme", "DayTheme" );
        editor.commit();
        reloadApplication();
    }

    public void NightThemeMode(){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("theme", "NightTheme" );
        editor.commit();
        reloadApplication();
    }
    private void reloadApplication() {
        Intent mStartActivity = new Intent(EditProfileFragment.this, EditProfileFragment.class);            //For reload application
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(EditProfileFragment.this, mPendingIntentId, mStartActivity,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) EditProfileFragment.this.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    public void setProfileİmg(){


        curentUserRef.child("profileImage").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Glide.with(getApplicationContext())
                        .load(String.valueOf(dataSnapshot.getValue()))
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transform(new CircleTransform(EditProfileFragment.this))
                        .into(profileİmg);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    private class UpdateDB extends AsyncTask<String, String, String> {      // update işlemini asynctask ile gerçekleştiriyoruz.

        @Override
        protected String doInBackground(String... params) {
            writeNewDatasToDB();
            return null;

        }
    }

    public void writeNewDatasToDB(){
        final String newName = adSoyad.getText().toString();

        //gözlemlemek için yazdım
        Log.i("newDatas", "writeNewDatasToDB: " + newName +" - " + newName.isEmpty());

        if(!newName.isEmpty()){
            curentUserRef.child("namesurname").setValue(newName);
            mQueryUserLost.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot tasksSnapshot) {
                    for (DataSnapshot snapshot: tasksSnapshot.getChildren()) {
                        snapshot.getRef().child("name").setValue(newName);
                        Toast.makeText(EditProfileFragment.this, "Bilgileriniz Güncellendi!", Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }

            });
            mQueryUserFind.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot tasksSnapshot) {
                    for (DataSnapshot snapshot: tasksSnapshot.getChildren()) {
                        snapshot.getRef().child("name").setValue(newName);

                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }

            });

        }
        if(newImageUri != null){
            StorageReference filePath = imgStorageRef.child(newImageUri.getLastPathSegment());
            filePath.putFile(newImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final String downloadUri = taskSnapshot.getDownloadUrl().toString();
                    curentUserRef.child("profileImage").setValue(downloadUri);
                    //-----------------------------------------------------------------------------postlardaki profil resmini güncellemek için
                    mQueryUserLost.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot tasksSnapshot) {
                            for (DataSnapshot snapshot: tasksSnapshot.getChildren()) {
                                snapshot.getRef().child("image").setValue(downloadUri);
                                Toast.makeText(EditProfileFragment.this, "Bilgileriniz Güncellendi!", Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }

                    });
                    mQueryUserFind.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot tasksSnapshot) {
                            for (DataSnapshot snapshot: tasksSnapshot.getChildren()) {
                                snapshot.getRef().child("image").setValue(downloadUri);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }

                    });
                }
            });

        }
        try {
            if (!fullAddress.equals(null)) {
                curentUserRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (write == false) {
                            curentUserRef.child("fullAddress").setValue(fullAddress);
                            curentUserRef.child("cityName").setValue(addressName);
                            curentUserRef.child("latLng").setValue(addressLatLng);
                            Toast.makeText(EditProfileFragment.this, "Bilgileriniz Güncellendi!", Toast.LENGTH_LONG).show();
                            write = true;
                        }

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });

            } else {

            }
        }catch (Exception e){

        }
    }


    public void getLocation(){
        curentUserRef.child("latLng").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                latMarkers = (double) snapshot.child("latitude").getValue();
                lngMarkers = (double) snapshot.child("longitude").getValue();
                user = new LatLng(latMarkers, lngMarkers);
                saveLocationToShared();                     //çekilen lat lng değerlerinin kaydı için
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    // veritabanından çekilen locationın lat lng değerlerinin sharedprefences ile kayıtlı tutlması
    private void saveLocationToShared() {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putFloat("position_lat", (float) user.latitude );
        editor.putFloat("position_lon", (float) user.longitude);
        editor.commit();
    }

    // son kayıtlı olan location bilgisini sharedprefences yardımıyla çekme
    private void getLocationFromShared() {
        sharedpreferences = getSharedPreferences(PREFS,0);
        latMarkerss = sharedpreferences.getFloat("position_lat", 39f);          //eğer null ise lat degeri 39f
        lngMarkerss = sharedpreferences.getFloat("position_lon", 30f);          //eğer null ise lng değeri 30f
        user = new LatLng(latMarkerss, lngMarkerss);
    }

    public void pickImage(){
        Intent imagepick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(imagepick, IMAGE_PICK_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data == null ){
            Log.i("TESTGALLERY", "NULL ABİSİ"); /* ****** */
        }
        if(requestCode == IMAGE_PICK_REQUEST && resultCode == RESULT_OK && data != null){
            Log.i("TESTGALLERY", data.getData().toString());
            newImageUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), newImageUri);
                profileİmg.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(requestCode==PLACE_PICKER_REQUEST){
            if(resultCode == RESULT_OK){
                Place place = PlacePicker.getPlace(data,EditProfileFragment.this);
                fullAddress = (String) place.getAddress();
                addressLatLng = (LatLng) place.getLatLng();

                if(fullAddress.contains("/")) {
                    String[] city = fullAddress.split("/");

                    String part1 = city[0];
                    String part2 = city[1];
                    String[] cityname = part2.split(",");
                    addressName = cityname[0];

                }else{
                    String[] cityname = fullAddress.split(",");
                    addressName = cityname[0];
                }

                write = false;

            }
        }
    }

    public void setArguments(){

    }
}