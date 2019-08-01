package com.codepath.bigheartapp.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.codepath.bigheartapp.PostAdapter;
import com.codepath.bigheartapp.R;
import com.codepath.bigheartapp.model.Post;
import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class FragmentHelper {

    private Post.Query query;

    public FragmentHelper(Post.Query postQuery) {
        query = postQuery;
    }

    public void fetchPosts(final FetchResults curFragment) {
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if(e == null) {
                    for(int i = 0; i < objects.size(); i++) {
                        curFragment.onFetchSuccess(objects, i);
                    }
                } else {
                    curFragment.onFetchFailure();
                }
            }
        });
    }
}
