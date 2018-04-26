package to.rent.rentto.Listing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import to.rent.rentto.Models.Item;
import to.rent.rentto.R;
import to.rent.rentto.Utils.BottomNavigationViewHelper;
import to.rent.rentto.Utils.UniversalImageLoader;

/**
 * Created by Sora on 2/15/2018.
 */

public class ItemsListActivity extends AppCompatActivity {

    private static final String TAG = "ItemsListActivity";
    private static final int NUM_COLUMNS = 3;
    private static final int REQUEST_CATEGORY_CODE = 1000;
    private static final int LOCATION_PERMISSION_REQUEST = 1;

    private android.support.v4.app.FragmentManager fragmentManager;
    private Context mContext;
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<String> iDs = new ArrayList<>();
    private ArrayList<String> zipcodes = new ArrayList<>();
    private DatabaseReference mReference;
    private RecyclerViewAdapter staggeredRecyclerViewAdapter;
    private String filter;
    private double miles;
    private TextView textView;
    private SwipeRefreshLayout swipeLayout;
    private ArrayList<Item> mItems = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        Log.d(TAG, "onCreate: Started.");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_list);
        mContext = ItemsListActivity.this;
        mReference = FirebaseDatabase.getInstance().getReference();
        setupBottomNavigationView();
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        miles = 20;
        final int width = getScreenSizeX();
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        initImageLoader();
                        initRecyclerView(width);
                        initImageBitMaps();
                        swipeLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });



        initImageLoader();
        initRecyclerView(width);
        initImageBitMaps();

        textView = (TextView) findViewById(R.id.textView6);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchFilter(v);
            }
        });
    }


    private void initImageBitMaps(){
        //grabs all the photos back
        Log.d(TAG, "initimagebitmaps");
        Query query = mReference.child(mContext.getString(R.string.dbname_items));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String currCity = findCurrentCity();
                mImageUrls.clear();
                iDs.clear();
                zipcodes.clear();
                mItems.clear();
                for(DataSnapshot zips : dataSnapshot.getChildren()) {
                    if (distanceBetweenZip(zips.getKey(), currCity) < miles) {
                        for (DataSnapshot singleSnapShot : dataSnapshot.child(zips.getKey()).getChildren()) {
                            String keyID = singleSnapShot.getKey(); //photoIDs
                            Item mItem = singleSnapShot.getValue(Item.class);
                            String photo_path = mItem.imageURL;
                            System.out.println(mItem.category);
                            if (filter != null) {
                                if (filter.equals(mItem.category)) {
                                    mImageUrls.add(photo_path);
                                    iDs.add(keyID);
                                    mItems.add(mItem);
                                    zipcodes.add(zips.getKey());
                                }
                            } else {
                                mImageUrls.add(photo_path);
                                iDs.add(keyID);
                                mItems.add(mItem);
                                zipcodes.add(zips.getKey());
                            }
                        }
                    }
                }
                staggeredRecyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //To-do here find the current city
    private String findCurrentCity(){
        if(ContextCompat.checkSelfPermission(ItemsListActivity.this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(ItemsListActivity.this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions(ItemsListActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            } else {
                ActivityCompat.requestPermissions(ItemsListActivity.this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            }
        } else {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            try {
                Toast.makeText(ItemsListActivity.this, "Location found", Toast.LENGTH_SHORT).show();
                return getZipcode(location.getLatitude(), location.getLongitude());
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(ItemsListActivity.this, "Location not found, using default zip", Toast.LENGTH_SHORT).show();
                return "98105";
            }
        }
        return "";
    };

    private String getZipcode(double lat, double lon){
        String location = "";

        Geocoder geocoder = new Geocoder(ItemsListActivity.this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);
            if(addresses.size() > 0){
                location = addresses.get(0).getPostalCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(ItemsListActivity.this, "Location not found, using default zip", Toast.LENGTH_SHORT).show();
            return "98105";
        }
        return location;
    }


    private double distanceBetweenZip(String zipOne, String zipTwo){
        String locationOne = zipOne + ", " + "United States";
        String locationTwo = zipTwo + ", " + "United States";
        Geocoder geoCoder = new Geocoder(mContext, Locale.getDefault());
        try {
            List<Address> addresses = geoCoder.getFromLocationName(locationOne, 1);
            double lat = addresses.get(0).getLatitude();
            double lon = addresses.get(0).getLongitude();
            List<Address> addressesTwo = geoCoder.getFromLocationName(locationTwo, 1);
            double latTwo = addressesTwo.get(0).getLatitude();
            double lonTwo = addressesTwo.get(0).getLongitude();
            float[] res = {3};
            Location.distanceBetween(lat, lon, latTwo, lonTwo, res);
            // Convert meters to miles
            return (res[0]/1609);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_bar, menu);
        return true; //we've provided a menu!
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }


    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up bottomnavigationview");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);
    }

    private void initRecyclerView(int width) {
        Log.d(TAG, "initRecyclerView staggered view");
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        staggeredRecyclerViewAdapter =
                new RecyclerViewAdapter(this, iDs, mImageUrls, width, findCurrentCity(), zipcodes, mItems);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(NUM_COLUMNS, LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setAdapter(staggeredRecyclerViewAdapter);
    }

    private int getScreenSizeX () {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        return width;
    }

    private int getScreenSizeY () {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        return height;
    }

    public void launchFilter(View view) {
        Intent intent = new Intent(getApplicationContext(), FilterActivity.class);
        startActivityForResult(intent, REQUEST_CATEGORY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CATEGORY_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    String filterCatData = FilterActivity.getCategory(data);
                    filter = filterCatData;
                    if (filter.equals("No Filter")) {
                        filter = null;
                    }
                    double filterDistanceData = FilterActivity.getDistance(data);
                    miles = filterDistanceData;
                    initImageBitMaps();
                    Log.d(TAG, "filter is " + filterCatData);
                    Log.d(TAG, "distance is " + filterDistanceData);
                } else {
                    Log.d(TAG, "Filter canceled");
                }
        }
    }

    private void getDistanceFromZip(String zip1, String zip2){

    }
}