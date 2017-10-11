package com.team3s.lostpropertyse.Post;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.team3s.lostpropertyse.MainPage.BottomBarActivity;
import com.team3s.lostpropertyse.Maps.PropMaps;
import com.team3s.lostpropertyse.R;


public class EditActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{
    private String post_key = null;

    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseLike;
    private ImageView mPostImage;
    private TextView mPostTitle;
    private TextView mPostDesc;
    private TextView mPostCity;
    private TextView mPostCommentCounter;
    private Button mPostDelete;
    private Button mPostUpdate;
    private ImageButton comments;

    private FirebaseAuth auth;

    private boolean mProcessLike = false;


    private Button showMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Icerik");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");

        auth = FirebaseAuth.getInstance();

        post_key = getIntent().getExtras().getString("post_id");

        mPostImage = (ImageView) findViewById(R.id.post_image_btn);
        mPostTitle = (TextView) findViewById(R.id.post_titleET);
        mPostDesc = (TextView) findViewById(R.id.post_descET);
        mPostCity = (TextView) findViewById(R.id.post_cityName);
        mPostCommentCounter = (TextView) findViewById(R.id.commentArticleCounter);
        mPostDelete = (Button) findViewById(R.id.deleteBtnSc);
        mPostUpdate = (Button) findViewById(R.id.editBtnSc);
        comments = (ImageButton) findViewById(R.id.btnArticleComments);
        showMap = (Button) findViewById(R.id.showMap);

        comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              /*Intent editActivity = new Intent(EditActivity.this, NewsComment.class);
                editActivity.putExtra("post_id", post_key);
                startActivity(editActivity);*/
            }
        });
        showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editActivity = new Intent(EditActivity.this, PropMaps.class);
                editActivity.putExtra("post_id", post_key);
                startActivity(editActivity);
            }
        });

        mPostDelete.setVisibility(View.GONE);
        mPostUpdate.setVisibility(View.GONE);
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
                String post_title = (String) dataSnapshot.child("questions").getValue();
                String post_desc = (String) dataSnapshot.child("desc").getValue();
                String post_img = (String) dataSnapshot.child("post_image").getValue();
                String post_id = (String) dataSnapshot.child("uid").getValue();
                String post_city = (String) dataSnapshot.child("city").getValue();


                mPostTitle.setText(post_title);
                mPostDesc.setText(post_desc);
                mPostCity.setText(post_city);
                Glide.with(getApplicationContext())
                        .load(post_img)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .animate(R.anim.shake)
                        .into(mPostImage);
                if(auth.getCurrentUser().getUid().equals(post_id)){
                    mPostDelete.setVisibility(View.VISIBLE);
                    mPostUpdate.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mPostDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child(post_key).removeValue();
                mDatabaseLike.child(post_key).child(auth.getCurrentUser().getUid()).removeValue();
                mProcessLike = false;

                Intent mainIntent = new Intent(EditActivity.this, BottomBarActivity.class);
                startActivity(mainIntent);
            }
        });

    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.poupup_menu, popup.getMenu());
        popup.show();

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                        case R.id.sikayetBtn:

                        break;

                    default:
                        break;
                }

                return true;
            }
        });
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sikayetBtn:
                return true;
            default:
                return false;
        }
    }
}
