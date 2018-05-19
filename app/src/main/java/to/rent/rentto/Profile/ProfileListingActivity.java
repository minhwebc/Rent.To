package to.rent.rentto.Profile;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import to.rent.rentto.Models.Item;
import to.rent.rentto.Models.User;
import to.rent.rentto.R;
import to.rent.rentto.Utils.BottomNavigationViewHelper;

/**
 * Created by Sora on 2/14/2018.
 */

public class ProfileListingActivity extends AppCompatActivity {
    private static final String TAG = "ProfileListingActivity";
    private static final int ACTIVITY_NUM = 3;
    private Context mContext;
    private String ITEM_ID;
    private String CITY;
    private Item mItem;
    private DatabaseReference mReference;
    private FirebaseAuth mAuth;
    private User currentUser;
    private Button markAsRentedButton;
    private Button markasDeletedButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_listing);
        mContext = ProfileListingActivity.this;

        mAuth = FirebaseAuth.getInstance();
        Log.d(TAG, "onCreate: Started.");

        ITEM_ID = getIntent().getStringExtra("ITEM_ID");
        CITY = getIntent().getStringExtra("CITY");

        mReference = FirebaseDatabase.getInstance().getReference();
        Query query = mReference.child("users").child(mAuth.getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                Log.d(TAG, currentUser.getUsername());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        initButtons();
        setupBottomNavigationView();
        grabTheItem();

    }

    /**
     * Sets buttons as fields and adds on click listeners
     * Buttons include mark as rented buton, mark as deleted button, backarrow button
     */
    private void initButtons() {
        markAsRentedButton = (Button) findViewById(R.id.rentedButton);
        markasDeletedButton = (Button) findViewById(R.id.deletedButton);
        markAsRentedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Mark as rented button clicked");
            }
        });
        markasDeletedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Mark as deleted button clicked");
            }
        });
        ImageView backarrow = (ImageView) findViewById(R.id.backArrow);
        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Finishing activity");
                finish();
            }
        });
    }

    private void grabTheItem(){
        Query query = mReference.child(mContext.getString(R.string.dbname_items)).child(CITY).child(ITEM_ID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, dataSnapshot.getValue()+"");
                mItem = dataSnapshot.getValue(Item.class);
                if(mItem == null) {
                    Log.d(TAG, "grabTheItem/onDataChange mItem is null");
                    return;
                }
                TextView item_name = findViewById(R.id.textView1);
                TextView description = findViewById(R.id.textView2);
                TextView condition = findViewById(R.id.textView3);
                TextView price = findViewById(R.id.textView5);
                ImageView post_image = findViewById(R.id.imageView);
                RequestOptions requestOptions = new RequestOptions()
                        .placeholder(R.drawable.ic_launcher_background);
                Glide.with(mContext)
                        .load(mItem.imageURL)
                        .apply(requestOptions)
                        .into(post_image);
                item_name.setText(mItem.title);
                description.setText(mItem.description);
                price.setText(mItem.rate+"");
                condition.setText(mItem.condition);
                //post_image.setScaleType(ImageView.ScaleType.FIT_XY);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up bottomnavigationview");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}