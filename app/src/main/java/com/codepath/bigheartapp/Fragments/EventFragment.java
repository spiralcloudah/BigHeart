package com.codepath.bigheartapp.Fragments;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import com.codepath.bigheartapp.EndlessRecyclerViewScrollListener;
import com.codepath.bigheartapp.FilterActivity;
import com.codepath.bigheartapp.PostAdapter;
import com.codepath.bigheartapp.R;
import com.codepath.bigheartapp.model.Post;
import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.parse.Parse.getApplicationContext;

public class EventFragment extends Fragment {
    // Store a member variable for the listener
    private EndlessRecyclerViewScrollListener scrollListener;

    ArrayList<Post> posts;
    public RecyclerView rvEventPosts;
    SearchView searchEvents;
    PostAdapter adapter;
    ImageButton ibFilter;
    private SwipeRefreshLayout swipeContainer;
    private final int REQUEST_CODE = 120;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_event, container, false);

        posts = new ArrayList<>();

        adapter = new PostAdapter(posts, 0);
        rvEventPosts = (RecyclerView) rootView.findViewById(R.id.rvEventPosts);
        searchEvents = (SearchView) rootView.findViewById(R.id.searchEvents);
        ibFilter = (ImageButton) rootView.findViewById(R.id.ibFilter);

        rvEventPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvEventPosts.setAdapter(adapter);

        // Configure the RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvEventPosts.setLayoutManager(linearLayoutManager);

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
        rvEventPosts.addOnScrollListener(scrollListener);

        rvEventPosts.addItemDecoration(new EventFragment.VerticalSpaceItemDecoration(12));

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

        ibFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toFilterActivity = new Intent(getContext(), FilterActivity.class);
                startActivityForResult(toFilterActivity, REQUEST_CODE);
            }
        });

        loadTopPosts();

        return rootView;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        searchEvents.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchEvents.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchEvents.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // signup recycler view when query submitted
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // signup recycler view when text is changed
                adapter.getFilter().filter(query);
                return false;
            }
        });
    }

    public void loadTopPosts(){
        final Post.Query postQuery = new Post.Query();
        postQuery.getTop().withUser();
        postQuery.addDescendingOrder(Post.KEY_DATE);

        postQuery.whereEqualTo(Post.KEY_IS_EVENT, true);

        postQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if(e == null) {
                    adapter.clear();
                    for(int i = 0; i < objects.size(); i++) {
                        posts.add(objects.get(i));
                        adapter.notifyItemInserted(posts.size() - 1);
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to query posts", Toast.LENGTH_LONG).show();
                }
                swipeContainer.setRefreshing(false);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            Toast.makeText(getContext(), "Showing filtered events", Toast.LENGTH_LONG).show();
            posts.clear();

            final Post.Query postQuery = new Post.Query();
            postQuery.getTop().withUser();
            postQuery.addDescendingOrder(Post.KEY_DATE);

            postQuery.whereEqualTo(Post.KEY_IS_EVENT, true);
            postQuery.whereEqualTo(Post.KEY_DAY, data.getStringExtra(Post.KEY_DAY));

            postQuery.findInBackground(new FindCallback<Post>() {
                @Override
                public void done(List<Post> objects, ParseException e) {
                    if(e == null) {
                        adapter.clear();
                        for(int i = 0; i < objects.size(); i++) {
                            posts.add(objects.get(i));
                            adapter.notifyItemInserted(posts.size() - 1);
                        }
                    } else {
                        Toast.makeText(getContext(), "No posts matching applied filters", Toast.LENGTH_LONG).show();
                    }
                    swipeContainer.setRefreshing(false);
                }
            });
        }

    }

    //put space between cardviews
    public class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {

        private final int verticalSpaceHeight;

        public VerticalSpaceItemDecoration(int verticalSpaceHeight) {
            this.verticalSpaceHeight = verticalSpaceHeight;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            outRect.top = verticalSpaceHeight;
        }
    }
}