package dotheyappearwellgroomed.com.tempappname.Data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by MikeM on 4/27/2018.
 */

public class BusyRatingFunctions {

    // Returns all the entered busy ratings in a list, these can then be averaged for an overall rating
    public static ArrayList<BusyRating> jsonResultToSingleRatingList(JSONArray jsonArray)
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

            Log.d("jsonResult", "result = " + address+","+city+","+state+","+rating);

            if (address!=null && city!=null && state !=null && rating !=null)
            {
                BusyRating singleRating = new BusyRating();
                //TODO we dont currently have access to all the location fields we need
                LocationInfo locationInfo = new LocationInfo("");
                locationInfo.address=address;
                locationInfo.city=city;
                locationInfo.state=state;
                singleRating.locationInfo = locationInfo;
                singleRating.busy_rating=Double.parseDouble(rating);

                resultingRatingList.add(singleRating);
            }

        }

        return resultingRatingList;
    }

    // im just going to go ahead and assume the locations in the list are all the same :D
    public static BusyRating createAverageBusyRating(ArrayList<BusyRating> ratingsList)
    {
        BusyRating finalRating = new BusyRating();
        finalRating.busy_rating=0.0;
        if(ratingsList.size()==0)
            return finalRating;

        double ratingSum = 0.0;
        for (BusyRating currentRating : ratingsList) {
            //these should be positive numbers..
            if (currentRating.busy_rating>0)
                ratingSum+=currentRating.busy_rating;
        }

        finalRating.busy_rating = (ratingSum/ratingsList.size());

        return finalRating;
    }
}
