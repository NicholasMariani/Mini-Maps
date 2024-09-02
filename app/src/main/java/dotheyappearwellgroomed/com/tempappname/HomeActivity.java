package dotheyappearwellgroomed.com.tempappname;

import static com.google.android.libraries.places.widget.AutocompleteActivity.RESULT_ERROR;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ScaleXSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import org.json.JSONArray;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import dotheyappearwellgroomed.com.tempappname.Data.BusyRating;
import dotheyappearwellgroomed.com.tempappname.Data.BusyRatingFunctions;
import dotheyappearwellgroomed.com.tempappname.Data.LocationInfo;
import dotheyappearwellgroomed.com.tempappname.Database.ApiConnector;
import dotheyappearwellgroomed.com.tempappname.Services.BusyRatingTask;
import dotheyappearwellgroomed.com.tempappname.Services.LocationService;

/**
 * Created by MikeM on 4/1/2018.
 */

public class HomeActivity extends AppCompatActivity {

    LocationInfo active_location;
    ImageButton ib_favorite;

    // How Busy Features
    TimePicker tp_time_selector;
    TextView tv_busy_label;
    TextView tv_busy_rating;
    BusyRatingTask busyRatingTask;

    // Rating Features
    TextView tv_rate_label;
    TextView tv_sb_label_empty;
    TextView tv_sb_label_busy;
    SeekBar sb_busy_rater;
    Button btn_rate;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if(item.getItemId() == R.id.action_home) {
                return true;
            } else if (item.getItemId() == R.id.action_search) {
                Intent locationPicker = new Intent(getApplicationContext(), MapsActivity.class);
                startActivityForResult(locationPicker, FragmentEnum.MAPS_ID.ordinal());
                return true;
            }
            else if (item.getItemId() == R.id.action_favorites) {
                Intent favoritesIntent = new Intent(getApplicationContext(), FavoritesActivity.class);
                startActivityForResult(favoritesIntent, FragmentEnum.FAVORITES_ID.ordinal());
                return true;
            }
            else if (item.getItemId() == R.id.action_history) {
                Intent historyIntent = new Intent(getApplicationContext(), HistoryActivity.class);
                startActivityForResult(historyIntent, FragmentEnum.HISTORY_ID.ordinal());
                return true;
            }

            return false;
        }
    };

    public HomeActivity(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_fragment);

        // Kick off location service
        startService(new Intent(this, LocationService.class));

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.action_home);

        // Initialize shared pref favorites and history
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        // DEBUG===Clear SHARED PREFS====
        //editor.clear();
        //editor.commit();
        //===============================

        Set<String> user_favorites = preferences.getStringSet("user_favorites_string_set", null);
        if (user_favorites == null)
        {
            user_favorites = new LinkedHashSet<>();
        }
        editor.putStringSet("user_favorites_string_set", user_favorites);

        Set<String> user_search_history = preferences.getStringSet("user_searches_string_set", null);
        if (user_search_history == null)
        {
            user_search_history = new LinkedHashSet<>();
        }
        editor.putStringSet("user_searches_string_set", user_search_history);

        // Commit
        editor.commit();

        initializeButtons();

        checkLocationPermission();

        //set the proper view
        if(didUserRateLastHour() || !isLocationEnabled()) {
            showHowBusyView();
        }else{
            showRateLocationView();
        }

        Places.initialize(getApplicationContext(), "*");

// Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

    }

    private void initializeButtons()
    {
        ib_favorite = findViewById(R.id.favorite);
        ib_favorite.setOnClickListener(new View.OnClickListener()   {
            public void onClick(View v)  {
                favoriteLocation(v);
            }
        });

        ImageView search = findViewById(R.id.home_search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS_COMPONENTS,
                        Place.Field.LAT_LNG, Place.Field.VIEWPORT);

                Intent search_data = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(HomeActivity.this);
                startActivityForResult(search_data, 1);
            }
        });

        tp_time_selector = findViewById(R.id.timePicker);
        setTimePickerToCurrentTime();
        tp_time_selector.setEnabled(false);

        tv_busy_label = findViewById(R.id.textView2);
        tv_busy_rating = findViewById(R.id.busy_rating_value2);

        tv_rate_label = findViewById(R.id.rate_your_location);

        sb_busy_rater = findViewById(R.id.rating_seek_bar);
        sb_busy_rater.setMax(10);
        sb_busy_rater.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setBusyRatingValue((double)i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        tv_sb_label_empty = findViewById(R.id.seek_bar_label1);
        tv_sb_label_busy = findViewById(R.id.seek_bar_label2);

        btn_rate = findViewById(R.id.rate_button);
        btn_rate.setOnClickListener(new View.OnClickListener()   {
            public void onClick(View v)  {
                rateLocation();
            }
        });
    }

    /*
     * Auto-Complete Search Handler
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.d("searchResult", place.getAddress().toString());
                validateUserSearch(place.getAddress());
            } else if (resultCode == RESULT_ERROR) {
                // TODO: Handle the error.
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    public void validateUserSearch(CharSequence place) {
        List<Address> addressList = null;
        if (place.toString() != null || !place.toString().equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(place.toString(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(!addressList.isEmpty())
            {
                // Set active location -- The how busy info for the selected location
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("active_location", place.toString());

                // Add what we searched into the search history
                Set<String> user_search_history = preferences.getStringSet("user_searches_string_set", null);
                user_search_history.add(place.toString());
                editor.putStringSet("user_searches_string_set", user_search_history);

                // Commit all
                editor.commit();

                showHowBusyView();
            }
            else
            {
                SpannableStringBuilder ssBuilder = new SpannableStringBuilder("\nInvalid Address");
                StyleSpan span = new StyleSpan(Typeface.NORMAL);
                ScaleXSpan span1 = new ScaleXSpan(1);
                ssBuilder.setSpan(span, 0, 5, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                ssBuilder.setSpan(span1, 0, 5, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

                AlertDialog alertDialog = new AlertDialog.Builder(HomeActivity.this).create();
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

                if(button != null)
                {
                    button.setTextColor(Color.WHITE);
                }
            }
        }
    }

    private void enableForLocation(String requested_location)
    {
        setDestinationLabelText(requested_location);
        active_location = new LocationInfo(requested_location);
        activeLocationIsFavorited();
    }

    private void setHowBusyFeaturesVisible(boolean enabled)
    {
        int view = enabled ? View.VISIBLE : View.GONE;

        tv_busy_label.setVisibility(view);
    }

    private void setRatingButtonsVisible(boolean enabled)
    {
        int view = enabled ? View.VISIBLE : View.GONE;

        tv_rate_label.setVisibility(view);
        sb_busy_rater.setVisibility(view);
        tv_sb_label_empty.setVisibility(view);
        tv_sb_label_busy.setVisibility(view);
        btn_rate.setVisibility(view);
    }

    private void setRatingButtonsEnabled(boolean enabled)
    {
        sb_busy_rater.setEnabled(enabled);
        btn_rate.setEnabled(enabled);
    }

    private void displayBusyRatingForActiveLocation()
    {
        // Stop a previous timer if it existed
        if(busyRatingTask!=null)
        {
            busyRatingTask.shutdown();
        }
        // Start a service to check for the rating every so often
        busyRatingTask = new BusyRatingTask(tv_busy_rating, active_location);
        busyRatingTask.startTimer();
    }

    private void rateLocation()
    {
        Toast.makeText(this, "Thank you for your rating!", Toast.LENGTH_SHORT).show();

        //String starRating = getActiveStarRating()+"";
        //setActiveStarRating(0);
        setRatingButtonsEnabled(false);

        new SearchLocationTask(this).execute(ApiConnector.LOCATION_RATE, sb_busy_rater.getProgress()+"");

        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat( "dd/MM/yyyy HH");
        String dateFormatted = dateFormat.format(currentDate);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("last_user_rating", dateFormatted);

        Log.d("rateLocation", "rated location w/ time: " + dateFormatted);

        //Set this location as our active
        editor.putString("active_location", active_location.toString());

        // Commit all
        editor.commit();

        showHowBusyView();
    }

    private boolean activeLocationIsFavorited()
    {
        if(active_location==null)
            return false;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> user_favorites = preferences.getStringSet("user_favorites_string_set", null);
        boolean isFavorite = user_favorites.contains(active_location.toString());
        Log.d("isFavorited", isFavorite +"");
        if(isFavorite)
        {
            ib_favorite.setImageResource(R.drawable.ic_favorite_white_18dp);
        }
        else
        {
            ib_favorite.setImageResource(R.drawable.ic_favorite_border_white_18dp);
        }
        return isFavorite;
    }

    private void favoriteLocation(View view)
    {
        if(activeLocationIsFavorited()) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            Set<String> user_favorites = preferences.getStringSet("user_favorites_string_set", null);

            user_favorites.remove(active_location.toString());

            editor.putStringSet("user_favorites_string_set", user_favorites);
            editor.commit();

            ib_favorite.setImageResource(R.drawable.ic_favorite_border_white_18dp);

            Toast.makeText(this, "Removed from favorites.",
                    Toast.LENGTH_SHORT).show();
        }
        else
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            Set<String> user_favorites = preferences.getStringSet("user_favorites_string_set", null);

            user_favorites.add(active_location.toString());

            editor.putStringSet("user_favorites_string_set", user_favorites);
            editor.commit();

            ib_favorite.setImageResource(R.drawable.ic_favorite_white_18dp);

            Toast.makeText(this, "Added to favorites.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void setBusyRatingValue(Double rating){
        ((TextView) findViewById(R.id.busy_rating_value2)).setText(rating+"");
    }

    private void setDestinationLabelText(String text){
        Log.d("HomeActivity", text);
        ((TextView) findViewById(R.id.destination_string)).setText(text);
    }

    private void setTimePickerToCurrentTime()
    {
        DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        Calendar calendar = Calendar.getInstance();
        String dateString = dateFormat.format(calendar.getTime());
        Log.d("TIME:", dateString);

        int hour = Integer.parseInt(dateString.toString().split(":")[0]);
        int minutes = Integer.parseInt(dateString.toString().split(":")[1].split(" ")[0]);
        String amPM = dateString.toString().split(" ")[1];
        if(amPM.equals("PM"))
        {
            hour+=12;
        }
        tp_time_selector.setHour(hour);
        tp_time_selector.setMinute(minutes);
    }

    private void showRateLocationView()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String myLocation = preferences.getString("last_pinged_location", null);
        // Set the destination to where we are currently located
        if(myLocation != null) {
            enableForLocation(myLocation);
        } else {
            Toast.makeText(this, "Unable to get location to rate...", Toast.LENGTH_LONG).show();
            showHowBusyView();
        }

        setHowBusyFeaturesVisible(false);
        setRatingButtonsVisible(true);
        setRatingButtonsEnabled(true);
    }

    private void showHowBusyView()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String destinationString = preferences.getString("active_location", null);
        if(destinationString != null) {
            // Sets the active location and such
            enableForLocation(destinationString);
            setHowBusyFeaturesVisible(true);
            setRatingButtonsVisible(false);
            setRatingButtonsEnabled(false);
            displayBusyRatingForActiveLocation();
        } else
        {
            setHowBusyFeaturesVisible(false);
            setRatingButtonsVisible(false);
            setRatingButtonsEnabled(false);
        }
    }

    private void checkLocationPermission() {
        Log.d("HomeActivity", "checkLocationPermission");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(HomeActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        LocationService.MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LocationService.MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }else
        {
            Log.d("HomeActivity", "already have location permission");
        }
    }

    private boolean isLocationEnabled()
    {
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        try {
            if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                Log.d("LocationEnabled", "Is Enabled.");
                return true;
            }
            else
            {
                Log.d("LocationEnabled", "Not Enabled.");
                return false;
            }
        }catch (Exception ex){}

        return false;
    }

    private boolean didUserRateLastHour()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String user_rating = preferences.getString("last_user_rating", null);
        if(user_rating!=null)
        {
            Date currentDate = new Date();
            DateFormat dateFormat = new SimpleDateFormat( "dd/MM/yyyy HH");
            String dateFormatted = dateFormat.format(currentDate);

            String currentDay=dateFormatted.split(" ")[0];
            String lastRatingDay=user_rating.split(" ")[0];

            if(currentDay.equals(lastRatingDay))
            {
                String currentHour = dateFormatted.split(" ")[1];
                String lastRatingHour = user_rating.split(" ")[1];

                if (currentHour.equals(lastRatingHour))
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("HomeActivity", "onRequestPermissionsResult");
        switch (requestCode) {
            case LocationService.MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        showRateLocationView();
                    }
                } else {
                    showHowBusyView();
                    Toast.makeText(this, "Location Permission Denied.", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    // DATABASE CONNECTION - HomeActivity
    private class SearchLocationTask extends AsyncTask<String, Long, JSONArray>
    {
        private Context taskContext;

        public SearchLocationTask(Context context){
            taskContext = context;
        }

        @Override
        protected JSONArray doInBackground(String... params) {
            ApiConnector apiConnector = new ApiConnector();
            String apiCommand = params[0];

            if (apiCommand.equals(ApiConnector.LOCATION_SEARCH)) {
                return apiConnector.SearchLocation(active_location.getAddress(), active_location.getCity(), active_location.getState());
            }
            else if(apiCommand.equals(ApiConnector.LOCATION_RATE)) {
                String android_id = Settings.Secure.getString(taskContext.getContentResolver(), Settings.Secure.ANDROID_ID);
                String rating = params[1];
                Log.d("AsyncTask", "Passing rating of "+rating+" to the ApiConnector.");
                //(String locationName, String locationCity, String locationState, String locationAddress, String rating, String userId)
                apiConnector.rateLocation(active_location.getAddress(),
                        active_location.getCity(),
                        active_location.getState(),
                        active_location.getAddress(),
                        rating,
                        android_id);
                return null;
            } else {
                Log.d("CRITICAL", "SearchLocationTask received a command it couldn't handle. No task will be performed.");
                return null;
            }
            //return params[0].SearchLocation();
            //return params[0].SearchLocation(active_location.getAddress(), active_location.getCity(), active_location.getState());
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            // really only applies to LOCATION_SEARCH
            if(jsonArray!=null) {
                ArrayList<BusyRating> ratingsList = BusyRatingFunctions.jsonResultToSingleRatingList(jsonArray);
                BusyRating finalRating = BusyRatingFunctions.createAverageBusyRating(ratingsList);
                Log.d("busy rating", finalRating.getBusyRating() + "");
                setBusyRatingValue(finalRating.getBusyRating());
            }else {
                ((TextView) findViewById(R.id.busy_rating_value2)).setText("No Rating");
            }
        }
    }
}
