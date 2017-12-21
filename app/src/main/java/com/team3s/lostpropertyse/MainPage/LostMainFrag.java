package com.team3s.lostpropertyse.MainPage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;

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

import org.json.JSONArray;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class LostMainFrag extends Fragment {

    private ImageButton commentButton;
    private static RecyclerView lost_main_list;
    private static RelativeLayout relativeLayLost_Main;
    private DatabaseReference database,mDatabaseUsers,mDatabaseUsersProfile,mDatabaseLikeCounter,mDatabaseUsersFilter;
    private DatabaseReference mDatabaseLike, mDatabaseDistance;
    public Query mQueryUserFindCat,mQueryUserLostCat;
    public DatabaseReference mDatabaseFindCat,mDatabaseLostCat;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private Query mQueryIcerik;
    private boolean mProcessLike = false;
    private static String tokenUser = null;
    private static String questionName = null;
    private static AppBarLayout appBarLayout;
    private static TextView toolbarText;
    private static String question = null;
    private static String username = null;

    public static final String ARG_TITLE = "arg_title";
    public String user_key = null;
    public String userNames;
    public String cityFilter = "Genel";
    public static String themeStr;

    private String latUsers;
    private String lngUsers;

    private String latPost;
    private String lngPost;

    double latPostL;
    double lngPostL;

    double latUserL;
    double lngUserL;
    double dist;
    double distanceUserSelection;

    String distStr;
    SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;

    private final static String TAG_FRAGMENT = "TAG_FRAGMENT";
    public static final String PREFS = "MyPrefs" ;
    private boolean showPost;
    FragmentManager manager;
    public String currentuid;

    final ArrayList<String> categorynames = new ArrayList<String>();
    final ArrayList<Category> categoriesInformation = new ArrayList<Category>();

    public String category;



    public LostMainFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_lost_main, container, false);
        manager = getFragmentManager();


        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);

        //for crate home button
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        appBarLayout = (AppBarLayout) v.findViewById(R.id.LostappBarLayout);
        relativeLayLost_Main = (RelativeLayout) v.findViewById(R.id.relativeLayLost_Main);

        sharedpreferences = getActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        category = sharedpreferences.getString("categoryShared", "Hepsi");

        currentuid = user.getUid();
        appBarLayout.setVisibility(View.VISIBLE);
        toolbarText = (TextView) v.findViewById(R.id.toolbarText);
        mDatabaseUsersFilter = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        mDatabaseDistance = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("latLng");

        Typeface type = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/Ubuntu-B.ttf");
        toolbarText.setTypeface(type);

        mDatabaseUsersFilter.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userNames = (String) dataSnapshot.child("username").getValue();
                editor.putString("username", userNames);
                editor.putString("sender_uid", currentuid);


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

        lost_main_list = (RecyclerView) v.findViewById(R.id.lost_main_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        lost_main_list.setHasFixedSize(true);
        lost_main_list.setLayoutManager(layoutManager);

        themeStr = sharedpreferences.getString("theme", "DayTheme");

        if(themeStr.equals("NightTheme")){
            lost_main_list.setBackgroundColor(Color.parseColor("#1a2f40"));
            relativeLayLost_Main.setBackgroundColor(Color.parseColor("#1a2f40"));
            appBarLayout.setBackgroundColor(Color.parseColor("#142629"));
            toolbarText.setTextColor(Color.parseColor("#BDC7C1"));


        }else if(themeStr.equals("DayTheme")){
            lost_main_list.setBackgroundColor(Color.parseColor("#9E9E9E"));
            relativeLayLost_Main.setBackgroundColor(Color.parseColor("#9E9E9E"));
            appBarLayout.setBackgroundColor(Color.parseColor("#EEEEEE"));
            toolbarText.setTextColor(Color.parseColor("#000000"));

        }


        return v;
    }


    public void onStart() {
        super.onStart();
        final String currentUserId = auth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance().getReference().child("Icerik").child("Kayiplar");
        mQueryIcerik = database.orderByChild("category").equalTo(category);

        if (category.equals("Hepsi")) {
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
                    viewHolder.setDate(model.getPost_date());


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
                                distStr = String.format("%.2f", dist);

                                viewHolder.distanceUser.setText(distStr + " km");


                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
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
                            bundleComment.putString("post_id", post_key);
                            bundleComment.putString("post_type", "Kayiplar");
                            appBarLayout.setVisibility(View.GONE);

                            PostDetailFrag fragmentDet = new PostDetailFrag();
                            fragmentDet.setArguments(bundleComment);
                            getFragmentManager()
                                    .beginTransaction()
                                    .add(R.id.postdetpfr, fragmentDet, "addPostDetail")
                                    .addToBackStack(null)
                                    .commit();

                        }
                    });
                    viewHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Bundle bundleComment = new Bundle();
                            bundleComment.putString("post_id_key", post_key);
                            bundleComment.putString("post_type", "Kayiplar");
                            appBarLayout.setVisibility(View.GONE);

                            CommentFrag fragmentCom = new CommentFrag();
                            fragmentCom.setArguments(bundleComment);
                            getFragmentManager()
                                    .beginTransaction()
                                    .add(R.id.postdetpfr, fragmentCom, "addPostComment")
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
                                    editor.putString("username", userNames);
                                    editor.commit();

                                    Bundle bundle = new Bundle();
                                    bundle.putString("key", user_key); // User ID çekip anotherUserProfile ekranını açmak için
                                    appBarLayout.setVisibility(View.GONE);
                                    if (currentUserId.equals(user_key)) {     //User ID ve CurrentUserID aynı ise kendi profil sayfasına gitmek için
                                        UsersProfiFrag fragment2 = new UsersProfiFrag();
                                        getFragmentManager()
                                                .beginTransaction()
                                                .add(R.id.postdetpfr, fragment2, "addUserProfile")
                                                .addToBackStack(null)
                                                .commit();
                                    } else {
                                        appBarLayout.setVisibility(View.GONE);
                                        AnotherUsersProfiFrag fragment3 = new AnotherUsersProfiFrag();
                                        fragment3.setArguments(bundle);
                                        getFragmentManager()
                                                .beginTransaction()
                                                .add(R.id.postdetpfr, fragment3, "addAnotherProfile")
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


                }
            };
            lost_main_list.setAdapter(firebaseRecyclerAdapter);
            firebaseRecyclerAdapter.notifyDataSetChanged();


        }else{
            {
                FirebaseRecyclerAdapter<AdapterClass, ShareViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<AdapterClass, ShareViewHolder>(
                        AdapterClass.class,
                        R.layout.list,
                        ShareViewHolder.class,
                        mQueryIcerik
                ) {
                    @Override
                    protected void populateViewHolder(final ShareViewHolder viewHolder, final AdapterClass model, final int position) {

                        final String post_key = getRef(position).getKey();
                        viewHolder.setQuestions(model.getQuestions());
                        viewHolder.setCity(model.getaddressname());
                        viewHolder.setPost_image(getActivity().getApplicationContext(), model.getPost_image());
                        viewHolder.setName(model.getName());
                        viewHolder.setImage(getActivity().getApplicationContext(), model.getImage());
                        viewHolder.setDate(model.getPost_date());


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
                                    distStr = String.format("%.2f", dist);

                                    viewHolder.distanceUser.setText(distStr + " km");


                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
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
                                bundleComment.putString("post_id", post_key);
                                bundleComment.putString("post_type", "Kayiplar");
                                appBarLayout.setVisibility(View.GONE);

                                PostDetailFrag fragmentDet = new PostDetailFrag();
                                fragmentDet.setArguments(bundleComment);
                                getFragmentManager()
                                        .beginTransaction()
                                        .add(R.id.postdetpfr, fragmentDet, "addPostDetail")
                                        .addToBackStack(null)
                                        .commit();

                            }
                        });
                        viewHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Bundle bundleComment = new Bundle();
                                bundleComment.putString("post_id_key", post_key);
                                bundleComment.putString("post_type", "Kayiplar");
                                appBarLayout.setVisibility(View.GONE);

                                CommentFrag fragmentCom = new CommentFrag();
                                fragmentCom.setArguments(bundleComment);
                                getFragmentManager()
                                        .beginTransaction()
                                        .add(R.id.postdetpfr, fragmentCom, "addPostComment")
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
                                        editor.putString("username", userNames);
                                        editor.commit();

                                        Bundle bundle = new Bundle();
                                        bundle.putString("key", user_key); // User ID çekip anotherUserProfile ekranını açmak için
                                        appBarLayout.setVisibility(View.GONE);
                                        if (currentUserId.equals(user_key)) {     //User ID ve CurrentUserID aynı ise kendi profil sayfasına gitmek için
                                            UsersProfiFrag fragment2 = new UsersProfiFrag();
                                            getFragmentManager()
                                                    .beginTransaction()
                                                    .add(R.id.postdetpfr, fragment2, "addUserProfile")
                                                    .addToBackStack(null)
                                                    .commit();
                                        } else {
                                            appBarLayout.setVisibility(View.GONE);
                                            AnotherUsersProfiFrag fragment3 = new AnotherUsersProfiFrag();
                                            fragment3.setArguments(bundle);
                                            getFragmentManager()
                                                    .beginTransaction()
                                                    .add(R.id.postdetpfr, fragment3, "addAnotherProfile")
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


                    }
                };
                lost_main_list.setAdapter(firebaseRecyclerAdapter);
                lost_main_list.invalidate();
            }
        }
    }

    public static class ShareViewHolder extends RecyclerView.ViewHolder {

        View mView;

        ImageButton commentBtn;

        ImageView profile;
        ImageView postImg;

        TextView commentCount;
        TextView distanceUser;
        TextView question_text;
        TextView category;

        LinearLayout linearLayout;
        LinearLayout linearListLay2;

        DatabaseReference mDatabaseLike;
        FirebaseAuth mAuth;



        public ShareViewHolder(View itemView) {
            super(itemView);


            mView = itemView;
            commentBtn = (ImageButton) mView.findViewById(R.id.btnComments);

            commentCount = (TextView) mView.findViewById(R.id.commentCount);
            distanceUser = (TextView) mView.findViewById(R.id.distanceTxt);
            question_text = (TextView) mView.findViewById(R.id.question_text);
            category = (TextView) mView.findViewById(R.id.category);

            profile = (ImageView) mView.findViewById(R.id.user_profile);
            postImg = (ImageView) mView.findViewById(R.id.share_img);
            linearLayout = (LinearLayout) mView.findViewById(R.id.linearListLay);
            linearListLay2 = (LinearLayout) mView.findViewById(R.id.linearListLay2);

            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
            mAuth = FirebaseAuth.getInstance();

            if(themeStr.equals("NightTheme")){
                linearLayout.setBackgroundColor(Color.parseColor("#142634"));
                linearListLay2.setBackgroundColor(Color.parseColor("#142634"));
                lost_main_list.setBackgroundColor(Color.parseColor("#1a2f40"));
                relativeLayLost_Main.setBackgroundColor(Color.parseColor("#1a2f40"));
                appBarLayout.setBackgroundColor(Color.parseColor("#142629"));
                question_text.setTextColor(Color.parseColor("#BDC7C1"));
                category.setTextColor(Color.parseColor("#7E8889"));
                commentCount.setTextColor(Color.parseColor("#7E8889"));
                toolbarText.setTextColor(Color.parseColor("#BDC7C1"));


            }else if(themeStr.equals("DayTheme")){
                linearLayout.setBackgroundColor(Color.parseColor("#EEEEEE"));
                linearListLay2.setBackgroundColor(Color.parseColor("#EEEEEE"));
                lost_main_list.setBackgroundColor(Color.parseColor("#9E9E9E"));
                relativeLayLost_Main.setBackgroundColor(Color.parseColor("#9E9E9E"));
                appBarLayout.setBackgroundColor(Color.parseColor("#EEEEEE"));
                question_text.setTextColor(Color.parseColor("#000000"));
                category.setTextColor(Color.parseColor("#000000"));
                commentCount.setTextColor(Color.parseColor("#000000"));
                toolbarText.setTextColor(Color.parseColor("#000000"));

            }

        }

        public void setQuestions(String questions){
            TextView questions_title = (TextView) mView.findViewById(R.id.question_text);
            questions_title.setText(questions);
        }



        public void setCity(String city){

            TextView city_name = (TextView) mView.findViewById(R.id.category);
            city_name.setText(city);

        }
        public void setDate(String date){

            TextView date_yy = (TextView) mView.findViewById(R.id.time);
            date_yy.setText(date);

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
    @Override
    public void onResume() {        // click back button

        super.onResume();

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK){
                    appBarLayout.setVisibility(View.VISIBLE);

                    AnotherUsersProfiFrag fragmentAnother = (AnotherUsersProfiFrag) manager.findFragmentByTag("addAnotherProfile");
                    FragmentTransaction transaction = manager.beginTransaction();
                    if(fragmentAnother != null){
                        transaction.remove(fragmentAnother);
                        transaction.commit();
                    }
                    PostDetailFrag fragmentPostDetail = (PostDetailFrag) manager.findFragmentByTag("addPostDetail");
                    FragmentTransaction transactionPostDet = manager.beginTransaction();
                    if(fragmentPostDetail != null){
                        transactionPostDet.remove(fragmentPostDetail);
                        transactionPostDet.commit();
                    }
                    CommentFrag fragmentPostComment = (CommentFrag) manager.findFragmentByTag("addPostComment");
                    FragmentTransaction transactionPostComment = manager.beginTransaction();
                    if(fragmentPostComment != null){
                        transactionPostComment.remove(fragmentPostComment);
                        transactionPostComment.commit();
                    }
                    UsersProfiFrag fragmentUserProfile = (UsersProfiFrag) manager.findFragmentByTag("addUserProfile");
                    FragmentTransaction transactionUserProfile = manager.beginTransaction();
                    if(fragmentUserProfile != null){
                        transactionUserProfile.remove(fragmentUserProfile);
                        transactionUserProfile.commit();
                    }

                }
                return false;
            }
        });
    }
}
