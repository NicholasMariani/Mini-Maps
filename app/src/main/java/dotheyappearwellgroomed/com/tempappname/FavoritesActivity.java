package dotheyappearwellgroomed.com.tempappname;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.NonNull;

import java.util.Set;

/**
 * Created by MikeM on 4/1/2018.
 */

public class FavoritesActivity extends AppCompatActivity {

    private ListView favoritesListView;

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
                return true;
            } else if(item.getItemId() == R.id.action_history) {
                Intent historyIntent = new Intent(getApplicationContext(), HistoryActivity.class);
                startActivityForResult(historyIntent, FragmentEnum.HISTORY_ID.ordinal());
                return true;
            }
            return false;
        }
    };

    public FavoritesActivity(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites_fragment);

        favoritesListView = findViewById(R.id.favorites_list);
        listFavorites();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.action_favorites);
    }

    private void listFavorites()
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> user_favorites = preferences.getStringSet("user_favorites_string_set", null);
        if(user_favorites!=null && user_favorites.size() > 0) {
            FavoritesAdapter adapter = new FavoritesAdapter(this, user_favorites);
            favoritesListView.setAdapter(adapter);
        }
    }
}
