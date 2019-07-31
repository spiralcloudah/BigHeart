package com.codepath.bigheartapp;

import android.app.Application;

import com.codepath.bigheartapp.model.Post;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // register Post class
        ParseObject.registerSubclass(Post.class);

        // Setup a parse for the app on heroku
        final Parse.Configuration configuration = new Parse.Configuration.Builder(this)
                .applicationId("biggerheart")
                .clientKey("tlandc")
                .server("http://big-heart.herokuapp.com/parse")
                .build();
        Parse.initialize(configuration);
    }
}