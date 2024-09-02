package dotheyappearwellgroomed.com.tempappname.Services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;


import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import dotheyappearwellgroomed.com.tempappname.Data.BusyRating;
import dotheyappearwellgroomed.com.tempappname.Data.BusyRatingFunctions;
import dotheyappearwellgroomed.com.tempappname.Data.LocationInfo;
import dotheyappearwellgroomed.com.tempappname.Database.ApiConnector;
import dotheyappearwellgroomed.com.tempappname.FavoritesSingleFragment;

import static android.app.Service.START_NOT_STICKY;

/**
 * Created by MikeM on 4/23/2018.
 * On an interval, pings the database for a busy rating and sets a TextField to the resulting value
 */

public class BusyRatingTask extends TimerTask {

    private final int INTERVAL = 5000;

    private Timer timer;
    private boolean keepRunning;
    private TextView viewToUpdate;
    private LocationInfo locationToCheck;

    public BusyRatingTask(TextView viewToUpdate, LocationInfo locationInfo)
    {
        this.viewToUpdate = viewToUpdate;
        this.locationToCheck = locationInfo;
        timer = new Timer();
    }

    public void startTimer() {

        Log.d("BusyRatingTask", "================= " + locationToCheck.toString() + " BUSY RATING TIMER TASK STARTED =================");
        keepRunning = true;
        timer.scheduleAtFixedRate(this,0, INTERVAL);
    }

    public void run()
    {
        if(keepRunning) {
            Log.d("BusyRatingTask", "Fetching busy rating for: " + locationToCheck.toString());
            //Kick off database task
            new SearchLocationTask().execute(new ApiConnector());
        }
    }
    public void shutdown()
    {
        timer.cancel();
        keepRunning = false;
    }

    // DATABASE CONNECTION - BusyRatingTask
    private class SearchLocationTask extends AsyncTask<ApiConnector,Long,JSONArray>
    {
        @Override
        protected JSONArray doInBackground(ApiConnector... params) {
            return params[0].SearchLocation(locationToCheck.getAddress(), locationToCheck.getCity(), locationToCheck.getState());
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            Log.d("onPostExecute", "received jsonArray");
            if(jsonArray!=null) {
                ArrayList<BusyRating> ratingsList = BusyRatingFunctions.jsonResultToSingleRatingList(jsonArray);
                BusyRating finalRating = BusyRatingFunctions.createAverageBusyRating(ratingsList);
                viewToUpdate.setText(finalRating.getBusyRating() + "");
            }else {
                viewToUpdate.setText("No Rating");
            }
        }
    }
}
