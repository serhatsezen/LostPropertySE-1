package com.team3s.lostpropertyse.Profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.team3s.lostpropertyse.CircleTransform;
import com.team3s.lostpropertyse.LoginSign.TabsHeaderActivity;
import com.team3s.lostpropertyse.Post.EditActivity;
import com.team3s.lostpropertyse.R;
import com.team3s.lostpropertyse.Share;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

public class AnotherUsersProfiFrag extends Fragment {

    private TextView u_fullname,u_username,u_city;
    private ImageView profileImg,backgroundImg;
    private View backgroundView;
    public TextView num_post;

    private ImageButton editprof;

    private RecyclerView profileList;

    private DatabaseReference database,mDatabaseUsers,mDatabaseUsersPostNum;
    private DatabaseReference mDatabaseCurrentUsers;
    private Query mQueryUser;
    private DatabaseReference mDatabaseLike;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    private FirebaseUser user;
    private String currentUserId;
    private String post_key_user = null;

    public AnotherUsersProfiFrag() {
        // Required empty public constructor
    }



  @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.user_profil_frag, container, false);

      Bundle bundle = getArguments();
      post_key_user = bundle.getString("key");

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        //get current user
       user = FirebaseAuth.getInstance().getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    Intent loginIntent = new Intent(getActivity(),TabsHeaderActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);

                }
            }
        };

        database = FirebaseDatabase.getInstance().getReference().child("Icerik");
        database.keepSynced(true);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(post_key_user);
        mDatabaseUsers.keepSynced(true);
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseUsersPostNum = FirebaseDatabase.getInstance().getReference().child("Users").child(post_key_user).child("PostsId");



        u_fullname = (TextView) v.findViewById(R.id.fullnameuser);
        u_username = (TextView) v.findViewById(R.id.usernameprof);
        u_city = (TextView) v.findViewById(R.id.city);
        num_post = (TextView) v.findViewById(R.id.find_counter);
        backgroundView = v.findViewById(R.id.background);

        currentUserId = auth.getCurrentUser().getUid();
        mDatabaseCurrentUsers = FirebaseDatabase.getInstance().getReference().child("Icerik");
        mQueryUser = mDatabaseCurrentUsers.orderByChild("uid").equalTo(post_key_user);

        profileImg = (ImageView) v.findViewById(R.id.ivUserProfilePhoto);
        backgroundImg = (ImageView) v.findViewById(R.id.imageView3);

        profileList = (RecyclerView) v.findViewById(R.id.rvUserProfile);

        editprof = (ImageButton) v.findViewById(R.id.edit_prof_btn);
        editprof.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View view) {
            showDialog();
          }
        });


        mQueryUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    num_post.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mDatabaseUsers.child("profileImage").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Glide.with(getActivity().getApplicationContext())
                        .load(String.valueOf(snapshot.getValue()))
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transform(new CircleTransform(getActivity()))
                        .animate(R.anim.shake)
                        .into(profileImg);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
      mDatabaseUsers.child("backgroundImage").addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot snapshot) {
              Glide.with(getActivity().getApplicationContext())
                      .load(String.valueOf(snapshot.getValue()))
                      .centerCrop()
                      .diskCacheStrategy(DiskCacheStrategy.ALL)
                      .into(backgroundImg);
          }
          @Override
          public void onCancelled(DatabaseError databaseError) {
          }
      });
        mDatabaseUsers.child("namesurname").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                u_fullname.setText(String.valueOf(snapshot.getValue()));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        mDatabaseUsers.child("username").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                u_username.setText("@"+String.valueOf(snapshot.getValue())+",");
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        mDatabaseUsers.child("fullAddress").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String fullad = String.valueOf(snapshot.getValue());        //tam adres içinden şehir ve ülke yi yazıyorum.
                if(fullad.contains("/")) {
                    String textStr[] = fullad.split("/");
                    u_city.setText(textStr[1]);
                }else{
                    u_city.setText(fullad);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


      LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        profileList.setHasFixedSize(true);
        profileList.setLayoutManager(layoutManager);
        setHasOptionsMenu(true);

        return v;
    }


    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Bir işlem seç")
                .setItems(R.array.anotherUserEditButtonOptions, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        System.out.println("*************"+which);
                        switch (which){
                            case 0:// şikayet et butonu

                                break;

                        }

                    }
                });
        builder.create();
        builder.show();
    }


    public void onStart(){
        super.onStart();
        FirebaseRecyclerAdapter<Share, ShareViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Share, ShareViewHolder>(
                Share.class,
                R.layout.profile_row,
                ShareViewHolder.class,
                mQueryUser
        ) {
            @Override
            protected void populateViewHolder(final ShareViewHolder viewHolder, Share model, final int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setQuestions(model.getQuestions());
                viewHolder.setPost_image(getActivity().getApplicationContext(),model.getPost_image());
                viewHolder.setPost_date(model.getPost_date());
                viewHolder.setPost_time(model.getPost_time());

                viewHolder.mView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent editActivity = new Intent(getActivity(), EditActivity.class);
                        editActivity.putExtra("post_id", post_key);
                        startActivity(editActivity);
                    }
                });

            }
        };

        profileList.setAdapter(firebaseRecyclerAdapter);

    }


    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
    }

    public static class ShareViewHolder extends RecyclerView.ViewHolder {

        View mView;

        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;

        public ShareViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
            mAuth = FirebaseAuth.getInstance();
        }



        public void setQuestions(String questions){

            TextView questions_title = (TextView) mView.findViewById(R.id.titleProfileText);
            questions_title.setText(questions);
        }
        public void setPost_date(String post_date){

            TextView date = (TextView) mView.findViewById(R.id.dateTxt);
            date.setText(post_date);
        }
        public void setPost_time(String post_time){

            TextView time = (TextView) mView.findViewById(R.id.timeTxt);
            time.setText(post_time);
        }

        public void setPost_image(Context ctx, String post_image){
            ImageView post_img = (ImageView) mView.findViewById(R.id.post_img);
            Glide.with(ctx)
                    .load(post_image)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .animate(R.anim.shake)
                    .into(post_img);
        }

    }

}
