package com.team3s.lostpropertyse.MainPage;

import android.Manifest.permission;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.team3s.lostpropertyse.Chat.UsersDMList;
import com.team3s.lostpropertyse.Maps.PropMaps;
import com.team3s.lostpropertyse.Profile.UsersProfiFrag;
import com.team3s.lostpropertyse.R;
import com.team3s.lostpropertyse.ShareSc.ShareActivity;
import com.team3s.lostpropertyse.services.MyService;
import java.util.ArrayList;
import java.util.List;

public class BottomBarActivity extends AppCompatActivity {

    private static final String TAG_FRAGMENT_NEWS = "tag_frag_news";
    private static final String TAG_FRAGMENT_SHARE = "tag_frag_share";
    private static final String TAG_FRAGMENT_PROFILE = "tag_frag_profile";
    private static final String TAG_FRAGMENT_DM_LIST = "tag_frag_dm_list";

    Context ctx;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private static final String PREFS = "MyPrefs";


    private BottomNavigationView bottomNavigationView;

    /**
     * Maintains a list of Fragments for {@link BottomNavigationView}
     */
    private List<MainPage> fragments = new ArrayList<>(1);
    private List<UsersProfiFrag> fragmentsPro = new ArrayList<>(1);
    private List<UsersDMList> fragmentsDmList = new ArrayList<>(1);

    private FirebaseAuth auth;
    private String currentUserId;

    public String usersdmlist;
    public String googlesign;


    @Override
    protected void onStart() {
        checkPermissions();
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_bar);

        Intent servIntent = new Intent(BottomBarActivity.this, MyService.class);
        startService(servIntent);

        Intent pages = getIntent();
        usersdmlist = pages.getStringExtra("usersDMlist");

        auth = FirebaseAuth.getInstance();

        currentUserId = auth.getCurrentUser().getUid();
        preferences = getSharedPreferences(PREFS, Context.MODE_PRIVATE);


        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_nav);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_bottombar_main:
                                switchFragment(0, TAG_FRAGMENT_NEWS);
                                return true;
                            case R.id.action_bottombar_maps:
                                Intent mapint = new Intent(BottomBarActivity.this,PropMaps.class);
                                mapint.putExtra("post_id", "");
                                startActivity(mapint);
                                return true;
                            case R.id.action_bottombar_share:
                                Intent shareint = new Intent(BottomBarActivity.this,ShareActivity.class);
                                startActivity(shareint);
                                return true;
                            case R.id.action_bottombar_profil:
                                switchFragment2(0, TAG_FRAGMENT_PROFILE);
                                return true;
                        }
                        return false;
                    }
                });
        preferences = getSharedPreferences(PREFS,0);
        editor = preferences.edit();

        googlesign = preferences.getString("GoogleSign","");

        if(googlesign.equals("")){
            editor.putString("USERKEY_SHARED", currentUserId);

        }else if(googlesign.equals("googlesign")){
            askLocation();

        }

        buildFragmentsList();

        // Set the 0th Fragment to be displayed by default.
        switchFragment(0, TAG_FRAGMENT_NEWS);

    }

    private void askLocation() {
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }

    }
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Konumu açıp yerinizi güncellemeniz sizin için daha faydalı olacaktır.")
                .setCancelable(false)
                .setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        editor.putString("GoogleSign", "");
                        editor.putString("USERKEY_SHARED", currentUserId);
                        editor.commit();
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("İptal", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        editor.putString("GoogleSign", "");
                        editor.putString("USERKEY_SHARED", currentUserId);
                        editor.commit();
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    private void switchFragment(int pos, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.group, fragments.get(pos), "tag_frag_main")
                .addToBackStack("addmain")
                .commit();
        editor.putString("USERKEY_SHARED", currentUserId);
        editor.commit();
    }
    private void switchFragment2(int pos, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.group, fragmentsPro.get(pos), "tag_frag_profile")
                .addToBackStack("addprof")
                .commit();
        editor.putString("USERKEY_SHARED", currentUserId);
        editor.commit();
    }
    private void switchFragment3(int pos, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_fragmentholder, fragmentsDmList.get(pos), tag)
                .addToBackStack(null)
                .commit();
    }
    private void buildFragmentsList() {
        MainPage mainScreen = buildFragment();
        UsersProfiFrag profileScreen = buildFragmentt();
        UsersDMList dmlistScreen = buildFragmentDmList();

        fragments.add(mainScreen);
        fragmentsPro.add(profileScreen);
        fragmentsDmList.add(dmlistScreen);

    }


    private MainPage buildFragment() {
        MainPage fragment = new MainPage();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }


    private UsersProfiFrag buildFragmentt() {
        UsersProfiFrag fragment = new UsersProfiFrag();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }
    private UsersDMList buildFragmentDmList() {
        UsersDMList fragment = new UsersDMList();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {     //backpress
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
                super.onBackPressed();
            } else {
                moveTaskToBack(false);

            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    // API 23 ve üzeri için gerekli.
    public void checkPermissions(){
        ActivityCompat.requestPermissions(this, new String[] {
            permission.ACCESS_COARSE_LOCATION,
            permission.ACCESS_FINE_LOCATION,
            permission.WRITE_EXTERNAL_STORAGE}, 1
        );
    }
}