package com.team3s.lostpropertyse.Profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.team3s.lostpropertyse.Chat.Chat;
import com.team3s.lostpropertyse.Utils.CircleTransform;
import com.team3s.lostpropertyse.LoginSign.TabsHeaderActivity;
import com.team3s.lostpropertyse.R;

import java.util.ArrayList;
import java.util.List;

public class AnotherUsersProfiFrag extends Fragment {

    private TextView u_fullname,u_username,u_city;
    private ImageView profileImg,backgroundImg;
    private ImageButton dm_imgBtn_another;
    private View backgroundView;
    public TextView num_post;

    private ImageButton editprof;

    private RecyclerView profileList;

    private DatabaseReference database,mDatabase,mDatabaseUsers,mDatabaseUsersPostNum;
    private DatabaseReference mDatabaseCurrentUsers;
    private Query mQueryUser;
    private DatabaseReference mDatabaseLike;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    private FirebaseUser user;
    private String currentUserId;
    private String post_key_user = null;

    private String receiver_name;
    private String senderName;
    private String receiverToken;

    private SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs" ;

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
      senderName = bundle.getString("username");

      ViewPager viewPager = (ViewPager) v.findViewById(R.id.viewpager);
      setupViewPager(viewPager);
      // Set Tabs inside Toolbar
      TabLayout tabs = (TabLayout) v.findViewById(R.id.result_tabs);
      tabs.setupWithViewPager(viewPager);
      tabs.setTabGravity(TabLayout.GRAVITY_CENTER);

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
            }}
      };

      database = FirebaseDatabase.getInstance().getReference().child("Icerik");
      database.keepSynced(true);
      mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(post_key_user);
      mDatabaseUsers.keepSynced(true);
      mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
      mDatabaseUsersPostNum = FirebaseDatabase.getInstance().getReference().child("Users").child(post_key_user).child("PostsId");

      sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);


      u_fullname = (TextView) v.findViewById(R.id.fullnameuser);
      u_username = (TextView) v.findViewById(R.id.usernameprof);
      u_city = (TextView) v.findViewById(R.id.city);
      //num_post = (TextView) v.findViewById(R.id.find_counter);
      backgroundView = v.findViewById(R.id.background);

      currentUserId = auth.getCurrentUser().getUid();
      mDatabaseCurrentUsers = FirebaseDatabase.getInstance().getReference().child("Icerik").child("Bulunanlar");
      mQueryUser = mDatabaseCurrentUsers.orderByChild("uid").equalTo(post_key_user);
      profileImg = (ImageView) v.findViewById(R.id.ivUserProfilePhoto);
      backgroundImg = (ImageView) v.findViewById(R.id.imageView3);
      dm_imgBtn_another = (ImageButton) v.findViewById(R.id.dm_imgBtn_another);

      editprof = (ImageButton) v.findViewById(R.id.edit_prof_btn);
      editprof.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View view) {
            showDialog();
          }
        });


      dm_imgBtn_another.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
              SharedPreferences.Editor editor = sharedpreferences.edit();
              editor.putString("receiver_name", receiver_name);
              editor.commit();
              Intent senddm = new Intent(getActivity(), Chat.class);
              senddm.putExtra("sender_uid",currentUserId);
              senddm.putExtra("receiver_uid",post_key_user);
              senddm.putExtra("receiverToken",receiverToken);
              startActivity(senddm);
          }
      });

      mDatabaseUsers.child("token").addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot snapshot) {
              receiverToken = String.valueOf(snapshot.getValue());
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
              receiver_name = String.valueOf(snapshot.getValue());
              receiver_name = receiver_name.toLowerCase();
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
      return v;
    }
    private void setupViewPager(ViewPager viewPager) {
        UsersProfiFrag.Adapter adapter = new UsersProfiFrag.Adapter(getChildFragmentManager());
        adapter.addFragment(new LostProp_Fragment(), "\nKaybettiklerim");
        adapter.addFragment(new FindProp_Fragment(), "Bulduklarım");
        viewPager.setAdapter(adapter);
    }
    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title,String userKey) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
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
    @Override
    public void onResume() {
        super.onResume();

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    FrameLayout layout = (FrameLayout) v.findViewById(R.id.another_user_frag);
                    layout.removeAllViewsInLayout();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();

                    return true;
                }
                return false;
            }
        });

    }
}
