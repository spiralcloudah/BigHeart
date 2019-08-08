package com.codepath.bigheartapp.Fragments;

import android.Manifest;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import com.codepath.bigheartapp.helpers.FragmentUpdated;
import com.codepath.bigheartapp.helpers.HorizontalSpaceItemDecoration;
import com.codepath.bigheartapp.helpers.PostBroadcastReceiver;
import com.codepath.bigheartapp.helpers.VerticalSpaceItemDecoration;
import com.codepath.bigheartapp.model.Post;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.parse.ParseGeoPoint;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class EventFragment extends Fragment implements FetchResults, FragmentUpdated {

    // Store variables to use in the event fragment
    private EndlessRecyclerViewScrollListener scrollListener;
    public RecyclerView rvEventPosts;
    SearchView searchEvents;
    ImageButton ibFilter;
    private final static String KEY_LOCATION = "location";
    private final int MY_LOCATION_REQUEST_CODE = 130;

    public static Location mCurrentLocation;
    private SwipeRefreshLayout swipeContainer;
    private ArrayList<Post> posts;
    private PostAdapter adapter;
    private final int REQUEST_CODE = 120;
    private final double MAX_DISTANCE = 10.0;
    private BroadcastReceiver detailsChangedReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_event, container, false);

        posts = new ArrayList<>();
        adapter = new PostAdapter(posts);

        detailsChangedReceiver = new PostBroadcastReceiver(this);
        // Register for the particular broadcast based on ACTION string
        IntentFilter filter = new IntentFilter(PostDetailsActivity.ACTION);
        getActivity().registerReceiver(detailsChangedReceiver, filter);

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
                    public void run() {
                    }
                });
            }
        };

        // Add the scroll listener and item decoration to recyclerview
        rvEventPosts.addOnScrollListener(scrollListener);
        rvEventPosts.addItemDecoration(new VerticalSpaceItemDecoration(12));
        rvEventPosts.addItemDecoration(new HorizontalSpaceItemDecoration(12));
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
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorAccent),
                getResources().getColor(R.color.colorAccentDark),
                getResources().getColor(R.color.colorPrimaryDark)
        );

        ibFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toFilterActivity = new Intent(getContext(), FilterActivity.class);
                startActivityForResult(toFilterActivity, REQUEST_CODE);
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        // Allows us to filter and search for events in the Filter Activity
        searchEvents.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchEvents.setMaxWidth(Integer.MAX_VALUE);

        if (savedInstanceState != null && savedInstanceState.keySet().contains(KEY_LOCATION)) {

            // KEY_LOCATION was found in the Bundle, so mCurrentLocation is not null
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            getMyLocation();
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_REQUEST_CODE);
        }

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

    public void loadTopPosts() {
        FragmentHelper fragmentHelper = new FragmentHelper(getPostQuery());
        fragmentHelper.fetchPosts(this);
    }

    @Override
    public Post.Query getPostQuery() {
        final Post.Query postQuery = new Post.Query();
        postQuery.getTop().withUser();
        postQuery.addDescendingOrder(Post.KEY_DATE);

        // Only loads posts that are events
        postQuery.whereEqualTo(Post.KEY_IS_EVENT, true);
        if (mCurrentLocation != null) {
            ParseGeoPoint userLocation = new ParseGeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            postQuery.whereWithinMiles(Post.KEY_LOCATION, userLocation, MAX_DISTANCE);
            Toast.makeText(getContext(), "Showing events within " + MAX_DISTANCE + " miles of you", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "Could not load user location", Toast.LENGTH_LONG).show();
        }
        return postQuery;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        getActivity().onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getMyLocation();
        } else {
            Toast.makeText(getContext(), "App does not have access to user's location", Toast.LENGTH_LONG).show();
            loadTopPosts();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {


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

            double distance = data.getDoubleExtra(Post.KEY_LOCATION, 0.0);
            // Do not take distance into account in the filter if it is null
            if (distance != 0.0) {
                if (mCurrentLocation != null) {
                    ParseGeoPoint userLocation = new ParseGeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                    postQuery.whereWithinMiles(Post.KEY_LOCATION, userLocation, data.getDoubleExtra(Post.KEY_LOCATION, 0.0));
                    Toast.makeText(getContext(), "Showing events within " + distance + " miles of you", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "Cannot get current location", Toast.LENGTH_LONG).show();
                }
            }

            posts.clear();
            adapter.clear();
            FragmentHelper fragmentHelper = new FragmentHelper(postQuery);
            fragmentHelper.fetchPosts(this);
        }

    }

    // Function to find the location set by the user
    @SuppressWarnings({"MissingPermission"})
    void getMyLocation() {
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(getContext());
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            onLocationChanged(location);
                            loadTopPosts();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Error trying to get last GPS location", Toast.LENGTH_LONG).show();
                        loadTopPosts();
                    }
                });
    }

    public void onLocationChanged(Location location) {

        // GPS may be turned off; nothing happens
        if (location == null) {
            return;
        }

        // If not null, then set current location to changed location
        mCurrentLocation = location;
    }

    @Override
    public void onFetchSuccess(List<Post> objects) {
        if (objects.size() == 0) {
            Toast.makeText(getContext(), "No results for filter", Toast.LENGTH_LONG).show();
        } else {Toast.makeText(getContext(), "Showing filtered events", Toast.LENGTH_LONG).show();}
        posts.addAll(objects);
        adapter.notifyDataSetChanged(); //or range inserted
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