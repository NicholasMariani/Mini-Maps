package dotheyappearwellgroomed.com.tempappname.Data;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dotheyappearwellgroomed.com.tempappname.Database.ApiConnector;

/**
 * Created by MikeM on 4/7/2018.
 */

@Deprecated
/*
 * Classes are now using async tasks to retrieve busy ratings
 * This was an an idea to separate the responsibility but unfortunately in java its not easy to implement
 * function callbacks which is what this class would have to do to return back to the activity that called it
 */
public class DatabaseReader extends AsyncTask<ApiConnector, Long, JSONArray>
{

    private static DatabaseReader databaseReaderInstance;
    private ApiConnector apiConnector;

    private DatabaseReader()
    {
        apiConnector = new ApiConnector();
    }

    @Override
    protected JSONArray doInBackground(ApiConnector... apiConnectors) {
        return null;
    }

    public static DatabaseReader getInstance()
    {
        Log.d("DatabaseReader", "here");
        if (databaseReaderInstance==null)
        {
            databaseReaderInstance = new DatabaseReader();
        }

        Log.d("DatabaseReader", "instance="+databaseReaderInstance.toString());
        return databaseReaderInstance;
    }

    public BusyRating getBusyRatingForLocation(LocationInfo requested_location)
    {
        Log.d("jsonResult", "here");
        ArrayList<BusyRating> ratingsList = jsonResultToSingleRatingList(apiConnector.SearchLocation("Applebooze", "somePlace", "NJ"));
        BusyRating finalRating = createAverageBusyRating(ratingsList);
        return finalRating;
    }

    // Returns all the entered busy ratings in a list, these can then be averaged for an overall rating
    private ArrayList<BusyRating> jsonResultToSingleRatingList(JSONArray jsonArray)
    {
        ArrayList<BusyRating> resultingRatingList = new ArrayList<>();
        Log.d("jsonResult", "resulting size = " + jsonArray.length());
        // for each resulting rating
        for(int i=0; i<jsonArray.length();i++)
        {
            String address = null;
            String city = null;
            String state = null;
            String rating = null;

            JSONObject json = null;

            try {
                json = jsonArray.getJSONObject(i);
                address = json.getString("streetAddress");
                city =  json.getString("city");
                state =  json.getString("state");
                rating = json.getString("busyRating");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (address!=null && city!=null && state !=null && rating !=null)
            {
                BusyRating singleRating = new BusyRating();
                singleRating.locationInfo.address=address;
                singleRating.locationInfo.city=city;
                singleRating.locationInfo.state=state;
                singleRating.busy_rating=Double.parseDouble(rating);
                resultingRatingList.add(singleRating);
            }

        }

        return resultingRatingList;
    }

    // im just going to go ahead and assume the locations in the list are all the same :D
    private BusyRating createAverageBusyRating(ArrayList<BusyRating> ratingsList)
    {
        BusyRating finalRating = new BusyRating();
        finalRating.busy_rating=0.0;
        if(ratingsList.size()==0)
            return finalRating;

        double ratingSum = 0.0;
        for (BusyRating currentRating : ratingsList) {
            //these should be positive numbers..
            if (currentRating.busy_rating>0)
                ratingSum+=ratingSum;
        }

        finalRating.busy_rating = (ratingSum/ratingsList.size());

        return finalRating;
    }
}
