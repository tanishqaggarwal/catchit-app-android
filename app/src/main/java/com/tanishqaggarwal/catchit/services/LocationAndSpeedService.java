package com.tanishqaggarwal.catchit.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.LatLng;
import com.tanishqaggarwal.catchit.Constants;

public class LocationAndSpeedService extends Service implements LocationListener {

    private static final String TAG = "Catch It";
    private LocationManager mLocationManager;
    private LatLng location;
    private double speed;

    public LocationAndSpeedService() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public double getSpeed() {
        return speed;
    } //TODO: Stub; replace with Google Maps speed query

    public void reportLatLngAndSpeed() {
        Bundle paramsBundle = new Bundle();
        paramsBundle.putString("emailAddress", getSharedPreferences("CatchItPreferences", 0).getString("emailAddress", ""));
        paramsBundle.putString("addressToken", getSharedPreferences("CatchItPreferences", 0).getString("addressToken", ""));
        paramsBundle.putDouble("latitude", location.latitude);
        paramsBundle.putDouble("longitude", location.longitude);
        paramsBundle.putDouble("speed", getSpeed());

        Log.i(TAG, new HTTPHelper(paramsBundle, HTTPHelper.REPORT_BUS_SIGHTING).returnResult());
    }

    public boolean checkWithinSchool() {
        Location currentLocation = new Location("");
        currentLocation.setLatitude(location.latitude);
        currentLocation.setLongitude(location.longitude);

        Location schoolLoc = new Location("");
        schoolLoc.setLatitude(getSharedPreferences("CatchItPreferences", 0).getFloat("busRouteStopLatitude", Constants.SOUTH_LATITUDE));
        schoolLoc.setLongitude(getSharedPreferences("CatchItPreferences", 0).getFloat("busRouteStopLatitude", Constants.SOUTH_LONGITUDE));

        float distance = currentLocation.distanceTo(schoolLoc);
        return distance < 300;
    }
}
