package com.tanishqaggarwal.catchit.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.tanishqaggarwal.catchit.R;
import com.tanishqaggarwal.catchit.services.HTTPHelper;

public class TrackLaterActivity extends AppCompatActivity {

    private TextView activityText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_later);

        activityText = (TextView) findViewById(R.id.track_later_text);

        Bundle paramsBundle = new Bundle();
        paramsBundle.putString("busRouteName", getSharedPreferences("CatchItPreferences", 0).getString("busRouteName", ""));
        paramsBundle.putString("emailAddress", getSharedPreferences("CatchItPreferences", 0).getString("emailAddress", ""));
        paramsBundle.putString("addressToken", getSharedPreferences("CatchItPreferences", 0).getString("addressToken", ""));

        String getResult = new HTTPHelper(paramsBundle, HTTPHelper.GET_EARLIEST_ON_ROUTE).returnResult();
        try {
            int result = Integer.parseInt(getResult);
            int resultHour = (result / 60) % 12;
            int resultMinute = result % 60;
            String resultTime = String.format("%02d:%02d", resultHour, resultMinute);
            String tryLaterTxt = String.format("No one using Catch It is currently on the bus you take. The earliest anyone will be on your bus is %s PM. Come back to this app then!", resultTime);
            activityText.setText(tryLaterTxt);
        } catch (Exception e) {
            activityText.setText("No one using Catch It is currently on the bus you take. Come back to this app later!");
        }
    }

    protected void onPause() {
        super.onPause();
        finish();
    }
}
