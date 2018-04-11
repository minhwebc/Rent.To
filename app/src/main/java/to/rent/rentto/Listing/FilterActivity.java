package to.rent.rentto.Listing;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import to.rent.rentto.R;


public class FilterActivity extends AppCompatActivity {

    private final String[] categoryValues = {"Manual Tools", "Motor Tools", "Sports Equipment", "Cookware", "Videogames", "Electronics", "Movies", "Parking Spot", "Party Supplies", "Other"};
    private static final String TAG = "FilterFragment";
    private Context mContext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        Log.d(TAG, "inside of ItemsListActivity.java");
        mContext = FilterActivity.this;
    }

}
