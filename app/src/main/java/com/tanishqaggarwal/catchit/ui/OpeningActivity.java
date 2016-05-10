package com.tanishqaggarwal.catchit.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.tanishqaggarwal.catchit.services.HTTPHelper;

import java.util.Calendar;

public class OpeningActivity extends AppCompatActivity {

    private static final String TAG = "Catch It";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences("CatchItPreferences", 0);

        Intent intent;
        if (!prefs.contains("name")) {
            intent = new Intent(this, com.tanishqaggarwal.catchit.ui.setup.InitialStartupActivity.class);
        } else {
            String busRouteName = prefs.getString("busRouteName", "");
            //Look up earliest time in server for that particular bus route
            Bundle paramsBundle = new Bundle();
            paramsBundle.putString("emailAddress", prefs.getString("emailAddress", ""));
            paramsBundle.putString("addressToken", prefs.getString("addressToken", ""));
            paramsBundle.putString("busRouteName", prefs.getString("busRouteName", ""));

            int earliestTime = Integer.MAX_VALUE;
            try {
                earliestTime = Integer.parseInt(new HTTPHelper(paramsBundle, HTTPHelper.GET_EARLIEST_ON_ROUTE).returnResult());
            } catch (Exception e) {
                Log.i(TAG, "No number returned by server");
            }


            int currentTime = 60 * Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + Calendar.getInstance().get(Calendar.MINUTE);

            if (currentTime <= earliestTime) {
                intent = new Intent(this, com.tanishqaggarwal.catchit.ui.TrackLaterActivity.class);
            } else {
                intent = new Intent(this, com.tanishqaggarwal.catchit.ui.TrackMapActivity.class);
            }
        }
        startActivity(intent);
        finish();
    }
}
