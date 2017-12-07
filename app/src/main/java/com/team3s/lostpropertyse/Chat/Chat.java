package com.team3s.lostpropertyse.Chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.team3s.lostpropertyse.R;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class Chat extends AppCompatActivity {

    private LinearLayout layout;
    private ImageView sendButton;
    private EditText messageArea;
    private ScrollView scrollView;
    private TextView dmUserNameTxt;
    private DatabaseReference reference1,reference2;
    private String receiver_name;
    private String senderName;
    private String receiver_uid;
    private String sender_uid;
    private String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        layout = (LinearLayout)findViewById(R.id.layout1);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        messageArea = (EditText)findViewById(R.id.messageArea);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        dmUserNameTxt = (TextView)findViewById(R.id.dmUserNameTxt);

        Intent uids = getIntent();
        sender_uid = uids.getStringExtra("sender_uid");
        receiver_uid = uids.getStringExtra("receiver_uid");

        mPrefs = getSharedPreferences(MyPREFERENCES,0);
        senderName = mPrefs.getString("username", "");
        receiver_name = mPrefs.getString("receiver_name","");
        receiver_name = receiver_name.replaceAll("\\s+","");
        receiver_name.toLowerCase();
        senderName = senderName.replaceAll("\\s+","");
        senderName.toLowerCase();

        dmUserNameTxt.setText(receiver_name);

        reference1 = FirebaseDatabase.getInstance().getReference().child("messages").child(senderName+"_"+receiver_name);
        reference2 = FirebaseDatabase.getInstance().getReference().child("messages").child(receiver_name+"_"+senderName);


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dmtxt = messageArea.getText().toString();
                if(!dmtxt.equals("")){
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("message", dmtxt);
                    map.put("user", senderName);
                    map.put("sender_uid", sender_uid);
                    map.put("receiver_uid", receiver_uid);
                    reference1.push().setValue(map);
                    reference1.child("senderUid").setValue(sender_uid);
                    reference1.child("receiver_uid").setValue(receiver_uid);

                    Map<String, String> maprece = new HashMap<String, String>();
                    maprece.put("message", dmtxt);
                    maprece.put("user", senderName);
                    maprece.put("sender_uid", receiver_uid);
                    maprece.put("receiver_uid", sender_uid);
                    reference2.push().setValue(maprece);
                    reference2.child("senderUid").setValue(receiver_uid);
                    reference2.child("receiver_uid").setValue(sender_uid);
                }

                messageArea.getText().clear();
            }
        });
        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String message = String.valueOf(dataSnapshot.child("message").getValue());
                String userName = String.valueOf(dataSnapshot.child("user").getValue());
                if(message != "null") {
                    if (userName.equals(senderName)) {
                        addMessageBox(/*"" +*/ message, 1);
                    } else {
                        addMessageBox(/*receiver_name + "" +*/ message, 2);
                    }
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addMessageBox(String message, int type){
        TextView textView = new TextView(Chat.this);
        textView.setText(message);

        if(type == 1) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(100, 0, 0, 10);
            lp.gravity = Gravity.RIGHT;
            textView.setLayoutParams(lp);
            textView.setBackgroundResource(R.drawable.rounded_corner1);
        }
        else{
            LinearLayout.LayoutParams lpp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lpp.setMargins(0, 0, 100, 10);
            lpp.gravity = Gravity.LEFT;
            textView.setLayoutParams(lpp);
            textView.setBackgroundResource(R.drawable.rounded_corner2);
        }
        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }
}