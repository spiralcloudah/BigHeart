package com.codepath.bigheartapp;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.codepath.bigheartapp.model.Post;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.Serializable;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class PostDetailsActivity extends AppCompatActivity {
    public static final String ACTION = "com.codepath.bigheartapp";
    public static final String WHICH_RECEIVER = "details";

    // the post to display
    Post post;

    // The view and button objects
    ImageView ivImage;
    ImageView ivProfilePic;
    TextView tvUser2;
    TextView tvMonth;
    TextView tvDay;
    TextView tvYear;
    TextView tvTime;
    TextView tvDescription;
    TextView tvDate;
    ImageView ivHeart;
    ImageButton ibBookmark;
    TextView tvTitle;
    TextView tvLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        // Resolve the view and button objects
        ivImage = (ImageView) findViewById(R.id.ivImage);
        ivProfilePic = (ImageView) findViewById(R.id.ivProfilePic);
        tvUser2 = (TextView) findViewById(R.id.tvUser2);
        tvDescription = (TextView) findViewById(R.id.tvDescription);
        ivHeart = (ImageView) findViewById(R.id.ivDetailsHeart);
        ibBookmark = (ImageButton) findViewById(R.id.ibBookmark);
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvMonth = (TextView) findViewById(R.id.tvDate);
        tvDay = (TextView) findViewById(R.id.tvDay);
        tvYear = (TextView) findViewById(R.id.tvYear);
        tvTime = (TextView) findViewById(R.id.tvTime);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvLocation = (TextView) findViewById(R.id.tvLocation);

        // Unwrap the post passed in via intent, using its simple name as a key
        post = (Post) getIntent().getParcelableExtra(Post.class.getSimpleName());
        Log.d("PostDetailsActivity", String.format("Showing details for '%s'", post.getDescription()));

        // Set the texts after post has been created
        tvDescription.setText(post.getDescription());
        tvDay.setText(post.getDay());
        tvTime.setText(post.getTime());
        tvTitle.setText(post.getEventTitle());
        tvLocation.setText(post.getAddress());

        // If a post is already liked, set the heart image to be filled
        if(post.isLiked()) {
            ivHeart.setBackgroundResource(R.drawable.hot_pink_heart);
        }

        // If a post is not an event, make the visibility of the bookmark GONE
        if (post.getIsEvent() == false) {
            ibBookmark.setVisibility(View.GONE);
        }

        // If a post is bookmarked, set the bookmark image to be filled
//        if(post.isBookmarked()) {
//            ibBookmark.setBackgroundResource(R.drawable.save_filled);
//        }

        // Event title only visible for an event
        if (post.getEventTitle() == null) {
            tvTitle.setVisibility(View.INVISIBLE);
        } else {
            tvTitle.setVisibility(View.VISIBLE);
        }

        // Address only visible for an event
        if (post.getAddress() == null) {
            tvLocation.setVisibility(View.INVISIBLE);
        } else {
            tvLocation.setVisibility(View.VISIBLE);
        }

        // onClickListener for the like button
        ivHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!post.isLiked()) {

                    // If a post is not yet liked, start the animation and set the heart to filled
                    post.likePost(ParseUser.getCurrentUser());
                    ivHeart.setBackgroundResource(R.drawable.hot_pink_heart);
                    ivHeart.setBackgroundResource(R.drawable.animation);
                    AnimationDrawable heartStart;
                    heartStart = (AnimationDrawable) ivHeart.getBackground();
                    heartStart.start();

                    // save the post as liked
                    post.saveInBackground();

                } else {

                    // If a post is already liked, start the animation and set the heart to unfilled
                    post.unlikePost(ParseUser.getCurrentUser());
                    ivHeart.setBackgroundResource(R.drawable.heart_logo_vector);
                    ivHeart.setBackgroundResource(R.drawable.animationstop);
                    AnimationDrawable heartStop;
                    heartStop = (AnimationDrawable) ivHeart.getBackground();
                    heartStop.start();

                    // save the post as not liked
                    post.saveInBackground();
                }
            }
        });

        // onClickListener for the bookmark image button
        ibBookmark.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                if(!post.isBookmarked()) {
//
//                    // If a post is not yet bookmarked, set the bookmark to filled
//                    post.bookmarkPost(ParseUser.getCurrentUser());
//                    ibBookmark.setBackgroundResource(R.drawable.save_filled);
//
//                    // save the post as bookmarked
//                    post.saveInBackground();
//
//                } else {
//
//                    // If a post is already bookmarked, set the bookmark to unfilled
//                    post.unbookmarkPost(ParseUser.getCurrentUser());
//                    ibBookmark.setBackgroundResource(R.drawable.save);
//
//                    // save the post as not bookmarked
//                    post.saveInBackground();
//                }
            }
        });

        // Set the image for the post
        ParseFile photo = post.getImage();
        if (photo != null) {
            Glide.with(PostDetailsActivity.this)
                    .load(photo.getUrl())
                    .bitmapTransform(new CenterCrop(PostDetailsActivity.this))
                    .into(ivImage);
        } else {

            // Set the visibility of the event image to GONE if no picture is taken
            ivImage.setVisibility(View.GONE);
        }
        try {

            // Set the username for the current user
            tvUser2.setText("@" + post.getUser().fetchIfNeeded().getUsername());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Get the date for when the post was created
        tvDate.setText(ParseRelativeDate.getRelativeTimeAgo(post.getCreatedAt()));

        // If not null, set the user profile picture
        ParseFile p = post.getUser().getParseFile("profilePicture");


        if (p != null) {
            Glide.with(this)
                    .load(p.getUrl())
                    .bitmapTransform(new CropCircleTransformation(PostDetailsActivity.this))
                    .into(ivProfilePic);
        }
    }


    @Override
    public void onBackPressed() {
        Intent backHome = new Intent();
        backHome.setAction(ACTION);
        backHome.putExtra(Post.class.getSimpleName(), (Serializable) post);
        backHome.putExtra(getString(R.string.result_code), RESULT_OK);
        sendBroadcast(backHome);
        super.onBackPressed();
    }
}