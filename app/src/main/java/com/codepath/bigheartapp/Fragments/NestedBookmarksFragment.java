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

public class NestedBookmarksFragment extends Fragment {
    private static final String TAG = "NestedBookmarksFragment";
    public static final String ARG_PAGE = "ARG_PAGE";
    public static final String POST_TYPE = "Post_Type";
    RecyclerView rvUserBookmarks;
    ArrayList<Post> bookmarksArrayList;
    PostAdapter bookmarksAdapter;

    public static NestedBookmarksFragment newInstance(int page, int postType) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        args.putInt(POST_TYPE, postType);
        NestedBookmarksFragment fragment = new NestedBookmarksFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.nested_fragment_posts, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        rvUserBookmarks = view.findViewById(R.id.rvNestBookmarks);

        bookmarksArrayList = new ArrayList<>();

        bookmarksAdapter = new PostAdapter(bookmarksArrayList, 2);

        rvUserBookmarks.setAdapter(bookmarksAdapter);

        rvUserBookmarks.setLayoutManager(new LinearLayoutManager(getContext()));

        loadBookmarkedEvents();
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
                    bookmarksArrayList.clear();
                    for ( int i = 0; i < bookmarked.length() - 1 ; i++) {
                        for (int j = 0; j < objects.size(); j++) {
                            try {
                                if (objects.get(j).getObjectId().equals(bookmarked.get(i).toString())) {
                                    bookmarksArrayList.add(0,objects.get(j));
                                    bookmarksAdapter.notifyItemInserted(bookmarksArrayList.size() - 1);
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
