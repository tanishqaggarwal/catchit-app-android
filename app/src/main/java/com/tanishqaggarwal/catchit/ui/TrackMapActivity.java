package com.tanishqaggarwal.catchit.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tanishqaggarwal.catchit.Constants;
import com.tanishqaggarwal.catchit.R;
import com.tanishqaggarwal.catchit.services.BusIsAlmostHereReceiver;
import com.tanishqaggarwal.catchit.services.HTTPHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;

public class TrackMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "Catch It";
    private GoogleMap mMap;
    private SharedPreferences prefs;

    public static int getEta(SharedPreferences prefs, LatLng busLocation) {

        Bundle paramsBundle2 = new Bundle();
        paramsBundle2.putString("busRouteName", prefs.getString("busRouteName", ""));
        paramsBundle2.putString("emailAddress", prefs.getString("emailAddress", ""));
        paramsBundle2.putString("addressToken", prefs.getString("addressToken", ""));
        String routeInfo = new HTTPHelper(paramsBundle2, HTTPHelper.GET_ROUTE_INFO).returnResult();
        try {
            JSONObject reader2 = new JSONObject(routeInfo);

            String fourOrFive = prefs.getInt("busRouteTime", 960) > 1010 ? "5" : "4"; //Time 1010 = 4:50 PM
            int southTime = reader2.getInt("High School South - " + fourOrFive);
            int northTime = reader2.getInt("High School North - " + fourOrFive);
            int groverTime = reader2.getInt("Community Middle School - " + fourOrFive);
            int communityTime = reader2.getInt("Grover Middle School - " + fourOrFive);

            int currentTime = 60 * Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + Calendar.getInstance().get(Calendar.MINUTE);

            ArrayList<TimeLocationPair> waypoints = new ArrayList<>();
            if (southTime > currentTime)
                waypoints.add(new TimeLocationPair(southTime, "High School South", Constants.SOUTH_LOCATION));
            if (northTime > currentTime)
                waypoints.add(new TimeLocationPair(northTime, "High School North", Constants.NORTH_LOCATION));
            if (groverTime > currentTime)
                waypoints.add(new TimeLocationPair(groverTime, "Grover Middle School", Constants.GROVER_LOCATION));
            if (communityTime > currentTime)
                waypoints.add(new TimeLocationPair(southTime, "Community Middle School", Constants.COMMUNITY_LOCATION));
            Collections.sort(waypoints);

            String waypointString = "";
            for (TimeLocationPair pair : waypoints) {
                if (pair.getLocation().equals(prefs.getString("busRouteName", ""))) {
                    break;
                }
                waypointString += String.format("via:%f,%f|", pair.getLatLng().latitude, pair.getLatLng().longitude);
            }

            JSONObject gmAPIRequest = new JSONObject()
                    .put("origin", String.format("%f,%f", busLocation.latitude, busLocation.longitude))
                    .put("destination", String.format("%f,%f", busLocation.latitude, busLocation.longitude))
                    .put("waypoints", waypointString)
                    .put("key", Constants.MAPS_API_KEY);
            String requestURL = "https://maps.googleapis.com/maps/api/directions/json";

            //Do HTTP response
            String directionsResponse = "";

            JSONObject reader3 = new JSONObject(directionsResponse);
            int eta = currentTime + reader3.getJSONObject("route").getJSONObject("duration").getInt("value") / 60;
            return eta;
        } catch (JSONException error) {
            return -1;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        prefs = getSharedPreferences("CatchItPreferences", 0);

        LatLng stopLocation = new LatLng(prefs.getFloat("busRouteStopLatitude", Constants.SOUTH_LATITUDE), prefs.getFloat("busRouteStopLongitude", Constants.SOUTH_LONGITUDE));
        Marker currentLocation = mMap.addMarker(new MarkerOptions()
                .position(stopLocation)
                .title(prefs.getString("busRouteStop", ""))
                .snippet("You're Here"));
        currentLocation.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(stopLocation));

        Bundle paramsBundle = new Bundle();
        paramsBundle.putString("busRouteName", prefs.getString("busRouteName", ""));
        paramsBundle.putString("emailAddress", prefs.getString("emailAddress", ""));
        paramsBundle.putString("addressToken", prefs.getString("addressToken", ""));
        String sighting = new HTTPHelper(paramsBundle, HTTPHelper.GET_BUS_SIGHTING).returnResult();

        try {
            JSONObject reader = new JSONObject(sighting);
            if (reader.getString("status").equals("success")) {
                float latitude = Float.parseFloat(reader.getString("latitude"));
                float longitude = Float.parseFloat(reader.getString("longitude"));
                LatLng busLocation = new LatLng(latitude, longitude);

                MarkerOptions marker = new MarkerOptions()
                        .position(busLocation)
                        .title(prefs.getString("busRouteName", ""))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                int eta = getEta(prefs, busLocation);
                if (eta != -1) {
                    Long time = new GregorianCalendar().getTimeInMillis() + (eta - 60 * Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + Calendar.getInstance().get(Calendar.MINUTE)) * 60 * 1000;
                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, time, PendingIntent.getBroadcast(this, 1, new Intent(this, BusIsAlmostHereReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT));

                    int etaHour = (eta / 60) % 12;
                    int etaMinute = eta % 60;
                    marker.snippet(String.format("Estimated Time of Arrival: %02d:%02d PM", etaHour, etaMinute));
                } else {
                    marker.snippet("Could not get estimated time of arrival.");
                }

                Marker busLocationMarker = mMap.addMarker(marker);
                busLocationMarker.showInfoWindow();
                mMap.moveCamera(CameraUpdateFactory.newLatLng(busLocation));
            } else {
                throw new Exception("Could not successfully find bus");
            }
        } catch (Exception exception) {
            if (exception instanceof JSONException) {
                Log.e(TAG, "Invalid response from website");
            } else {
                Log.i(TAG, "No one has reported bus location yet");
            }
            Intent intent = new Intent(this, TrackNowActivity.class);
            startActivity(intent);
            Toast.makeText(TrackMapActivity.this, "We can't find the bus right now; maybe no one's on it yet. Check back later!", Toast.LENGTH_SHORT).show();
            finish();
        }

        mMap.animateCamera(CameraUpdateFactory.zoomTo(13.0f)); //Magnification is approximately town-level
    }

    private static class TimeLocationPair implements Comparable<TimeLocationPair> {
        private int time;
        private String location;
        private LatLng latlng;

        public TimeLocationPair(int time, String location, LatLng latlng) {
            this.time = time;
            this.location = location;
            this.latlng = latlng;
        }

        public int compareTo(TimeLocationPair t) {
            if (t.getTime() < this.time)
                return 1;
            else if (t.getTime() == this.time)
                return 0;
            return -1;
        }

        public int getTime() {
            return time;
        }

        public String getLocation() {
            return location;
        }

        public LatLng getLatLng() {
            return latlng;
        }
    }
}