//Tutorial Video https://www.youtube.com/watch?v=4Soj22OMc98
//This guy queries the database for a specific location and returns a JSON Array with any locations with said name, city, and state

package dotheyappearwellgroomed.com.tempappname.Database;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by tahseen0amin on 16/02/2014.
 */
public class ApiConnector {

    public static String LOCATION_SEARCH = "LOCATION_SEARCH";
    public static String LOCATION_RATE = "LOCATION_RATE";

    public JSONArray SearchLocation(String name, String city, String state)
    {
        // URL for getting all customers
        //String url = "http://192.168.0.7/~tahseen0amin/Tutorial/getAllCustomers.php";
        
        //Examples for rating and adding locations.  Just plug in correct string values 
        //http://isitbusy.net/rateLocation.php?LocationName=Applebooze&LocationCity=somePlace&LocationState=NJ&LocationStreetAddress=123 bob road&LocationRating=5&PersonName=66666
        //http://isitbusy.net/addLocation.php?LocationName=MSE&LocationCity=somePlace&LocationState=NJ&LocationStreetAddress=not bob road&LocationType=diner
        try {
            String url = "http://isitbusy.net/searchLocation.php?" +
                    "LocationName=" + URLEncoder.encode(name, "UTF-8") +
                    "&LocationCity=" + URLEncoder.encode(city, "UTF-8") +
                    "&LocationState=" + URLEncoder.encode(state, "UTF-8");

            HttpEntity httpEntity = executeURL(url);

            // Convert HttpEntity into JSON Array
            JSONArray jsonArray = null;
            if (httpEntity != null) {
                try {
                    String entityResponse = EntityUtils.toString(httpEntity);
                    Log.e("Entity Response  : ", entityResponse);
                    if(!entityResponse.equals("no go sry bro")) {
                        jsonArray = new JSONArray(entityResponse);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return jsonArray;
        }catch (Exception e){}

        return null;
    }

    public void rateLocation(String locationName, String locationCity, String locationState, String locationAddress, String rating, String userId)
    {
        try {
            String url = "http://isitbusy.net/rateLocation.php?"+
                    "LocationName="+URLEncoder.encode(locationName, "UTF-8")+
                    "&LocationCity="+URLEncoder.encode(locationCity, "UTF-8")+
                    "&LocationState="+URLEncoder.encode(locationState, "UTF-8")+
                    "&LocationStreetAddress="+URLEncoder.encode(locationAddress, "UTF-8")+
                    "&LocationRating="+URLEncoder.encode(rating, "UTF-8")+
                    "&PersonName="+URLEncoder.encode(userId, "UTF-8");

            executeURL(url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private HttpEntity executeURL(String url)
    {
        HttpEntity httpEntity = null;

        Log.d("executeURL", url);

        try
        {
            DefaultHttpClient httpClient = new DefaultHttpClient();  // Default HttpClient
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            httpEntity = httpResponse.getEntity();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Return the entity for the caller to parse the result
        return httpEntity;
    }

}
