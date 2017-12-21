package com.team3s.lostpropertyse.Post;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.andreilisun.swipedismissdialog.SwipeDismissDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.team3s.lostpropertyse.Chat.CommentAct;
import com.team3s.lostpropertyse.Chat.CommentFrag;
import com.team3s.lostpropertyse.MainPage.BottomBarActivity;
import com.team3s.lostpropertyse.MainPage.MainPage;
import com.team3s.lostpropertyse.Maps.PropMaps;
import com.team3s.lostpropertyse.R;

public class PostDetailAct extends AppCompatActivity {

    private String post_key = null;
    private String post_type = null;
    private String post_img;
    private String post_desc;
    private String themeStr;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseLike;
    private ImageView mPostImage;
    private TextView mPostDesc;
    private TextView mPostCity;
    private TextView mPostCommentCounter;
    private ImageButton comments,popupMenuBTN;
    private Intent intent;
    private RelativeLayout relativeLayout;

    private FirebaseAuth auth;

    private Button showMap;

    private final static String TAG_FRAGMENT = "TAG_FRAGMENT";

    private SharedPreferences sharedpreferences;
    public static final String PREFS = "MyPrefs" ;

    public PostDetailAct() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_post_detail);

        auth = FirebaseAuth.getInstance();

        Intent bundlecom = getIntent();                          //mainFragment ten post un keyini çekiyoruz.
        post_key = bundlecom.getStringExtra("post_key");
        post_type = bundlecom.getStringExtra("post_type");



        mDatabase = FirebaseDatabase.getInstance().getReference().child("Icerik").child(post_type);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        relativeLayout = (RelativeLayout) findViewById(R.id.relativePostDet);


        sharedpreferences = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        themeStr = sharedpreferences.getString("theme", "DayTheme");          //eğer null ise DayTheme


        mPostImage = (ImageView) findViewById(R.id.post_image_btn);
        mPostDesc = (TextView) findViewById(R.id.post_descET);
        mPostCity = (TextView) findViewById(R.id.post_cityName);
        mPostCommentCounter = (TextView) findViewById(R.id.commentArticleCounter);
        comments = (ImageButton) findViewById(R.id.btnArticleComments);
        popupMenuBTN = (ImageButton) findViewById(R.id.popupMenuBTN);
        showMap = (Button) findViewById(R.id.showMap);

        comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostDetailAct.this, CommentAct.class);
                intent.putExtra("post_id_key",post_key);
                intent.putExtra("post_type", post_type);
                startActivity(intent);

            }
        });
        showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editActivity = new Intent(PostDetailAct.this, PropMaps.class);
                editActivity.putExtra("post_id", post_key);
                editActivity.putExtra("post_type", post_type);
                startActivity(editActivity);
            }
        });

        popupMenuBTN.setVisibility(View.GONE);
        mDatabase.child(post_key).child("Comments").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    mPostCommentCounter.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mDatabase.child(post_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                post_desc = (String) dataSnapshot.child("questions").getValue();
                post_img = (String) dataSnapshot.child("post_image").getValue();
                String post_id = (String) dataSnapshot.child("uid").getValue();
                String post_city = (String) dataSnapshot.child("addressname").getValue();


                mPostDesc.setText(post_desc);
                mPostCity.setText(post_city);

                Glide.with(getApplicationContext())
                        .load(post_img)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mPostImage);


                if(auth.getCurrentUser().getUid().equals(post_id)){
                    popupMenuBTN.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        popupMenuBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(PostDetailAct.this, popupMenuBTN);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.poupup_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.deleteBtn:
                                if(post_type == "Kayiplar") {
                                    mDatabase.child(post_key).removeValue();
                                    mDatabaseLike.child(post_key).child(auth.getCurrentUser().getUid()).removeValue();
                                    Intent intent = new Intent(PostDetailAct.this, BottomBarActivity.class);
                                    startActivity(intent);
                                }else if(post_type == "Bulunanlar"){
                                    mDatabase.child(post_key).removeValue();
                                    mDatabaseLike.child(post_key).child(auth.getCurrentUser().getUid()).removeValue();
                                    Intent intent = new Intent(PostDetailAct.this, BottomBarActivity.class);
                                    startActivity(intent);
                                }
                                return true;

                        }
                        return true;
                    }
                });

                popup.show();//showing popup menu
            }
        });

        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                                           /////----------------------Resme tıklayınca resmi büyütüyor ve o resmi kaydırarak kapatabliyorsun
                View dialog = LayoutInflater.from(PostDetailAct.this).inflate(R.layout.custom_image_dialog, null);
                final SwipeDismissDialog swipeDismissDialog = new SwipeDismissDialog.Builder(PostDetailAct.this)
                        .setView(dialog)
                        .build()
                        .show();
                ImageView image = (ImageView) dialog.findViewById(R.id.image);
                Glide.with(getApplicationContext())
                        .load(post_img)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(image);
            }
        });

        if(themeStr.equals("NightTheme")){
            relativeLayout.setBackgroundColor(Color.parseColor("#142634"));
            mPostDesc.setTextColor(Color.WHITE);
            mPostCity.setTextColor(Color.WHITE);
            mPostCommentCounter.setTextColor(Color.WHITE);
        }else if(themeStr.equals("DayTheme")){
            relativeLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
            mPostDesc.setTextColor(Color.BLACK);
            mPostCity.setTextColor(Color.BLACK);
            mPostCommentCounter.setTextColor(Color.BLACK);
        }
    }
     @Override
    public void onBackPressed() {
        Intent intent = new Intent(PostDetailAct.this, BottomBarActivity.class);
        startActivity(intent);
    }
}