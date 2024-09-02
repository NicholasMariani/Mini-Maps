package dotheyappearwellgroomed.com.tempappname.Data;

import android.util.Log;

import java.util.Arrays;
import java.util.List;

/**
 * Created by MikeM on 4/7/2018.
 */

public class LocationInfo {

    String toString;
    String address;
    String city;
    String state;
    String zip;
    String country;

    public LocationInfo(String fullAddress)
    {
        toString = fullAddress;
        parseFullAddress(fullAddress);
    }

    public String toString() {return toString;}
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getZip() { return zip; }
    public String getCountry() { return country; }

    private void parseFullAddress(String fullAddress)
    {
        // We receive the address from google as such
        // ADDRESS, CITY, STATE_ABRV ZIP, COUNTRY
        List<String> someAddress = Arrays.asList(fullAddress.split(","));
        if(someAddress.size() < 4)
        {
            Log.d("parseFullAddress()", "Error: address given did not contain full address info split by 3 commas");
            return;
        }

        address = someAddress.get(0);
        city = someAddress.get(1);
        state = someAddress.get(2).split(" ")[1];
        zip = someAddress.get(2).split(" ")[2];
        country = someAddress.get(3);

        Log.d("parseFullAddress()", "Parsed Address, City, State, Zip, Country: " + address + ", "+ city + ", " + state + ", " + zip + ", " + country);
    }
}
