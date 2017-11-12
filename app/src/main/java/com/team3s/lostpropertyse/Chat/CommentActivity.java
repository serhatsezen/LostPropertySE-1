package com.team3s.lostpropertyse.Chat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.team3s.lostpropertyse.R;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public class CommentActivity extends AppCompatActivity {
  ListView commentListView;
  ImageButton sendCommentButton;
  EditText commentText;
  ImageView postImage;
  private String postId;
  private String username;
  private String tokenUser;
  private String path_to_image;
  private  String comment;
  private LinkedHashMap<String, String> commentsMap;
  FirebaseDatabase firebaseDatabase;
  DatabaseReference databaseCommentsRef;
  DatabaseReference databaseUsersRef;
  FirebaseUser currentUser;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_comment);
    postImage = (ImageView) findViewById(R.id.postImage);
    commentListView = (ListView) findViewById(R.id.commentListView);
    sendCommentButton = (ImageButton) findViewById(R.id.sendCommentButton);
    commentText = (EditText) findViewById(R.id.commentText);
    commentText.setTextColor(Color.WHITE);
    firebaseDatabase = FirebaseDatabase.getInstance();
    databaseCommentsRef = firebaseDatabase.getReference("Comments");
    databaseUsersRef = firebaseDatabase.getReference("Users");
    currentUser = FirebaseAuth.getInstance().getCurrentUser();
    Intent i = getIntent();
    postId = i.getStringExtra("post_id");
    commentsMap = new LinkedHashMap<>();


    // post resmini getiriyor
    Query imageRef = FirebaseDatabase.getInstance().getReference("Icerik").orderByKey().equalTo(postId);
    imageRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        for(DataSnapshot icerik : dataSnapshot.getChildren()){
          System.out.println("INFO ICERIK >>>>> " + icerik.getValue());
          HashMap<String,String> hashMap = (HashMap<String, String>) icerik.getValue();
          path_to_image = hashMap.get("post_image");
          System.out.println("PATH TO IMAGE >>>>>>>>>>>> " + path_to_image);
          Glide.with(getApplicationContext()).load(path_to_image).into(postImage);
          tokenUser = hashMap.get("token");
          System.out.println("****************************** " + tokenUser);
        }
      }



      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });

    // userıd'den username çekiyor
    Query ref = FirebaseDatabase.getInstance().getReference("Users").orderByKey().equalTo(currentUser.getUid());
    ref.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        for(DataSnapshot user : dataSnapshot.getChildren()){
          System.out.println("*******************************USERINFO>>"+user.getValue());
          HashMap<String,String> hashMap = (HashMap<String, String>) user.getValue();
          username = hashMap.get("username");
          System.out.println("****************************** " + username);
        }
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {

      }
    });


    //Query comments1 = FirebaseDatabase.getInstance().getReference("Comments").orderByChild("postId").equalTo(postId);
    Query comments1 = FirebaseDatabase.getInstance().getReference("Icerik").child(postId).child("Comments").orderByChild("commentDate/time");
    //listviewi oluştur
  FirebaseListAdapter<CommentModel> firebaseListAdapter = new FirebaseListAdapter<CommentModel>(this,CommentModel.class,android.R.layout.simple_list_item_2,comments1) {
      @SuppressLint("WrongConstant")
      @Override
      protected void populateView(View v, CommentModel model, int position) {
        TextView textView1 = (TextView) v.findViewById(android.R.id.text1);
        TextView textView2 = (TextView) v.findViewById(android.R.id.text2);
        textView2.setTextColor(Color.RED);
        textView2.setTextAlignment(3);
        textView2.setTextSize(12);
        textView1.setTextColor(Color.WHITE);
        textView1.setTypeface(Typeface.SANS_SERIF);
        textView1.setText(model.getCommentText());
        textView2.setText(model.getCommentUsername());
      }
    };
    commentListView.setAdapter(firebaseListAdapter);

  }

  //TODO sıralı hale getirilmesi gerek.
  public void sendComment(View view){
        comment = commentText.getText().toString();
        String userId = currentUser.getUid();
        Date currentTime = Calendar.getInstance().getTime();
        UUID commentId = UUID.randomUUID();
        CommentModel model = new CommentModel(comment, currentTime,userId,commentId.toString(),username,postId);
        DatabaseReference comRef = firebaseDatabase.getInstance().getReference("Icerik").child(postId).child("Comments");
        comRef.child(model.getCommentId()).setValue(model);
        new Send().execute();
        commentText.getText().clear();


  }



  class Send extends AsyncTask<String, Void,Long > {



    protected Long doInBackground(String... urls) {


      HttpClient httpclient = new DefaultHttpClient();
      HttpPost httppost = new HttpPost("http://aydinserhatsezen.com/fcm/LostP/lpyorum.php");

      try {
        // Add your data
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
        nameValuePairs.add(new BasicNameValuePair("tokendevice", tokenUser));
        nameValuePairs.add(new BasicNameValuePair("cevap", comment));
        nameValuePairs.add(new BasicNameValuePair("userName", username));
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
