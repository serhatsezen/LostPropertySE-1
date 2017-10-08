package com.team3s.lostpropertyse.MainPage;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ShareFragment extends Fragment {
    private ImageView selectImage;
    private EditText questionField, descField,kategoriname;

    private String userNameU;
    private String tokenUsers;
    private String bildirimPost;
    private String fullAddress;
    private String addressName;
    private Button submitBtn;
    private ProgressBar progressBar,progressBarKonum;
    public String question_val;
    private Uri imageUri = null;

    private FirebaseAuth auth;

    private FirebaseUser currentUser;

    private StorageReference storage;
    private DatabaseReference database;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseIcerik;
    private DatabaseReference mDatabasePost;
    private DatabaseReference mDatabaseNotificationFilter;
    private DatabaseReference mDatabaseNotificationFTokenValue;

    private Query mQueryUsers;
    ValueEventListener valueEventListener;
    private static View view;


    private static final int GALLERY_REQUEST = 1;
    private static final int CAMERA_REQUEST_CODE = 2;

    private TextView get_place_road;
    int PLACE_PICKER_REQUEST = 3;
    private LatLng addressLatLng;

    private String latUsers;
    private String lngUsers;
    private int rangeDest = 5;
    double dist;
    String distStr;
    public ShareFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_share, container, false);
       // getActivity().stopService(new Intent(getActivity().getApplicationContext(), MyService.class));


        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        storage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());
        mDatabasePost = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid()).child("PostsId");
        mDatabaseIcerik = FirebaseDatabase.getInstance().getReference().child("Icerik");
        mDatabaseNotificationFilter = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseNotificationFTokenValue = FirebaseDatabase.getInstance().getReference().child("Users");



        selectImage = (ImageView) v.findViewById(R.id.post_image_btn);
        questionField = (EditText) v.findViewById(R.id.post_titleET);
        descField = (EditText) v.findViewById(R.id.post_descET);
        submitBtn = (Button) v.findViewById(R.id.editBtnSc);
        get_place_road = (TextView) v.findViewById(R.id.getlocationbtn);

        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        progressBarKonum = (ProgressBar) v.findViewById(R.id.progressBarKonum);


        get_place_road.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                get_place_road.setVisibility(View.GONE);
                progressBarKonum.setVisibility(View.VISIBLE);
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                Intent intentroadpic;

                try {
                    intentroadpic = builder.build(getActivity());
                    startActivityForResult(intentroadpic,PLACE_PICKER_REQUEST );
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, GALLERY_REQUEST);
            }
        });


        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitBtn.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                     startPosting();
            }

        });

            return v;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK && null != data) {
            imageUri = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            android.database.Cursor cursor =getActivity().getContentResolver().query(imageUri,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            selectImage.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        }else if(requestCode==PLACE_PICKER_REQUEST){
            if(resultCode == RESULT_OK){
                Place place = PlacePicker.getPlace(data,getActivity());
                fullAddress = (String) place.getAddress();
                addressName = (String) place.getName();
                addressLatLng = (LatLng) place.getLatLng();
                get_place_road.setVisibility(View.VISIBLE);
                progressBarKonum.setVisibility(View.GONE);
                get_place_road.setText(addressName);

            }
        }
    }


    private void startPosting() {
        question_val = questionField.getText().toString().trim();
        final String desc_val = descField.getText().toString().trim();
            if (!TextUtils.isEmpty(question_val) && !TextUtils.isEmpty(desc_val) && imageUri != null) {
                StorageReference filepath = storage.child("Shares_Image").child(imageUri.getLastPathSegment());

                filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        final DatabaseReference newPost2 = mDatabaseIcerik.push();

                        final Time today = new Time(Time.getCurrentTimezone());
                        today.setToNow();

                        mDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                userNameU = (String) dataSnapshot.child("username").getValue();

                                newPost2.child("questions").setValue(question_val);
                                newPost2.child("desc").setValue(desc_val);
                                newPost2.child("fulladdress").setValue(fullAddress);
                                newPost2.child("addressname").setValue(addressName);
                                newPost2.child("latlng").setValue(addressLatLng);
                                newPost2.child("token").setValue(dataSnapshot.child("token").getValue());
                                newPost2.child("post_image").setValue(downloadUrl.toString());
                                newPost2.child("post_time").setValue(today.format("%k:%M"));
                                newPost2.child("post_date").setValue(today.format("%d/%m/%Y"));
                                newPost2.child("uid").setValue(currentUser.getUid());
                                newPost2.child("image").setValue(dataSnapshot.child("profileImage").getValue());
                                newPost2.child("name").setValue(userNameU).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                           // notificationFilter();
                                            startActivity(new Intent(getActivity(), BottomBarActivity.class));
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
    }
   /* public void notificationFilter(){
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance()
                .getReference();
        DatabaseReference node = mDatabaseReference.child("Users");

        node.orderByChild("latLng").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    latUsers = data.child("latLng").child("lat").getValue().toString();
                    lngUsers = data.child("latLng").child("lng").getValue().toString();

                    tokenUsers = "";

                    double latUserL = Double.parseDouble(String.valueOf(latUsers));
                    double lngUserL = Double.parseDouble(String.valueOf(lngUsers));

                    Location destination = new Location("destination");
                    destination.setLatitude(latUserL);
                    destination.setLongitude(lngUserL);

                    Location current = new Location("destination");
                    current.setLatitude(addressLatLng.latitude);
                    current.setLongitude(addressLatLng.longitude);

                    dist = current.distanceTo(destination) / 1000;
                    distStr = String.format("%.2f", dist );

                    bildirimPost = question_val + " "+ addressName + " sana "+ distStr + " km uzaklıkta";

                    if(dist <= rangeDest) {
                        tokenUsers = String.valueOf(data.child("token").getValue());
                        new Send().execute();
                    }


                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

    }

    class Send extends AsyncTask<String, Void,Long > {

        protected Long doInBackground(String... urls) {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://eam3s.atspace.cc/php/newPost.php");

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("tokendevice", tokenUsers));
                nameValuePairs.add(new BasicNameValuePair("bildirimPost", bildirimPost));
                nameValuePairs.add(new BasicNameValuePair("userName", userNameU));


                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);

            } catch (Exception e) {
                // TODO Auto-generated catch block
            }
            return null;
        }
        protected void onProgressUpdate(Integer... progress) {
        }
        protected void onPostExecute(Long result) {
        }
    }*/

}

