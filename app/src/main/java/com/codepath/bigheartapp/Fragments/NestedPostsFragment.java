package com.codepath.bigheartapp.Fragments;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codepath.bigheartapp.PostAdapter;
import com.codepath.bigheartapp.PostDetailsActivity;
import com.codepath.bigheartapp.R;
import com.codepath.bigheartapp.helpers.FragmentHelper;
import com.codepath.bigheartapp.model.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class NestedPostsFragment extends Fragment implements FragmentHelper.BaseFragment {
    private static final String TAG = "NestedPostsFragment";
    public static final String ARG_PAGE = "ARG_PAGE";
    public static final String POST_TYPE = "Post_Type";
    RecyclerView rvUserPosts;
    RecyclerView rvBookies;
    ArrayList<Post> bookiesArrayList;
    ArrayList<Post> postArrayList;
    PostAdapter bookiesAdapter;
    PostAdapter postsAdapter;
    public int postType;

    public static NestedPostsFragment newInstance(int page, int postType) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        args.putInt(POST_TYPE, postType);
        NestedPostsFragment fragment = new NestedPostsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // inflates recycler view in viewpager
        View rootView = inflater.inflate(R.layout.nested_fragment_posts, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // link variable to layout id
        rvUserPosts = view.findViewById(R.id.rvNestPosts);

        rvUserPosts.addItemDecoration(new VerticalSpaceItemDecoration(12));
        rvUserPosts.addItemDecoration(new HorizontalSpaceItemDecoration(6));

        //create new array list  for posts
        postArrayList = new ArrayList<>();
        // create adapter and link posts
        postsAdapter = new PostAdapter(postArrayList);
        rvUserPosts.setAdapter(postsAdapter);


        rvUserPosts.setLayoutManager(new LinearLayoutManager(getContext()));

        // toggle created to display user posts in database and user bookmarks in database
        int postType = getArguments().getInt(POST_TYPE);
        if (postType == 0) {
            loadTopPosts();
        } else {
            loadBookmarkedEvents();

        }
    }

    // function that reloads recycler view on case of bookmarks selected
    public void loadBookmarkedEvents() {
        final Post.Query eventsQuery = new Post.Query();

        ParseUser currentUser = ParseUser.getCurrentUser();
        final JSONArray bookmarked = currentUser.getJSONArray("bookmarked");

        eventsQuery.whereEqualTo(Post.KEY_IS_EVENT, true);

        eventsQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if (e == null) {
                    postArrayList.clear();
                    for ( int i = 0; i < bookmarked.length(); i++) {
                        for (int j = 0; j < objects.size(); j++) {
                            try {
                                if (objects.get(j).getObjectId().equals(bookmarked.get(i).toString())) {
                                    postArrayList.add(0,objects.get(j));
                                    postsAdapter.notifyItemInserted(postArrayList.size() - 1);
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }

            }
        });
    }

    public void loadTopPosts(){
        postsAdapter.clear();
        FragmentHelper fragmentHelper = new FragmentHelper(getPostQuery());
        fragmentHelper.fetchPosts(this);
//        swipeContainer.setRefreshing(false);
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
        postArrayList.add(objects.get(i));
        postsAdapter.notifyItemInserted(posts.size() - 1);
    }



    @Override
    public void onFetchFailure() {
        Toast.makeText(getContext(), "Failed to query posts", Toast.LENGTH_LONG).show();
//        swipeContainer.setRefreshing(false);
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
