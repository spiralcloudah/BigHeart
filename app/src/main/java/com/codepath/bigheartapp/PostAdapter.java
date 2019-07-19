package com.codepath.bigheartapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.bigheartapp.model.Post;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.io.Serializable;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;


public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private List<Post> mPosts;
    Context context;

    int whichFragment;

    //pass in the post array
    public PostAdapter(List<Post> posts, int whichFragment) {
        mPosts = posts;
        this.whichFragment = whichFragment;
    }

    public TextView tvDate;

    // Clean all elements of the recycler
    public void clear() {
        mPosts.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Post> p) {
        mPosts.addAll(p);
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View postView = inflater.inflate(R.layout.item_post, parent, false);
        ViewHolder viewHolder = new ViewHolder(postView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Post post = mPosts.get(position);

            try {
                holder.tvDate.setText(ParseRelativeDate.getRelativeTimeAgo(post.getCreatedAt()));
                holder.tvUserName2.setText(post.getUser().fetchIfNeeded().getUsername());
                holder.tvUserName.setText(post.getUser().fetchIfNeeded().getUsername()); //????

            } catch (ParseException e) {
                e.printStackTrace();
            }


            ParseFile p = post.getUser().getParseFile("profilePicture");
            if (p != null) {
                Glide.with(context)
                        .load(p.getUrl())
                        .bitmapTransform(new CropCircleTransformation(context))
                        .into(holder.ivProfilePic);
            }

//            holder.ibProfilePic.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    ((TimelineActivity) context).showProfileFragment(post);
//                }
//            });

            holder.tvDesc.setText(post.getDescription());

            if (!(post.getImage() == null)){
                Glide.with(context)
                        .load(post.getImage().getUrl())
                        .into(holder.ivImage);
            } else {
                holder.ivImage.setVisibility(View.GONE);
            }

    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView ivProfilePic;
        public ImageView ivImage;
        public TextView tvUserName;
        public TextView tvUserName2;
        public TextView tvDesc;
        public TextView tvDate;


        public ViewHolder(View itemView) {
            super(itemView);

            ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
            tvUserName = (TextView) itemView.findViewById(R.id.tvUser);
            tvUserName2 = (TextView) itemView.findViewById(R.id.tvUser2);
            tvDesc = (TextView) itemView.findViewById(R.id.tvDescription);
            tvDate = (TextView) itemView.findViewById(R.id.tvDate);
            ivProfilePic = (ImageView) itemView.findViewById(R.id.ivProfilePic);

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