package dotheyappearwellgroomed.com.tempappname;

import static com.google.android.libraries.places.widget.AutocompleteActivity.RESULT_ERROR;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.preference.PreferenceManager;
//import android.support.annotation.NonNull;
//import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
//import android.support.v4.app.FragmentActivity;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ScaleXSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.ButtCap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.AddressComponents;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.GeocodedWaypoint;
import com.google.maps.model.TravelMode;
import com.google.maps.android.PolyUtil;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    GoogleMap mGoogleMap;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    FusedLocationProviderClient mFusedLocationClient;
    Location curr = new Location("");
    AutocompleteSupportFragment placeAutoComplete;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int overview = 0;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    List<Polyline> lines = new LinkedList<>();
    boolean canGoBack = false;
    double lat;
    double lon;
    Address markerAddress;

    // Active direction results
    DirectionsResult active_direction_results;
    public static List<String> steps = new ArrayList<>();

    // Activity Buttons
    Button btn_navigation;
    Button btn_busy_rating;
    Button btn_directions;
    ImageView btn_back;

    private PlacesClient mGeoDataClient;

    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;

    boolean flag = false;
    int addressIndex = 0;
    Geocoder geocoder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        Places.initialize(getApplicationContext(), "AIzaSyBMEW8o8MSjVbuP6Ybh82TjTyee0i1zj6Y");

        mGeoDataClient = Places.createClient(this);

        geocoder = new Geocoder(this);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.action_search);

        ImageView search = (ImageView) findViewById(R.id.search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.progressBar_cyclic).setVisibility(View.VISIBLE);
                List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS_COMPONENTS,
                        Place.Field.LAT_LNG, Place.Field.VIEWPORT);

                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(MapsActivity.this);
                startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mapFragment.getMapAsync(this);

        initializeButtons();

        findViewById(R.id.progressBar_cyclic).setVisibility(View.INVISIBLE);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (item.getItemId() == R.id.action_home) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivityForResult(intent, FragmentEnum.HOME_ID.ordinal());
                return true;
            } else if (item.getItemId() == R.id.action_search) {
                return true;
            } else if (item.getItemId() == R.id.action_favorites) {
                Intent favoritesIntent = new Intent(getApplicationContext(), FavoritesActivity.class);
                startActivityForResult(favoritesIntent, FragmentEnum.FAVORITES_ID.ordinal());
                return true;
            } else if (item.getItemId() == R.id.action_history) {
                Intent historyIntent = new Intent(getApplicationContext(), HistoryActivity.class);
                startActivityForResult(historyIntent, FragmentEnum.HISTORY_ID.ordinal());
                return true;
            }

            return false;
        }
    };

    private void initializeButtons() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int button_width = width / 3;

        btn_navigation = findViewById(R.id.navigate);
        btn_navigation.setLayoutParams(new LinearLayout.LayoutParams(button_width, RelativeLayout.LayoutParams.WRAP_CONTENT));
        btn_navigation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (active_direction_results != null) {
                    // Hide routing buttons
                    showRouteButtons(false);
                    // Show navigation buttons
                    findViewById(R.id.back).setVisibility(View.VISIBLE);
                    //mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(curr.getLatitude(), curr.getLongitude()), 20.0f));

                    double lat = active_direction_results.routes[overview].legs[overview].endLocation.lat;
                    double lng = active_direction_results.routes[overview].legs[overview].endLocation.lng;
                    String format = "google.navigation:q=" + lat + "," + lng;
                    Uri uri = Uri.parse(format);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    Log.d("btn_navigation.onClick", "Error: active_direction_results was null..");
                }
            }
        });

        btn_busy_rating = findViewById(R.id.busy_rating);
        btn_busy_rating.setLayoutParams(new LinearLayout.LayoutParams(button_width, RelativeLayout.LayoutParams.WRAP_CONTENT));
        btn_busy_rating.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String destination_str = "";
                boolean notNull = false;
                if (active_direction_results != null) {
                    destination_str = active_direction_results.routes[overview].legs[overview].endAddress;
                    notNull = true;
                }

                if (mLikelyPlaceAddresses != null) {
                    destination_str = mLikelyPlaceAddresses[addressIndex];
                    notNull = true;
                }

                if (markerAddress != null) {
                    destination_str = markerAddress.getAddressLine(0);
                    notNull = true;
                }
                Log.d("btn_busy_rating.onClick", "Destination String: " + destination_str);

                if (notNull)
                    putStringInSharedPrefs(destination_str);

                Bundle bundle = new Bundle();
                Intent intent = new Intent(MapsActivity.this, HomeActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        btn_directions = findViewById(R.id.directions);
        btn_directions.setLayoutParams(new LinearLayout.LayoutParams(button_width, RelativeLayout.LayoutParams.WRAP_CONTENT));
        btn_directions.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("Directions", (ArrayList<String>) steps);
                Intent intent = new Intent(MapsActivity.this, DisplayDirectionsActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        btn_back = findViewById(R.id.back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mGoogleMap.clear();
                showRouteButtons(false);
                findViewById(R.id.search).setVisibility(View.VISIBLE);
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(curr.getLatitude(), curr.getLongitude()), 15));
            }
        });

        showRouteButtons(false);
    }

    /**
     * Sets up the options menu.
     * @param menu The options menu.
     * @return Boolean.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    private void showCurrentPlace(final LatLng latlng) {
        if (mGoogleMap == null) {
            return;
        }
        mGoogleMap.clear();
        // Get the likely places - that is, the businesses and other points of interest that
        // are the best match for the device's current location.

        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME);

        // Use the builder to create a FindCurrentPlaceRequest.
        FindCurrentPlaceRequest request =
                FindCurrentPlaceRequest.builder(placeFields).build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mGeoDataClient.findCurrentPlace(request).addOnSuccessListener(((response) -> {
            for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                Log.i("TEST", String.format("Place '%s' has likelihood: %f",
                        placeLikelihood.getPlace().getName(),
                        placeLikelihood.getLikelihood()));
            }
        })).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e("TEST", "Place not found: " + apiException.getStatusCode());
            }
        });


//        @SuppressWarnings("MissingPermission") final
//        Task<PlaceLikelihood> placeResult =
//                mGeoDataClient.findCurrentPlace(null);
//        placeResult.addOnCompleteListener
//                (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
//                    @Override
//                    public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
//                        if (task.isSuccessful() && task.getResult() != null) {
//                            PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
//
//                            // Set the count, handling cases where less than 5 entries are returned.
//                            int count;
//                            if (likelyPlaces.getCount() < 50) {
//                                count = likelyPlaces.getCount();
//                            } else {
//                                count = 50;
//                            }
//
//                            int i = 0;
//                            mLikelyPlaceNames = new String[count];
//                            mLikelyPlaceAddresses = new String[count];
//                            mLikelyPlaceAttributions = new String[count];
//                            mLikelyPlaceLatLngs = new LatLng[count];
//
//                            for (PlaceLikelihood placeLikelihood : likelyPlaces) {
//                                // Build a list of likely places to show the user.
//                                mLikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
//                                mLikelyPlaceAddresses[i] = (String) placeLikelihood.getPlace()
//                                        .getAddress();
//                                mLikelyPlaceAttributions[i] = (String) placeLikelihood.getPlace()
//                                        .getAttributions();
//                                mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();
//
//                                i++;
//                                if (i > (count - 1)) {
//                                    break;
//                                }
//                            }
//
//                            // Release the place likelihood buffer, to avoid memory leaks.
//                            likelyPlaces.release();
//
//                            // Show a dialog offering the user the list of likely places, and add a
//                            // marker at the selected place.
//                            double lat = Double.parseDouble(String.format("%.1f", latlng.latitude));
//                            double lon = Double.parseDouble(String.format("%.1f", latlng.longitude));
//
//                            LatLng roundedCoords = new LatLng(lat, lon);
//
//                            double currLat = Double.parseDouble(String.format("%.1f", mLikelyPlaceLatLngs[0].latitude));
//                            double currLon = Double.parseDouble(String.format("%.1f", mLikelyPlaceLatLngs[0].longitude));
//
//                            LatLng currRoundedCoords = new LatLng(currLat, currLon);
//
//                            Log.d("LATLNG: ", "Click: " + roundedCoords + ", Curr: " + currRoundedCoords);
//                            Log.d("COMPARE: ", "Comparison: " + roundedCoords.equals(currRoundedCoords));
//                            if(roundedCoords.equals(currRoundedCoords))
//                                openPlacesDialog();
//
//                        } else {
//                            Log.e("TAG", "Exception: %s", task.getException());
//
//                        }
//                    }
//                });
    }


    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    private void openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The "which" argument contains the position of the selected item.
                LatLng markerLatLng = mLikelyPlaceLatLngs[which];
                String markerSnippet = mLikelyPlaceAddresses[which];
                if (mLikelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions[which];
                }

                // Add a marker for the selected place, with an info window
                // showing information about that place.
                mGoogleMap.addMarker(new MarkerOptions()
                        .title(mLikelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet));

                // Position the map's camera at the location of the marker.
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, 18));

                btn_busy_rating.setVisibility(View.VISIBLE);
                addressIndex = which;
                flag = true;
            }
        };

        // Display the dialog.
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Where Are You?")
                .setItems(mLikelyPlaceNames, listener)
                .show();
    }

    private void putStringInSharedPrefs(String destination_str)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("active_location", destination_str);
        editor.commit();
    }

    private void showRouteButtons(boolean show)
    {
        int btn_visibility = show ? View.VISIBLE : View.INVISIBLE;
        btn_navigation.setVisibility(btn_visibility);
        btn_busy_rating.setVisibility(btn_visibility);
        btn_directions.setVisibility(btn_visibility);
        btn_back.setVisibility(btn_visibility);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.d("PLACE: ", place.toString());

                onSearch(place);
                findViewById(R.id.progressBar_cyclic).setVisibility(View.INVISIBLE);
            } else if (resultCode == RESULT_ERROR) {
                // TODO: Handle the error.
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setSmallestDisplacement(5); //update every 5 meters
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        setupGoogleMapScreenSettings(googleMap);

        ImageView layers = (ImageView) findViewById(R.id.layers);
        layers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch(mGoogleMap.getMapType()){
                    case 1:
                        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        findViewById(R.id.bar).setBackgroundColor(getResources().getColor(R.color.moreOpaque));
                        break;
                    case 4:
                        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        findViewById(R.id.bar).setBackgroundColor(getResources().getColor(R.color.opaque));
                        break;
                    default:
                        break;
                }
            }
        });

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                getLocation(geocoder, latLng);
            }
        });

        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                showCurrentPlace(latLng);
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
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
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                curr.setLatitude(mLastLocation.getLatitude());
                curr.setLongitude(mLastLocation.getLongitude());
            }
        }
    };

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mGoogleMap.setMyLocationEnabled(true);
                    }
                } else {
                    // permission denied, disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void onSearch(Place place) {
        mGoogleMap.clear();
        steps.clear();

        StringBuilder address1 = new StringBuilder();

        // Get each component of the address from the place details,
        // and then fill-in the corresponding field on the form.
        // Possible AddressComponent types are documented at https://goo.gle/32SJPM1
        for (AddressComponent component : place.getAddressComponents().asList()) {
            String type = component.getTypes().get(0);
            switch (type) {
                case "street_number":
                case "route":
                case "locality":
                case "country":
                case "administrative_area_level_1":
                case "postal_code": {
                    address1.append(component.getName() + " ");
                    break;
                }
            }
        }
        System.out.println(address1);

        String location = address1.toString();
        Log.d("Location String: ", location);
        List<Address> addressList = null;
        List<Address> current = null;
        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);
                current = geocoder.getFromLocation(curr.getLatitude(), curr.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!addressList.isEmpty()) {
                Address address = addressList.get(0);
//            if((address.getLatitude() > 41.25 || address.getLatitude() < -120.9762) &&
//                    (address.getLongitude() > -31.96 || address.getLongitude() < 115.84)) {
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10.0f));
                Log.d("Place: ", address.getAddressLine(0));

                active_direction_results = getDirectionsDetails(current.get(0).getAddressLine(0), address.getAddressLine(0), TravelMode.DRIVING);
                startRouting();
                findViewById(R.id.search).setVisibility(View.INVISIBLE);

                // Add what we searched into the search history
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = preferences.edit();
                Set<String> user_search_history = preferences.getStringSet("user_searches_string_set", null);
                user_search_history.add(place.toString());
                editor.putStringSet("user_searches_string_set", user_search_history);

                // Commit all
                editor.commit();
            } else {
                SpannableStringBuilder ssBuilder = new SpannableStringBuilder("\nInvalid Address");
                StyleSpan span = new StyleSpan(Typeface.NORMAL);
                ScaleXSpan span1 = new ScaleXSpan(1);
                ssBuilder.setSpan(span, 0, 5, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                ssBuilder.setSpan(span1, 0, 5, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
                alertDialog.setTitle("Whoops!\n");
                alertDialog.setMessage(ssBuilder);
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "RECOGNIZE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                Button button = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                if (button != null) {
                    button.setTextColor(Color.WHITE);
                }
            }
        }
    }

    private DirectionsResult getDirectionsDetails(String origin, String destination, TravelMode mode) {
        DateTime now = new DateTime();
        try {
            return DirectionsApi.newRequest(getGeoContext())
                    .alternatives(true)
                    .mode(mode)
                    .origin(origin)
                    .destination(destination)
                    .departureTime(now)
                    .await();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return null;
        } catch (com.google.maps.errors.ApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setupGoogleMapScreenSettings(GoogleMap mMap) {
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setTrafficEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setMyLocationButtonEnabled(false);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(false);
    }

    private void addMarkersToMap(DirectionsResult results, GoogleMap mMap) {
        MarkerOptions start = new MarkerOptions();
        MarkerOptions end = new MarkerOptions();
        start.position(new LatLng(results.routes[overview].legs[overview].startLocation.lat, results.routes[overview].legs[overview].startLocation.lng)).title(results.routes[overview].legs[overview].startAddress);
        end.position(new LatLng(results.routes[overview].legs[overview].endLocation.lat, results.routes[overview].legs[overview].endLocation.lng)).title(results.routes[overview].legs[overview].endAddress);
        start.icon(BitmapDescriptorFactory.fromResource(R.drawable.location));
        mMap.addMarker(start);
        mMap.addMarker(end.snippet(getEndLocationTitle(results)));
    }

    private void positionCamera(DirectionsRoute route, GoogleMap mMap) {
        LatLng start = new LatLng(route.legs[overview].startLocation.lat, route.legs[overview].startLocation.lng);
        LatLng end = new LatLng(route.legs[overview].endLocation.lat, route.legs[overview].endLocation.lng);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        builder.include(start);
        builder.include(end);

        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.30);

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);

        mMap.animateCamera(cu);
    }

    private void addPolyline(final String[][] dirs, final DirectionsResult results, GoogleMap mMap) {
        for(Polyline polyline : lines)
        {
            polyline.remove();
        }
        lines.clear();
        steps.clear();

        for(int i = 0; i < results.routes.length; i++) {
            List<LatLng> decodedPath = PolyUtil.decode(results.routes[i].overviewPolyline.getEncodedPath());

            if(i == 0)
            {
                PolylineOptions options = new PolylineOptions().addAll(decodedPath).width(30).color(Color.BLUE)
                        .zIndex(6).startCap(new ButtCap()).endCap(new ButtCap());
                Polyline line = mMap.addPolyline(options);
                line.setClickable(true);
                lines.add(line);
                line.setTag("" + i);
                positionCamera(results.routes[i], mGoogleMap);
                for(int j = 0; j < dirs[i].length; j++)
                    steps.add(dirs[i][j]);
            }
            else
            {
                PolylineOptions options = new PolylineOptions().addAll(decodedPath).width(30).color(Color.LTGRAY)
                        .zIndex(1).startCap(new ButtCap()).endCap(new ButtCap());
                Polyline line = mMap.addPolyline(options);
                line.setTag("" + i);
                line.setClickable(true);
                lines.add(line);
            }
        }
        mGoogleMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            public void onPolylineClick(Polyline polyline) {
                steps.clear();
                for (int i = 0; i < results.routes.length; i++) {
                    if(lines.get(i).getTag().equals(polyline.getTag()))
                    {
                        polyline.setColor(Color.BLUE);
                        polyline.setZIndex(6);
                        positionCamera(results.routes[i], mGoogleMap);
                        for(int j = 0; j < dirs[i].length; j++)
                            steps.add(dirs[i][j]);
                    }
                    else {
                        lines.get(i).setColor(Color.LTGRAY);
                        lines.get(i).setZIndex(1);
                    }
                }
            }
        });
    }

    private String getEndLocationTitle(DirectionsResult results) {
        return "Time: " + results.routes[overview].legs[overview].duration.humanReadable + " Distance: " + results.routes[overview].legs[overview].distance.humanReadable;
    }

    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext
                .setQueryRateLimit(10)
                .setApiKey("*")
                .setConnectTimeout(0, TimeUnit.SECONDS)
                .setReadTimeout(0, TimeUnit.SECONDS)
                .setWriteTimeout(0, TimeUnit.SECONDS);
    }

    public void startRouting() {
        canGoBack = true;
        if (active_direction_results != null) {
            if(active_direction_results.routes.length != 0) {
                String[][] dirs = new String[active_direction_results.routes.length][500];
                for (int r = 0; r < active_direction_results.routes.length; r++) {
                    for (int i = 0; i < active_direction_results.routes[r].legs[0].steps.length; i++) {
                        dirs[r][i] = "<br>" + active_direction_results.routes[r].legs[0].steps[i].distance + ": " + active_direction_results.routes[r].legs[0].steps[i].htmlInstructions;
                        Log.i("Route " + r, dirs[r][i]);
                    }
                }
                addPolyline(dirs, active_direction_results, mGoogleMap);
                addMarkersToMap(active_direction_results, mGoogleMap);

                showRouteButtons(true);
            }
        }
        else
        {
            Log.d("startRouting():", "Error: active_direction_results was null..");
        }
    }


    @Override
    public void onBackPressed() {
        if (canGoBack) {
            mGoogleMap.clear();
            findViewById(R.id.directions).setVisibility(View.INVISIBLE);
            findViewById(R.id.navigate).setVisibility(View.INVISIBLE);
            findViewById(R.id.back).setVisibility(View.INVISIBLE);
            findViewById(R.id.search).setVisibility(View.VISIBLE);
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(curr.getLatitude(), curr.getLongitude()), 10.0f));
            canGoBack = false;
        } else {
            super.onBackPressed();
        }
    }

    public void getLocation(final Geocoder geocoder, final LatLng latLng)
    {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME);

        // Use the builder to create a FindCurrentPlaceRequest.
        FindCurrentPlaceRequest request =
                FindCurrentPlaceRequest.builder(placeFields).build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mGeoDataClient.findCurrentPlace(request).addOnSuccessListener(((response) -> {
            for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                Log.i("TEST", String.format("Place '%s' has likelihood: %f",
                        placeLikelihood.getPlace().getName(),
                        placeLikelihood.getLikelihood()));
            }
        })).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e("TEST", "Place not found: " + apiException.getStatusCode());
            }
        });

//        @SuppressWarnings("MissingPermission") final
//        Task<PlaceLikelihoodBufferResponse> placeResult =
//                mPlaceDetectionClient.getCurrentPlace(null);
//        placeResult.addOnCompleteListener
//                (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
//                    @Override
//                    public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
//                        if (task.isSuccessful() && task.getResult() != null) {
//                            PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
//
//                            // Set the count, handling cases where less than 5 entries are returned.
//                            int count;
//                            if (likelyPlaces.getCount() < 50) {
//                                count = likelyPlaces.getCount();
//                            } else {
//                                count = 50;
//                            }
//
//                            int i = 0;
//                            mLikelyPlaceNames = new String[count];
//                            mLikelyPlaceAddresses = new String[count];
//                            mLikelyPlaceAttributions = new String[count];
//                            mLikelyPlaceLatLngs = new LatLng[count];
//
//                            for (PlaceLikelihood placeLikelihood : likelyPlaces) {
//                                // Build a list of likely places to show the user.
//                                mLikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
//                                mLikelyPlaceAddresses[i] = (String) placeLikelihood.getPlace()
//                                        .getAddress();
//                                mLikelyPlaceAttributions[i] = (String) placeLikelihood.getPlace()
//                                        .getAttributions();
//                                mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();
//
//                                i++;
//                                if (i > (count - 1)) {
//                                    break;
//                                }
//                            }
//
//                            // Release the place likelihood buffer, to avoid memory leaks.
//                            likelyPlaces.release();
//
//                            List<Address> addressList = null;
//                            try {
//                                addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//
//                            if(addressList != null) {
//                                double lat = Double.parseDouble(String.format("%.3f", latLng.latitude));
//                                double lon = Double.parseDouble(String.format("%.3f", latLng.longitude));
//
//                                LatLng roundedCoords = new LatLng(lat, lon);
//
//                                double currLat = Double.parseDouble(String.format("%.3f", curr.getLatitude()));
//                                double currLon = Double.parseDouble(String.format("%.3f", curr.getLongitude()));
//
//                                LatLng currRoundedCoords = new LatLng(currLat, currLon);
//
//                                if (roundedCoords.equals(currRoundedCoords)) {
//                                    mGoogleMap.clear();
//                                    String title = "";
//                                    for(int j = 0; j < mLikelyPlaceAddresses.length; j++)
//                                    {
//                                        Log.d("ADDRESSES: ", mLikelyPlaceAddresses[j]);
//                                        Log.d("ADDRESSES: ", addressList.get(0).getAddressLine(0));
//
//                                        if (mLikelyPlaceAddresses[j].equals(addressList.get(0).getAddressLine(0))) {
//                                            title = mLikelyPlaceNames[j];
//                                        }
//                                    }
//
//                                    if(title.equals(""))
//                                    {
//                                        title = addressList.get(0).getFeatureName();
//                                    }
//                                    Log.d("ADDRESS: ", "" + addressList.get(0).getFeatureName());
//                                    Log.d("ADDRESS: ", "" + addressList.get(0).getAddressLine(0));
//                                    markerAddress = addressList.get(0);
//                                    mGoogleMap.addMarker(new MarkerOptions()
//                                            .title(title)
//                                            .position(new LatLng(addressList.get(0).getLatitude(), addressList.get(0).getLongitude()))
//                                            .snippet(addressList.get(0).getAddressLine(0)));
//
//                                    // Position the map's camera at the location of the marker.
//                                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
//
//                                    btn_busy_rating.setVisibility(View.VISIBLE);
//                                }
//                            }
//                        }
//                    }
//                });
    }
}