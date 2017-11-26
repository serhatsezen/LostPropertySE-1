package com.team3s.lostpropertyse.Chat;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.team3s.lostpropertyse.CircleTransform;
import com.team3s.lostpropertyse.R;
import com.team3s.lostpropertyse.Share;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class CommentFrag extends Fragment {
    private EditText cevap;
    private ImageButton cevaponay;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    private DatabaseReference database;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseAnswer;
    private DatabaseReference mDatabasePost;
    private DatabaseReference mDatabasePToken;

    private RecyclerView cevapList;

    private String post_key = null;
    private String tokenUser = null;
    private String nameFuser = null;
    private String mUID = null;
    private String currentUID = null;

    public String user_key = null;
    public String cevap_val;

    public CommentFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_comment, container, false);
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        Bundle bundlecom = getArguments();                          //mainFragment ten post un keyini çekiyoruz.
        post_key = bundlecom.getString("post_id_key");

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid());
        mDatabaseAnswer = FirebaseDatabase.getInstance().getReference().child("Icerik").child(post_key).child("Comments");
        database = FirebaseDatabase.getInstance().getReference().child("Icerik");
        mDatabasePToken = FirebaseDatabase.getInstance().getReference().child("Icerik").child(post_key);
        mDatabasePToken.addValueEventListener(new ValueEventListener() {            //postun sahibinin token bilgisini çekiyoruz.
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tokenUser = (String) dataSnapshot.child("token").getValue();
                mUID = (String) dataSnapshot.child("uid").getValue();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        cevapList = (RecyclerView) v.findViewById(R.id.comment_list);
        cevap = (EditText) v.findViewById(R.id.edtx_comment);
        cevaponay = (ImageButton) v.findViewById(R.id.comment_submit);
        progressBar = (ProgressBar) v.findViewById(R.id.news_cevap_progressBar);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        cevapList.setLayoutManager(layoutManager);
        cevaponay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cevaponay.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                startPosting();
            }
        });
        return v;
    }

    private void startPosting() {
        cevap_val = cevap.getText().toString().trim();
        if (!TextUtils.isEmpty(cevap_val)) {
            final DatabaseReference newCevap = mDatabaseAnswer.push();      //veritabanına push işlemi
            final Time today = new Time(Time.getCurrentTimezone());         //posta verilen cavabın zamanını çekmek için.
            today.setToNow();
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {               //verilen cevabın veritabanına yazılması
                    nameFuser = (String) dataSnapshot.child("username").getValue();
                    currentUID = currentUser.getUid();
                    newCevap.child("commentText").setValue(cevap_val);
                    newCevap.child("uid").setValue(currentUID);
                    newCevap.child("image").setValue(dataSnapshot.child("profileImage").getValue());
                    newCevap.child("post_time").setValue(today.format("%k:%M"));
                    newCevap.child("post_date").setValue(today.format("%d/%m/%Y"));
                    newCevap.child("commentUsername").setValue(nameFuser).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                if(!mUID.equals(currentUID)) {
                                    new Send().execute();                           //başarılı ise notification gönderme
                                }
                                cevap.getText().clear();
                            }
                        }
                    });
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
            progressBar.setVisibility(View.GONE);
            cevaponay.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
            cevaponay.setVisibility(View.VISIBLE);
        }
    }

    public void onStart() {         //bu bölümde o postun altında bulunan comment child ına ait cevapları yazdırmak için.
        super.onStart();

        FirebaseRecyclerAdapter<Share, ShareViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Share, ShareViewHolder>(
                Share.class,
                R.layout.comment_row,
                ShareViewHolder.class,
                mDatabaseAnswer
        ) {
            @Override
            protected void populateViewHolder(final ShareViewHolder viewHolder, Share model, final int position) {
                final String post_key = getRef(position).getKey();
                database.child(post_key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        user_key = (String) dataSnapshot.child("uid").getValue();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
                viewHolder.setcommentText(model.getcommentText());          //comment text
                viewHolder.setcommentUsername(model.getcommentUsername());  //comment user name
                viewHolder.setImage(getActivity().getApplicationContext(), model.getImage());   //comment user image
            }
        };
        cevapList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class ShareViewHolder extends RecyclerView.ViewHolder {       // burada bizim oluşturmuş olduğumuz comment_row layoutundaki bileşenleri kullandığımız yer
        View mViewRoad;
        FirebaseAuth mAuth;
        TextView counterComment;
        public ShareViewHolder(View itemView) {
            super(itemView);
            mViewRoad = itemView;
            counterComment = (TextView) mViewRoad.findViewById(R.id.counterLike);
            mAuth = FirebaseAuth.getInstance();
        }
        public void setcommentText(String commentText) {            //burada bulunan commentText ile firebasedeki child ın altındaki node aynı olmak zorunda ayrıca bunları Share.java classında tanımlıyoruz. get fonksiyonları share classdan çekiyoruz.
            TextView mAnswer = (TextView) mViewRoad.findViewById(R.id.commentTxt);
            mAnswer.setText(commentText);
        }
        public void setcommentUsername(String commentUsername) {
            TextView shaUsername = (TextView) mViewRoad.findViewById(R.id.shaUsernameCom);
            shaUsername.setText(commentUsername);
        }
        public void setImage(Context ctx, String image) {
            ImageView user_Pic = (ImageView) mViewRoad.findViewById(R.id.user_profile_com);
            Glide.with(ctx)
                    .load(image)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transform(new CircleTransform(ctx))
                    .animate(R.anim.shake)
                    .into(user_Pic);
        }
    }

    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
    }

    class Send extends AsyncTask<String, Void,Long > {          //burası notification kısmı.
        protected Long doInBackground(String... urls) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://aydinserhatsezen.com/fcm/LostP/lpyorum.php");
            try {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
                nameValuePairs.add(new BasicNameValuePair("tokendevice", tokenUser));
                nameValuePairs.add(new BasicNameValuePair("cevap", cevap_val));
                nameValuePairs.add(new BasicNameValuePair("userName", nameFuser));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
            } catch (Exception e) {
                // TODO Auto-generated catch block
            }
            return null;
        }
        protected void onProgressUpdate(Integer... progress) {
        }
        protected void onPostExecute(Long result) {
        }
    }
}