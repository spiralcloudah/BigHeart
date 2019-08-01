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

    public void fetchPosts(final BaseFragment curFragment) {
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


    // base interface for fragments
    public interface BaseFragment {

        // Set created variables to new elements or corresponding layouts
        ArrayList<Post> posts = new ArrayList<>();
        PostAdapter adapter = new PostAdapter(posts);

        void onFetchSuccess(List<Post> objects, int i);
        void onFetchFailure();
        Post.Query getPostQuery();

        // put space between cardviews
        class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {

            // Specify a final variable for git  between cardviews
            private final int verticalSpaceHeight;

            // function to set the space height
            public VerticalSpaceItemDecoration(int verticalSpaceHeight) {
                this.verticalSpaceHeight = verticalSpaceHeight;
            }
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                       RecyclerView.State state) {
                outRect.top = verticalSpaceHeight;
            }
        }

        // add padding to side
        class HorizontalSpaceItemDecoration extends RecyclerView.ItemDecoration {

            // Specify a final variable for space between cardviews
            private final int horizontalSpaceWidth;

            // function to set the space height
            public HorizontalSpaceItemDecoration(int horizontalSpaceWidth) {
                this.horizontalSpaceWidth = horizontalSpaceWidth;
            }
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                       RecyclerView.State state) {
                outRect.right = horizontalSpaceWidth;
                outRect.left = horizontalSpaceWidth;
            }
        }

        // Define the callback for what to do when data is received
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
    }
}
