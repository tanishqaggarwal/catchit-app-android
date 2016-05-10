package com.tanishqaggarwal.catchit.services;

import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Tanishq on 4/28/2016.
 */
public class HTTPHelper {
    public static final String CHECK_EMAIL = "check_email_address";
    public static final String CHECK_DISTRICT_PASSCODE = "check_district_passcode";
    public static final String REGISTER_USER = "register";
    public static final String LOGIN_USER = "login";
    public static final String GET_EARLIEST_ON_ROUTE = "get_earliest_time_on_route";
    public static final String REPORT_BUS_SIGHTING = "sighting/report";
    public static final String GET_BUS_SIGHTING = "sighting/get";
    public static final String GET_ROUTE_INFO = "route_info";
    public static final String EDIT_PREFERENCES = "edit_preferences";

    private static final String TAG = "Catch It";

    private String api;
    private Bundle params;

    public HTTPHelper(Bundle params, String api) {
        this.api = api;
        this.params = params;
    }

    public String returnResult() {
        String returnResponse;

        try {
            HttpURLConnection urlConnection = (HttpURLConnection) (new URL("http://catchit-app.appspot.com/api/" + api)).openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestMethod("POST");


            DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());

            String queryString = "";
            for (String key : params.keySet()) {
                Object value = params.get(key);

                queryString = queryString + key + "=";
                if (value instanceof Integer) {
                    queryString += "%d";
                } else if (value instanceof Double || value instanceof Float) {
                    queryString += "%f";
                } else {
                    queryString += "%s";
                }

                queryString += URLEncoder.encode(value.toString(), "UTF-8");
                queryString += "&";
            }

            out.writeBytes(queryString.substring(0, queryString.length() - 1)); //To get rid of the extra "&" at the end
            out.flush();
            out.close();

            int responseCode = urlConnection.getResponseCode();
            StringBuffer response = new StringBuffer();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            }

            returnResponse = response.toString();
            urlConnection.disconnect();
        } catch (MalformedURLException e) {
            Log.e(TAG, "Invalid checking URL");
            return "";
        } catch (IOException e) {
            Log.e(TAG, "Unable to connect to server");
            return "";
        } catch (Exception e) {
            Log.e(TAG, "Unknown error caused check to fail");
            return "";
        }

        return returnResponse;
    }
}
