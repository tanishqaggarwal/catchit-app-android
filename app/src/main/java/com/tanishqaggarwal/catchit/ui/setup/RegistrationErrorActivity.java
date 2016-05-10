package com.tanishqaggarwal.catchit.ui.setup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.tanishqaggarwal.catchit.R;

public class RegistrationErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_error);
    }

    public void exitApp(View v) {
        finish();
    }

    public void goBackToSetup(View v) {
        Intent intent = new Intent(this, SetupActivity.class);
        startActivity(intent);
        finish();
    }
}
