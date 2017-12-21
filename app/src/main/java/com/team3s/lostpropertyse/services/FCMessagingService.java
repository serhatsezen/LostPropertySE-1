package com.team3s.lostpropertyse.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.team3s.lostpropertyse.Chat.DmMessage;
import com.team3s.lostpropertyse.Chat.CommentAct;
import com.team3s.lostpropertyse.Post.PostDetailAct;
import com.team3s.lostpropertyse.R;

public class FCMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = "FirebaseMessagingServic";
    String sender_name;
    String receiver_name;
    String message;
    String title;
    String click_action;
    String postKey;
    String postType;


    private String PREFS = "MyPrefs" ;
    SharedPreferences mPrefs;


    public FCMessagingService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            click_action = remoteMessage.getData().get("click_action"); //get clickaction

            if (click_action.equals("Chat")) {                          //-------------------------------------------------------------------Get information for DM from php
                sender_name = remoteMessage.getData().get("sender_name");
                receiver_name = remoteMessage.getData().get("receiver_name"); //get message
                message = remoteMessage.getData().get("msg"); //get message
                title = remoteMessage.getData().get("title"); //get title
                System.out.print("-**********************-////////////////*****************/*/*/*/*///////////-------------"+sender_name);
                sendNotification(title, message, sender_name,receiver_name,click_action);
            }else if (click_action.equals("Comment")) {                 //-------------------------------------------------------------------Get information for Comment from php

                message = remoteMessage.getData().get("msg"); //get message
                title = remoteMessage.getData().get("title"); //get title
                postKey = remoteMessage.getData().get("post_key"); //get message
                postType = remoteMessage.getData().get("post_type"); //get title
                sendNotification(title, message, postKey,postType,click_action);
            }else if (click_action.equals("NewPost")) {                 //-------------------------------------------------------------------Get information for NewPost from php
                message = remoteMessage.getData().get("msg"); //get message
                title = remoteMessage.getData().get("title"); //get title
                postKey = remoteMessage.getData().get("post_key"); //get message
                postType = remoteMessage.getData().get("post_type"); //get title
                sendNotification(title, message, postKey,postType,click_action);
            }

        }
    }

    @Override
    public void onDeletedMessages() {

    }

    private void sendNotification(String title,String message, String sender_name, String receiver_name, String click_action) {
        Intent intent = null;
        PendingIntent pendingIntent = null;
        if (click_action.equals("Chat")) {
            intent = new Intent(this, DmMessage.class);
            intent.putExtra("receiver_name",sender_name);
            intent.putExtra("title",title);

        }
        if (click_action.equals("Comment")) {
            intent = new Intent(this, CommentAct.class);
            intent.putExtra("post_key",sender_name);
            intent.putExtra("post_type",receiver_name);
        }
        if(click_action.equals("NewPost")) {
            intent = new Intent(this, PostDetailAct.class);
            intent.putExtra("post_key",sender_name);
            intent.putExtra("post_type",receiver_name);
        }
        pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.ic_launcher);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = null;
        notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(bitmap)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
