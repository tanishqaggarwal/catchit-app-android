package com.tanishqaggarwal.catchit.services;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Tanishq on 4/28/2016.
 */
public class ValidationUtils {

    public static boolean checkEmail(EditText mEmailAddress) {

        String emailAddress = mEmailAddress.getText().toString();
        Bundle params = new Bundle();
        params.putString("emailAddress", emailAddress);

        String checkResponse = new HTTPHelper(params, HTTPHelper.CHECK_EMAIL).returnResult();

        if (!checkResponse.equals("valid email address")) {
            mEmailAddress.setError("This email address is already in our system.");
            return false;
        }
        return true;
    }

    public static boolean checkEmailEmpty(EditText mEmailAddress) {
        if (TextUtils.isEmpty(mEmailAddress.getText())) {
            mEmailAddress.setError("You need to fill this out!");
            return false;
        }
        return true;
    }

    public static boolean checkEmailValid(EditText mEmailAddress) {
        String email = mEmailAddress.getText().toString();

        final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        if (!matcher.find()) {
            mEmailAddress.setError("This needs to be a valid email address!");
            return false;
        }
        return true;
    }

    public static boolean checkPasscode(EditText mDistrictPasscode) {

        String districtPasscode = mDistrictPasscode.getText().toString();
        Bundle params = new Bundle();
        params.putString("districtPasscode", districtPasscode);

        String checkResponse = new HTTPHelper(params, HTTPHelper.CHECK_DISTRICT_PASSCODE).returnResult();

        if (!checkResponse.equals("valid district passcode")) {
            mDistrictPasscode.setError("This is an invalid district passcode.");
            return false;
        }
        return true;
    }

    public static boolean checkPasscodeEmpty(EditText mDistrictPasscode) {
        if (TextUtils.isEmpty(mDistrictPasscode.getText())) {
            mDistrictPasscode.setError("You need to fill this out!");
            return false;
        }
        return true;
    }
}