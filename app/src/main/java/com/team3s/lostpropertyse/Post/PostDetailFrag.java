package com.team3s.lostpropertyse.Post;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.andreilisun.swipedismissdialog.SwipeDismissDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.team3s.lostpropertyse.Chat.CommentFrag;
import com.team3s.lostpropertyse.MainPage.BottomBarActivity;
import com.team3s.lostpropertyse.MainPage.MainPage;
import com.team3s.lostpropertyse.Maps.PropMaps;
import com.team3s.lostpropertyse.Profile.UsersProfiFrag;
import com.team3s.lostpropertyse.R;
import com.team3s.lostpropertyse.Utils.UniversalImageLoader;

import java.io.File;

import static android.content.ContentValues.TAG;

public class PostDetailFrag extends Fragment{

    private String post_key = null;
    private String post_type = null;
    private String post_img;
    private String post_desc;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseUsers;
    private DatabaseReference mDatabaseLike;
    private ImageView mPostImage;
    private TextView mPostDesc;
    private TextView mPostCity;
    private TextView mPostCommentCounter;
    private ImageButton comments,popupMenuBTN;
    private Intent intent;

    private FirebaseAuth auth;

    private Button showMap;

    private final static String TAG_FRAGMENT = "TAG_FRAGMENT";

    public PostDetailFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_post_detail, container, false);

        auth = FirebaseAuth.getInstance();

        Bundle bundlecom = getArguments();                          //mainFragment ten post un keyini çekiyoruz.
        post_key = bundlecom.getString("post_id");
        post_type = bundlecom.getString("post_type");

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Icerik").child(post_type);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");


        mPostImage = (ImageView) v.findViewById(R.id.post_image_btn);
        mPostDesc = (TextView) v.findViewById(R.id.post_descET);
        mPostCity = (TextView) v.findViewById(R.id.post_cityName);
        mPostCommentCounter = (TextView) v.findViewById(R.id.commentArticleCounter);
        comments = (ImageButton) v.findViewById(R.id.btnArticleComments);
        popupMenuBTN = (ImageButton) v.findViewById(R.id.popupMenuBTN);
        showMap = (Button) v.findViewById(R.id.showMap);

        comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundleComment = new Bundle();
                bundleComment.putString("post_id_key",post_key);
                bundleComment.putString("post_type", post_type);

                CommentFrag fragmentCom = new CommentFrag();
                fragmentCom.setArguments(bundleComment);
                getFragmentManager()
                        .beginTransaction()
                        .add(R.id.detailfrag, fragmentCom, TAG_FRAGMENT)
                        .addToBackStack(null)
                        .commit();

            }
        });
        showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editActivity = new Intent(getActivity(), PropMaps.class);
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
                String post_city = (String) dataSnapshot.child("city").getValue();


                mPostDesc.setText(post_desc);
                mPostCity.setText(post_city);

                Glide.with(getActivity().getApplicationContext())
                        .load(post_img)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .animate(R.anim.shake)
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
                PopupMenu popup = new PopupMenu(getActivity(), popupMenuBTN);
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
                                    MainPage fragmentMain = new MainPage();
                                    getFragmentManager()
                                            .beginTransaction()
                                            .add(R.id.postdetpfr, fragmentMain, TAG_FRAGMENT)
                                            .addToBackStack(null)
                                            .commit();
                                }else if(post_type == "Bulunanlar"){
                                    mDatabase.child(post_key).removeValue();
                                    mDatabaseLike.child(post_key).child(auth.getCurrentUser().getUid()).removeValue();
                                    MainPage fragmentMain = new MainPage();
                                    getFragmentManager()
                                            .beginTransaction()
                                            .add(R.id.postdetr, fragmentMain, TAG_FRAGMENT)
                                            .addToBackStack(null)
                                            .commit();
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
            public void onClick(View v) {       /////----------------------Resme tıklayınca resmi büyütüyor ve o resmi kaydırarak kapatabliyorsun
                View dialog = LayoutInflater.from(getActivity()).inflate(R.layout.custom_image_dialog, null);
                final SwipeDismissDialog swipeDismissDialog = new SwipeDismissDialog.Builder(getActivity())
                        .setView(dialog)
                        .build()
                        .show();
                ImageView image = (ImageView) dialog.findViewById(R.id.image);
                Glide.with(getActivity().getApplicationContext())
                        .load(post_img)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(image);
            }
        });
        return v;
    }
    public void fullScreen() {          //-----------------------uygulamayı tam ekran yapmak için örnek olarak: bazı telefonlarda alt taraftaki geri gibi butonları gizlemek için.

        // BEGIN_INCLUDE (get_current_ui_flags)
        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int uiOptions = getActivity().getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        // END_INCLUDE (get_current_ui_flags)
        // BEGIN_INCLUDE (toggle_ui_flags)
        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.i(TAG, "Turning immersive mode mode off. ");
        } else {
            Log.i(TAG, "Turning immersive mode mode on.");
        }

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        // Immersive mode: Backward compatible to KitKat.
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
        // Sticky immersive mode differs in that it makes the navigation and status bars
        // semi-transparent, and the UI flag does not get cleared when the user interacts with
        // the screen.
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        getActivity().getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        //END_INCLUDE (set_ui_flags)
    }
}