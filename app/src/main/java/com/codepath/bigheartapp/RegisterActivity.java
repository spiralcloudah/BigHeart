package com.codepath.bigheartapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.io.File;

public class RegisterActivity extends AppCompatActivity {

    // Instantiate layout variables
    EditText etFirstName;
    EditText etLastName;
    EditText etEmail;
    EditText etUsername;
    EditText etPassword;
    Button btnRegister;
    Button btnLogin;
    Button btnAddProfilePic;
    ImageView ivProfilePic;

    // Declare variables for use in register activity
    public final String APP_TAG = "BigHeart";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "profile.jpg";
    File photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Find pointers to IDs for text/image views in layout
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        ivProfilePic = findViewById(R.id.ivProfilePic);

        // Find pointers to ids for button views in layout
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);
        btnAddProfilePic = findViewById(R.id.btnAddProfilePic);

        // onClickListener for the register button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Set final variables to the entered information
                final String firstName = etFirstName.getText().toString();
                final String lastName = etLastName.getText().toString();
                final String email = etEmail.getText().toString();
                final String username = etUsername.getText().toString();
                final String password = etPassword.getText().toString();

                // Tell user to take a profile picture
                 if (photoFile == null || ivProfilePic.getDrawable() == null) {
                    Toast.makeText(RegisterActivity.this, "Please take a profile picture", Toast.LENGTH_LONG).show();
                    return;
                }

                 // Calls a function to register user
                registerUser(firstName, lastName, email, username, password, photoFile);
            }
        });

        // Login button to take user back to login page
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToLogin();
            }
        });

        // button to add a profile picture; fires camera intent to capture photo
        btnAddProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLaunchCam(v);
            }
        });
    }

    // Function to register the user
    private void registerUser(String firstName, String lastName, String email, String username, String password, File profilePic) {

        // Create new parse user
        final ParseUser user = new ParseUser();

        // Insert string values into User ParseObject
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.put("firstName", firstName);
        user.put("lastName", lastName);

        // Insert Image file into User ParseObject
        final ParseFile profileImg = new ParseFile(profilePic);
        profileImg.saveInBackground(new SaveCallback() {
            public void done(ParseException e) {

                // If successful, add file to user and signUpInBackground
                if(e == null) {
                    user.put("profilePicture", profileImg);
                    loginUser(user);
                } else {
                    loginUser(user);
                }
            }
        });
    }

    // Function to login the user
    public void loginUser(ParseUser user) {
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if ( e == null ) {
                    Log.d("RegisterActivity", "Register Success!");

                    // If login successful, bring the user to the Home Activity
                    Intent toTimeline = new Intent(RegisterActivity.this, HomeActivity.class);
                    startActivity(toTimeline);
                    finish();
                } else {

                    // If login unsuccessful, let user know unable to register
                    Log.d("RegisterActivity", "Register Failure");
                    e.printStackTrace();
                }
            }
        });
    }

    // Function to bring user back to the login page (Main Activity)
    private void backToLogin() {
        Intent backToLogin = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(backToLogin);
        finish();
    }
    private void onLaunchCam(View v) {

        // Create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Create a File reference to access to future access
        photoFile = getPhotoFileUri(photoFileName);

        // Wrap File object into a content provider
        Uri fileProvider = FileProvider.getUriForFile(RegisterActivity.this, "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // So as long as the result is not null, call the intent
        if (intent.resolveActivity(getPackageManager()) != null) {

            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {

        // Get safe storage directory for photos
        File mediaStorageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(APP_TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);
        return file;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

                // Set the profile picture to the image taken
                ivProfilePic.setImageBitmap(takenImage);
            } else {
                // Result was a failure; notify user
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_LONG).show();
            }
        }
    }
}