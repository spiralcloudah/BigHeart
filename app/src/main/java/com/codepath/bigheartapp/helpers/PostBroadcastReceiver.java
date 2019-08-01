package com.codepath.bigheartapp.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.codepath.bigheartapp.R;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class PostBroadcastReceiver extends BroadcastReceiver {

    FragmentUpdated fragmentUpdated;

    public PostBroadcastReceiver(FragmentUpdated fragUpdated) {
        fragmentUpdated = fragUpdated;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int resultCode = intent.getIntExtra(context.getString(R.string.result_code), RESULT_CANCELED);

        if (resultCode == RESULT_OK) {
            fragmentUpdated.updatePosts(intent);
        }
    }
}
