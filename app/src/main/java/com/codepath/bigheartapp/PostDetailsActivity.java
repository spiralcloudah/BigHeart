package com.codepath.bigheartapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.codepath.bigheartapp.model.Post;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class PostDetailsActivity extends AppCompatActivity {

    // the movie to display
    Post post;

    // the view objects
    ImageView ivImage;
    ImageView ivProfilePic;
    TextView tvUser;
    TextView tvUser2;
    TextView tvMonth;
    TextView tvDay;
    TextView tvYear;
    TextView tvTime;
    TextView tvDescription;
    TextView tvDate;
    ImageView ivHeart;
    TextView tvTitle;
    TextView tvLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        // resolve the view objects
        ivImage = (ImageView) findViewById(R.id.ivImage);
        ivProfilePic = (ImageView) findViewById(R.id.ivProfilePic);
        tvUser = (TextView) findViewById(R.id.tvUser);
        tvUser2 = (TextView) findViewById(R.id.tvUser2);
        tvDescription = (TextView) findViewById(R.id.tvDescription);
        ivHeart = (ImageView) findViewById(R.id.ivHeart);
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvMonth = (TextView) findViewById(R.id.tvMonth);
        tvDay = (TextView) findViewById(R.id.tvDay);
        tvYear = (TextView) findViewById(R.id.tvYear);
        tvTime = (TextView) findViewById(R.id.tvTime);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvLocation = (TextView) findViewById(R.id.tvLocation);

        // unwrap the post passed in via intent, using its simple name as a key
        post = (Post) getIntent().getParcelableExtra(Post.class.getSimpleName());
        Log.d("PostDetailsActivity", String.format("Showing details for '%s'", post.getDescription()));

        tvDescription.setText(post.getDescription());
        tvDay.setText(post.getDay());
        tvTime.setText(post.getTime());
        tvTitle.setText(post.getEventTitle());
        tvLocation.setText(post.getAddress());

        if(post.isLiked()) {
            ivHeart.setBackgroundResource(R.drawable.hot_pink_heart);
        }

        if (post.getEventTitle() == null) {
            tvTitle.setVisibility(View.INVISIBLE);
        } else {
            tvTitle.setVisibility(View.VISIBLE);
        }

        if (post.getAddress() == null) {
            tvLocation.setVisibility(View.INVISIBLE);
        } else {
            tvLocation.setVisibility(View.VISIBLE);
        }

        ivHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!post.isLiked()) {
                    post.likePost(ParseUser.getCurrentUser());
                    ivHeart.setBackgroundResource(R.drawable.hot_pink_heart);

                    post.saveInBackground();

                } else {
                    post.unlikePost(ParseUser.getCurrentUser());
                    ivHeart.setBackgroundResource(R.drawable.heart_logo_vector);

                    post.saveInBackground();
                }
            }
        });

        ParseFile photo = post.getImage();
        if(photo != null) {
            Glide.with(PostDetailsActivity.this)
                    .load(photo.getUrl())
                    .bitmapTransform(new CenterCrop(PostDetailsActivity.this))
                    .into(ivImage);
        } else {
            ivImage.setVisibility(View.GONE);
        }
        try {
            tvUser.setText(post.getUser().fetchIfNeeded().getUsername());
            tvUser2.setText(post.getUser().fetchIfNeeded().getUsername());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        tvDate.setText(ParseRelativeDate.getRelativeTimeAgo(post.getCreatedAt()));

        ParseFile p = post.getUser().getParseFile("profilePicture");

        if(p != null) {
            Glide.with(this)
                    .load(p.getUrl())
                    .bitmapTransform(new CropCircleTransformation(PostDetailsActivity.this))
                    .into(ivProfilePic);
        }
    }
}
