package dotheyappearwellgroomed.com.tempappname.Data;

/**
 * Created by MikeM on 4/1/2018.
 */

public class UserFavorite {

    private LocationInfo locationInfo;

    public UserFavorite(LocationInfo locationInfo)
    {
        this.locationInfo = locationInfo;
    }

    public LocationInfo getLocationInfo(){return locationInfo;}
}
