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

    // Create the variables for fragments and request code
    public HomeFragment homeFragment;
    public MapsFragment mapsFragment;
    public ProfileFragment profileFragment;
    public EventFragment eventFragment;
    private final int REQUEST_CODE = 20;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    public void toCompose(MenuItem menuItem) {

        // Creates a new intent switching from Home Activity to Compose Activity
        Intent toCompose = new Intent(HomeActivity.this, ComposeActivity.class);
        startActivityForResult(toCompose, REQUEST_CODE);
    }

    public void showDetailsFor(Serializable post) {

        // Create a new intent for the Post Activity
        Intent intent = new Intent(this, PostDetailsActivity.class);

        // Serialize the post using parceler; use its short name as a key
        intent.putExtra(Post.class.getSimpleName(), (Serializable) post);

        // Show the activity
        startActivityForResult(intent,123);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.tbMain);

        // Sets the Toolbar to act as the ActionBar for this Activity window
        setSupportActionBar(toolbar);

        // Creates new fragments
        homeFragment = new HomeFragment();
        mapsFragment = new MapsFragment();
        profileFragment = new ProfileFragment();
        eventFragment = new EventFragment();

        // Sets the action bar to the corresponding ID
        setSupportActionBar((Toolbar) findViewById(R.id.tbMain));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Setup the fragment manager and bottom naviagtion view
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
                                fragment = eventFragment;
                                break;

                                // default to home fragment
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