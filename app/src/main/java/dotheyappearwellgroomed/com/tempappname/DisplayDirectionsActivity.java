package dotheyappearwellgroomed.com.tempappname;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import java.util.ArrayList;


public class DisplayDirectionsActivity extends ListActivity {

    ArrayList<String> items = new ArrayList<>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        ArrayList<String> steps = (ArrayList<String>)bundle.getSerializable("Directions");
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);

        setListAdapter(adapter);

        for(int i = 0; i < steps.size(); i++)
        {
            if(steps.get(i) != null) {
                String plainText = steps.get(i).replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", " ");
                items.add(plainText);
                adapter.notifyDataSetChanged();
            }
        }
    }
}