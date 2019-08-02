package com.codepath.bigheartapp;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.text.DateFormatSymbols;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Spinner;
import com.codepath.bigheartapp.model.Post;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FilterActivity extends AppCompatActivity {

    // Create strings and buttons for filtering
    Button btnFilter;
    private Button btnDatePicker;
    private Button btnTimePicker;
    private Spinner spDistance;
    public String day;
    public String time;
    public int ampm;

    // Set the date and time picker listeners
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private TimePickerDialog.OnTimeSetListener timeSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        // Set buttons to their corresponding IDs
        btnFilter = (Button) findViewById(R.id.btnFilter);
        btnDatePicker = findViewById(R.id.btnDatePicker);
        btnTimePicker = findViewById(R.id.btnTimePicker);
        spDistance = (Spinner) findViewById(R.id.spDistance);

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
                DatePickerDialog dialog = new DatePickerDialog(FilterActivity.this,
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
                TimePickerDialog dialog = new TimePickerDialog(FilterActivity.this, timeSetListener,
                        hour, minute, DateFormat.is24HourFormat(FilterActivity.this));
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
                    time = new SimpleDateFormat("K:mm a").format(dateObj);
                } catch (java.text.ParseException e) {
                    e.printStackTrace();
                }
                btnTimePicker.setText(time);
            }
        };

        // Set onClickListener for the filter button
        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Double dist;
                String distance = spDistance.getSelectedItem().toString();
                if (!distance.equals("")) {
                    dist = Double.parseDouble(distance.substring(0, distance.indexOf(" ")));
                } else {
                    dist = 0.0;
                }


                // Create a new intent that takes user to the filtered event fragment
                Intent backToEvents = new Intent();
                backToEvents.putExtra(Post.KEY_DAY, day);
                backToEvents.putExtra(Post.KEY_TIME, time);
                backToEvents.putExtra(Post.KEY_LOCATION, dist);
                setResult(RESULT_OK, backToEvents);
                finish();
            }
        });
    }
}