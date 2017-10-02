package com.team3s.lostpropertyse.MainPage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.team3s.lostpropertyse.CircleTransform;
import com.team3s.lostpropertyse.LoginSign.TabsHeaderActivity;
import com.team3s.lostpropertyse.R;
import com.team3s.lostpropertyse.Share;


public class MainPage extends Fragment {

    private ImageButton addBtn,profileBtn,roadBtn;

    private RecyclerView shareList;

    private DatabaseReference database,mDatabaseUsers,mmDatabaseUsers,mDatabaseUsersProfile,mDatabaseLikeCounter,mDatabaseUsersFilter;
    private DatabaseReference mDatabaseLike;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private Query mQueryIcerik;
    private boolean mProcessLike = false;
    private static String tokenUser = null;
    private static String questionName = null;


    private static String question = null;
    private static String username = null;

    public static final String ARG_TITLE = "arg_title";
    public String user_key = null;
    public static String userNames = null;
    public String cityFilter = "Genel";
    ProgressBar imgPg;

    public MainPage() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main_screen, container, false);


        imgPg = (ProgressBar) v.findViewById(R.id.progressBar3);
        imgPg.setVisibility(View.VISIBLE);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        String title = getArguments().getString(ARG_TITLE, "");
        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        mDatabaseUsersFilter = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());

        mDatabaseUsersFilter.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userNames = (String) dataSnapshot.child("username").getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseUsersFilter.child("filterCity").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cityFilter = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    Intent loginIntent = new Intent(getActivity(),TabsHeaderActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);

                }
            }
        };

        mDatabaseLikeCounter = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseLike.keepSynced(true);

        shareList = (RecyclerView) v.findViewById(R.id.share_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        shareList.setHasFixedSize(true);
        shareList.setLayoutManager(layoutManager);
        checkUser();

        return v;
    }

    public void onStart(){
        super.onStart();
        String currentUserId = auth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance().getReference().child("Icerik");
        mQueryIcerik = database.orderByChild("city").equalTo(cityFilter);


        FirebaseRecyclerAdapter<Share, ShareViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Share, ShareViewHolder>(
                    Share.class,
                    R.layout.main_row,
                    ShareViewHolder.class,
                    database
            ) {
                @Override
                protected void populateViewHolder(final ShareViewHolder viewHolder, Share model, final int position) {

                    final String post_key = getRef(position).getKey();

                    viewHolder.setQuestions(model.getQuestions());
                    viewHolder.setDesc(model.getDesc());
                    viewHolder.setCity(model.getaddressname());
                    viewHolder.setPost_image(getActivity().getApplicationContext(), model.getPost_image());
                    viewHolder.setName(model.getName());
                    viewHolder.setImage(getActivity().getApplicationContext(), model.getImage());
                    imgPg.setVisibility(View.GONE);

                    viewHolder.setLiikeBtn(post_key);

                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                           /* Intent editActivity = new Intent(getActivity(), EditActivity.class);
                            editActivity.putExtra("post_id", post_key);
                            startActivity(editActivity);*/
                        }
                    });
                    viewHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                           /* Intent editActivity = new Intent(getActivity(), NewsComment.class);
                            editActivity.putExtra("post_id", post_key);
                            startActivity(editActivity);*/
                        }
                    });
                    viewHolder.profile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            mDatabaseUsersProfile = FirebaseDatabase.getInstance().getReference().child("Icerik");

                            mDatabaseUsersProfile.child(post_key).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    user_key = (String) dataSnapshot.child("uid").getValue();
                                   /* Intent profActivity = new Intent(getActivity(), AnotherProfileActivity.class);
                                    profActivity.putExtra("user_id", user_key);
                                    startActivity(profActivity);*/
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    // Failed to read value
                                }
                            });

                        }
                    });
                    database.child(post_key).child("Cevaplar").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                                viewHolder.commentCount.setText(String.valueOf(dataSnapshot.getChildrenCount()));  //displays the key for the node

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    mDatabaseLikeCounter.child(post_key).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                                viewHolder.counterLike.setText(String.valueOf(dataSnapshot.getChildrenCount()));  //displays the key for the node


                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    viewHolder.mLikebtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mProcessLike = true;
                            database.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    tokenUser = (String) dataSnapshot.child(post_key).child("token").getValue();
                                    questionName = (String) dataSnapshot.child(post_key).child("questions").getValue();

                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (mProcessLike) {
                                        if (dataSnapshot.child(post_key).hasChild(auth.getCurrentUser().getUid())) {
                                            mDatabaseLike.child(post_key).child(auth.getCurrentUser().getUid()).removeValue();
                                            mProcessLike = false;
                                        } else {
                                            mDatabaseLike.child(post_key).child(auth.getCurrentUser().getUid()).setValue(userNames);
                                            mProcessLike = false;
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }
                    });
                }
            };

            shareList.setAdapter(firebaseRecyclerAdapter);


    }


    private void checkUser() {

        final String user_id = auth.getCurrentUser().getUid();

        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(user_id)) {
                   /* Intent setupIntent = new Intent(getActivity(), ProfileActivity.class);
                    setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(setupIntent);*/
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




    public static class ShareViewHolder extends RecyclerView.ViewHolder {

        View mView;

        ImageButton mLikebtn;
        ImageButton commentBtn;
        RelativeLayout profile;
        LinearLayout likeUsers;

        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;

        TextView counterLike;
        TextView commentCount;



        public ShareViewHolder(View itemView) {
            super(itemView);


            mView = itemView;
            mLikebtn = (ImageButton) mView.findViewById(R.id.likeBtn);
            commentBtn = (ImageButton) mView.findViewById(R.id.btnComments);

            counterLike = (TextView) mView.findViewById(R.id.counterLike);
            commentCount = (TextView) mView.findViewById(R.id.commentCount);

            profile = (RelativeLayout) mView.findViewById(R.id.profile);
            likeUsers = (LinearLayout) mView.findViewById(R.id.like_users);




            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
            mAuth = FirebaseAuth.getInstance();
        }

        public void setLiikeBtn(final String post_key){

            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())){
                        mLikebtn.setImageResource(R.drawable.ic_heart_red);

                    }else{
                        mLikebtn.setImageResource(R.drawable.ic_heart_outline_grey);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


        public void setQuestions(String questions){
            TextView questions_title = (TextView) mView.findViewById(R.id.question_text);
            questions_title.setText(questions);
            question = questions;
        }

        public void setDesc(String desc){

            TextView share_desc = (TextView) mView.findViewById(R.id.desc_text);
            share_desc.setText(desc);

        }

        public void setCity(String city){

            TextView city_name = (TextView) mView.findViewById(R.id.category);
            city_name.setText(city);

        }

        public void setName(String name){
            TextView shaUsername = (TextView) mView.findViewById(R.id.shaUsername);
            shaUsername.setText(name);
            username = name;
        }

        public void setImage(Context ctx, String image){
            ImageView user_Pic = (ImageView) mView.findViewById(R.id.user_profile);
           // Picasso.with(ctx).load(image).networkPolicy(NetworkPolicy.OFFLINE).fit().centerCrop().into(user_Pic);
            Glide.with(ctx)
                    .load(image)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transform(new CircleTransform(ctx))
                    .animate(R.anim.shake)
                    .into(user_Pic);

        }


        public void setPost_image(Context ctx, String post_image){
            ImageView share_img = (ImageView) mView.findViewById(R.id.share_img);
            //Picasso.with(ctx).load(post_image).networkPolicy(NetworkPolicy.OFFLINE).fit().centerCrop().into(share_img);
            Glide.with(ctx)
                    .load(post_image)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .animate(R.anim.shake)
                    .into(share_img);


        }

    }
}
