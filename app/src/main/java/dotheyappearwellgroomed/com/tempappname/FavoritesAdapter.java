package dotheyappearwellgroomed.com.tempappname;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import dotheyappearwellgroomed.com.tempappname.Data.LocationInfo;
import dotheyappearwellgroomed.com.tempappname.Data.UserFavorite;

/**
 * Created by MikeM on 4/17/2018.
 */

public class FavoritesAdapter extends BaseAdapter {

    private Context context;
    private List<String> favorites;

    //We have to pass the currentUser to every activity so we pass it for when view post button is hit
    public FavoritesAdapter(Context context, Set<String> favorites)
    {
        this.context = context;
        this.favorites = new ArrayList<>(favorites);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public Object getItem(int position)
    {
        return favorites.get(position);
    }

    @Override
    public int getCount()
    {
        if(favorites == null)
        {
            return 0;
        }

        return favorites.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        FavoritesSingleFragment favoritesSingleFragment;
        UserFavorite userFavorite = new UserFavorite(new LocationInfo(favorites.get(position)));

        if(convertView == null)
        {
            favoritesSingleFragment = new FavoritesSingleFragment(context, userFavorite);
        }
        else
        {
            favoritesSingleFragment = (FavoritesSingleFragment) convertView;
            favoritesSingleFragment.setTextViewData(userFavorite);
        }
        return favoritesSingleFragment;
    }
}
