package to.rent.rentto.Listing;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import to.rent.rentto.R;


public class FilterFragment extends android.support.v4.app.Fragment {

    final String[] categoryValues= {"Manual Tools", "Motor Tools", "Sports Equipment", "Cookware", "Videogames", "Electronics", "Movies", "Parking Spot", "Party Supplies", "Other"};
    private static final String TAG = "FilterFragment";


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_location, container, false);
        Log.d(TAG, "inside of ItemsListActivity.java");
        return view;
    }
}
