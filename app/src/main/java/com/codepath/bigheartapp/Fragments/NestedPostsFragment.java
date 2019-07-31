package com.codepath.bigheartapp.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.bigheartapp.PostAdapter;
import com.codepath.bigheartapp.R;
import com.codepath.bigheartapp.model.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class NestedPostsFragment extends Fragment {
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
//                        try {
//                            Log.d("FeedActivity", "Post ["+i+"] = "
//                                    + objects.get(i).getDescription()
//                                    + "\n username = " + objects.get(i).getUser().fetchIfNeeded().getUsername()
//                                    + " o k ");
//                        } catch (ParseException e1) {
//                            e1.printStackTrace();
//                        }

                        postArrayList.add(0,objects.get(i));
                        postsAdapter.notifyItemInserted(postArrayList.size() - 1);

                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
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
                    for ( int i = 0; i < bookmarked.length() - 1 ; i++) {
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

}
