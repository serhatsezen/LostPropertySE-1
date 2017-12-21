package com.team3s.lostpropertyse.MainPage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.team3s.lostpropertyse.LoginSign.TabsHeaderActivity;
import com.team3s.lostpropertyse.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CategorySelect extends Fragment {


    private GridView categorylist;
    public Query mQueryUserFindCat,mQueryUserLostCat;
    public DatabaseReference mDatabaseFindCat,mDatabaseLostCat;
    public FirebaseAuth.AuthStateListener authListener;
    public FirebaseAuth auth;
    public FirebaseUser user;
    public DatabaseReference mDatabaseUsers;
    public String currentUserId;
    public String str;
    private SharedPreferences sharedpreferences;
    public static String themeStr;
    private static AppBarLayout appBarLayout;

    final ArrayList<String> categorynames = new ArrayList<String>();
    final ArrayList<Category> categoriesInformation = new ArrayList<Category>();
    CategoryAdapter categoryAdapter;
    Typeface tf1;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private static final String PREFS = "MyPrefs";

    public TextView selectedCategory;
    public TextView toolbarText;
    public TextView categoryAll;
    public TextView txtategory;
    public LinearLayout gridLinear;
    public String category;
    public CategorySelect() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_category_select, container, false);

        categorylist = (GridView) v.findViewById(R.id.categorylist);
        selectedCategory = (TextView) v.findViewById(R.id.selectedCategory);
        categoryAll = (TextView) v.findViewById(R.id.categoryAll);
        toolbarText = (TextView) v.findViewById(R.id.textView);
        txtategory = (TextView) v.findViewById(R.id.txtategory);
        appBarLayout = (AppBarLayout) v.findViewById(R.id.kategoriBarLayout);
        gridLinear = (LinearLayout) v.findViewById(R.id.gridLinear);

        Typeface type = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/Ubuntu-B.ttf");
        toolbarText.setTypeface(type);

        categoryAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString("categoryShared", "Hepsi");
                editor.commit();
                selectedCategory.setText("Hepsi");


                Intent refresh = new Intent(getActivity(), BottomBarActivity.class);
                startActivity(refresh);//Start the same Activity

            }
        });
        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        sharedpreferences = getActivity().getSharedPreferences(PREFS,0);
        themeStr = sharedpreferences.getString("theme", "DayTheme");          //eğer null ise DayTheme
        category = sharedpreferences.getString("categoryShared", "Hepsi");          //eğer null ise DayTheme
        selectedCategory.setText(category);


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

        mDatabaseFindCat = FirebaseDatabase.getInstance().getReference().child("Icerik").child("Bulunanlar");
        mDatabaseFindCat.keepSynced(true);
        mDatabaseLostCat = FirebaseDatabase.getInstance().getReference().child("Icerik").child("Kayiplar");
        mDatabaseLostCat.keepSynced(true);

        currentUserId = auth.getCurrentUser().getUid();
        mQueryUserFindCat = mDatabaseFindCat.orderByChild("category");
        mQueryUserLostCat = mDatabaseLostCat.orderByChild("category");

        new CategoryGet().execute();

        if(themeStr.equals("NightTheme")){
            appBarLayout.setBackgroundColor(Color.parseColor("#142629"));
            toolbarText.setTextColor(Color.parseColor("#BDC7C1"));
            gridLinear.setBackgroundColor(Color.parseColor("#1a2f40"));
            selectedCategory.setTextColor(Color.parseColor("#BDC7C1"));
            txtategory.setTextColor(Color.parseColor("#BDC7C1"));


        }else if(themeStr.equals("DayTheme")){
            appBarLayout.setBackgroundColor(Color.parseColor("#EEEEEE"));
            toolbarText.setTextColor(Color.parseColor("#000000"));
            gridLinear.setBackgroundColor(Color.parseColor("#9E9E9E"));
            selectedCategory.setTextColor(Color.parseColor("#000000"));
            txtategory.setTextColor(Color.parseColor("#000000"));

        }




        return v;
    }
    private class CategoryGet extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
            mQueryUserLostCat.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        try {
                            String names = data.child("category").getValue().toString();
                            if(!categorynames.contains(names)){
                                categoriesInformation.add(new Category(names));     //custom listview e bu bilgileri koyuyoruz,
                                categorynames.add(names);
                            }
                        }catch (Exception e){

                        }

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            mQueryUserFindCat.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        try {
                            String names = data.child("category").getValue().toString();
                            if(!categorynames.contains(names)){
                                categoriesInformation.add(new Category(names));     //custom listview e bu bilgileri koyuyoruz,
                                categorynames.add(names);
                            }
                        }catch (Exception e){

                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

                categoryAdapter = new CategoryAdapter(getActivity(), categoriesInformation);
                categorylist.setAdapter(categoryAdapter);
            }catch (Exception e){

            }
            categorylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    TextView textViewUid = (TextView) view.findViewById(R.id.categoryName);
                    String textuid = textViewUid.getText().toString();
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString("categoryShared", textuid);
                    editor.commit();
                    selectedCategory.setText(textuid);
                    Typeface type = Typeface.createFromAsset(getActivity().getAssets(),
                            "fonts/Ubuntu-B.ttf");
                    selectedCategory.setTypeface(type);

                    Intent refresh = new Intent(getActivity(), BottomBarActivity.class);
                    startActivity(refresh);//Start the same Activity

                }
            });
            return null;

        }
    }
    public void onStart(){
        super.onStart();


    }


    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();

    }


}
