package to.rent.rentto.Listing;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import to.rent.rentto.Models.Item;
import to.rent.rentto.Models.User;
import to.rent.rentto.R;
import to.rent.rentto.Utils.BottomNavigationViewHelper;

/**
 * Created by Sora on 2/14/2018.
 */

public class ListingActivity extends AppCompatActivity {
    private static final String TAG = "ListingActivity";
    private Context mContext;
    private String ITEM_ID;
    private String CITY;
    private Item mItem;
    private DatabaseReference mReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);
        mContext = ListingActivity.this;
        Log.d(TAG, "onCreate: Started.");

        ITEM_ID = getIntent().getStringExtra("ITEM_ID");
        CITY = getIntent().getStringExtra("CITY");

        mReference = FirebaseDatabase.getInstance().getReference();

        setupBottomNavigationView();
        grabTheItem();
        ImageView backarrow = (ImageView) findViewById(R.id.backArrow);
        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Navigating back to 'HomeActivity'");
                finish();
            }
        });
    }

    private void grabTheItem(){
        Query query = mReference.child(mContext.getString(R.string.dbname_items)).child(CITY).child(ITEM_ID);
        query.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, dataSnapshot.getValue()+"");
                mItem = dataSnapshot.getValue(Item.class);
                TextView item_name = findViewById(R.id.textView2);
                TextView description = findViewById(R.id.textView3);
                TextView price = findViewById(R.id.textView4);
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
                //post_image.setScaleType(ImageView.ScaleType.FIT_XY);

                Query query = mReference.child(mContext.getString(R.string.dbname_users)).child(mItem.userUID);
                query.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        TextView userField = findViewById(R.id.textView5);
                        userField.setText(user.getUsername());
                        ImageButton mButton = findViewById(R.id.imageButton);
                        mButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String token = FirebaseInstanceId.getInstance().getToken();
                                Log.d(TAG, "Token : " + token);
                                Toast.makeText(mContext, token, Toast.LENGTH_SHORT).show();

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
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
    }
}
