//This guy acts as a temporary driver for now
//It provides the data needed for ApiConnector.java to make its query
//It then parses the data into a nice little list view

package dotheyappearwellgroomed.com.tempappname.Database;

import android.app.Activity;
import android.os.AsyncTask;
//import android.support.v7.app.ActionBarActivity;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity //extends ActionBarActivity
{

    private TextView responseTextView;

    //@Override
    protected void onCreate(Bundle savedInstanceState) {
       // super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);


        //this.responseTextView = (TextView) this.findViewById(R.id.responseTextView);


      //  new SearchLocationTask().execute(new ApiConnector());


    }

    public void setTextToTextView(JSONArray jsonArray)
    {
        String s  = "";
        for(int i=0; i<jsonArray.length();i++){

            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);
                s = s +
                        "Name: "+json.getString("name")+"\n" +
						"Adress: " +json.getString("streetAddress")+"\n" +
                        "City: "+json.getInt("city") + "\n" +
                        "State: "+json.getString("state") + "\n" +
						"Type: "+json.getString("type") + "\n" +
						"Rating: "+json.getString("busyRating") + "\n\n";
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        this.responseTextView.setText(s);

    }


}
