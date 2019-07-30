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

    private List<Post> mPosts;
    private List<Post> mFilteredPosts;
    Context context;
    int whichFragment;
    final int TYPE_POST = 101;
    final int TYPE_EVENT = 102;

    //pass in the post array
    public PostAdapter(List<Post> posts, int whichFragment) {
        mPosts = posts;
        mFilteredPosts = posts;
        this.whichFragment = whichFragment;
    }



    public void addAll(List<Post> p, View view) {
        mFilteredPosts.addAll(p);
        view.post(new Runnable() {
            public void run() {
                // There is no need to use notifyDataSetChanged()
                notifyDataSetChanged();
            }
        });
    }


    // Clean all elements of the recycler
    public void clear() {
        mFilteredPosts.clear();
        notifyDataSetChanged();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();

        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(context);

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
                View otherView = inflater.inflate(R.layout.item_post_cardview, parent, false);
                viewHolder = new PostViewHolder(otherView){
                };
                break;
        }
        return viewHolder;
    }

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
            case TYPE_POST:
                PostViewHolder postViewHolder = (PostViewHolder) holder;
                configurePostViewHolder(postViewHolder, position);
                break;
            case TYPE_EVENT:
                EventViewHolder eventViewHolder = (EventViewHolder) holder;
                configureEventViewHolder(eventViewHolder, position);
                break;
            default:
                PostViewHolder vh = (PostViewHolder) holder;
                configurePostViewHolder(vh, position);
                break;
        }

    }

    public void configureEventViewHolder(final EventViewHolder holder, int position) {
        final Post post = mFilteredPosts.get(position);

        try {
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
        }

        if(post.isBookmarked()) {
            holder.ibBookmark.setBackgroundResource(R.drawable.save_filled);
        }

       holder.ivHeart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!post.isLiked()) {
                    post.likePost(ParseUser.getCurrentUser());
                    holder.ivHeart.setBackgroundResource(R.drawable.hot_pink_heart);

                    holder.ivHeart.setBackgroundResource(R.drawable.animation);
                    AnimationDrawable heartStart;
                    heartStart = (AnimationDrawable) holder.ivHeart.getBackground();
                    heartStart.start();

                    post.saveInBackground();

                } else {
                    post.unlikePost(ParseUser.getCurrentUser());
                    holder.ivHeart.setBackgroundResource(R.drawable.heart_logo_vector);

                    holder.ivHeart.setBackgroundResource(R.drawable.animationstop);
                    AnimationDrawable heartStop;
                    heartStop = (AnimationDrawable) holder.ivHeart.getBackground();
                    heartStop.start();

                    post.saveInBackground();
                }
            }
        });

        holder.ibBookmark.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!post.isBookmarked()) {
                    post.bookmarkPost(ParseUser.getCurrentUser());
                    holder.ibBookmark.setBackgroundResource(R.drawable.save_filled);

                    post.saveInBackground();

                } else {
                    post.unbookmarkPost(ParseUser.getCurrentUser());
                    holder.ibBookmark.setBackgroundResource(R.drawable.save);

                    post.saveInBackground();
                }
            }
        });

        ParseFile p = post.getUser().getParseFile("profilePicture");
        if (p != null) {
            Glide.with(context)
                    .load(p.getUrl())
                    .bitmapTransform(new CropCircleTransformation(context))
                    .into(holder.ivUserProfile);
        } else {
            holder.ivUserProfile.setImageResource(R.drawable.profile);
        }

//            holder.ivProfilePic.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    ((TimelineActivity) context).showProfileFragment(post);
//                }
//            });

        holder.tvEventDesc.setText(post.getDescription());

        if (!(post.getImage() == null)){
            Glide.with(context)
                    .load(post.getImage().getUrl())
                    .bitmapTransform(new CenterCrop(context))
                    .into(holder.ivEventImage);
        } else {
            holder.ivEventImage.setVisibility(View.GONE);
        }

        if (!(post.getDay() == null)){
            holder.tvDateOfEvent.setText(post.getDay());
            holder.tvTime.setText(post.getTime());

            holder.tvEventTitle.setText(post.getEventTitle());

        }
    }

    public void configurePostViewHolder(final PostViewHolder holder, int position){
            final Post post = mFilteredPosts.get(position);

            try {
                holder.tvDate.setText(ParseRelativeDate.getRelativeTimeAgo(post.getCreatedAt()));
                holder.tvLocation.setText(post.getAddress());
                holder.tvUserName2.setText(post.getUser().fetchIfNeeded().getUsername());
                holder.tvUserName.setText(post.getUser().fetchIfNeeded().getUsername());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if(post.isLiked()) {
                holder.ivHeart.setBackgroundResource(R.drawable.hot_pink_heart);
            }

        if(post.isBookmarked()) {
            holder.ibBookmark.setBackgroundResource(R.drawable.save_filled);
        }

            holder.ivHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!post.isLiked()) {
                        post.likePost(ParseUser.getCurrentUser());
                        holder.ivHeart.setBackgroundResource(R.drawable.hot_pink_heart);

                        holder.ivHeart.setBackgroundResource(R.drawable.animation);
                        AnimationDrawable heartStart;
                        heartStart = (AnimationDrawable) holder.ivHeart.getBackground();
                        heartStart.start();

                        post.saveInBackground();

                    } else {
                        post.unlikePost(ParseUser.getCurrentUser());
                        holder.ivHeart.setBackgroundResource(R.drawable.heart_logo_vector);

                        holder.ivHeart.setBackgroundResource(R.drawable.animationstop);
                        AnimationDrawable heartStop;
                        heartStop = (AnimationDrawable) holder.ivHeart.getBackground();
                        heartStop.start();

                        post.saveInBackground();
                    }
                }
            });

        holder.ibBookmark.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!post.isBookmarked()) {
                    post.bookmarkPost(ParseUser.getCurrentUser());
                    holder.ibBookmark.setBackgroundResource(R.drawable.save_filled);

                    post.saveInBackground();

                } else {
                    post.unbookmarkPost(ParseUser.getCurrentUser());
                    holder.ibBookmark.setBackgroundResource(R.drawable.save);

                    post.saveInBackground();
                }
            }
        });

            ParseFile p = post.getUser().getParseFile("profilePicture");
            if (p != null) {
                Glide.with(context)
                        .load(p.getUrl())
                        .bitmapTransform(new CropCircleTransformation(context))
                        .into(holder.ivProfilePic);
            } else {
                holder.ivProfilePic.setImageResource(R.drawable.profile);
            }

//            holder.ivProfilePic.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    ((TimelineActivity) context).showProfileFragment(post);
//                }
//            });

            holder.tvDesc.setText(post.getDescription());

            // don't show if null
            if (!(post.getImage() == null)){
                Glide.with(context)
                        .load(post.getImage().getUrl())
                        .bitmapTransform(new CenterCrop(context))
                        .into(holder.ivImage);
            } else {
                holder.ivImage.setVisibility(View.GONE);
            }

        if (!(post.getAddress() == null)){
            holder.tvLocation.setVisibility(View.VISIBLE);
        } else {
            holder.tvLocation.setVisibility(View.GONE);
        }



    }

    @Override
    public int getItemCount() {
        return mFilteredPosts.size();
    }

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

            ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
            tvUserName = (TextView) itemView.findViewById(R.id.tvUser);
            tvUserName2 = (TextView) itemView.findViewById(R.id.tvUser2);
            tvDesc = (TextView) itemView.findViewById(R.id.tvDescription);
            tvDate = (TextView) itemView.findViewById(R.id.tvDate);
            ivProfilePic = (ImageView) itemView.findViewById(R.id.ivProfilePic);
            ivHeart = (ImageView) itemView.findViewById(R.id.ivHeart);
            ibBookmark = (ImageButton) itemView.findViewById(R.id.ibBookmark);
            tvLocation = (TextView) itemView.findViewById(R.id.tvLocation);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // gets item position
            int position = getAdapterPosition();
            // make sure the position is valid, i.e. actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                // get the post at the position, this won't work if the class is static
                Post post = mFilteredPosts.get(position);
                // tell Feed Fragment to start the Details activity
                ((HomeActivity) context).showDetailsFor((Serializable) post);
            }
        }
    }

    public class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

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
            //images
            ivEventImage =  itemView.findViewById(R.id.ivEventImage);
            ivUserProfile =  itemView.findViewById(R.id.ivUserProfile);
            // text views
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

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // gets item position
            int position = getAdapterPosition();
            // make sure the position is valid, i.e. actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                // get the post at the position, this won't work if the class is static
                Post post = mFilteredPosts.get(position);
                // tell Feed Fragment to start the Details activity
                ((HomeActivity) context).showDetailsFor((Serializable) post);
            }
        }
    }
}