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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.codepath.bigheartapp.model.Post;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import cz.msebera.android.httpclient.Header;

import static java.lang.Double.parseDouble;

public class ComposeActivity extends AppCompatActivity {

    // instantiate layout properties...
    private Button btnPost;
    private EditText etDescription;
    private ImageView ivPicture;
    private Button btnAddPic;
    private Spinner sMonth;
    private Spinner sDay;
    private Spinner sYear;
    private Spinner sTime;
    private Spinner sAmPm;
    private EditText etLocation;
    private Switch switchEvent;

    //instantiate vars for image capture
    public final String APP_TAG = "Big<3";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "photo.jpg";
    public boolean isEvent = false;
    File photoFile;

    // API key and URL information..
    // TODO - store in a more secure place!
    private String API_KEY = "AIzaSyBlqBLcO4u2GXQ8utsYRlsV55kmCavovfI";
    private String BASE_URL = "https://maps.googleapis.com/maps/api/geocode/json?";

    // instantiate vars that will store retrieved lat and long coordinates
    public String lat;
    public String lng;
    public String address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        //create pointers to layout id values
        ivPicture = findViewById(R.id.ivPicture);
        etDescription = findViewById(R.id.etDescription);
        btnPost = findViewById(R.id.btnPost);
        sMonth = findViewById(R.id.sMonth);
        sDay = findViewById(R.id.sDay);
        sYear = findViewById(R.id.sYear);
        sTime = findViewById(R.id.sTime);
        sAmPm = findViewById(R.id.sAmPm);
        etLocation = findViewById(R.id.etLocation);
        switchEvent = findViewById(R.id.switchEvent);
        btnAddPic = findViewById(R.id.btnAddImage);

        //simple function that reveals extra input fields if post is an event.
        switchEvent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    sMonth.setVisibility(View.VISIBLE);
                    sDay.setVisibility(View.VISIBLE);
                    sYear.setVisibility(View.VISIBLE);
                    sTime.setVisibility(View.VISIBLE);
                    sAmPm.setVisibility(View.VISIBLE);
                    isEvent = true;
                } else {
                    sMonth.setVisibility(View.GONE);
                    sDay.setVisibility(View.GONE);
                    sYear.setVisibility(View.GONE);
                    sTime.setVisibility(View.GONE);
                    sAmPm.setVisibility(View.GONE);
                    isEvent = false;
                }
            }
        });

        btnAddPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLaunchCamera();
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create final strings to be passed into database
                final String description = etDescription.getText().toString();
                final ParseUser user = ParseUser.getCurrentUser();
                final String month = sMonth.getSelectedItem().toString();
                final String day = sDay.getSelectedItem().toString();
                final String time = sTime.getSelectedItem().toString() + " " + sAmPm.getSelectedItem().toString();
                final String year = sYear.getSelectedItem().toString();
                address = etLocation.getText().toString();
                final String location = etLocation.getText().toString().replace(" ","+");


                // run function that calls to API and creates post
                // TODO - (Gene) is this bad code writing? Could i break this function up into 2?
                createPostWithCoords(description, user, month, day, year, time, location, address);

            }
        });

    }

    private void createPostWithCoords(final String description, final ParseUser user, final String month, final String day, final String year, final String time, final String location, final String address) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("address", location );
        params.put("key", API_KEY );
        client.get(BASE_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.d("ComposeActivity", "Request Success!");
                    // retrieve json lat and long coordinates
                    lat = ((JSONArray)response.get("results")).getJSONObject(0).getJSONObject("geometry")
                            .getJSONObject("location").get("lat").toString();


                    lng = ((JSONArray)response.get("results")).getJSONObject(0).getJSONObject("geometry")
                            .getJSONObject("location").get("lng").toString();

                    // TODO - for some reason, retrieving the formatted address has an infinite/veryy long runtime. For now i will  just have the String location be what the user inputs.
                    // address = ((JSONArray)response.get("results")).getJSONObject(0).getJSONObject("formatted_address").toString();

                    // since async API call Post object must me created within the onSuccess function to ba able to access lat and lng data.
                    Post newPost = new Post();

                    // set values to post object
                    newPost.setDescription(description);
                    newPost.setUser(user);
                    newPost.setAddress(address);

                    newPost.setIsEvent(isEvent);

                    // if post is an event, then date information will be updated in Parse DB
                    if(isEvent) {
                        newPost.setDay(day);
                        newPost.setMonth(month);
                        newPost.setYear(year);
                        newPost.setTime(time);
                    }

                    // create new ParseGeopoint to store lat and lng as doubles...
                    ParseGeoPoint coordinates = new ParseGeoPoint(parseDouble(lat),parseDouble(lng));
                    newPost.setLocation(coordinates);

                    // checks for optional photo, if photo exists, adds to post object.
                    final File file = photoFile;
                    if (file != null ) {
                        ParseFile postPic = new ParseFile(file);
                        postPic.saveInBackground();
                        newPost.setImage(postPic);
                    }

                    // finally save post object to parse database
                    newPost.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(ComposeActivity.this, "Successfully posted", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                e.printStackTrace();
                            }
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }


            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("ComposeActivity", "Request Failure.");
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("ComposeActivity", "Request Success!");
                throwable.printStackTrace();
            }


        });

    }


    public void onLaunchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference to access to future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(ComposeActivity.this, "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(ComposeActivity.this.getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(ComposeActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

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
                // Load the taken image into a preview
                ivPicture.setImageBitmap(takenImage);
                ivPicture.setVisibility(View.VISIBLE);

            } else { // Result was a failure
                Toast.makeText(ComposeActivity.this, "No picture taken", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
