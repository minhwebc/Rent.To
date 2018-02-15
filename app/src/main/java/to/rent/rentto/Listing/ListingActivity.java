package to.rent.rentto.Listing;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

import to.rent.rentto.R;
import to.rent.rentto.Utils.BottomNavigationViewHelper;

import static to.rent.rentto.Utils.BottomNavigationViewHelper.setupBottomNavigationView;


/**
 * Created by Sora on 2/14/2018.
 */

public class ListingActivity extends AppCompatActivity {
    private static final String TAG = "ListingActivity";
    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);
        mContext = ListingActivity.this;
        Log.d(TAG, "onCreate: Started.");

        /*
        ImageView backarrow = (ImageView) findViewByID(R.id.backArrow);
        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Navigating back to 'HomeActivity'");
                finish();
            }
        });
        */

        setupBottomNavigationView();
    }

    private void setupListing() {
        Log.d(TAG, "setupListing: initializing 'Listing' list");
        //ListView listView = (ListView) findViewById(R.id.lvListing);
        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.submit_listing));
    }

    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up bottomnavigationview");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);
    }
}
