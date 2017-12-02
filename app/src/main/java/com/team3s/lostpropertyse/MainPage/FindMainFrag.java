package com.team3s.lostpropertyse.MainPage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.team3s.lostpropertyse.Chat.CommentFrag;
import com.team3s.lostpropertyse.Utils.CircleTransform;
import com.team3s.lostpropertyse.LoginSign.TabsHeaderActivity;
import com.team3s.lostpropertyse.Post.PostDetailFrag;
import com.team3s.lostpropertyse.Profile.AnotherUsersProfiFrag;
import com.team3s.lostpropertyse.Profile.UsersProfiFrag;
import com.team3s.lostpropertyse.R;
import com.team3s.lostpropertyse.Share;

public class FindMainFrag extends Fragment {

    private ImageButton commentButton;
    private RecyclerView find_main_list;

    private DatabaseReference database,mDatabaseUsers,mDatabaseUsersProfile,mDatabaseLikeCounter,mDatabaseUsersFilter;
    private DatabaseReference mDatabaseLike, mDatabaseDistance;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private Query mQueryIcerik;
    private boolean mProcessLike = false;
    private static String tokenUser = null;
    private static String questionName = null;
    AppBarLayout appBarLayout;

    private static String question = null;
    private static String username = null;

    public static final String ARG_TITLE = "arg_title";
    public String user_key = null;
    public static String userNames = null;
    public String cityFilter = "Genel";

    private String latUsers;
    private String lngUsers;

    private String latPost;
    private String lngPost;

    double latPostL;
    double lngPostL;

    double latUserL;
    double lngUserL;
    double dist;
    String distStr;

    private final static String TAG_FRAGMENT = "TAG_FRAGMENT";
    public static final String MyPREFERENCES = "MyPrefs" ;

    SharedPreferences sharedpreferences;


    public FindMainFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_find_main, container, false);
        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        appBarLayout = (AppBarLayout) v.findViewById(R.id.findappBarLayout);
        mDatabaseUsersFilter = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        mDatabaseDistance = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("latLng");

        mDatabaseUsersFilter.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userNames = (String) dataSnapshot.child("username").getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseDistance.addValueEventListener(new ValueEventListener() {      //kullanıcının lat lng değerleri
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                latUsers = dataSnapshot.child("latitude").getValue().toString();
                lngUsers = dataSnapshot.child("longitude").getValue().toString();


                latUserL = Double.parseDouble(String.valueOf(latUsers));
                lngUserL = Double.parseDouble(String.valueOf(lngUsers));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseUsersFilter.child("filterCity").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userNames = (String) dataSnapshot.child("username").getValue();
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

        find_main_list = (RecyclerView) v.findViewById(R.id.find_main_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        find_main_list.setHasFixedSize(true);
        find_main_list.setLayoutManager(layoutManager);

        return v;
    }

    public void onStart(){
        super.onStart();
        final String currentUserId = auth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance().getReference().child("Icerik").child("Bulunanlar");
        mQueryIcerik = database.orderByChild("city").equalTo(cityFilter);


        FirebaseRecyclerAdapter<Share, ShareViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Share, ShareViewHolder>(
                Share.class,
                R.layout.row,
                ShareViewHolder.class,
                database
        ) {
            @Override
            protected void populateViewHolder(final ShareViewHolder viewHolder, Share model, final int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setQuestions(model.getQuestions());
                viewHolder.setCity(model.getaddressname());
                viewHolder.setPost_image(getActivity().getApplicationContext(), model.getPost_image());
                viewHolder.setName(model.getName());
                viewHolder.setImage(getActivity().getApplicationContext(), model.getImage());

                viewHolder.setLiikeBtn(post_key);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundleComment = new Bundle();
                        bundleComment.putString("post_id",post_key);
                        bundleComment.putString("post_type","Bulunanlar");


                        PostDetailFrag fragmentD = new PostDetailFrag();
                        fragmentD.setArguments(bundleComment);
                        getFragmentManager()
                                .beginTransaction()
                                .add(R.id.postdetr, fragmentD, TAG_FRAGMENT)
                                .addToBackStack(null)
                                .commit();

                    }
                });
                viewHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundleComment = new Bundle();
                        bundleComment.putString("post_id_key",post_key);
                        bundleComment.putString("post_type","Bulunanlar");

                        CommentFrag fragmentCom = new CommentFrag();
                        fragmentCom.setArguments(bundleComment);
                        getFragmentManager()
                                .beginTransaction()
                                .add(R.id.postdetr, fragmentCom, TAG_FRAGMENT)
                                .addToBackStack(null)
                                .commit();

                           /*Intent editActivity = new Intent(getActivity(), CommentActivity.class);
                            editActivity.putExtra("post_id", post_key);
                            startActivity(editActivity);*/
                    }
                });
                viewHolder.profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        database.child(post_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                user_key = (String) dataSnapshot.child("uid").getValue();
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString("USERKEY_SHARED", user_key);
                                editor.commit();

                                Bundle bundle = new Bundle();
                                bundle.putString("key",user_key); // User ID çekip anotherUserProfile ekranını açmak için
                                appBarLayout.setVisibility(View.GONE);
                                if(currentUserId.equals(user_key)){     //User ID ve CurrentUserID aynı ise kendi profil sayfasına gitmek için
                                    UsersProfiFrag fragment2 = new UsersProfiFrag();
                                    getFragmentManager()
                                            .beginTransaction()
                                            .add(R.id.postdetr, fragment2,TAG_FRAGMENT)
                                            .addToBackStack(null)
                                            .commit();

                                }else {


                                    AnotherUsersProfiFrag fragment2 = new AnotherUsersProfiFrag();
                                    fragment2.setArguments(bundle);
                                    getFragmentManager()
                                            .beginTransaction()
                                            .add(R.id.postdetr, fragment2, TAG_FRAGMENT)
                                            .addToBackStack(null)
                                            .commit();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                // Failed to read value
                            }
                        });

                    }
                });
                database.child(post_key).child("Comments").addValueEventListener(new ValueEventListener() {
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

                database.child(post_key).child("latlng").addValueEventListener(new ValueEventListener() {       // her postun lat lng değerleri
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {


                            Location destination = new Location("destination");
                            destination.setLatitude(latUserL);
                            destination.setLongitude(lngUserL);


                            latPost = dataSnapshot.child("latitude").getValue().toString();
                            lngPost = dataSnapshot.child("longitude").getValue().toString();


                            latPostL = Double.parseDouble(String.valueOf(latPost));
                            lngPostL = Double.parseDouble(String.valueOf(lngPost));


                            Location current = new Location("destination");
                            current.setLatitude(latPostL);
                            current.setLongitude(lngPostL);

                            dist = current.distanceTo(destination) / 1000;                      //kullanıcının lat lng değerleri ile posttaki lat lng değerlerinin karşılarştırılması ve km olarak hesaplanması
                            distStr = String.format("%.2f", dist );

                            viewHolder.distanceUser.setText(distStr+" km");

                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        find_main_list.setAdapter(firebaseRecyclerAdapter);
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

        TextView distanceUser;



        public ShareViewHolder(View itemView) {
            super(itemView);


            mView = itemView;
            mLikebtn = (ImageButton) mView.findViewById(R.id.likeBtn);
            commentBtn = (ImageButton) mView.findViewById(R.id.btnComments);

            counterLike = (TextView) mView.findViewById(R.id.counterLike);
            commentCount = (TextView) mView.findViewById(R.id.commentCount);
            distanceUser = (TextView) mView.findViewById(R.id.distanceTxt);

            profile = (RelativeLayout) mView.findViewById(R.id.users_info);
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
            Glide.with(ctx)
                    .load(post_image)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .animate(R.anim.shake)
                    .into(share_img);


        }

    }
    @Override
    public void onResume() {
        super.onResume();

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    FrameLayout layoutt = (FrameLayout) v.findViewById(R.id.postdetr);
                    layoutt.removeAllViewsInLayout();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    appBarLayout.setVisibility(View.VISIBLE);

                    return true;
                }
                return false;
            }
        });

    }

}
