package to.rent.rentto.Listing;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import to.rent.rentto.R;
import to.rent.rentto.Utils.BottomNavigationViewHelper;
import to.rent.rentto.Utils.UniversalImageLoader;

/**
 * Created by Sora on 2/15/2018.
 */

public class ItemsListActivity extends AppCompatActivity {

    private static final String TAG = "ItemsListActivity";
    private static final int NUM_COLUMNS = 2;

    private Context mContext;
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<String> iDs = new ArrayList<>();
    private DatabaseReference mReference;
    private RecyclerViewAdapter staggeredRecyclerViewAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        Log.d(TAG, "onCreate: Started.");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_list);
        mContext = ItemsListActivity.this;
        mReference = FirebaseDatabase.getInstance().getReference();


        setupBottomNavigationView();

        ImageView backarrow = (ImageView) findViewById(R.id.backArrow);
        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Navigating back to 'HomeActivity'");
                finish();
            }
        });
        initImageLoader();
        initRecyclerView();
        initImageBitMaps();
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
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    String keyID = singleSnapshot.getKey(); //photoIDs
                    iDs.add(keyID);
                    Query photoPath = mReference.child(mContext.getString(R.string.dbname_items)).child(findCurrentCity()).child(keyID).child("imageURL");
                    photoPath.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d(TAG, dataSnapshot.getValue()+"");
                            String photo_path = (String) dataSnapshot.getValue();
                            mImageUrls.add(photo_path);
                            staggeredRecyclerViewAdapter.notifyDataSetChanged();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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

    private void initRecyclerView() {
        Log.d(TAG, "initRecyclerView staggered view");
        RecyclerView recyclerView = findViewById(R.id.recylerView);
        staggeredRecyclerViewAdapter =
                new RecyclerViewAdapter(this, iDs, mImageUrls);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(NUM_COLUMNS, LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setAdapter(staggeredRecyclerViewAdapter);
    }
}