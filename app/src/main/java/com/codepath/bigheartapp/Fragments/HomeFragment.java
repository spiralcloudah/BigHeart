package com.codepath.bigheartapp.Fragments;

import android.content.BroadcastReceiver;
import android.content.Intent;
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
import com.codepath.bigheartapp.PostAdapter;
import com.codepath.bigheartapp.PostDetailsActivity;
import com.codepath.bigheartapp.R;
import com.codepath.bigheartapp.helpers.FetchResults;
import com.codepath.bigheartapp.helpers.FragmentHelper;
import com.codepath.bigheartapp.helpers.HorizontalSpaceItemDecoration;
import com.codepath.bigheartapp.helpers.PostBroadcastReceiver;
import com.codepath.bigheartapp.helpers.FragmentUpdated;
import com.codepath.bigheartapp.helpers.VerticalSpaceItemDecoration;
import com.codepath.bigheartapp.model.Post;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements FetchResults, FragmentUpdated {

    // Store variables to use in the home fragment
    private EndlessRecyclerViewScrollListener scrollListener;
    public static RecyclerView rvPost;
    private SwipeRefreshLayout swipeContainer;
    public static ArrayList<Post> posts;
    public static PostAdapter adapter;
    private BroadcastReceiver detailsChangedReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        posts = new ArrayList<>();
        adapter = new PostAdapter(posts);

        detailsChangedReceiver = new PostBroadcastReceiver(this); //new interface
        IntentFilter filter = new IntentFilter(PostDetailsActivity.ACTION);
        getActivity().registerReceiver(detailsChangedReceiver, filter);


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
        rvPost.addItemDecoration(new HorizontalSpaceItemDecoration(12));
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
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorAccent),
                getResources().getColor(R.color.colorAccentDark),
                getResources().getColor(R.color.colorPrimaryDark)
        );


        loadTopPosts();
        return rootView;
    }

    // load the latest posts
    public void loadTopPosts(){
        FragmentHelper fragmentHelper = new FragmentHelper(getPostQuery());
        fragmentHelper.fetchPosts(this);
    }

    @Override
    public Post.Query getPostQuery() {
        final Post.Query postQuery = new Post.Query();
        postQuery.getTop().withUser();
        postQuery.addDescendingOrder(Post.KEY_DATE);
        return postQuery;
    }

    @Override
    public void onFetchSuccess(List<Post> objects) {
        posts.addAll(objects);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onFetchFailure() {
        Toast.makeText(getContext(), "Failed to query posts", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFetchFinish() {
        swipeContainer.setRefreshing(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the listener when the application is paused
        getActivity().unregisterReceiver(detailsChangedReceiver);
    }

    @Override
    public void updatePosts(Intent intent) {
        Post postChanged = (Post) intent.getSerializableExtra(Post.class.getSimpleName());
        int indexOfChange = -1;
        for (int i = 0; i < posts.size(); i++) {
            if (posts.get(i).hasSameId(postChanged)) {
                indexOfChange = i;
                break;
            }
        }
        if (indexOfChange != -1) {
            posts.set(indexOfChange, postChanged);
            adapter.notifyItemChanged(indexOfChange);
        }
    }
}
