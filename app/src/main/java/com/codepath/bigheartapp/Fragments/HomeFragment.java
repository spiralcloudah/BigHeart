package com.codepath.bigheartapp.Fragments;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codepath.bigheartapp.EndlessRecyclerViewScrollListener;
import com.codepath.bigheartapp.PostDetailsActivity;
import com.codepath.bigheartapp.R;
import com.codepath.bigheartapp.helpers.FragmentHelper;
import com.codepath.bigheartapp.model.Post;

import java.util.List;

public class HomeFragment extends Fragment implements FragmentHelper.BaseFragment {

    // Store variables to use in the home fragment
    private EndlessRecyclerViewScrollListener scrollListener;
    public static RecyclerView rvPost;
    private SwipeRefreshLayout swipeContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Register for the particular broadcast based on ACTION string
        IntentFilter filter = new IntentFilter(PostDetailsActivity.ACTION);
        getActivity().registerReceiver(detailsChangedReceiver, filter);


        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_home, container, false);


        rvPost = (RecyclerView) rootView.findViewById(R.id.rvPost);
        rvPost.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPost.setAdapter(adapter);

        // Configure the RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvPost.setLayoutManager(linearLayoutManager);

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
        rvPost.addOnScrollListener(scrollListener);
        rvPost.addItemDecoration(new VerticalSpaceItemDecoration(12));
        rvPost.addItemDecoration(new HorizontalSpaceItemDecoration(6));
        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);

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


        loadTopPosts();
        return rootView;
    }

    // load the latest posts
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
        postQuery.addDescendingOrder(Post.KEY_DATE);
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
    public void onDestroy() {
        super.onDestroy();
        // Unregister the listener when the application is paused
        getActivity().unregisterReceiver(detailsChangedReceiver);
    }
}
