package dotheyappearwellgroomed.com.tempappname;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by MikeM on 4/1/2018.
 * The history view is almost identical to the favorites view
 * I will reuse code for here but just pass in the list of previous searches..
 */

public class HistoryActivity extends AppCompatActivity {

    private ListView historyListView;
    private Button btn_clear;
    private Context my_context;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if(item.getItemId() == R.id.action_home) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivityForResult(intent, FragmentEnum.HOME_ID.ordinal());
                return true;
            } else if(item.getItemId() == R.id.action_search) {
                Intent locationPicker = new Intent(getApplicationContext(), MapsActivity.class);
                startActivityForResult(locationPicker, FragmentEnum.MAPS_ID.ordinal());
                return true;
            } else if(item.getItemId() == R.id.action_favorites) {
                Intent favoritesIntent = new Intent(getApplicationContext(), FavoritesActivity.class);
                startActivityForResult(favoritesIntent, FragmentEnum.FAVORITES_ID.ordinal());
                return true;
            } else if(item.getItemId() == R.id.action_history) {
                return true;
            }
            return false;
        }
    };

    public HistoryActivity(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_fragment);

        historyListView = findViewById(R.id.favorites_list);
        btn_clear = findViewById(R.id.clear_button);

        my_context = this;

        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(my_context);
                SharedPreferences.Editor editor = preferences.edit();
                Set<String> user_search_history = new LinkedHashSet<>();
                editor.putStringSet("user_searches_string_set", user_search_history);

                // Commit
                editor.commit();

                Intent refreshIntent = new Intent(getApplicationContext(), HistoryActivity.class);
                startActivityForResult(refreshIntent, FragmentEnum.HISTORY_ID.ordinal());
            }
        });

        listFavorites();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.action_history);
    }

    private void listFavorites()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> user_search_history = preferences.getStringSet("user_searches_string_set", null);
        if(user_search_history!=null && user_search_history.size() > 0) {
            FavoritesAdapter adapter = new FavoritesAdapter(this, user_search_history);
            historyListView.setAdapter(adapter);
        }
    }

}
