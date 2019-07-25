package com.codepath.bigheartapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.codepath.bigheartapp.model.Post;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;


public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Post> mPosts;
    Context context;
    int whichFragment;
    final int TYPE_POST = 101;
    final int TYPE_EVENT = 102;

    //pass in the post array
    public PostAdapter(List<Post> posts, int whichFragment) {
        mPosts = posts;
        this.whichFragment = whichFragment;
    }

    // Clean all elements of the recycler
    public void clear(final View view) {
        mPosts.clear();
        view.post(new Runnable() {
            public void run() {
                // There is no need to use notifyDataSetChanged()
                notifyDataSetChanged();
            }
        });
    }

    // Add a list of items -- change to type used
    public void addAll(List<Post> p, View view) {
        mPosts.addAll(p);
        view.post(new Runnable() {
            public void run() {
                // There is no need to use notifyDataSetChanged()
                notifyDataSetChanged();
            }
        });
    }


    // Clean all elements of the recycler
    public void clear() {
        mPosts.clear();
        notifyDataSetChanged();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();

        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(context);

        switch (viewType) {
            case TYPE_POST:
                View postView = inflater.inflate(R.layout.item_post, parent, false);
                viewHolder = new PostViewHolder(postView);
                break;
            case TYPE_EVENT:
                View eventView = inflater.inflate(R.layout.item_event, parent, false);
                viewHolder = new EventViewHolder(eventView);
                break;
            default:
                View otherView = inflater.inflate(R.layout.item_post, parent, false);
                viewHolder = new PostViewHolder(otherView){
                };
                break;
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        if (!(mPosts.get(position).getIsEvent())) {
            return TYPE_POST;
        } else if (mPosts.get(position).getIsEvent()) {
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
        final Post post = mPosts.get(position);

        try {
            holder.tvDate.setText(ParseRelativeDate.getRelativeTimeAgo(post.getCreatedAt()));
            holder.tvUserName2.setText(post.getUser().fetchIfNeeded().getUsername());
            holder.tvUserName.setText(post.getUser().fetchIfNeeded().getUsername());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(post.isLiked()) {
            holder.ivHeart.setImageResource(R.drawable.hot_pink_heart);
        }

        holder.ivHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!post.isLiked()) {
                    post.likePost(ParseUser.getCurrentUser());
                    holder.ivHeart.setImageResource(R.drawable.hot_pink_heart);

                    post.saveInBackground();

                } else {
                    post.unlikePost(ParseUser.getCurrentUser());
                    holder.ivHeart.setImageResource(R.drawable.heart_logo_vector);

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

        if (!(post.getImage() == null)){
            Glide.with(context)
                    .load(post.getImage().getUrl())
                    .bitmapTransform(new CenterCrop(context))
                    .into(holder.ivImage);
        } else {
            holder.ivImage.setVisibility(View.GONE);
        }

        if (!(post.getMonth() == null)){
            holder.tvMonth.setText(post.getMonth());
            holder.tvDay.setText(post.getDay());
            holder.tvYear.setText(post.getYear());
            holder.tvTime.setText(post.getTime());
            holder.tvTitle.setText(post.getEventTitle());
        }
    }

    public void configurePostViewHolder(final PostViewHolder holder, int position){
            final Post post = mPosts.get(position);

            try {
                holder.tvDate.setText(ParseRelativeDate.getRelativeTimeAgo(post.getCreatedAt()));
                holder.tvUserName2.setText(post.getUser().fetchIfNeeded().getUsername());
                holder.tvUserName.setText(post.getUser().fetchIfNeeded().getUsername());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if(post.isLiked()) {
                holder.ivHeart.setImageResource(R.drawable.hot_pink_heart);
            }

            holder.ivHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!post.isLiked()) {
                        post.likePost(ParseUser.getCurrentUser());
                        holder.ivHeart.setImageResource(R.drawable.hot_pink_heart);

                        post.saveInBackground();

                    } else {
                        post.unlikePost(ParseUser.getCurrentUser());
                        holder.ivHeart.setImageResource(R.drawable.heart_logo_vector);

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

            if (!(post.getImage() == null)){
                Glide.with(context)
                        .load(post.getImage().getUrl())
                        .bitmapTransform(new CenterCrop(context))
                        .into(holder.ivImage);
            } else {
                holder.ivImage.setVisibility(View.GONE);
            }

    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView ivProfilePic;
        public ImageView ivImage;
        public TextView tvUserName;
        public TextView tvUserName2;
        public TextView tvDesc;
        public TextView tvDate;
        public ImageView ivHeart;


        public PostViewHolder(View itemView) {
            super(itemView);

            ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
            tvUserName = (TextView) itemView.findViewById(R.id.tvUser);
            tvUserName2 = (TextView) itemView.findViewById(R.id.tvUser2);
            tvDesc = (TextView) itemView.findViewById(R.id.tvDescription);
            tvDate = (TextView) itemView.findViewById(R.id.tvDate);
            ivProfilePic = (ImageView) itemView.findViewById(R.id.ivProfilePic);
            ivHeart = (ImageView) itemView.findViewById(R.id.ivHeart);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // gets item position
            int position = getAdapterPosition();
            // make sure the position is valid, i.e. actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                // get the post at the position, this won't work if the class is static
                Post post = mPosts.get(position);
                // tell Feed Fragment to start the Details activity
                ((HomeActivity) context).showDetailsFor((Serializable) post);
            }
        }
    }

    public class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView ivProfilePic;
        public ImageView ivImage;
        public TextView tvUserName;
        public TextView tvUserName2;
        public TextView tvDesc;
        public TextView tvDate;
        public ImageView ivHeart;
        public TextView tvMonth;
        public TextView tvDay;
        public TextView tvYear;
        public TextView tvTime;
        public TextView tvTitle;


        public EventViewHolder(View itemView) {
            super(itemView);

            ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
            tvUserName = (TextView) itemView.findViewById(R.id.tvUser);
            tvUserName2 = (TextView) itemView.findViewById(R.id.tvUser2);
            tvDesc = (TextView) itemView.findViewById(R.id.tvDescription);
            tvDate = (TextView) itemView.findViewById(R.id.tvDate);
            ivProfilePic = (ImageView) itemView.findViewById(R.id.ivProfilePic);
            ivHeart = (ImageView) itemView.findViewById(R.id.ivHeart);
            tvMonth = (TextView) itemView.findViewById(R.id.tvMonth);
            tvDay = (TextView) itemView.findViewById(R.id.tvDay);
            tvYear = (TextView) itemView.findViewById(R.id.tvYear);
            tvTime = (TextView) itemView.findViewById(R.id.tvTime);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // gets item position
            int position = getAdapterPosition();
            // make sure the position is valid, i.e. actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                // get the post at the position, this won't work if the class is static
                Post post = mPosts.get(position);
                // tell Feed Fragment to start the Details activity
                ((HomeActivity) context).showDetailsFor((Serializable) post);
            }
        }
    }
}