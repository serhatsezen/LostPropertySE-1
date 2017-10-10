package com.team3s.lostpropertyse.Chat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class CommentActivity extends AppCompatActivity {
  ListView commentListView;
  ImageButton sendCommentButton;
  EditText commentText;
  private String postId;
  private String username;
  FirebaseDatabase firebaseDatabase;
  DatabaseReference databaseCommentsRef;
  DatabaseReference databaseUsersRef;
  FirebaseUser currentUser;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_comment);
    commentListView = (ListView) findViewById(R.id.commentListView);
    sendCommentButton = (ImageButton) findViewById(R.id.sendCommentButton);
    commentText = (EditText) findViewById(R.id.commentText);
    firebaseDatabase = FirebaseDatabase.getInstance();
    databaseCommentsRef = firebaseDatabase.getReference("Comments");
    databaseUsersRef = firebaseDatabase.getReference("Users");
    currentUser = FirebaseAuth.getInstance().getCurrentUser();
    Intent i = getIntent();
    postId = i.getStringExtra("post_id");

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
    //listviewi oluştur
    FirebaseListAdapter<CommentModel> firebaseListAdapter = new FirebaseListAdapter<CommentModel>(this,CommentModel.class,android.R.layout.simple_list_item_2,databaseCommentsRef) {
      @Override
      protected void populateView(View v, CommentModel model, int position) {
        TextView textView1 = (TextView) v.findViewById(android.R.id.text1);
        TextView textView2 = (TextView) v.findViewById(android.R.id.text2);
        textView2.setTextColor(Color.RED);
        textView2.setTextSize(12);
        textView1.setText(model.getCommentText());
        textView2.setText(model.getCommentUsername());

      }
    };
    commentListView.setAdapter(firebaseListAdapter);
    //TextView t = (TextView) findViewById(R.id.textView2);
   // t.setText(id);
  }

  //TODO sıralı hale getirilmesi gerek.
  public void sendComment(View view){
        String comment = commentText.getText().toString();
        String userId = currentUser.getUid();
        Date currentTime = Calendar.getInstance().getTime();
        UUID commentId = UUID.randomUUID();
        CommentModel model = new CommentModel(comment, currentTime,userId,commentId.toString(),username);

        databaseCommentsRef.child(model.getCommentId()).setValue(model);



  }



}
