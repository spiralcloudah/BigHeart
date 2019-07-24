package com.codepath.bigheartapp.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.codepath.bigheartapp.EndlessRecyclerViewScrollListener;
import com.codepath.bigheartapp.MainActivity;
import com.codepath.bigheartapp.PostAdapter;
import com.codepath.bigheartapp.R;
import com.codepath.bigheartapp.model.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static com.parse.Parse.getApplicationContext;

public class EventFragment extends Fragment {
    // Store a member variable for the listener
    private EndlessRecyclerViewScrollListener scrollListener;
    private SwipeRefreshLayout swipeContainer;
    ImageView ivCurrentProfile;
    TextView tvCurrentUser;
    int whichFragment = 1;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
    public final String APP_TAG = "MyCustomApp";
    File photoFile;

    String currentPath;
    ParseFile parseFile;

    ArrayList<Post> posts;
    public RecyclerView rvPostView;
    PostAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ivCurrentProfile = view.findViewById(R.id.ivCurrentProfile);
        String currentUser = ParseUser.getCurrentUser().getUsername(); // this will now be null
        System.out.println("The current user is "+ currentUser);
        tvCurrentUser = (TextView) view.findViewById(R.id.tvCurrentUser);
        tvCurrentUser.setText(currentUser);


        ParseFile p = ParseUser.getCurrentUser().getParseFile("profilePicture");
        if(p != null) {
            Glide.with(getContext())
                    .load(p.getUrl())
                    .bitmapTransform(new CropCircleTransformation(getContext()))
                    .into(ivCurrentProfile);
        }

        rvPostView= view.findViewById(R.id.rvUserPosts);

        loadTopPosts();
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
                    public void run() {
                        //not quite sure if we need this?
                    }
                });
            }
        };
        // Adds the scroll listener to RecyclerView
        rvPostView.addOnScrollListener(scrollListener);


        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code here
                Toast.makeText(getApplicationContext(), "Refreshed!", Toast.LENGTH_LONG).show();
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



        loadTopPosts();


        getConfiguration();
    }

    public void getConfiguration() {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

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

        postsQuery.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());

        postsQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e==null){
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
                        adapter.notifyItemInserted(posts.size()-1);

                    }
                } else {
                    e.printStackTrace();
                }
                swipeContainer.setRefreshing(false);
            }
        });
    }


}