package com.github.astronoodles.crowncatch2;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.github.astronoodles.crowncatch2.cards.NewsRecyclerFragment;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabLayout tabs = findViewById(R.id.tabLayout);
        tabs.addOnTabSelectedListener(this);

        MapActivity2.getPermissionAccess(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION});


        // Make sure that the leaderboard option is not available until you register
        SharedPreferences.Editor editor = getSharedPreferences(MapActivity2.PREFERENCE_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(PointsFragment.LEADERBOARD_AVAILABLE_KEY, false);
        editor.commit();

        FragmentTransaction toIntro = getSupportFragmentManager().beginTransaction();
        toIntro.replace(R.id.main_fragment, new IntroFragment());
        toIntro.commit();

        System.out.println("width: " + Resources.getSystem().getDisplayMetrics().widthPixels);
        System.out.println("height" + Resources.getSystem().getDisplayMetrics().heightPixels);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        FragmentTransaction transact = getSupportFragmentManager().beginTransaction();
        Fragment f;
        if(tab.getText().equals(getString(R.string.intro))){
            f = new IntroFragment();
        } else if(tab.getText().equals(getString(R.string.map))) {
            Intent i = new Intent(this, MapActivity2.class);
            startActivity(i);
            return;
        } else if(tab.getText().equals(getString(R.string.points))){
            f = new PointsFragment();
        } else if(tab.getText().equals(getString(R.string.news))){
          f = new NewsRecyclerFragment();
        } else if(tab.getText().equals(getString(R.string.connect)) || tab.getText().equals(getString(R.string.find))){
          f = new BluetoothFragment();
        } else {
            f = new IntroFragment();
        }
        transact.replace(R.id.main_fragment, f);
        transact.commit();
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) { }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) { }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.crowncatchmenu, menu);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 1){
            MapActivity2.handlePermissions(this, permissions, grantResults);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public static void selectMenuOptions(final Context c, MenuItem item){
        switch(item.getItemId()) {
            case R.id.findFood:
                Intent i = new Intent(c, RestaurantActivity.class);
                c.startActivity(i);
                break;
            case R.id.leaderboard:
                if (c.getSharedPreferences(MapActivity2.PREFERENCE_NAME, MODE_PRIVATE).
                        getBoolean(PointsFragment.LEADERBOARD_AVAILABLE_KEY, false)) {
                    Intent in = new Intent(c, LeaderboardActivity.class);
                    c.startActivity(in);
                } else {
                    new AlertDialog.Builder(c)
                            .setTitle("Register For The Leaderboard!")
                            .setMessage("Make sure to register for the leaderboard before entering it!")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).create().show();
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        selectMenuOptions(this, item);
        return true;
    }
    }
