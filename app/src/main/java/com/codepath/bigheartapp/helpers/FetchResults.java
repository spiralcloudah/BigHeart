package com.codepath.bigheartapp.helpers;

import com.codepath.bigheartapp.model.Post;

import java.util.List;

public interface FetchResults {

    void onFetchSuccess(List<Post> objects, int i);
    void onFetchFailure();
    Post.Query getPostQuery();
}
