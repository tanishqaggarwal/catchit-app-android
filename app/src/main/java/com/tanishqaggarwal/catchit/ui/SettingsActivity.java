package com.tanishqaggarwal.catchit.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.tanishqaggarwal.catchit.Constants;
import com.tanishqaggarwal.catchit.R;
import com.tanishqaggarwal.catchit.services.HTTPHelper;
import com.tanishqaggarwal.catchit.ui.utils.CustomTimePicker;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "Catch It";
    private Spinner busRouteName;
    private Spinner busRouteStop;
    private CustomTimePicker busRouteTime;
    private Switch locationServicesEnabled;
    private TextView emailName;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        busRouteName = (Spinner) findViewById(R.id.settings_bus_route_name);
        busRouteStop = (Spinner) findViewById(R.id.settings_bus_route_stop);
        busRouteTime = (CustomTimePicker) findViewById(R.id.settings_bus_route_time);
        locationServicesEnabled = (Switch) findViewById(R.id.settings_location_services);
        emailName = (TextView) findViewById(R.id.settings_email_name);

        prefs = getSharedPreferences("CatchItPreferences", 0);
        busRouteName.setSelection(((ArrayAdapter<String>) busRouteName.getAdapter()).getPosition(prefs.getString("busRouteName", "Select a Bus Route...")));
        busRouteStop.setSelection(((ArrayAdapter<String>) busRouteName.getAdapter()).getPosition(prefs.getString("busRouteStop", "Select a School...")));

        busRouteTime.setCurrentHour(prefs.getInt("busRouteTimeHour", 16));
        busRouteTime.setCurrentMinute(prefs.getInt("busRouteTimeMinute", 0));

        locationServicesEnabled.setChecked(prefs.getBoolean("locationServicesEnabled", true));

        emailName.setText(String.format("Signed in as: %s", prefs.getString("emailAddress", "")));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_settings:
                if (busRouteName.getSelectedItem().toString().equals("Select a Bus Route...")) {
                    Toast.makeText(SettingsActivity.this, "You need to select a valid bus route!", Toast.LENGTH_SHORT).show();
                    break;
                }
                if (busRouteStop.getSelectedItem().toString().equals("Select a School...")) {
                    Toast.makeText(SettingsActivity.this, "You need to select a valid school!", Toast.LENGTH_SHORT).show();
                    break;
                }
                int time = busRouteTime.getCurrentHour() * 60 + busRouteTime.getCurrentMinute();
                if (time < 945 || time > 1110) {
                    Toast.makeText(SettingsActivity.this, "You need to select a valid bus pickup time!", Toast.LENGTH_SHORT).show();
                    break;
                }
                new PreferenceEditorTask().execute(
                        busRouteName.getSelectedItem().toString(),
                        busRouteStop.getSelectedItem().toString(),
                        String.format("%d", busRouteTime.getCurrentHour()),
                        String.format("%d", busRouteTime.getCurrentMinute()),
                        locationServicesEnabled.isChecked() ? "true" : "false");

                startActivity(new Intent(this, OpeningActivity.class));
                finish();
                break;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void signOut(View v) {
        prefs.edit().clear().apply();
        startActivity(new Intent(this, OpeningActivity.class));
        finish();
    }

    private class PreferenceEditorTask extends AsyncTask<String, Void, Bundle> {

        protected Bundle doInBackground(String... params) {
            Bundle returnBundle = new Bundle();
            returnBundle.putString("status", "failure");

            String busRouteName = params[0];
            String busRouteStop = params[1];
            String busRouteTimeHour = params[2];
            String busRouteTimeMinute = params[3];
            String locationServices = params[4];

            String emailAddress = prefs.getString("emailAddress", "");
            String addressToken = prefs.getString("addressToken", "");

            Bundle paramsBundle = new Bundle();
            paramsBundle.putString("emailAddress", emailAddress);
            paramsBundle.putString("addressToken", addressToken);
            paramsBundle.putString("busRouteName", busRouteName);
            paramsBundle.putString("busRouteStop", busRouteStop);
            paramsBundle.putInt("busRouteTimeHour", Integer.parseInt(busRouteTimeHour));
            paramsBundle.putInt("busRouteTimeMinute", Integer.parseInt(busRouteTimeMinute));
            paramsBundle.putBoolean("locationServicesEnabled", locationServices == "true");

            String result = new HTTPHelper(paramsBundle, HTTPHelper.EDIT_PREFERENCES).returnResult();
            if (result.equals("settings change successful")) {
                returnBundle.putString("status", "success");
                SharedPreferences.Editor prefEditor = prefs.edit();
                prefEditor.putString("busRouteName", busRouteName);
                prefEditor.putString("busRouteStop", busRouteStop);
                prefEditor.putInt("busRouteTimeHour", Integer.parseInt(busRouteTimeHour));
                prefEditor.putInt("busRouteTimeMinute", Integer.parseInt(busRouteTimeMinute));
                prefEditor.putInt("busRouteTime", 60 * Integer.parseInt(busRouteTimeHour) + Integer.parseInt(busRouteTimeMinute));

                prefEditor.putBoolean("locationServicesEnabled", locationServices == "true");


                float latitude = 0;
                float longitude = 0;
                switch (busRouteStop) {
                    case "High School South":
                        latitude = Constants.SOUTH_LATITUDE;
                        longitude = Constants.SOUTH_LONGITUDE;
                        break;
                    case "High School North":
                        latitude = Constants.NORTH_LATITUDE;
                        longitude = Constants.NORTH_LONGITUDE;
                        break;
                    case "Grover Middle School":
                        latitude = Constants.GROVER_LATITUDE;
                        longitude = Constants.GROVER_LONGITUDE;
                        break;
                    case "Community Middle School":
                        latitude = Constants.COMMUNITY_LATITUDE;
                        longitude = Constants.COMMUNITY_LONGITUDE;
                        break;
                }
                prefEditor.putFloat("busRouteStopLatitude", latitude);
                prefEditor.putFloat("busRouteStopLongitude", longitude);

                prefEditor.apply();

            } else {
                returnBundle.putString("reason", result);
            }

            return returnBundle;
        }

        protected void onPostExecute(Bundle result) {
            if (result.getString("status").equals("success")) {
                Toast.makeText(SettingsActivity.this, "Settings saved successfully.", Toast.LENGTH_SHORT).show();
            } else {
                String reason = result.getString("reason");
                if (reason == null || reason.equals(""))
                    reason = "Unknown error caused settings editing to fail";
                Toast.makeText(SettingsActivity.this, String.format("Settings not saved successfully: %s", reason), Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }
}
