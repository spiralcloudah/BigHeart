package com.codepath.bigheartapp.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.bigheartapp.EndlessRecyclerViewScrollListener;
import com.codepath.bigheartapp.MainActivity;

import com.codepath.bigheartapp.ProfilePagesAdapter;

import com.codepath.bigheartapp.PostDetailsActivity;

import com.codepath.bigheartapp.R;
import com.codepath.bigheartapp.helpers.FragmentHelper;
import com.codepath.bigheartapp.model.Post;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;




public class ProfileFragment extends Fragment implements FragmentHelper.BaseFragment {

    // Store variables to use in the event fragment

    private EndlessRecyclerViewScrollListener scrollListener;
    private SwipeRefreshLayout swipeContainer;
    Button logoutBtn;
    ImageView ivCurrentProfile;
    TextView tvCurrentUser;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    int whichFragment=1;

    public final static int PICK_PHOTO_CODE = 1046;

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
    File photoFile;
    String currentPath;
    ParseFile parseFile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        //viewPager = container.findViewById(R.id.viewpager);
        // tabLayout = container.findViewById(R.id.tabLayout);
        // profileAdapter = new ProfilePagesAdapter(getFragmentManager());

        //profileAdapter.AddFragment(new NestedPostsFragment (), "Posts ");
       // profileAdapter.AddFragment(new NestedBookmarksFragment (), "Bookmarks");

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // Sets variables to corresponding xml layouts
        logoutBtn = view.findViewById(R.id.btnLogout);
        ivCurrentProfile = view.findViewById(R.id.ivCurrentProfile);

        // Get the current user's username and display it
        final String currentUser = ParseUser.getCurrentUser().getUsername();
        System.out.println("The current user is "+ currentUser);
        tvCurrentUser = (TextView) view.findViewById(R.id.tvCurrentUser);
        tvCurrentUser.setText(currentUser);

        // Set the profile picture for the current user
        ParseFile p = ParseUser.getCurrentUser().getParseFile("profilePicture");
        if(p != null) {
            Glide.with(getContext())
                    .load(p.getUrl())

                    // Profile picture will be shown with a circle border rather than a rectangle
                    .bitmapTransform(new CropCircleTransformation(getContext()))
                    .into(ivCurrentProfile);
        }



        tabLayout = view.findViewById(R.id.tabLayout);

        // gets view pager and sets its PageAdapter so it can display items
        viewPager = view.findViewById(R.id.vpContainer);
        viewPager.setAdapter(new ProfilePagesAdapter(getChildFragmentManager(), getContext()));

        //give TabLayout the ViewPager
        tabLayout.setupWithViewPager(viewPager);


        // Set the onClickListener for the logout button
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) { logoutUser(v); }
        });
//        ivCurrentProfile.setOnClickListener(new View.OnClickListener() {
        //           @Override
        //           public void onClick(View v) {
        //               Activity activity = (Activity) ProfileFragment.this.getActivity();

        //               Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //               File mediaStorage = null;
        //               try {
        //                  mediaStorage = getTempImageFile(getContext());
        //              } catch (IOException e) {
        ////                  e.printStackTrace();
        //              }
        //              // Create the storage directory if it does not exist
        //             if (!mediaStorage.exists() && !mediaStorage.mkdirs()){
        //                  Log.d(APP_TAG, "failed to create directory");
        //              }

        //             String path = mediaStorage.getAbsolutePath();
        //             Uri uri = FileProvider.getUriForFile(activity, "com.codepath.parsetagram", mediaStorage);
        //             intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

        //             photoFile = new File(path);
        //
        //            startActivityForResult(intent,
        //                     CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        //         }
        //     });

//        posts = new ArrayList<>();
//
//        adapter = new PostAdapter(posts, whichFragment);
//        //rvPostView = (RecyclerView) view.findViewById(R.id.rvUserPosts);
//
//        rvPostView.setAdapter(adapter);
//
//        // Configure the RecyclerView
//
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
//
//        rvPostView.setLayoutManager(linearLayoutManager);
//
//        // Retain an instance so that you can call `resetState()` for fresh searches
//        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
//            @Override
//            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
//
//                view.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        // should have something to load more posts...
//                    }
//                });
//            }
//        };
//        // Adds the scroll listener to RecyclerView
//        rvPostView.addOnScrollListener(scrollListener);


//        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
//        // Setup refresh listener which triggers new data loading
//        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                // Your code here
//                Toast.makeText(getApplicationContext(), "Refreshed!", Toast.LENGTH_LONG).show();
//                // To keep animation for 4 seconds
//                posts.clear();
//                adapter.clear();
//                loadTopPosts();
//
//            }
//        });

//        // Scheme colors for animation
//        swipeContainer.setColorSchemeColors(
//                getResources().getColor(android.R.color.holo_blue_bright),
//                getResources().getColor(android.R.color.holo_green_light),
//                getResources().getColor(android.R.color.holo_orange_light),
//                getResources().getColor(android.R.color.holo_red_light)
//        );


//        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//
//                switch (tab.getPosition()) {
//                    case 0:
//                    //    loadTopPosts();
//                        break;
//                    case 1:
//                    //    loadBookmarkedEvents();
//                        break;
//
//
//                }
//
//
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//
//            }
//        });

       // loadTopPosts();

    }


    // Function to logout the current user
    public void logoutUser(View view) {
        Toast.makeText(getContext(), ParseUser.getCurrentUser().getUsername() + " is now logged out", Toast.LENGTH_LONG).show();
        ParseUser.logOut();
        ParseUser currentUser = null; // this will now be null
        // System.out.println("The current user is "+ currentUser);

        // Change activities back to the login screen
        Intent i = new Intent(getContext(), MainActivity.class);
        startActivity(i);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

                // Set the current user's profile picture
                currentPath = photoFile.getPath();
                Bitmap bitmap = BitmapFactory.decodeFile(currentPath);
                ivCurrentProfile.setImageBitmap(bitmap);
                final BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
                ivCurrentProfile.setBackgroundDrawable(ob);
                parseFile = new ParseFile(photoFile);
                ParseUser.getCurrentUser().put("profilePic", parseFile);
                ParseUser.getCurrentUser().saveInBackground();
            }
        }
    }

    public void loadTopPosts(){
        adapter.clear();
        FragmentHelper fragmentHelper = new FragmentHelper(getPostQuery());
        fragmentHelper.fetchPosts(this);
        swipeContainer.setRefreshing(false);
    }

    @Override
    public Post.Query getPostQuery() {
        final Post.Query postQuery = new Post.Query();
        postQuery.getTop().withUser();
        // Only load the current user's posts
        postQuery.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());
        return postQuery;
    }


    @Override
    public void onFetchSuccess(List<Post> objects, int i) {
        posts.add(objects.get(i));
        adapter.notifyItemInserted(posts.size() - 1);
    }



    @Override
    public void onFetchFailure() {
        Toast.makeText(getContext(), "Failed to query posts", Toast.LENGTH_LONG).show();
        swipeContainer.setRefreshing(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Register for the particular broadcast based on ACTION string
        IntentFilter filter = new IntentFilter(PostDetailsActivity.ACTION);
        getActivity().registerReceiver(detailsChangedReceiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Unregister the listener when the application is paused
        getActivity().unregisterReceiver(detailsChangedReceiver);
    }

}