package com.codepath.bigheartapp;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
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
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    // Create variables needed in PostAdapter
    private List<Post> mPosts;
    private List<Post> mFilteredPosts;
    Context context;
    int whichFragment;
    final int TYPE_POST = 101;
    final int TYPE_EVENT = 102;

    // Pass in the post array
    public PostAdapter(List<Post> posts, int whichFragment) {
        mPosts = posts;
        mFilteredPosts = posts;
        this.whichFragment = whichFragment;
    }

    // Clear all elements of the recycler
    public void clear() {
        mFilteredPosts.clear();
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // Set the recycler view
        context = parent.getContext();
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(context);

        // Switch between views depending on whether the post is regular or an event
        switch (viewType) {
            case TYPE_POST:
                View postView = inflater.inflate(R.layout.item_post_cardview, parent, false);
                viewHolder = new PostViewHolder(postView);
                break;
            case TYPE_EVENT:
                View eventView = inflater.inflate(R.layout.item_event_cardview, parent, false);
                viewHolder = new EventViewHolder(eventView);
                break;
            default:

                // Default view is a regular post
                View otherView = inflater.inflate(R.layout.item_post_cardview, parent, false);
                viewHolder = new PostViewHolder(otherView){
                };
                break;
        }
        return viewHolder;
    }

    // Function to determine what the view type of the post is
    @Override
    public int getItemViewType(int position) {
        if (!(mFilteredPosts.get(position).getIsEvent())) {
            return TYPE_POST;
        } else if (mFilteredPosts.get(position).getIsEvent()) {
            return TYPE_EVENT;
        }
        return -1;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {

            // Call different functions depending on whether the post is regular or an event
            case TYPE_POST:
                PostViewHolder postViewHolder = (PostViewHolder) holder;
                configurePostViewHolder(postViewHolder, position);
                break;
            case TYPE_EVENT:
                EventViewHolder eventViewHolder = (EventViewHolder) holder;
                configureEventViewHolder(eventViewHolder, position);
                break;

                // Default call is a function specific to a regular post
            default:
                PostViewHolder vh = (PostViewHolder) holder;
                configurePostViewHolder(vh, position);
                break;
        }
    }

    // Configure the event
    public void configureEventViewHolder(final EventViewHolder holder, int position) {
        final Post post = mFilteredPosts.get(position);
        try {

            // Set the IDs for the views in post
            holder.tvTimePosted.setText(ParseRelativeDate.getRelativeTimeAgo(post.getCreatedAt()));
            holder.tvUsertag.setText("@" + post.getUser().fetchIfNeeded().getUsername());
            holder.tvFirstLast.setText(post.getUser().fetchIfNeeded().get("firstName").toString() + " " + post.getUser().fetchIfNeeded().get("lastName").toString());
            holder.tvEventTitle.setText(post.getEventTitle());
            holder.tvAddress.setText(post.getAddress());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(post.isLiked()) {
            holder.ivHeart.setBackgroundResource(R.drawable.hot_pink_heart);
        } else {
            holder.ivHeart.setBackgroundResource(R.drawable.heart_logo_vector);
        }
        if(post.isBookmarked()) {

            // Set the bookmark image to filled if a post is already bookmarked
            holder.ibBookmark.setBackgroundResource(R.drawable.save_filled);
        }

        // onClickListener for the heart image button
       holder.ivHeart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!post.isLiked()) {

                    // If a post is not yet liked, start the animation and set the heart to filled
                    post.likePost(ParseUser.getCurrentUser());
                    holder.ivHeart.setBackgroundResource(R.drawable.hot_pink_heart);
                    holder.ivHeart.setBackgroundResource(R.drawable.animation);
                    AnimationDrawable heartStart;
                    heartStart = (AnimationDrawable) holder.ivHeart.getBackground();
                    heartStart.start();

                    // save the post as liked
                    post.saveInBackground();
                } else {

                    // If a post is already liked, start the animation and set the heart to unfilled
                    post.unlikePost(ParseUser.getCurrentUser());
                    holder.ivHeart.setBackgroundResource(R.drawable.heart_logo_vector);
                    holder.ivHeart.setBackgroundResource(R.drawable.animationstop);
                    AnimationDrawable heartStop;
                    heartStop = (AnimationDrawable) holder.ivHeart.getBackground();
                    heartStop.start();

                    // save the post as not liked
                    post.saveInBackground();
                }
            }
        });

        // onClickListener for the bookmark image button
        holder.ibBookmark.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!post.isBookmarked()) {

                    // If a post is not yet bookmarked, set the bookmark to filled
                    post.bookmarkPost(ParseUser.getCurrentUser());
                    holder.ibBookmark.setBackgroundResource(R.drawable.save_filled);

                    // save the post as bookmarked
                    post.saveInBackground();

                } else {

                    // If a post is already bookmarked, set the bookmark to unfilled
                    post.unbookmarkPost(ParseUser.getCurrentUser());
                    holder.ibBookmark.setBackgroundResource(R.drawable.save);

                    // save the post as not bookmarked
                    post.saveInBackground();
                }
            }
        });

        // Set the profile picture for the user
        ParseFile p = post.getUser().getParseFile("profilePicture");
        if (p != null) {
            Glide.with(context)
                    .load(p.getUrl())
                    .bitmapTransform(new CropCircleTransformation(context))
                    .into(holder.ivUserProfile);
        } else {

            // Default image in case user does not have a profile image
            holder.ivUserProfile.setImageResource(R.drawable.profile);
        }

        // Set the event description
        holder.tvEventDesc.setText(post.getDescription());
        if (!(post.getImage() == null)){

            // Set the image to be posted
            Glide.with(context)
                    .load(post.getImage().getUrl())
                    .bitmapTransform(new CenterCrop(context))
                    .into(holder.ivEventImage);
        } else {

            // Set the visibility of the event image to GONE if no picture is taken
            holder.ivEventImage.setVisibility(View.GONE);
        }
        if (!(post.getDay() == null)){

            // Set the date, time, and event title if a date is given by user
            holder.tvDateOfEvent.setText(post.getDay());
            holder.tvTime.setText(post.getTime());
            holder.tvEventTitle.setText(post.getEventTitle());
        }
    }

    // Configure the post
    public void configurePostViewHolder(final PostViewHolder holder, int position){
            final Post post = mFilteredPosts.get(position);
            try {

                // Set the IDs for the views in post
                holder.tvDate.setText(ParseRelativeDate.getRelativeTimeAgo(post.getCreatedAt()));
                holder.tvLocation.setText(post.getAddress());
                holder.tvUserName2.setText(post.getUser().fetchIfNeeded().getUsername());
                holder.tvUserName.setText(post.getUser().fetchIfNeeded().getUsername());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(post.isLiked()) {
                holder.ivHeart.setBackgroundResource(R.drawable.hot_pink_heart);
            } else {
                holder.ivHeart.setBackgroundResource(R.drawable.heart_logo_vector);
            }
        if(post.isBookmarked()) {

            // Set the bookmark image to filled if a post is already bookmarked
            holder.ibBookmark.setBackgroundResource(R.drawable.save_filled);
        }

        // onClickListener for the heart image button
            holder.ivHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!post.isLiked()) {

                        // If a post is not yet liked, start the animation and set the heart to filled
                        post.likePost(ParseUser.getCurrentUser());
                        holder.ivHeart.setBackgroundResource(R.drawable.hot_pink_heart);
                        holder.ivHeart.setBackgroundResource(R.drawable.animation);
                        AnimationDrawable heartStart;
                        heartStart = (AnimationDrawable) holder.ivHeart.getBackground();
                        heartStart.start();

                        // save the post as liked
                        post.saveInBackground();
                    } else {

                        // If a post is already liked, start the animation and set the heart to unfilled
                        post.unlikePost(ParseUser.getCurrentUser());
                        holder.ivHeart.setBackgroundResource(R.drawable.heart_logo_vector);
                        holder.ivHeart.setBackgroundResource(R.drawable.animationstop);
                        AnimationDrawable heartStop;
                        heartStop = (AnimationDrawable) holder.ivHeart.getBackground();
                        heartStop.start();

                        // save the post as not liked
                        post.saveInBackground();
                    }
                }
            });

        // onClickListener for the bookmark image button
        holder.ibBookmark.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!post.isBookmarked()) {

                    // If a post is not yet bookmarked, set the bookmark to filled
                    post.bookmarkPost(ParseUser.getCurrentUser());
                    holder.ibBookmark.setBackgroundResource(R.drawable.save_filled);

                    // save the post as bookmarked
                    post.saveInBackground();
                } else {

                    // If a post is already bookmarked, set the bookmark to unfilled
                    post.unbookmarkPost(ParseUser.getCurrentUser());
                    holder.ibBookmark.setBackgroundResource(R.drawable.save);

                    // save the post as not bookmarked
                    post.saveInBackground();
                }
            }
        });

        // Set the profile picture for the user
            ParseFile p = post.getUser().getParseFile("profilePicture");
            if (p != null) {
                Glide.with(context)
                        .load(p.getUrl())
                        .bitmapTransform(new CropCircleTransformation(context))
                        .into(holder.ivProfilePic);
            } else {

                // Default image in case user does not have a profile image
                holder.ivProfilePic.setImageResource(R.drawable.profile);
            }

            // Set the description for the post
            holder.tvDesc.setText(post.getDescription());
            if (!(post.getImage() == null)){

                // Set the image to be posted
                Glide.with(context)
                        .load(post.getImage().getUrl())
                        .bitmapTransform(new CenterCrop(context))
                        .into(holder.ivImage);
            } else {

                // Set the visibility of the post image to GONE if no picture is taken
                holder.ivImage.setVisibility(View.GONE);
            }

        if (!(post.getAddress() == null)){
            holder.tvLocation.setVisibility(View.VISIBLE);
        } else {

            // Set the visibility of the location text view to GONE if no location is given
            holder.tvLocation.setVisibility(View.GONE);
        }
    }

    // Gets the number of posts
    @Override
    public int getItemCount() {
        return mFilteredPosts.size();
    }

    // Function to filter the posts
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    mFilteredPosts = mPosts;
                } else {
                    List<Post> filteredList = new ArrayList<>();
                    for (Post row : mPosts) {

                        // match event title
                        if (row.getEventTitle().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    mFilteredPosts = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = mFilteredPosts;
                return filterResults;
            }
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mFilteredPosts = (ArrayList<Post>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Define the variables for the views and buttons
        public ImageView ivProfilePic;
        public ImageView ivImage;
        public TextView tvUserName;
        public TextView tvUserName2;
        public TextView tvDesc;
        public TextView tvDate;
        public ImageView ivHeart;
        public ImageButton ibBookmark;
        public TextView tvLocation;

        public PostViewHolder(View itemView) {
            super(itemView);

            // Set the variables to the corresponding IDs in the xml layout
            ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
            tvUserName = (TextView) itemView.findViewById(R.id.tvUser);
            tvUserName2 = (TextView) itemView.findViewById(R.id.tvUser2);
            tvDesc = (TextView) itemView.findViewById(R.id.tvDescription);
            tvDate = (TextView) itemView.findViewById(R.id.tvDate);
            ivProfilePic = (ImageView) itemView.findViewById(R.id.ivProfilePic);
            ivHeart = (ImageView) itemView.findViewById(R.id.ivHeart);
            ibBookmark = (ImageButton) itemView.findViewById(R.id.ibBookmark);
            tvLocation = (TextView) itemView.findViewById(R.id.tvLocation);

            // onClickListener for the post details activity/view
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            // Gets item position
            int position = getAdapterPosition();

            // Make sure the position is valid, i.e. actually exists in the view
            if (position != RecyclerView.NO_POSITION) {

                // Get the post at the position
                Post post = mFilteredPosts.get(position);

                // Tell Home Fragment to start the Details activity
                ((HomeActivity) context).showDetailsFor((Serializable) post);
            }
        }
    }

    public class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Define the variables for the views and buttons
        public ImageView ivUserProfile;
        public ImageView ivEventImage;
        public TextView tvFirstLast;
        public TextView tvUsertag;
        public TextView tvEventDesc;
        public TextView tvEventTitle;
        public ImageView ivHeart;
        public ImageButton ibBookmark;
        public TextView tvMonth;
        public TextView tvDay;
        public TextView tvYear;
        public TextView tvTime;
        public TextView tvDateOfEvent;
        public TextView tvTimePosted;
        public TextView tvAddress;

        public EventViewHolder(View itemView) {
            super(itemView);

            // Set the variables to the corresponding IDs in the xml layout
            ivEventImage =  itemView.findViewById(R.id.ivEventImage);
            ivUserProfile =  itemView.findViewById(R.id.ivUserProfile);
            tvTime = (TextView) itemView.findViewById(R.id.tvTimeOfEvent);
            tvFirstLast = (TextView) itemView.findViewById(R.id.tvFirstLast);
            tvUsertag = (TextView) itemView.findViewById(R.id.tvUsertag);
            tvEventDesc = (TextView) itemView.findViewById(R.id.tvEventDesc);
            tvEventTitle = (TextView) itemView.findViewById(R.id.tvEventTitle);
            tvDateOfEvent = itemView.findViewById(R.id.tvDate);
            tvTimePosted = itemView.findViewById(R.id.tvTimePosted);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            ivHeart = (ImageView) itemView.findViewById(R.id.ivHeart);
            ibBookmark = (ImageButton) itemView.findViewById(R.id.ibBookmark);
            tvMonth = (TextView) itemView.findViewById(R.id.tvMonth);
            tvDay = (TextView) itemView.findViewById(R.id.tvDay);
            tvYear = (TextView) itemView.findViewById(R.id.tvYear);

            // onClickListener for the post details activity/view
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            // Gets item position
            int position = getAdapterPosition();

            // Make sure the position is valid, i.e. actually exists in the view
            if (position != RecyclerView.NO_POSITION) {

                // Get the post at the position
                Post post = mFilteredPosts.get(position);

                // Tell Home Fragment to start the Details activity
                ((HomeActivity) context).showDetailsFor((Serializable) post);
            }
        }
    }
}