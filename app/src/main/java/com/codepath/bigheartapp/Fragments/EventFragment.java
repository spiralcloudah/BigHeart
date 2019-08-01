package com.codepath.bigheartapp.Fragments;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.codepath.bigheartapp.PostDetailsActivity;
import com.codepath.bigheartapp.R;
import com.codepath.bigheartapp.helpers.FetchResults;
import com.codepath.bigheartapp.helpers.FragmentHelper;
import com.codepath.bigheartapp.helpers.HorizontalSpaceItemDecoration;
import com.codepath.bigheartapp.helpers.VerticalSpaceItemDecoration;
import com.codepath.bigheartapp.model.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class EventFragment extends Fragment implements FetchResults {

    // Store variables to use in the event fragment
    private EndlessRecyclerViewScrollListener scrollListener;
    public RecyclerView rvEventPosts;
    SearchView searchEvents;
    ImageButton ibFilter;
    private SwipeRefreshLayout swipeContainer;
    private ArrayList<Post> posts;
    private PostAdapter adapter;
    private final int REQUEST_CODE = 120;
    private final double MAX_DISTANCE = 10.0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_event, container, false);

        posts = new ArrayList<>();
        adapter = new PostAdapter(posts);

        // Set created variables to new elements or corresponding layouts
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
                    public void run() {}
                });
            }
        };

        // Add the scroll listener and item decoration to recyclerview
        rvEventPosts.addOnScrollListener(scrollListener);
        rvEventPosts.addItemDecoration(new VerticalSpaceItemDecoration(12));
        rvEventPosts.addItemDecoration(new HorizontalSpaceItemDecoration(6));
        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                // Keep animation for 4 seconds
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

        // Allows us to filter and search for events in the Filter Activity
        searchEvents.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchEvents.setMaxWidth(Integer.MAX_VALUE);

        // Listening to search query text change
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

        // Only loads posts that are events
        postQuery.whereEqualTo(Post.KEY_IS_EVENT, true);
        if(MapsFragment.mCurrentLocation != null) {
            ParseGeoPoint userLocation = new ParseGeoPoint(MapsFragment.mCurrentLocation.getLatitude(), MapsFragment.mCurrentLocation.getLongitude());
            postQuery.whereWithinMiles(Post.KEY_LOCATION, userLocation, MAX_DISTANCE);
            Toast.makeText(getContext(),"Showing events within " + MAX_DISTANCE + " miles of you", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(),"Could not load user location", Toast.LENGTH_LONG).show();
        }
        return postQuery;
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

            // Do not take day into account in the filter if it is null
            if (data.getStringExtra(Post.KEY_DAY) != null) {
                postQuery.whereEqualTo(Post.KEY_DAY, data.getStringExtra(Post.KEY_DAY));
            }

            // Do not take time into account in the filter if it is null
            if (data.getStringExtra(Post.KEY_TIME) != null) {
                postQuery.whereEqualTo(Post.KEY_TIME, data.getStringExtra(Post.KEY_TIME));
            }
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

    BroadcastReceiver detailsChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            int resultCode = intent.getIntExtra(context.getString(R.string.result_code), RESULT_CANCELED);

            if (resultCode == RESULT_OK) {
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
                } else {
                    Toast.makeText(context, "An error occurred", Toast.LENGTH_LONG).show();
                }

            }
        }
    };

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