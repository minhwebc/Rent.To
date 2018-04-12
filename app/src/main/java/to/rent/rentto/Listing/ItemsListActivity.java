package to.rent.rentto.Listing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

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

    private android.support.v4.app.FragmentManager fragmentManager;
    private Context mContext;
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<String> iDs = new ArrayList<>();
    private DatabaseReference mReference;
    private RecyclerViewAdapter staggeredRecyclerViewAdapter;
    private String filter;
    private TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        Log.d(TAG, "onCreate: Started.");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_list);
        mContext = ItemsListActivity.this;
        mReference = FirebaseDatabase.getInstance().getReference();
        setupBottomNavigationView();

        int width = getScreenSizeX();
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

    //To-do here find the current city
    private String findCurrentCity(){
        return "seattle";
    };

    private void initImageBitMaps(){
        //grabs all the photos back
        Log.d(TAG, "initimagebitmaps");
        Query query = mReference.child(mContext.getString(R.string.dbname_items)).child(findCurrentCity());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mImageUrls.clear();
                iDs.clear();
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    String keyID = singleSnapshot.getKey(); //photoIDs
                    Item mItem = singleSnapshot.getValue(Item.class);
                    String photo_path = mItem.imageURL;
                    System.out.println(mItem.category);
                    if(filter != null){
                        if(filter.equals(mItem.category)){
                            mImageUrls.add(photo_path);
                            iDs.add(keyID);
                        }
                    } else {
                        mImageUrls.add(photo_path);
                        iDs.add(keyID);
                    }
                }
                staggeredRecyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
                new RecyclerViewAdapter(this, iDs, mImageUrls, width);
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

    private void setFilter(View view) {

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
                    String filterData = FilterActivity.getResult(data);
                    filter = filterData;
                    initImageBitMaps();
                    Log.d(TAG, "filter is " + filterData);
                } else {
                    Log.d(TAG, "Filter canceled");
                }
        }
    }


}