package com.team3s.lostpropertyse.ShareSc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.team3s.lostpropertyse.MainPage.BottomBarActivity;
import com.team3s.lostpropertyse.R;
import com.team3s.lostpropertyse.Utils.UniversalImageLoader;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class NextActivity extends AppCompatActivity {

    private static final String TAG = "NextActivity";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    //widgets
    private EditText mCaption;

    //vars
    private String mAppend = "file:/";
    private int imageCount = 0;
    private String imgUrl;
    private Bitmap bitmap;
    private Intent intent;
    private TextView location,lostOrFind,share;
    int PLACE_PICKER_REQUEST = 3;
    private String fullAddress;
    private String addressName;
    private LatLng addressLatLng;
    private static View view;

    //--------------------------firebase--------------------------
    private FirebaseAuth auth;

    private FirebaseUser currentUser;
    private StorageReference storage;
    private DatabaseReference database;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseIcerik;
    private DatabaseReference mDatabasePost;
    private DatabaseReference mDatabaseNotificationFilter;
    private DatabaseReference mDatabaseNotificationFTokenValue;


    public int rangeDest;
    public double dist;
    public String distStr;
    public String rangeDestStr;

    public int z = 0;
    public int m = 0;

    public String description;
    public String userNameU;
    public String latUsers;
    public String lngUsers;
    public String tokenUsers;
    public String bildirimPost;
    public String kategori;
    public String bildirimBaslik;
    public String post_key;
    public Uri imageUri = null;

    public EditText edtxCategory;
    public TextView kmdegeri;

    ArrayList<String> tokenList = new ArrayList<String>();
    ArrayList<String> kmList = new ArrayList<String>();

    private RecyclerView horizontal_recycler_view;
    private ArrayList<String> horizontalListkm;
    private HorizontalAdapterKm horizontalAdapterkmKm;

    private RecyclerView horizontal_recycler_view_kategoriler;
    private ArrayList<String> horizontalListkategori;
    private HorizontalAdapterr horizontalAdapterkategori;
    private String category;
    private boolean mValid = true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        //------------------FIREBASE-----------------------------------------

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        storage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());
        mDatabasePost = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid()).child("PostsId");
        mDatabaseIcerik = FirebaseDatabase.getInstance().getReference().child("Icerik");
        mDatabaseNotificationFilter = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseNotificationFTokenValue = FirebaseDatabase.getInstance().getReference().child("Users");

        mDatabaseNotificationFilter.addValueEventListener(new ValueEventListener() {            //database de kaç tane user olduğu
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    z = (int) dataSnapshot.getChildrenCount();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        //------------------FIREBASE END-------------------------------------

        horizontal_recycler_view= (RecyclerView) findViewById(R.id.horizontal_recycler_view);

        horizontalListkm =new ArrayList<>();
        horizontalListkm.add("1");
        horizontalListkm.add("5");
        horizontalListkm.add("10");
        horizontalListkm.add("15");
        horizontalListkm.add("20");

        horizontalAdapterkmKm =new HorizontalAdapterKm(horizontalListkm);

        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(NextActivity.this, LinearLayoutManager.HORIZONTAL, false);
        horizontal_recycler_view.setLayoutManager(horizontalLayoutManagaer);

        horizontal_recycler_view.setAdapter(horizontalAdapterkmKm);

        //-------------------------------------for select km

        horizontal_recycler_view_kategoriler= (RecyclerView) findViewById(R.id.horizontal_recycler_view_kategoriler);

        horizontalListkategori =new ArrayList<>();
        horizontalListkategori.add("Kart");
        horizontalListkategori.add("Bisiklet");
        horizontalListkategori.add("Elektronik");
        horizontalListkategori.add("Cüzdan");
        horizontalListkategori.add("Kırtasiye");
        horizontalListkategori.add("Çanta");
        horizontalListkategori.add("Anahtar");


        horizontalAdapterkategori =new HorizontalAdapterr(horizontalListkategori);

        LinearLayoutManager horizontalLayoutManagaerKategori = new LinearLayoutManager(NextActivity.this, LinearLayoutManager.HORIZONTAL, false);

        horizontal_recycler_view_kategoriler.setLayoutManager(horizontalLayoutManagaerKategori);

        horizontal_recycler_view_kategoriler.setAdapter(horizontalAdapterkategori);

        //------------------------------------ for select category


        edtxCategory = (EditText) findViewById(R.id.edtxCategory);
        kmdegeri = (TextView) findViewById(R.id.kmdegeri);

        mCaption = (EditText) findViewById(R.id.caption) ;
        ImageView backArrow = (ImageView) findViewById(R.id.ivBackArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the activity");
                finish();
            }
        });

        lostOrFind = (TextView) findViewById(R.id.lostOrfind);
        lostOrFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lostOrFindMethod();
            }
        });


        location = (TextView) findViewById(R.id.location);
        location.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                location.setVisibility(View.GONE);
                methodGetLocation();

            }
        });


        methodGetLocation();


        setImage();


        share = (TextView) findViewById(R.id.tvShare);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                description = mCaption.getText().toString().trim();

                if(intent.hasExtra(getString(R.string.selected_image))){
                    imgUrl = intent.getStringExtra(getString(R.string.selected_image));
                    imageUri =Uri.fromFile(new File(imgUrl));

                }
                else if(intent.hasExtra(getString(R.string.selected_bitmap))){
                    imageUri = intent.getParcelableExtra(getString(R.string.selected_bitmap));
                }
                if(!TextUtils.isEmpty(description)) {
                    new ShareAsync().execute();
                    Toast.makeText(NextActivity.this, "Paylaşılıyor!", Toast.LENGTH_LONG).show();
                    share.setText("Bekleyiniz!");
                    Intent bottombar = new Intent(NextActivity.this,BottomBarActivity.class);
                    startActivity(bottombar);

                }

            }
        });


    }
    public class HorizontalAdapterKm extends RecyclerView.Adapter<HorizontalAdapterKm.MyViewHolder> {

        private List<String> horizontalListKm;
        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView txtView;
            private boolean mValid = true;

            public MyViewHolder(View view) {
                super(view);
                txtView = (TextView) view.findViewById(R.id.txtView);

            }
        }


        public HorizontalAdapterKm(List<String> horizontalList) {
            this.horizontalListKm = horizontalList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.horizontal_item_view, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            holder.txtView.setText(horizontalListKm.get(position));

            LinearLayout.LayoutParams lpp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lpp.setMargins(10, 0, 10, 0);
            lpp.gravity = Gravity.LEFT;
            holder.txtView.setLayoutParams(lpp);
            holder.txtView.setBackgroundResource(R.drawable.chatgreen);
            holder.txtView.setTextColor(getResources().getColor(R.color.white));


            holder.txtView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.txtView.setTextSize(19);
                    rangeDestStr = holder.txtView.getText().toString();
                    kmdegeri.setText(rangeDestStr);
                }
            });
        }

        @Override
        public int getItemCount() {
            return horizontalListKm.size();
        }
    }

    public class HorizontalAdapterr extends RecyclerView.Adapter<HorizontalAdapterr.MyViewHolder> {

        private List<String> horizontalListKategori;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView txtView;

            public MyViewHolder(View view) {
                super(view);
                txtView = (TextView) view.findViewById(R.id.txtView);

            }
        }


        public HorizontalAdapterr(List<String> horizontalList) {
            this.horizontalListKategori = horizontalList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.horizontal_item_view, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            holder.txtView.setText(horizontalListKategori.get(position));

            LinearLayout.LayoutParams lpp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lpp.setMargins(10, 0, 10, 0);
            lpp.gravity = Gravity.LEFT;
            holder.txtView.setLayoutParams(lpp);
            holder.txtView.setBackgroundResource(R.drawable.categorycolor);
            holder.txtView.setTextColor(getResources().getColor(R.color.white));

            holder.txtView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    edtxCategory.setText(holder.txtView.getText().toString());
                    category = holder.txtView.getText().toString();

                }
            });
        }

        @Override
        public int getItemCount() {
            return horizontalListKategori.size();
        }
    }
    public void lostOrFindMethod() {
        AlertDialog.Builder builder = new AlertDialog.Builder(NextActivity.this);
        builder.setTitle("Kategori seç")
                .setItems(R.array.lostorfind, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:
                                lostOrFind.setText("Kayıp");
                                kategori = "Kayiplar";
                                break;
                            case 1:
                                lostOrFind.setText("Buldum");
                                kategori = "Bulunanlar";
                                break;
                            default: break;
                        }

                    }
                });
        builder.create();
        builder.show();
    }

    private void methodGetLocation(){
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        Intent intentroadpic;

        try {
            intentroadpic = builder.build(NextActivity.this);
            startActivityForResult(intentroadpic,PLACE_PICKER_REQUEST );
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       if(requestCode==PLACE_PICKER_REQUEST){
            if(resultCode == RESULT_OK){
                Place place = PlacePicker.getPlace(data,NextActivity.this);
                fullAddress = (String) place.getAddress();
                addressLatLng = (LatLng) place.getLatLng();
                location.setVisibility(View.VISIBLE);

                if(fullAddress.contains("/")) {
                    String[] city = fullAddress.split("/");

                    String part1 = city[0];
                    String part2 = city[1];
                    String[] cityname = part2.split(",");
                    String addresscity = cityname[0];

                    String[] addressplit = fullAddress.split(",");
                    String address = addressplit[0];

                    addressName = address + "," + addresscity;

                    location.setText(addressName);


                }else{
                    String[] cityname = fullAddress.split(",");
                    addressName = cityname[0];

                    location.setText(addressName);

                }

            }else {
                location.setVisibility(View.VISIBLE);
            }
       }
    }

    private void setImage(){
        intent = getIntent();
        ImageView image = (ImageView) findViewById(R.id.imageShare);

        if(intent.hasExtra(getString(R.string.selected_image))){
            imgUrl = intent.getStringExtra(getString(R.string.selected_image));
            imageUri = Uri.fromFile(new File(imgUrl));
            UniversalImageLoader.setImage(imgUrl, image, null, mAppend);
        }
        else if(intent.hasExtra(getString(R.string.selected_bitmap))){
            imageUri = intent.getParcelableExtra(getString(R.string.selected_bitmap));
            Glide.with(NextActivity.this.getApplicationContext())
                    .load(imageUri)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(image);
        }
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareact = new Intent(NextActivity.this,ShareActivity.class);
                startActivity(shareact);
            }
        });
    }


    private void startPosting() {
        description = mCaption.getText().toString().trim();
        category = edtxCategory.getText().toString().trim();
        StorageReference filepath = storage.child("Shares_Image").child(imageUri.getLastPathSegment());

        filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                final DatabaseReference newPost2 = mDatabaseIcerik.child(kategori).push();
                post_key = newPost2.getKey();
                final Time today = new Time(Time.getCurrentTimezone());
                today.setToNow();

                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        userNameU = (String) dataSnapshot.child("username").getValue();
                        newPost2.child("questions").setValue(description);
                        newPost2.child("fulladdress").setValue(fullAddress);
                        newPost2.child("addressname").setValue(addressName);
                        newPost2.child("category").setValue(category);
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
                                    notificationFilter();
                                    Toast.makeText(NextActivity.this, "Paylaşıldı!", Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(NextActivity.this, "Hata Oluştu!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        });

    }

    //notification için kullanıcının bulunduğu konum ve post konumunun karşılarştırılması
    public void notificationFilter(){
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance()
                .getReference();
        DatabaseReference node = mDatabaseReference.child("Users");
        node.orderByChild("latLng").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    latUsers = data.child("latLng").child("latitude").getValue().toString();
                    lngUsers = data.child("latLng").child("longitude").getValue().toString();

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
                    rangeDest = Integer.parseInt(rangeDestStr);
                    m++;                                                                                        // user kontrol değeri
                    if(dist <= rangeDest) {                                                                     // eğer Kullanıcının belirttiği Xkm içindeyse notificaion gönder
                        tokenUsers = String.valueOf(data.child("token").getValue());
                        tokenList.add(tokenUsers);
                        kmList.add(distStr);

                        if(m == z){                                                                             // eğer user sayısıyla kontrol değeri aynıysa tüm kullanıcıların km değeri hesaplanmış demek, bildirim gönderme işlemini başlatabiliriz.
                            new Send().execute();
                        }
                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

    }

    private class ShareAsync extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            startPosting();
            return null;

        }
    }


    class Send extends AsyncTask<String, Void,Long > {

        protected Long doInBackground(String... urls) {

            int i = tokenList.size();                                                                           //tokenlist size i ye atıyorum

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://aydinserhatsezen.com/fcm/LostP/lpnewPost.php");        //web sitesi server üzerinden gönder

            try {
                for(int t = 0; t<=i; t++){                                                                      //tek tek bildirim gönderme işlemi
                    String myToken = tokenList.get(t);                                                          //sırayla listten çekip php ye göndermek için.
                    String myKm = kmList.get(t);

                    bildirimPost = kategori + " "+ addressName + " sana "+ myKm + " km uzaklıkta";

                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
                    nameValuePairs.add(new BasicNameValuePair("tokendevice", myToken));
                    nameValuePairs.add(new BasicNameValuePair("bildirimPost", bildirimPost));
                    nameValuePairs.add(new BasicNameValuePair("userName", userNameU));
                    nameValuePairs.add(new BasicNameValuePair("post_key", post_key));
                    nameValuePairs.add(new BasicNameValuePair("post_type", kategori));

                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
                    HttpResponse response = httpclient.execute(httppost);
                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
            }
            return null;
        }
        protected void onProgressUpdate(Integer... progress) {
        }
        protected void onPostExecute(Long result) {
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(NextActivity.this, ShareActivity.class);
        startActivity(intent);
    }
}
