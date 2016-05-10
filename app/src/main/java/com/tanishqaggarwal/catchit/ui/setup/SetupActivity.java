package com.tanishqaggarwal.catchit.ui.setup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.tanishqaggarwal.catchit.Constants;
import com.tanishqaggarwal.catchit.R;
import com.tanishqaggarwal.catchit.services.HTTPHelper;

import java.util.Locale;

import static com.tanishqaggarwal.catchit.services.ValidationUtils.checkEmail;
import static com.tanishqaggarwal.catchit.services.ValidationUtils.checkEmailEmpty;
import static com.tanishqaggarwal.catchit.services.ValidationUtils.checkEmailValid;
import static com.tanishqaggarwal.catchit.services.ValidationUtils.checkPasscode;
import static com.tanishqaggarwal.catchit.services.ValidationUtils.checkPasscodeEmpty;

public class SetupActivity extends AppCompatActivity {

    private static final String TAG = "Catch It";
    private EditText mEmailAddress;
    private EditText mDistrictPasscode;
    private EditText mName;
    private TimePicker mBusRouteTime;

    private Spinner mBusRouteName;
    private Spinner mBusRouteStop;

    private CheckBox mLocationServices;

    private Bundle registrationResult;

    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mName = (EditText) findViewById(R.id.setup_name);
        mEmailAddress = (EditText) findViewById(R.id.setup_email_address);
        mEmailAddress.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!checkEmailEmpty(mEmailAddress)) {
                        checkEmail(mEmailAddress);
                    }
                }
            }
        });

        mDistrictPasscode = (EditText) findViewById(R.id.setup_district_passcode);
        mDistrictPasscode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!checkPasscodeEmpty(mDistrictPasscode)) {
                        checkPasscode(mDistrictPasscode);
                    }
                }
            }
        });

        mBusRouteName = (Spinner) findViewById(R.id.setup_bus_route_name);
        mBusRouteStop = (Spinner) findViewById(R.id.setup_bus_route_stop);

        mBusRouteTime = (TimePicker) findViewById(R.id.setup_bus_route_time);

        mLocationServices = (CheckBox) findViewById(R.id.location_services);

        mLoginFormView = findViewById(R.id.setup_scroll);
        mProgressView = findViewById(R.id.login_progress);
    }

    public void onSubmit(View v) {
        //Submit form or else display errors
        ScrollView scrollView = (ScrollView) findViewById(R.id.setup_scroll);

        String fullName = mName.getText().toString();
        String emailAddress = mEmailAddress.getText().toString();
        String districtPasscode = mDistrictPasscode.getText().toString();

        String busRouteName = mBusRouteName.getSelectedItem().toString();
        String busRouteStop = mBusRouteStop.getSelectedItem().toString();

        String busRouteTime;
        int hour = mBusRouteTime.getCurrentHour();
        int minute = mBusRouteTime.getCurrentMinute();
        busRouteTime = String.format("%d:%02d", Locale.US, hour, minute);

        boolean locationServices = mLocationServices.isChecked();

        if (fullName.equals("")) {
            mName.setError("You need to fill this out!");
            scrollView.scrollTo(0, mName.getTop());
            return;
        }
        if (!checkEmailEmpty(mEmailAddress)) {
            scrollView.scrollTo(0, mEmailAddress.getTop());
            return;
        }
        if (!checkEmailValid(mEmailAddress)) {
            scrollView.scrollTo(0, mEmailAddress.getTop());
            return;
        }
        if (!checkEmail(mEmailAddress)) {
            scrollView.scrollTo(0, mEmailAddress.getTop());
            return;
        }
        if (!checkPasscodeEmpty(mDistrictPasscode)) {
            scrollView.scrollTo(0, mDistrictPasscode.getTop());
            return;
        }
        if (!checkPasscode(mDistrictPasscode)) {
            scrollView.scrollTo(0, mDistrictPasscode.getTop());
            return;
        }

        if (busRouteName.equals("") || busRouteName.equals("Select a Bus Route...")) {
            Toast.makeText(getApplicationContext(), "You need to select the late bus route you take!", Toast.LENGTH_SHORT).show();
            scrollView.scrollTo(0, mBusRouteName.getTop());
            return;
        }
        if (busRouteStop.equals("") || busRouteName.equals("Select a School...")) {
            Toast.makeText(getApplicationContext(), "You need to select the school you board the bus from!", Toast.LENGTH_SHORT).show();
            scrollView.scrollTo(0, mBusRouteStop.getTop());
            return;
        }
        if (busRouteTime.equals(":0")) {
            Toast.makeText(getApplicationContext(), "You need to fill in a bus pickup time!", Toast.LENGTH_SHORT).show();
            return;
        }

        int time = 60 * hour + minute;
        if (time < 945 || time > 1100) { //3:45 PM, 6:30 PM
            scrollView.scrollTo(0, mBusRouteStop.getTop());
            Toast.makeText(getApplicationContext(), "The time you've filled in is invalid!", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);
        new RegistrationTask().execute(fullName, emailAddress, districtPasscode, busRouteName, busRouteStop, "" + hour, "" + minute, locationServices ? "true" : "false");
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class RegistrationTask extends AsyncTask<String, Void, Bundle> {
        protected Bundle doInBackground(String... params) {

            Bundle returnBundle = new Bundle();
            returnBundle.putString("status", "failure");

            String name = params[0];
            String emailAddress = params[1];
            String password = params[2];
            String districtPasscode = params[3];
            String busRouteName = params[4];
            String busRouteStop = params[5];
            String locationServices = params[6];

            int busRouteTimeHour;
            int busRouteTimeMinute;
            try {
                busRouteTimeHour = Integer.parseInt(params[6]);
                busRouteTimeMinute = Integer.parseInt(params[7]);
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid bus route time");
                return returnBundle;
            }
            boolean locationServicesEnabled = locationServices.equals("true");

            Bundle paramsBundle = new Bundle();
            paramsBundle.putString("name", name);
            paramsBundle.putString("emailAddress", emailAddress);
            paramsBundle.putString("password", password);
            paramsBundle.putString("districtPasscode", districtPasscode);
            paramsBundle.putString("busRouteName", busRouteName);
            paramsBundle.putString("busRouteStop", busRouteStop);
            paramsBundle.putInt("busRouteTimeHour", busRouteTimeHour);
            paramsBundle.putInt("busRouteTimeMinute", busRouteTimeMinute);
            paramsBundle.putBoolean("locationServicesEnabled", locationServicesEnabled);

            String registrationResponse = new HTTPHelper(paramsBundle, HTTPHelper.REGISTER_USER).returnResult();

            if (registrationResponse.equals("registration successful")) {
                SharedPreferences.Editor prefs = getSharedPreferences("CatchItPreferences", 0).edit();

                prefs.putString("name", name);
                prefs.putString("emailAddress", emailAddress);
                prefs.putString("password", password);
                prefs.putString("busRouteName", busRouteName);
                prefs.putString("busRouteStop", busRouteStop);

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
                prefs.putFloat("busRouteStopLatitude", latitude);
                prefs.putFloat("busRouteStopLongitude", longitude);

                prefs.putInt("busRouteTime", 60 * busRouteTimeHour + busRouteTimeMinute);
                prefs.putInt("busRouteTimeHour", busRouteTimeHour);
                prefs.putInt("busRouteTimeMinute", busRouteTimeMinute);

                prefs.putBoolean("locationServicesEnabled", locationServicesEnabled);

                prefs.apply();
            }

            returnBundle.putString("status", "success");
            return returnBundle;
        }

        protected void onPostExecute(Bundle result) {
            showProgress(false);
            Intent intent;
            if (result.getString("status").equals("success")) {
                intent = new Intent(SetupActivity.this, YoureAllSetActivity.class);
            } else {
                String reason = registrationResult.getString("reason");
                if (reason == null || reason.equals(""))
                    reason = "Unknown error caused registration to fail";
                intent = new Intent(SetupActivity.this, RegistrationErrorActivity.class);
                intent.putExtra("failureReason", reason);
            }
            startActivity(intent);
            finish();
        }
    }
}