package com.team3s.lostpropertyse.Profile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.team3s.lostpropertyse.R;
import com.team3s.lostpropertyse.Utils.CircleTransform;

import java.io.IOException;
import java.sql.SQLOutput;

import static android.app.Activity.RESULT_OK;

public class EditProfileFragment extends Fragment {

    private EditText AdSoyad;
    private EditText Sehir;
    private String Uid;
    private ImageView profileİmg;
    private MapView mapView;
    private GoogleMap myGoogleMap;
    Button kaydetButton;
    Uri newImageUri;
    private StorageReference imgStorageRef;
    private FirebaseUser currentUser;
    private DatabaseReference curentUserRef;
    double latMarkers;
    double lngMarkers;
    String imgUrl;
    public LatLng user;
    private static final int IMAGE_PICK_REQUEST = 888;

    public EditProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_edit_profile, container, false);


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Uid = currentUser.getUid();
        profileİmg = (ImageView) v.findViewById(R.id.EditPP);
        curentUserRef = FirebaseDatabase.getInstance().getReference("Users").child(Uid /* şuanki kullanıcının idsi */);
        imgStorageRef = FirebaseStorage.getInstance().getReference("Profile_images");

        AdSoyad = (EditText) v.findViewById(R.id.editTextAdSoyad);
        Sehir = (EditText) v.findViewById(R.id.editTextSehir);
        kaydetButton = (Button) v.findViewById(R.id.kaydetButon);
        mapView = (MapView) v.findViewById(R.id.mapView);
        kaydetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeNewDatasToDB();
            }
        });
        mapView.onCreate(savedInstanceState);

        mapView.onResume(); // needed to get the map to display immediately

        setProfileİmg();
        getLocation();

        profileİmg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });
        curentUserRef.child("namesurname").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                AdSoyad.setHint(String.valueOf(dataSnapshot.getValue()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        curentUserRef.child("cityName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Sehir.setHint(String.valueOf(dataSnapshot.getValue()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                myGoogleMap = mMap;

                // For showing a move to my location button
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
                if(user==null){
                    getLocation();
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
                    

            }
        });
        return  v;
    }

    public void setProfileİmg(){


        curentUserRef.child("profileImage").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Glide.with(getActivity().getApplicationContext())
                        .load(String.valueOf(dataSnapshot.getValue()))
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transform(new CircleTransform(getActivity()))
                        .animate(R.anim.shake)
                        .into(profileİmg);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void writeNewDatasToDB(){
        String newName = AdSoyad.getText().toString();
        String newCity = Sehir.getText().toString();


        //gözlemlemek için yazdım
        Log.i("newDatas", "writeNewDatasToDB: " + newName +" - " + newName.isEmpty());
        Log.i("newDatas", "writeNewDatasToDB: " + newCity +" - " + newCity.isEmpty());

        if(!newName.isEmpty()){
            curentUserRef.child("namesurname").setValue(newName);
        }
        if(!newCity.isEmpty()){
            curentUserRef.child("cityName").setValue(newCity);
        }


    }

    public void getLocation(){
        curentUserRef.child("latLng").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                latMarkers = (double) snapshot.child("latitude").getValue();
                lngMarkers = (double) snapshot.child("longitude").getValue();
                user = new LatLng(latMarkers, lngMarkers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void pickImage(){
        Intent imagepick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(imagepick, IMAGE_PICK_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data == null ){
            Toast.makeText(getContext(),"Resim Seçmediniz",Toast.LENGTH_LONG).show();
            Log.i("TESTGALLERY", "NULL ABİSİ"); /* ****** */
        }
        if(requestCode == IMAGE_PICK_REQUEST && resultCode == RESULT_OK && data != null){
            Log.i("TESTGALLERY", data.getData().toString());
            newImageUri = data.getData();
            StorageReference filePath = imgStorageRef.child(newImageUri.getLastPathSegment());
            if (newImageUri != null) {
                filePath.putFile(newImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String downloadUri = taskSnapshot.getDownloadUrl().toString();
                        curentUserRef.child("profileImage").setValue(downloadUri);
                    }
                });
            }
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), newImageUri);
                profileİmg.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setArguments(){

    }





}
