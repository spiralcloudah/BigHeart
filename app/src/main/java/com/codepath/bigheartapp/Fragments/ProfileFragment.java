package com.codepath.bigheartapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codepath.bigheartapp.MainActivity;
import com.codepath.bigheartapp.ProfilePagesAdapter;
import com.codepath.bigheartapp.R;
import com.parse.ParseFile;
import com.parse.ParseUser;

import jp.wasabeef.glide.transformations.CropCircleTransformation;




public class ProfileFragment extends Fragment {

    // Store variables to use in the event fragment

    Button logoutBtn;
    ImageView ivCurrentProfile;
    TextView tvCurrentUser;
    private TabLayout tabLayout;
    private ViewPager viewPager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // Sets variables to corresponding xml layouts
        logoutBtn = view.findViewById(R.id.btnLogout);
        ivCurrentProfile = view.findViewById(R.id.ivCurrentProfile);

        // Get the current user's username and display it
        final String currentUser = ParseUser.getCurrentUser().getUsername();
        System.out.println("The current user is "+ currentUser);
        tvCurrentUser = (TextView) view.findViewById(R.id.tvCurrentUser);
        tvCurrentUser.setText(currentUser);

        // Set the profile picture for the current user
        ParseFile p = ParseUser.getCurrentUser().getParseFile("profilePicture");
        if(p != null) {
            Glide.with(getContext())
                    .load(p.getUrl())

                    // Profile picture will be shown with a circle border rather than a rectangle
                    .bitmapTransform(new CropCircleTransformation(getContext()))
                    .into(ivCurrentProfile);
        }



        tabLayout = view.findViewById(R.id.tabLayout);


        // gets view pager and sets its PageAdapter so it can display items
        viewPager = view.findViewById(R.id.vpContainer);
        viewPager.setAdapter(new ProfilePagesAdapter(getChildFragmentManager(), getContext()));


        //give TabLayout the ViewPager
        tabLayout.setupWithViewPager(viewPager);
        //set icon images when creating tabs;
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_bookmark_border_black_24dp);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_format_list_bulleted_black_24dp);


        // Set the onClickListener for the logout button
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) { logoutUser(v); }
        });

    }


    // Function to logout the current user
    public void logoutUser(View view) {
        Toast.makeText(getContext(), ParseUser.getCurrentUser().getUsername() + " is now logged out", Toast.LENGTH_LONG).show();
        ParseUser.logOut();

        // Change activities back to the login screen
        Intent i = new Intent(getContext(), MainActivity.class);
        startActivity(i);
        getActivity().finish();
    }
}