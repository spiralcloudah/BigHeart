package com.codepath.bigheartapp.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.codepath.bigheartapp.R;
import com.codepath.bigheartapp.model.Post;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class PostBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        int resultCode = intent.getIntExtra(context.getString(R.string.result_code), RESULT_CANCELED);
//
//        if (resultCode == RESULT_OK) {
//            Post postChanged = (Post) intent.getSerializableExtra(Post.class.getSimpleName());
//            int indexOfChange = -1;
//            for (int i = 0; i < posts.size(); i++) {
//                if (posts.get(i).hasSameId(postChanged)) {
//                    indexOfChange = i;
//                    break;
//                }
//            }
//            if (indexOfChange != -1) {
//                posts.set(indexOfChange, postChanged);
//                adapter.notifyItemChanged(indexOfChange);
//            } else {
//                Toast.makeText(context, "An error occurred", Toast.LENGTH_LONG).show();
//            }
//
//        }
    }
}
