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
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.tanishqaggarwal.catchit.R;
import com.tanishqaggarwal.catchit.services.HTTPHelper;
import com.tanishqaggarwal.catchit.services.ValidationUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "Catch It";
    private UserLoginTask mAuthTask = null;
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (!ValidationUtils.checkEmailEmpty(mEmailView)) {
            focusView = mEmailView;
            cancel = true;
        } else if (!ValidationUtils.checkEmailValid(mEmailView)) {
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 8;
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

    public void moveToRegister(View v) {
        startActivity(new Intent(this, SetupActivity.class));
        finish();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Bundle> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Bundle doInBackground(Void... params) {
            Bundle returnBundle = new Bundle();
            returnBundle.putString("status", "failure");

            Bundle loginParams = new Bundle();
            loginParams.putString("emailAddress", mEmail);
            loginParams.putString("password", mPassword);
            String registrationResponse = new HTTPHelper(loginParams, HTTPHelper.LOGIN_USER).returnResult();

            try {
                JSONObject reader = new JSONObject(registrationResponse);
                String name = reader.getString("name");
                String emailToken = reader.getString("emailToken");
                String busRouteName = reader.getString("busRouteName");
                String busRouteStop = reader.getString("busRouteStop");
                int busRouteTimeHour = reader.getInt("busRouteTimeHour");
                int busRouteTimeMinute = reader.getInt("busRouteTimeMinute");
                int busRouteTime = 60 * busRouteTimeHour + busRouteTimeMinute;

                SharedPreferences.Editor prefs = getSharedPreferences("CatchItPreferences", 0).edit();
                prefs.putString("name", name);
                prefs.putString("emailAddress", mEmail);
                prefs.putString("emailToken", emailToken);
                prefs.putString("password", mPassword);
                prefs.putString("busRouteName", busRouteName);
                prefs.putString("busRouteStop", busRouteStop);

                float latitude = 0;
                float longitude = 0;
                switch (busRouteStop) {
                    case "High School South":
                        latitude = 40.306130f;
                        longitude = -74.619806f;
                        break;
                    case "High School North":
                        latitude = 40.322581f;
                        longitude = -74.600277f;
                        break;
                    case "Grover Middle School":
                        latitude = 40.272366f;
                        longitude = -74.595156f;
                        break;
                    case "Community Middle School":
                        latitude = 40.324511f;
                        longitude = -74.599018f;
                        break;
                }
                prefs.putFloat("busRoutePlaceLatitude", latitude);
                prefs.putFloat("busRoutePlaceLongitude", longitude);

                prefs.putInt("busRouteTime", busRouteTime);

                prefs.apply();
            } catch (JSONException e) {
                returnBundle.putString("reason", "Could not read JSON from login stream");
                return returnBundle;
            }

            returnBundle.putString("status", "success");
            return returnBundle;
        }

        @Override
        protected void onPostExecute(final Bundle success) {
            mAuthTask = null;
            showProgress(false);

            if (success.getString("status").equals("success")) {
                Intent intent = new Intent(getApplicationContext(), YoureAllSetActivity.class);
                startActivity(intent);
                finish();
            } else {
                mPasswordView.setError("Incorrect login; either the email address or password is incorrect.");
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

