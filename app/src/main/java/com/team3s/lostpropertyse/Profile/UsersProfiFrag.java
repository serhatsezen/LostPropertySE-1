package com.team3s.lostpropertyse.Profile;

import android.app.AlertDialog;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
import com.team3s.lostpropertyse.Chat.UsersDMList;
import com.team3s.lostpropertyse.Post.PostDetailFrag;
import com.team3s.lostpropertyse.Utils.CircleTransform;
import com.team3s.lostpropertyse.LoginSign.TabsHeaderActivity;
import com.team3s.lostpropertyse.R;


import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class UsersProfiFrag extends Fragment {

    public StorageReference storageReference;
    public StorageReference backgroundStorageReference;
    public TextView u_fullname,u_username,u_city;
    public ImageView profileImg,backgroundImg;
    public View backgroundView;
    public TextView num_post;
    public ImageButton editprof,dm_imgBtn;

    public StorageReference mStorageImage;
    public StorageReference mStorageImageBack;

    public static final int GALLERY_REQUEST = 1;
    public static final int CAMERA_REQUEST_CODE = 2;

    public RecyclerView profileList;

    public DatabaseReference database,mDatabaseUsers,databaseFollowers,mDatabaseUsersPostNum;
    public DatabaseReference mDatabaseCurrentUsers;
    public Query mQueryUser;
    public Query mQueryUserLost;
    public Query mQueryUserFind;
    public DatabaseReference mDatabaseLike;
    public FirebaseAuth.AuthStateListener authListener;
    public FirebaseAuth auth;

    public Uri mProfImageUri = null;
    public Uri mBackImageUri = null;

    public TabLayout tlUserProfileTabs;
    public boolean backgroundImage = false;
    public boolean profileImage = false;
    public Query mQueryUserId;
    public FirebaseUser user;
    public String currentUserId;
    public String LostCount;

    public UsersProfiFrag() {
        // Required empty public constructor
    }



  @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.user_profil_frag, container, false);

        //get firebase auth instance
      auth = FirebaseAuth.getInstance();
      FirebaseStorage storage = FirebaseStorage.getInstance();
      storageReference = storage.getReference();
      backgroundStorageReference = storageReference.child("background_images");
        //get current user
      user = FirebaseAuth.getInstance().getCurrentUser();

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

      ViewPager viewPager = (ViewPager) v.findViewById(R.id.viewpager);
      setupViewPager(viewPager);
      // Set Tabs inside Toolbar
      TabLayout tabs = (TabLayout) v.findViewById(R.id.result_tabs);
      tabs.setupWithViewPager(viewPager);
      tabs.setTabGravity(TabLayout.GRAVITY_CENTER);

      database = FirebaseDatabase.getInstance().getReference().child("Icerik");
      database.keepSynced(true);
      mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
      mDatabaseUsers.keepSynced(true);
      mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
       // databaseFollowers = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("Takip_Edilenler");
      mDatabaseUsersPostNum = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid()).child("PostsId");
      mStorageImage = FirebaseStorage.getInstance().getReference().child("Profile_images");
      mStorageImageBack = FirebaseStorage.getInstance().getReference().child("Background_images");


      u_fullname = (TextView) v.findViewById(R.id.fullnameuser);
      u_username = (TextView) v.findViewById(R.id.usernameprof);
      u_city = (TextView) v.findViewById(R.id.city);
      backgroundView = v.findViewById(R.id.background);

      currentUserId = auth.getCurrentUser().getUid();
      mDatabaseCurrentUsers = FirebaseDatabase.getInstance().getReference().child("Icerik");
      mQueryUser = mDatabaseCurrentUsers.child("Kayiplar").orderByChild("uid").equalTo(currentUserId);
      mQueryUserLost = mDatabaseCurrentUsers.child("Kayiplar").orderByChild("uid").equalTo(currentUserId);
      mQueryUserFind = mDatabaseCurrentUsers.child("Bulunanlar").orderByChild("uid").equalTo(currentUserId);
      profileImg = (ImageView) v.findViewById(R.id.ivUserProfilePhoto);
      backgroundImg = (ImageView) v.findViewById(R.id.imageView3);
      dm_imgBtn = (ImageButton) v.findViewById(R.id.dm_imgBtn_another);


      editprof = (ImageButton) v.findViewById(R.id.edit_prof_btn);
      editprof.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View view) {
            showDialog();
          }
        });

      dm_imgBtn.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View view) {
              UsersDMList fragmentD = new UsersDMList();
              getFragmentManager()
                      .beginTransaction()
                      .add(R.id.another_user_frag, fragmentD)
                      .addToBackStack(null)
                      .commit();
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



        return v;
    }


    private void setupViewPager(ViewPager viewPager) {


        Adapter adapter = new Adapter(getChildFragmentManager());
        adapter.addFragment(new LostProp_Fragment(), "Kaybettiklerim");
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

        public void addFragment(Fragment fragment, String title) {
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
                .setItems(R.array.editButtonOptions, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which){
                            case 0: changeBackgroundImage();
                                backgroundImage = true;
                                break;
                            case 1: /*changeProfilePicture();*/
                                Intent edtprof = new Intent(getActivity(),EditProfileFragment.class);
                                startActivity(edtprof);
                                profileImage = true;
                                break;
                            case 2: signOut();
                                break;
                            default: break;
                        }

                    }
                });
        builder.create();
        builder.show();
    }
    public void changeBackgroundImage(){
        Intent backgroundPicker = new Intent(Intent.ACTION_PICK);
        backgroundPicker.setType("image/*");
        startActivityForResult(backgroundPicker, GALLERY_REQUEST);
    }
    public void changeProfilePicture(){
        Intent profilePicker = new Intent(Intent.ACTION_PICK);
        profilePicker.setType("image/*");
        startActivityForResult(profilePicker, GALLERY_REQUEST);

    }
    /*public void changeFragment(){
        FragmentManager manager = getFragmentManager();
        EditProfileFragment editProfileFragment = new EditProfileFragment();
        editProfileFragment.setArguments();
        manager.beginTransaction().replace(R.id.frame_fragmentholder, editProfileFragment, "TEST").commit();

    }*/

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if(profileImage==true) {
                try {
                    mProfImageUri = data.getData();
                    final InputStream imageStream = getActivity().getContentResolver().openInputStream(mProfImageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    profileImg.setImageBitmap(selectedImage);
                    profileImage=false;
                    StorageReference filepath = mStorageImage.child(mProfImageUri.getLastPathSegment());
                    if (mProfImageUri != null) {
                        filepath.putFile(mProfImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                final String downloadUri = taskSnapshot.getDownloadUrl().toString();
                                mDatabaseUsers.child("profileImage").setValue(downloadUri);
                                //-----------------------------------------------------------------------------postlardaki profil resmini güncellemek için
                                mQueryUserLost.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot tasksSnapshot) {
                                            for (DataSnapshot snapshot: tasksSnapshot.getChildren()) {
                                                snapshot.getRef().child("image").setValue(downloadUri);
                                            }
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                        }

                                    });
                                mQueryUserFind.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot tasksSnapshot) {
                                        for (DataSnapshot snapshot: tasksSnapshot.getChildren()) {
                                            snapshot.getRef().child("image").setValue(downloadUri);
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }

                                });


                            }
                        });
                    }


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Bir sorun oluştu", Toast.LENGTH_LONG).show();
                }
            }
            if(backgroundImage==true) {
                try {
                    mBackImageUri = data.getData();
                    final InputStream imageStream = getActivity().getContentResolver().openInputStream(mBackImageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    backgroundImg.setImageBitmap(selectedImage);
                    backgroundImage = false;
                    StorageReference filepathBack = mStorageImageBack.child(mBackImageUri.getLastPathSegment());
                    if (mBackImageUri != null) {
                        filepathBack.putFile(mBackImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                String downloadUri = taskSnapshot.getDownloadUrl().toString();
                                mDatabaseUsers.child("backgroundImage").setValue(downloadUri);
                            }
                        });
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Bir sorun oluştu", Toast.LENGTH_LONG).show();
                }
            }

        }else {
            backgroundImage = false;
            profileImage=false;
            Toast.makeText(getActivity(), "Resim Seçmedin",Toast.LENGTH_LONG).show();
        }
    }


    //sign out method
    public void signOut() {
        auth.signOut();
        Intent intent = new Intent(getActivity(), TabsHeaderActivity.class);
        startActivity(intent);
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
                    layout.requestLayout();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();

                    return true;
                }
                return false;
            }
        });

    }


}
