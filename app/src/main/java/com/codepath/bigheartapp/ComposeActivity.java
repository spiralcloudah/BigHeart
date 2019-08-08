package com.codepath.bigheartapp;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.DateFormatSymbols;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TimePicker;
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
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

import static java.lang.Double.parseDouble;

public class ComposeActivity extends AppCompatActivity {

    // instantiate layout properties...

    private Button btnPost;
    private EditText etDescription;
    private ImageView ivPicture;
    private Button btnAddPic;
    private Button btnDatePicker;
    private EditText etLocation;
    private Switch switchEvent;
    private EditText etEventTitle;
    private Button btnTimePicker;
    private ImageView ivCalendar;
    private ImageView ivClock;

    // Set the date and time picker listeners
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private TimePickerDialog.OnTimeSetListener timeSetListener;

    // Instantiate vars for image capture
    public final String APP_TAG = "Big<3";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "photo.jpg";
    File photoFile;

    // Default post is not an event
    public boolean isEvent = false;

    // API key and URL information..
    private String API_KEY = "AIzaSyBlqBLcO4u2GXQ8utsYRlsV55kmCavovfI";
    private String BASE_URL = "https://maps.googleapis.com/maps/api/geocode/json?";

    // Instantiate vars that will store retrieved lat and long coordinates
    public String lat;
    public String lng;
    public String day;
    public String time;
    public int ampm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        // Create pointers to layout id values
        ivPicture = findViewById(R.id.ivPicture);
        etDescription = findViewById(R.id.etDescription);
        btnPost = findViewById(R.id.btnPost);
        btnDatePicker = findViewById(R.id.btnDateChooser);
        etLocation = findViewById(R.id.etLocation);
        switchEvent = findViewById(R.id.switchEvent);
        btnAddPic = findViewById(R.id.btnAddImage);
        etEventTitle = findViewById(R.id.etEventTitle);
        btnTimePicker = findViewById(R.id.btnTimeChooser);
        ivCalendar = findViewById(R.id.ivCalendar);
        ivClock = findViewById(R.id.ivClock);

        // Function that reveals extra input fields if post is an event
        switchEvent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etEventTitle.setVisibility(View.VISIBLE);
                    etEventTitle.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
                    btnDatePicker.setVisibility(View.VISIBLE);
                    btnTimePicker.setVisibility(View.VISIBLE);
                    ivCalendar.setVisibility(View.VISIBLE);
                    ivClock.setVisibility(View.VISIBLE);
                    isEvent = true;
                } else {
                    etEventTitle.setVisibility(View.GONE);
                    btnDatePicker.setVisibility(View.GONE);
                    btnTimePicker.setVisibility(View.GONE);
                    ivCalendar.setVisibility(View.GONE);
                    ivClock.setVisibility(View.GONE);
                    isEvent = false;
                }
            }
        });

        // onClickListener to launch camera when button is clicked
        btnAddPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLaunchCamera();
            }
        });

        // onClickListener to post a post when button is clicked
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create final strings to be passed into database
                final String description = etDescription.getText().toString();
                final ParseUser user = ParseUser.getCurrentUser();
                final String eventTitle = etEventTitle.getText().toString();
                final String location = etLocation.getText().toString().replace(" ", "+");
                final String address = etLocation.getText().toString();

                // If there is no address provided...
                if (address.equals("")) {
                    Toast.makeText(getApplicationContext(), "Must select a location!", Toast.LENGTH_LONG).show();

                    // If there is no date provided...
                } else if (isEvent && day == null) {
                    Toast.makeText(getApplicationContext(), "Must select a date!", Toast.LENGTH_LONG).show();

                    // If there is no time provided...
                } else if (isEvent && time == null) {
                    Toast.makeText(getApplicationContext(), "Must specify a time!", Toast.LENGTH_LONG).show();

                    // If there is no event title provided...
                } else if (isEvent && eventTitle.equals("")) {
                    Toast.makeText(getApplicationContext(), "Must give event a title!", Toast.LENGTH_LONG).show();
                } else {

                    // run function that calls to API and creates post
                    // TODO - (Gene) is this bad code writing? Could i break this function up into 2?
                    createPostWithCoords(description, user, day, time, location, eventTitle, address);
                }
            }
        });

        // Set the onClickListener for the date picker
        btnDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();

                // Set variables for the date
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                // Create a new date picker dialog
                DatePickerDialog dialog = new DatePickerDialog(ComposeActivity.this,
                        dateSetListener, year, month, day);
                dialog.show();
            }
        });

        // Set the new date to the one selected
        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @SuppressLint("NewApi")
            @Override
            public void onDateSet(DatePicker view, int pickerYear, int pickerMonth, int dayOfMonth) {
                day = new DateFormatSymbols().getMonths()[pickerMonth] + " " + dayOfMonth + ", " + pickerYear;
                btnDatePicker.setText(day);
            }
        };

        // Set the onClickListener for the time picker
        btnTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();

                // Set variables for the time
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                ampm = calendar.get(Calendar.AM_PM);

                // Create a new time picker dialog
                TimePickerDialog dialog = new TimePickerDialog(ComposeActivity.this, timeSetListener,
                        hour, minute, DateFormat.is24HourFormat(ComposeActivity.this));
                dialog.show();
            }
        });

        // Set the new time to the one selected
        timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String preFormat = hourOfDay + ":" + minute;
                try {
                    final SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
                    final Date dateObj = sdf.parse(preFormat);
                    time = new SimpleDateFormat("h:mm a").format(dateObj);
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }
                btnTimePicker.setText(time);
            }
        };
    }

    private void createPostWithCoords(final String description, final ParseUser user, final String day, final String time, final String location, final String eventTitle, final String address) {

        // Set up the client and params
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("address", location);
        params.put("key", API_KEY);
        client.get(BASE_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {

                    // retrieve json lat and long coordinates
                    lat = ((JSONArray) response.get("results")).getJSONObject(0).getJSONObject("geometry")
                            .getJSONObject("location").get("lat").toString();
                    lng = ((JSONArray) response.get("results")).getJSONObject(0).getJSONObject("geometry")
                            .getJSONObject("location").get("lng").toString();
                } catch (JSONException e) {
                    lat = null;
                    lng = null;
                }
                // TODO - for some reason, retrieving the formatted address has an infinite/veryy long runtime. I will  just have the String location be what the user inputs.
                // address = ((JSONArray)response.get("results")).getJSONObject(0).getJSONObject("formatted_address").toString();
                // since async API call Post object must me created within the onSuccess function to ba able to access lat and lng data.
                final Post newPost = new Post();

                // set values to post object
                newPost.setDescription(description);
                newPost.setUser(user);
                newPost.setAddress(address);
                newPost.setIsEvent(isEvent);

                // if post is an event, then date information will be updated in Parse DB
                if (isEvent) {
                    newPost.setDay(day);
                    newPost.setTime(time);
                    newPost.setEventTitle(eventTitle);
                }

                if(lat != null && lng != null) {
                    // create new ParseGeopoint to store lat and lng as doubles...
                    ParseGeoPoint coordinates = new ParseGeoPoint(parseDouble(lat), parseDouble(lng));
                    newPost.setLocation(coordinates);
                } else {
                    ParseGeoPoint coordinates = new ParseGeoPoint(89.151011, 160.502924);
                    newPost.setLocation(coordinates);
                }

                // checks for optional photo, if photo exists, adds to post object.
                final File file = photoFile;
                if (file != null) {
                    ParseFile postPic = new ParseFile(file);
                    postPic.saveInBackground();
                    newPost.setImage(postPic);
                }

                // finally save post object to parse database
                newPost.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            if (isEvent)
                                Toast.makeText(ComposeActivity.this, "Successfully posted event!", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(ComposeActivity.this, "Successfully posted!", Toast.LENGTH_SHORT).show();
                            Intent backHome = new Intent();
                            backHome.putExtra(Post.class.getSimpleName(), (Serializable) newPost);
                            setResult(RESULT_OK, backHome);
                            finish();
                        } else {
                            if (isEvent)
                                Toast.makeText(ComposeActivity.this, "Failed to post event", Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(ComposeActivity.this, "Failed to post", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(ComposeActivity.this, "Failed to post event", Toast.LENGTH_LONG).show();
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Toast.makeText(ComposeActivity.this, "Failed to post event", Toast.LENGTH_LONG).show();
                throwable.printStackTrace();
            }
        });
    }

    // Function to launch a camera for picture taking
    public void onLaunchCamera() {

        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Create a File reference to access to future access
        photoFile = getPhotoFileUri(photoFileName);
        Uri fileProvider = FileProvider.getUriForFile(ComposeActivity.this, "com.codepath.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // Check to see if intent is null
        if (intent.resolveActivity(ComposeActivity.this.getPackageManager()) != null) {

            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    public File getPhotoFileUri(String fileName) {

        // Get safe storage directory for photos
        File mediaStorageDir = new File(ComposeActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
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

                // camera photo on disk set to the bitmap
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

                // Load the taken image into a preview
                ivPicture.setImageBitmap(takenImage);
                ivPicture.setVisibility(View.VISIBLE);

                // Result was a failure
            } else {
                photoFile = null;
            }
        }
    }
}