package dotheyappearwellgroomed.com.tempappname;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;

import dotheyappearwellgroomed.com.tempappname.Data.BusyRating;
import dotheyappearwellgroomed.com.tempappname.Data.BusyRatingFunctions;
import dotheyappearwellgroomed.com.tempappname.Data.DatabaseReader;
import dotheyappearwellgroomed.com.tempappname.Data.LocationInfo;
import dotheyappearwellgroomed.com.tempappname.Data.UserFavorite;
import dotheyappearwellgroomed.com.tempappname.Database.ApiConnector;
import dotheyappearwellgroomed.com.tempappname.Services.BusyRatingTask;

/**
 * Created by Mike on 3/6/2016.
 */
public class FavoritesSingleFragment extends RelativeLayout implements View.OnClickListener {

    //Buttons & Views
    private ImageButton searchLocation;
    private TextView tv_address;
    private TextView tv_city_state_zip;
    private TextView tv_busy_rating;

    BusyRatingTask busyRatingTask;

    private UserFavorite favorite;
    private Context context;

    public FavoritesSingleFragment(Context context, UserFavorite favorite)
    {
        super(context);

        this.context = context;
        this.favorite = favorite;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.favorites_single_fragment,this,true);

        searchLocation = findViewById(R.id.btn_favorites_search);
        tv_address = findViewById(R.id.tv_address);
        tv_city_state_zip = findViewById(R.id.tv_city_state_zip);
        tv_busy_rating = findViewById(R.id.tv_favorites_busy_rating);

        searchLocation.setOnClickListener(this);

        setTextViewData(favorite);

        //Kick off database task
        busyRatingTask = new BusyRatingTask(tv_busy_rating, favorite.getLocationInfo());
        busyRatingTask.startTimer();
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.btn_favorites_search) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("active_location", favorite.getLocationInfo().toString());
            editor.commit();

            Bundle bundle = new Bundle();
            Intent intent = new Intent(context, HomeActivity.class);
            intent.putExtras(bundle);
            context.startActivity(intent);
        }
    }

    public void setTextViewData(UserFavorite favorite)
    {
        LocationInfo locationInfo = favorite.getLocationInfo();
        tv_address.setText(locationInfo.getAddress());
        tv_city_state_zip.setText(locationInfo.getCity() + ", " + locationInfo.getState() + " " + locationInfo.getZip());
    }
}
