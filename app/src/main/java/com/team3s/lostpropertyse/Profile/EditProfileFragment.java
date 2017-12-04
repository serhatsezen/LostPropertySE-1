package com.team3s.lostpropertyse.Profile;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.team3s.lostpropertyse.R;

public class EditProfileFragment extends Fragment {

    private EditText AdSoyad;
    private EditText Sehir;
    private String Uid;
    private ImageView profileİmg;
    private MapView mapView;
    private GoogleMap myGoogleMap;


    private FirebaseUser currentUser;
    private DatabaseReference curentUserRef;


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

        AdSoyad = (EditText) v.findViewById(R.id.editTextAdSoyad);
        Sehir = (EditText) v.findViewById(R.id.editTextSehir);
        mapView = (MapView) v.findViewById(R.id.mapView);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                    myGoogleMap = googleMap;
                    myGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    

            }
        });
        return  v;
    }

    public void setArguments(){
        Log.i("SetArguments","Set");
    }






}
