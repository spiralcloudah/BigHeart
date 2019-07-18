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
import com.parse.SignUpCallback;

import java.io.File;

public class RegisterActivity extends AppCompatActivity {

    // instantiate layout variables
    EditText etFirstName;
    EditText etLastName;
    EditText etEmail;
    EditText etUsername;
    EditText etPassword;
    Button btnRegister;
    Button btnLogin;
    Button btnAddProfilePic;
    ImageView ivProfilePic;

    public final String APP_TAG = "BigHeart";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "profile.jpg";
    File photoFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //find pointers to ids for edit text views in layout
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        ivProfilePic = findViewById(R.id.ivProfilePic);

        //find pointers to ids for button views in layout
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);
        btnAddProfilePic = findViewById(R.id.btnAddProfilePic);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String firstName = etFirstName.getText().toString();
                final String lastName = etLastName.getText().toString();
                final String email = etEmail.getText().toString();
                final String username = etUsername.getText().toString();
                final String password = etPassword.getText().toString();

                if (photoFile == null || ivProfilePic.getDrawable() == null) {
                    Log.d("HomeActivity", "No image imported");
                    Toast.makeText(RegisterActivity.this, "You haven't taken a photo yet :/. Try again", Toast.LENGTH_LONG).show();
                    return;
                }

                registerUser(firstName, lastName, email, username, password, photoFile);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToLogin();
            }
        });

        btnAddProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLaunchCam(v);
                //fires camera intent to capture photo
                // captured photo goes into image view
            }
        });


    }

    private void registerUser(String firstName, String lastName, String email, String username, String password, File profilePic) {
        //create new parse user
        ParseUser user = new ParseUser();

        // insert string values into User ParseObject
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        // insert Image file into User ParseObject
        ParseFile profileImg = new ParseFile(profilePic);
        profileImg.saveInBackground();
        user.put("profilePicture", new ParseFile(profilePic));




        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if ( e == null ) {
                    Log.d("RegisterActivity", "Register Success!");

                    Intent toTimeline = new Intent(RegisterActivity.this, HomeActivity.class);
                    startActivity(toTimeline);
                    finish();
                } else {
                    Log.d("RegisterActivity", "Register Failure");
                    e.printStackTrace();
                }
            }
        });
    }

    private void backToLogin() {
        Intent backToLogin = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(backToLogin);
        finish();
    }

    private void onLaunchCam(View v) {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference to access to future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(RegisterActivity.this, "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
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
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // RESIZE BITMAP, see section below
                // Load the taken image into a preview
                // ImageView ivPreview = (ImageView) findViewById(R.id.ivPreview);
                ivProfilePic.setImageBitmap(takenImage);
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
