package com.codepath.bigheartapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.codepath.bigheartapp.Fragments.HomeFragment;
import com.codepath.bigheartapp.Fragments.MapsFragment;
import com.codepath.bigheartapp.Fragments.ProfileFragment;
import com.codepath.bigheartapp.Fragments.EventFragment;
import com.codepath.bigheartapp.model.Post;

import java.io.Serializable;

public class HomeActivity extends AppCompatActivity {

    public HomeFragment homeFragment;
    public MapsFragment mapsFragment;
    public ProfileFragment profileFragment;
    public EventFragment searchFragment;
    private final int REQUEST_CODE = 20;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    public void toCompose(MenuItem menuItem) {
        Intent toCompose = new Intent(HomeActivity.this, ComposeActivity.class);
        startActivityForResult(toCompose, REQUEST_CODE);
    }

    public void showDetailsFor(Serializable post) {
        // create intent for the new activity
        Intent intent = new Intent(this, PostDetailsActivity.class);
        // serialize the post using parceler, use its short name as a key
        intent.putExtra(Post.class.getSimpleName(), (Serializable) post);
        // show the activity
        startActivityForResult(intent,123);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.tbMain);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);


        homeFragment = new HomeFragment();
        mapsFragment = new MapsFragment();
        profileFragment = new ProfileFragment();
        searchFragment = new EventFragment();

        setSupportActionBar((Toolbar) findViewById(R.id.tbMain));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        final FragmentManager fragmentManager = getSupportFragmentManager();

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        // handle navigation selection
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment fragment;
                        switch (item.getItemId()) {
                            case R.id.miHome:
                                fragment = homeFragment;
                                break;
                            case R.id.miMaps:
                                fragment = mapsFragment;
                                break;
                            case R.id.miProfile:
                                fragment = profileFragment;
                                break;
                            case R.id.miSearch:
                                fragment = searchFragment;
                                break;

                                default:
                                fragment = homeFragment;
                                break;
                        }
                        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                        return true;
                    }
                });
        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.miHome);
    }

}
