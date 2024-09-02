package dotheyappearwellgroomed.com.tempappname;

import android.Manifest;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class NavigationActivity extends AppCompatActivity
        implements OnMapReadyCallback, SensorEventListener {

    Handler handler = new Handler();
    GoogleMap mGoogleMap;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    FusedLocationProviderClient mFusedLocationClient;
    Location curr = new Location("");
    ArrayList<String> steps;
    SensorManager sensor;
    float appliedAcceleration = 0;
    float currentAcceleration = 0;
    float velocity = 0;
    Date lastUpdate;
    double calibration = Double.NaN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_layout);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navMap);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        steps = (ArrayList<String>)bundle.getSerializable("Directions");

        for(int i = 0; i < steps.size(); i++)
        {
            if(steps.get(i) != null) {
                String plainText = steps.get(i).replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", " ");
                steps.remove(i);
                steps.add(i, plainText);
            }
        }

        lastUpdate = new Date(System.currentTimeMillis());
        sensor = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensor.registerListener(this,
                sensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);

        Timer updateTimer = new Timer("velocityUpdate");
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                updateGUI();
            }
        }, 0, 1000);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];

        double a = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));

        if (calibration == Double.NaN)
            calibration = a;
        else {
            updateVelocity();
            currentAcceleration = (float)a;
        }
    }

    private void updateGUI() {
        // Convert from meters per second to miles per hour.
        final double mph = (Math.round(100*velocity / 1.6 * 3.6))/100;

        // Update the GUI
        handler.post(new Runnable() {
            public void run() {
                TextView speed = (TextView)findViewById(R.id.stepView);
                speed.setText(String.valueOf(mph) + "mph");
            }
        });
    }

    private void updateVelocity() {
        // Calculate how long this acceleration has been applied.
        Date timeNow = new Date(System.currentTimeMillis());
        long timeDelta = timeNow.getTime()-lastUpdate.getTime();
        lastUpdate.setTime(timeNow.getTime());

        // Calculate the change in velocity at the
        // current acceleration since the last update.
        float deltaVelocity = appliedAcceleration * (timeDelta/1000);
        appliedAcceleration = currentAcceleration;

        // Add the velocity change to the current velocity.
        velocity += deltaVelocity;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(0); // two minute interval
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        ImageView layers = (ImageView) findViewById(R.id.layers);
        layers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(mGoogleMap.getMapType()){
                    case 1:
                        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    case 4:
                        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    default:
                        break;
                }
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mGoogleMap.setMyLocationEnabled(true);
            }
        } else {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                mLastLocation = location;
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.remove();
                }

                //Place current location
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                //move map camera
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));

                curr.setLatitude(mLastLocation.getLatitude());
                curr.setLongitude(mLastLocation.getLongitude());
            }
        }
    };

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}