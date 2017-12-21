package com.team3s.lostpropertyse.Profile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.team3s.lostpropertyse.AdapterClass;
import com.team3s.lostpropertyse.LoginSign.TabsHeaderActivity;
import com.team3s.lostpropertyse.Post.PostDetailFrag;
import com.team3s.lostpropertyse.R;

public class FindProp_Fragment extends Fragment {

    public static RecyclerView findPropList;
    public Query mQueryUser;
    public DatabaseReference mDatabase;
    public FirebaseAuth.AuthStateListener authListener;
    public FirebaseAuth auth;
    public FirebaseUser user;
    public DatabaseReference mDatabaseUsers;
    public String currentUserId;
    public String str;
    private SharedPreferences sharedpreferences;
    public static final String PREFS = "MyPrefs" ;
    public static String themeStr;
    public static RelativeLayout relativeFound_prop;


    public FindProp_Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_find_prop_, container, false);
        findPropList = (RecyclerView) v.findViewById(R.id.rvFindProp);

        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        sharedpreferences = getActivity().getSharedPreferences(PREFS,0);
        themeStr = sharedpreferences.getString("theme", "DayTheme");          //eğer null ise DayTheme
        relativeFound_prop = (RelativeLayout) v.findViewById(R.id.relativeFound_prop);
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
            str = sharedpreferences.getString("USERKEY_SHARED", "");
        }catch (Exception e){

        }

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Icerik").child("Bulunanlar");
        mDatabase.keepSynced(true);

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(str);

        mDatabaseUsers.keepSynced(true);

        currentUserId = auth.getCurrentUser().getUid();
        mQueryUser = mDatabase.orderByChild("uid").equalTo(str);


        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        findPropList.setHasFixedSize(true);
        findPropList.setLayoutManager(layoutManager);
        setHasOptionsMenu(true);

        return v;
    }
    public void onStart(){
        super.onStart();
        FirebaseRecyclerAdapter<AdapterClass, ShareViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<AdapterClass, ShareViewHolder>(
                AdapterClass.class,
                R.layout.profile_row,
                ShareViewHolder.class,
                mQueryUser
        ) {
            @Override
            protected void populateViewHolder(final ShareViewHolder viewHolder, AdapterClass model, final int position) {

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
                        bundlePostDetail.putString("post_type","Bulunanlar");

                        PostDetailFrag fragmentCom = new PostDetailFrag();
                        fragmentCom.setArguments(bundlePostDetail);
                        getFragmentManager()
                                .beginTransaction()
                                .add(R.id.another_user_frag, fragmentCom)
                                .addToBackStack(null)
                                .commit();


                    }
                });

            }
        };

        findPropList.setAdapter(firebaseRecyclerAdapter);

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
        RelativeLayout relativeLayFindProfile;
        TextView questions_title;
        TextView date;
        TextView time;

        public ShareViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
            mAuth = FirebaseAuth.getInstance();
            relativeLayFindProfile = (RelativeLayout) mView.findViewById(R.id.relativeLayFindProfile);
            questions_title = (TextView) mView.findViewById(R.id.titleProfileText);
            date = (TextView) mView.findViewById(R.id.dateTxt);
            time = (TextView) mView.findViewById(R.id.timeTxt);

            if(themeStr.equals("NightTheme")){
                relativeLayFindProfile.setBackgroundColor(Color.parseColor("#142634"));
                findPropList.setBackgroundColor(Color.parseColor("#142634"));
                relativeFound_prop.setBackgroundColor(Color.parseColor("#142634"));
                questions_title.setTextColor(Color.parseColor("#FFFFFF"));
                date.setTextColor(Color.parseColor("#FFFFFF"));
                time.setTextColor(Color.parseColor("#FFFFFF"));


            }else if(themeStr.equals("DayTheme")){
                relativeLayFindProfile.setBackgroundColor(Color.parseColor("#EEEEEE"));
                findPropList.setBackgroundColor(Color.parseColor("#EEEEEE"));
                relativeFound_prop.setBackgroundColor(Color.parseColor("#EEEEEE"));
                questions_title.setTextColor(Color.parseColor("#FFFFFF"));
                date.setTextColor(Color.parseColor("#FFFFFF"));
                time.setTextColor(Color.parseColor("#FFFFFF"));

            }


        }


        public void setQuestions(String questions){

            questions_title.setText(questions);
        }
        public void setPost_date(String post_date){
            date.setText(post_date);
        }
        public void setPost_time(String post_time){
            time.setText(post_time);
        }

        public void setPost_image(Context ctx, String post_image){
            ImageView post_img = (ImageView) mView.findViewById(R.id.post_img);
            Glide.with(ctx)
                    .load(post_image)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(post_img);
        }

    }

}
