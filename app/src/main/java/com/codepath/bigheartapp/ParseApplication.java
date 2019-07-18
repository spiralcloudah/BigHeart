package com.codepath.bigheartapp;

import android.app.Application;

import com.parse.Parse;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        final Parse.Configuration configuration = new Parse.Configuration.Builder(this)
                .applicationId("biggerheart")
                .clientKey("tlandc")
                .server("http://big-heart.herokuapp.com/parse")
                .build();

        Parse.initialize(configuration);
    }
}