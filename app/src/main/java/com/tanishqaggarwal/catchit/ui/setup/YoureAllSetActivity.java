package com.tanishqaggarwal.catchit.ui.setup;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.tanishqaggarwal.catchit.R;

public class YoureAllSetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youre_all_set);
    }

    public void exitApp(View v) {
        finish();
    }
}
