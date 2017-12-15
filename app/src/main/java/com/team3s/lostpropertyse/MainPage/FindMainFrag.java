package com.team3s.lostpropertyse.MainPage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.andreilisun.swipedismissdialog.SwipeDismissDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.team3s.lostpropertyse.AdapterClass;
import com.team3s.lostpropertyse.Chat.CommentFrag;
import com.team3s.lostpropertyse.Utils.CircleTransform;
import com.team3s.lostpropertyse.LoginSign.TabsHeaderActivity;
import com.team3s.lostpropertyse.Post.PostDetailFrag;
import com.team3s.lostpropertyse.Profile.AnotherUsersProfiFrag;
import com.team3s.lostpropertyse.Profile.UsersProfiFrag;
import com.team3s.lostpropertyse.R;

import java.util.ArrayList;

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
    public static final String PREFS = "MyPrefs" ;

    SharedPreferences sharedpreferences;
    public FindMainFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_find_main, container, false);

        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);

        AppCompatActivity activity = (AppCompatActivity) getActivity();    //-------------------------------------------actionbar spinner eklemek için
        activity.setSupportActionBar(toolbar);


        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        sharedpreferences = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        appBarLayout = (AppBarLayout) v.findViewById(R.id.findappBarLayout);
        mDatabaseUsersFilter = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        mDatabaseDistance = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("latLng");


        mDatabaseUsersFilter.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userNames = (String) dataSnapshot.child("username").getValue();
                userNames = userNames.toLowerCase();
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString("username", userNames);
                editor.commit();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        appBarLayout.setVisibility(View.VISIBLE);

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


        FirebaseRecyclerAdapter<AdapterClass, ShareViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<AdapterClass, ShareViewHolder>(
                AdapterClass.class,
                R.layout.list,
                ShareViewHolder.class,
                database
        ) {
            @Override
            protected void populateViewHolder(final ShareViewHolder viewHolder, final AdapterClass model, final int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setQuestions(model.getQuestions());
                viewHolder.setCity(model.getaddressname());
                viewHolder.setPost_image(getActivity().getApplicationContext(), model.getPost_image());
                viewHolder.setName(model.getName());
                viewHolder.setImage(getActivity().getApplicationContext(), model.getImage());

                viewHolder.postImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View dialog = LayoutInflater.from(getActivity()).inflate(R.layout.custom_image_dialog, null);
                        final SwipeDismissDialog swipeDismissDialog = new SwipeDismissDialog.Builder(getActivity())
                                .setView(dialog)
                                .build()
                                .show();
                        ImageView image = (ImageView) dialog.findViewById(R.id.image);
                        Glide.with(getActivity().getApplicationContext())
                                .load(model.getPost_image())
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(image);

                    }
                });

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundleComment = new Bundle();
                        bundleComment.putString("post_id",post_key);
                        bundleComment.putString("post_type","Bulunanlar");
                        appBarLayout.setVisibility(View.GONE);


                        PostDetailFrag fragmentD = new PostDetailFrag();
                        fragmentD.setArguments(bundleComment);
                        getFragmentManager()
                                .beginTransaction()
                                .add(R.id.group, fragmentD,"addPostDetail")
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
                        appBarLayout.setVisibility(View.GONE);

                        CommentFrag fragmentCom = new CommentFrag();
                        fragmentCom.setArguments(bundleComment);
                        getFragmentManager()
                                .beginTransaction()
                                .add(R.id.group, fragmentCom,"addPostComment")
                                .addToBackStack(null)
                                .commit();
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
                                            .add(R.id.group, fragment2,"addUserProfile")
                                            .addToBackStack(null)
                                            .commit();

                                }else {

                                    appBarLayout.setVisibility(View.GONE);

                                    Bundle bundleAnother = new Bundle();
                                    bundleAnother.putString("key",user_key); // User ID çekip anotherUserProfile ekranını açmak için
                                    bundleAnother.putString("username",userNames); // User ID çekip anotherUserProfile ekranını açmak için

                                    AnotherUsersProfiFrag fragment2 = new AnotherUsersProfiFrag();
                                    fragment2.setArguments(bundleAnother);
                                    getFragmentManager()
                                            .beginTransaction()
                                            .add(R.id.group, fragment2,"addAnotherProfile")
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


        ImageButton commentBtn;
        ImageView postImg;
        ImageView profile;

        FirebaseAuth mAuth;

        TextView commentCount;

        TextView distanceUser;


        public ShareViewHolder(View itemView) {
            super(itemView);


            mView = itemView;
            commentBtn = (ImageButton) mView.findViewById(R.id.btnComments);

            commentCount = (TextView) mView.findViewById(R.id.commentCount);
            distanceUser = (TextView) mView.findViewById(R.id.distanceTxt);

            profile = (ImageView) mView.findViewById(R.id.user_profile);
            postImg = (ImageView) mView.findViewById(R.id.share_img);
            mAuth = FirebaseAuth.getInstance();
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
                    .into(user_Pic);

        }

        public void setPost_image(Context ctx, String post_image){
            ImageView share_img = (ImageView) mView.findViewById(R.id.share_img);
            Glide.with(ctx)
                    .load(post_image)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(share_img);


        }

    }


}
