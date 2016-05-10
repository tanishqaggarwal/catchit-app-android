package com.tanishqaggarwal.catchit.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.tanishqaggarwal.catchit.R;

public class BusHasArrivedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_has_arrived);
    }

    public void exitApp(View v) {
        finish();
    }
}
