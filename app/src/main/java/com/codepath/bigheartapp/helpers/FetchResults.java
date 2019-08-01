package com.codepath.bigheartapp.helpers;

import com.codepath.bigheartapp.model.Post;

import java.util.List;

public interface FetchResults {

    void onFetchSuccess(List<Post> objects);
    void onFetchFailure();
    void onFetchFinish();
    Post.Query getPostQuery();
}
