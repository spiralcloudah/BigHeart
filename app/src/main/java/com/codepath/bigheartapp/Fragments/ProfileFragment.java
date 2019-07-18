package com.codepath.bigheartapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.bigheartapp.EndlessRecyclerViewScrollListener;
import com.codepath.bigheartapp.MainActivity;
import com.codepath.bigheartapp.PostAdapter;
import com.codepath.bigheartapp.R;
import com.parse.ParseUser;

public class ProfileFragment extends Fragment {
    private Button btnLogout;
    private TextView tvUser;
    private ImageView ivProfile;
    private RecyclerView rvPosts;
    private EndlessRecyclerViewScrollListener scrollListener;
    private SwipeRefreshLayout swipeRefreshLayout;
    PostAdapter postAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_profile, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        btnLogout = (Button) view.findViewById(R.id.btnLogout);
        tvUser = (TextView) view.findViewById(R.id.tvCurrentUser);
        ivProfile = (ImageView) view.findViewById(R.id.ivCurrentProfile);

        tvUser.setText(ParseUser.getCurrentUser().getUsername());

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogoutAction();
            }
        });

//        loadTopPosts();
//        //find RecyclerView
//        rvPosts = (RecyclerView) view.findViewById(R.id.rvUserPosts);
//        //init arraylist
//        posts = new ArrayList<>();
//        //construct adapter
//        postAdapter = new ProfilePostAdapter(posts);
//
//        GridLayoutManager linearLayoutManager = new GridLayoutManager(getContext(), 3);
//        rvPosts.setLayoutManager(linearLayoutManager);
//        scrollListener = new com.codepath.instagram.EndlessRecyclerViewScrollListener(linearLayoutManager) {
//            @Override
//            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
//                // Triggered only when new data needs to be appended to the list
//                // Add whatever code is needed to append new items to the bottom of the list
//                loadNextData(page);
//            }
//        };
//        rvPosts.addOnScrollListener(scrollListener);
//        //set the adapter
//        rvPosts.setAdapter(postAdapter);
//        //set swipe refresh layout



//        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
//
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                loadTopPosts();
//            }
//        });

//        // Configure the refreshing colors
//        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
//                android.R.color.holo_green_light,
//                android.R.color.holo_orange_light,
//                android.R.color.holo_red_light);
    }

    public void onLogoutAction() {
        ParseUser.logOut();
        final Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

//    @Override
//    public void loadTopPosts() {
//
//        final Post.Query postQuery = new Post.Query();
//        postQuery.getTop().withUser();
//        postQuery.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());
//
//        postQuery.findInBackground(new FindCallback<Post>() {
//            @Override
//            public void done(List<Post> objects, ParseException e) {
//                if(e == null) {
//                    postAdapter.clear();
//                    for(int i = 0; i < objects.size(); i++) {
//                        posts.add(objects.get(i));
//                        postAdapter.notifyItemInserted(posts.size() - 1);
////                        Log.i("HomeFragment", "Post " + i + " " + objects.get(i).getDescription());
//                    }
//                } else {
//                    Toast.makeText(getContext(), "Failed to query posts", Toast.LENGTH_SHORT).show();
//                }
////                swipeRefreshLayout.setRefreshing(false);
//            }
//        });
//    }
}

