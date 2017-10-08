package com.team3s.lostpropertyse.MainPage;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;


import com.team3s.lostpropertyse.Maps.PropMaps;
import com.team3s.lostpropertyse.R;
import com.team3s.lostpropertyse.services.MyService;

import java.util.ArrayList;
import java.util.List;

public class BottomBarActivity extends AppCompatActivity {

    private static final String TAG_FRAGMENT_NEWS = "tag_frag_news";
    private static final String TAG_FRAGMENT_SHARE = "tag_frag_share";

    Context ctx;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private static final String PREFS = "prefs";


    private BottomNavigationView bottomNavigationView;

    /**
     * Maintains a list of Fragments for {@link BottomNavigationView}
     */
    private List<MainPage> fragments = new ArrayList<>(1);
    private List<ShareFragment> fragmentsShare = new ArrayList<>(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_bar);


        Intent servIntent = new Intent(BottomBarActivity.this, MyService.class);
        startService(servIntent);

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
                                startActivity(mapint);
                                return true;
                            case R.id.action_bottombar_share:
                                switchFragment1(0, TAG_FRAGMENT_SHARE);
                                return true;
                        }
                        return false;
                    }
                });
        preferences = getSharedPreferences(PREFS,0);
        editor = preferences.edit();


        buildFragmentsList();

        // Set the 0th Fragment to be displayed by default.
        switchFragment(0, TAG_FRAGMENT_NEWS);

    }

    private void switchFragment(int pos, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_fragmentholder, fragments.get(pos), tag)
                .commit();
    }
    private void switchFragment1(int pos, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_fragmentholder, fragmentsShare.get(pos), tag)
                .commit();
    }


    private void buildFragmentsList() {
        MainPage mainScreen = buildFragment();
        ShareFragment shareScreen = buildFragmentShare();

        fragments.add(mainScreen);
        fragmentsShare.add(shareScreen);

    }


    private MainPage buildFragment() {
        MainPage fragment = new MainPage();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    private ShareFragment buildFragmentShare() {
        ShareFragment fragment = new ShareFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}