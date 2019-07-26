package com.codepath.bigheartapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import com.codepath.bigheartapp.model.Post;

public class FilterActivity extends AppCompatActivity {

    Spinner sMonth;
    Spinner sDay;
    Spinner sYear;
    Button btnFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        sMonth = (Spinner) findViewById(R.id.sMonthFilter);
        sDay = (Spinner) findViewById(R.id.sDayFilter);
        sYear = (Spinner) findViewById(R.id.sYearFilter);
        btnFilter = (Button) findViewById(R.id.btnFilter);

        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backToEvents = new Intent();
                backToEvents.putExtra(Post.KEY_DAY, sDay.getSelectedItem().toString());
                setResult(RESULT_OK, backToEvents);
                finish();
            }
        });
    }
}
