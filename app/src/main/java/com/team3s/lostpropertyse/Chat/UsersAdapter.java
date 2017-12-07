package com.team3s.lostpropertyse.Chat;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.team3s.lostpropertyse.R;
import com.team3s.lostpropertyse.Utils.CircleTransform;

import java.util.ArrayList;

/**
 * Created by serhat on 07/12/2017.
 */

public class UsersAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private ArrayList<Users> usersArrayList;
    Context context;
    public UsersAdapter(Context context, ArrayList<Users> usersArrayList) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.usersArrayList = usersArrayList;

    }

    @Override
    public int getCount() {
        return usersArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return usersArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = mInflater.inflate(R.layout.dmlist_row, null);
        TextView usernameTxt = (TextView) convertView.findViewById(R.id.list_row_textview_isim);
        TextView userUidTxt = (TextView) convertView.findViewById(R.id.list_row_textview_sure);
        ImageView userProfileImage = (ImageView) convertView.findViewById(R.id.list_row_imageview_padisah);
        Users users = usersArrayList.get(position);
        usernameTxt.setText(users.getIsim());
        userUidTxt.setText(users.getSure());
       // userProfileImage.setImageResource(users.getResimId());
        Glide.with(context)
                .load(users.getResimId())
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(new CircleTransform(context))
                .into(userProfileImage);
        return convertView;
    }

}