package com.team3s.lostpropertyse.Profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.team3s.lostpropertyse.Adapter;
import com.team3s.lostpropertyse.LoginSign.TabsHeaderActivity;
import com.team3s.lostpropertyse.Post.PostDetailFrag;
import com.team3s.lostpropertyse.R;

public class LostProp_Fragment extends Fragment {


    private RecyclerView lostPropList;
    private Query mQueryUser;
    private DatabaseReference mDatabase;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference mDatabaseUsers;
    private String currentUserId;
    private String str;
    public static final String MyPREFERENCES = "MyPrefs" ;
    public LostProp_Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_losted_prop_, container, false);

        lostPropList = (RecyclerView) v.findViewById(R.id.rvLostProp);

        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    Intent loginIntent = new Intent(getActivity(), TabsHeaderActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };

        try {
            SharedPreferences mPrefs = getActivity().getSharedPreferences(MyPREFERENCES,0);
            str = mPrefs.getString("USERKEY_SHARED", "");
        }catch (Exception e){

        }


        mDatabase = FirebaseDatabase.getInstance().getReference().child("Icerik").child("Kayıplar");
        mDatabase.keepSynced(true);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(str);
        mDatabaseUsers.keepSynced(true);

        currentUserId = auth.getCurrentUser().getUid();
        mQueryUser = mDatabase.orderByChild("uid").equalTo(str);


        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        lostPropList.setHasFixedSize(true);
        lostPropList.setLayoutManager(layoutManager);
        setHasOptionsMenu(true);

        return v;
    }

    public void onStart(){
        super.onStart();
        FirebaseRecyclerAdapter<Adapter, ShareViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Adapter, ShareViewHolder>(
                Adapter.class,
                R.layout.profile_row,
                ShareViewHolder.class,
                mQueryUser
        ) {
            @Override
            protected void populateViewHolder(final ShareViewHolder viewHolder, Adapter model, final int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setQuestions(model.getQuestions());
                viewHolder.setPost_image(getActivity().getApplicationContext(),model.getPost_image());
                viewHolder.setPost_date(model.getPost_date());
                viewHolder.setPost_time(model.getPost_time());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundlePostDetail = new Bundle();
                        bundlePostDetail.putString("post_id",post_key);
                        bundlePostDetail.putString("post_type","Kayıplar");

                        PostDetailFrag fragmentCom = new PostDetailFrag();
                        fragmentCom.setArguments(bundlePostDetail);
                        getFragmentManager()
                                .beginTransaction()
                                .add(R.id.postdetproflost, fragmentCom)
                                .addToBackStack(null)
                                .commit();

                    }
                });

            }
        };

        lostPropList.setAdapter(firebaseRecyclerAdapter);

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
            //Picasso.with(ctx).load(post_image).networkPolicy(NetworkPolicy.OFFLINE).fit().centerCrop().into(post_img);
            Glide.with(ctx)
                    .load(post_image)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .animate(R.anim.shake)
                    .into(post_img);
        }

    }
}
