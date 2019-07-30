package com.codepath.bigheartapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    // Create variables for Login page
    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set variables to corresponding IDs
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignUp);

        // User persistence
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser != null) {

            // Notify user and change activities to the Home Activity
            Toast.makeText(getApplicationContext(), "Welcome back " + currentUser.getUsername() + "!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(i);
            finish();
        }

        // onClickListener for the login button
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set the username and password to the ones entered by the user; login user
                final String username = etUsername.getText().toString();
                final String password = etPassword.getText().toString();
                loginUser(username, password);
            }
        });

        // onClickListener for the signup button
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Switch to Register Activity
                Intent toRegister = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(toRegister);
                finish();
            }
        });
    }

    private void loginUser(String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if ( e == null ) {
                    // If Login is successful, bring user to Home Activity
                    Intent toHome = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(toHome);
                    finish();
                } else {
                    // If Login failed, notify user
                    Toast.makeText(getApplicationContext(), "Invalid username or password", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}