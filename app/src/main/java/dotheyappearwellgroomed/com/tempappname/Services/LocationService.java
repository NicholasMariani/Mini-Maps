package dotheyappearwellgroomed.com.tempappname.Services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;


import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by MikeM on 4/23/2018.
 */

public class LocationService extends Service {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private final int LOCATION_INTERVAL = 5000;
    private boolean serviceIsActive;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest locationRequest;
    private Location currentLocation;

    public LocationService() {}

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        _startService();

        return START_NOT_STICKY;
    }

    protected void _startService() {

        Log.d("LocationService", "================= LOCATION SERVICE STARTED =================");

        serviceIsActive = true;

        // Should already be enabled..
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        createLocationCallback();

        mFusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(LOCATION_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                currentLocation = locationResult.getLastLocation();
                updateSharedPrefLastLocation();
            }
        };
    }

    private void updateSharedPrefLastLocation()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        Geocoder geo = new Geocoder(this, Locale.getDefault());
        List<Address> address;
        try {
            address = geo.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
            String stringLoc = address.get(0).getAddressLine(0);

            Log.d("Location Ping", stringLoc);

            editor.putString("last_pinged_location", stringLoc);
            editor.commit();
        }catch (IOException e) {}
    }

    // Service

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
