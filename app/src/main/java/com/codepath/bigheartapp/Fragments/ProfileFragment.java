package com.codepath.bigheartapp.Fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.bigheartapp.ComposeActivity;
import com.codepath.bigheartapp.EndlessRecyclerViewScrollListener;
import com.codepath.bigheartapp.MainActivity;
import com.codepath.bigheartapp.PostAdapter;
import com.codepath.bigheartapp.PostDetailsActivity;
import com.codepath.bigheartapp.R;
import com.codepath.bigheartapp.model.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    // Store variables to use in the event fragment
    private EndlessRecyclerViewScrollListener scrollListener;
    private SwipeRefreshLayout swipeContainer;
    Button logoutBtn;
    ImageView ivCurrentProfile;
    TextView tvCurrentUser;
    TabLayout tabLayout;
    int whichFragment = 1;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
    File photoFile;
    String currentPath;
    ParseFile parseFile;
    ArrayList<Post> posts;
    public RecyclerView rvPostView;
    PostAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Register for the particular broadcast based on ACTION string
        IntentFilter filter = new IntentFilter(PostDetailsActivity.ACTION);
        getActivity().registerReceiver(detailsChangedReceiver, filter);

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
        tabLayout = view.findViewById(R.id.tabLayout);
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

        // Only current user's posts are shown in profile fragment
        rvPostView= view.findViewById(R.id.rvUserPosts);

        // Set the onClickListener for the logout button
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { logoutUser(v); }
        });

        // Set variables to current user's stats
        posts = new ArrayList<>();
        adapter = new PostAdapter(posts, whichFragment);
        rvPostView = (RecyclerView) view.findViewById(R.id.rvUserPosts);
        rvPostView.setAdapter(adapter);

        // Configure the RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvPostView.setLayoutManager(linearLayoutManager);

        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                view.post(new Runnable() {
                    @Override
                    public void run() {}
                });
            }
        };

        // Adds the scroll listener and item decoration to RecyclerView
        rvPostView.addOnScrollListener(scrollListener);
        rvPostView.addItemDecoration(new ProfileFragment.VerticalSpaceItemDecoration(12));
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                // To keep animation for 4 seconds
                posts.clear();
                adapter.clear();
                loadTopPosts();
            }
        });

        // Scheme colors for animation
        swipeContainer.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light)
        );

        // Set tab layout to switch between user posts and bookmarked events
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        loadTopPosts();
        getConfiguration();
    }

    public void getConfiguration() {}

    // Function to logout the current user
    public void logoutUser(View view) {
        Toast.makeText(getContext(), ParseUser.getCurrentUser().getUsername() + " is now logged out", Toast.LENGTH_LONG).show();
        ParseUser.logOut();

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
        final Post.Query postsQuery = new Post.Query();
        postsQuery
                .getTop()
                .withUser();

        // Only load the current user's posts
        postsQuery.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());
        postsQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e == null){
                    Post post = new Post();
                    System.out.println("Success!");
                    for (int i = 0;i<objects.size(); i++){
                        try {
                            Log.d("FeedActivity", "Post ["+i+"] = "
                                    + objects.get(i).getDescription()
                                    + "\n username = " + objects.get(i).getUser().fetchIfNeeded().getUsername()
                                    + " o k ");
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                        posts.add(0,objects.get(i));
                        adapter.notifyItemInserted(posts.size() - 1);
                    }
                } else {
                    e.printStackTrace();
                }
                swipeContainer.setRefreshing(false);
            }
        });
    }

    //put space between cardviews
    public class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {

        // Specify a final variable for space between cardviews
        private final int verticalSpaceHeight;

        // function to set the space height
        public VerticalSpaceItemDecoration(int verticalSpaceHeight) {
            this.verticalSpaceHeight = verticalSpaceHeight;
        }
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            outRect.top = verticalSpaceHeight;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the listener when the application is paused
        getActivity().unregisterReceiver(detailsChangedReceiver);
    }

    // Define the callback for what to do when data is received
    private BroadcastReceiver detailsChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int resultCode = intent.getIntExtra(getString(R.string.result_code), RESULT_CANCELED);

            if (resultCode == RESULT_OK) {

                Post postChanged = (Post) intent.getSerializableExtra(Post.class.getSimpleName());
                int indexOfChange = -1;
                for(int i = 0; i < posts.size(); i++) {
                    if(posts.get(i).hasSameId(postChanged)) {
                        indexOfChange = i;
                        break;
                    }
                }
                if(indexOfChange != -1) {
                    posts.set(indexOfChange, postChanged);
                    adapter.notifyItemChanged(indexOfChange);
                } else {
                    Toast.makeText(getContext(), "An error occurred", Toast.LENGTH_LONG).show();
                }

            }
        }
    };
}