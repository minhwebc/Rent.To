package to.rent.rentto.Listing;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.UUID;

import to.rent.rentto.Models.Item;
import to.rent.rentto.Models.Message;
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
    private FirebaseAuth mAuth;
    private User currentUser;
    FloatingActionButton requestButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);
        mContext = ListingActivity.this;
        requestButton = (FloatingActionButton) findViewById(R.id.requestButton);
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

                Query query = mReference.child(mContext.getString(R.string.dbname_users)).child(mItem.userUID);
                query.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        TextView userField = findViewById(R.id.textView4);
                        userField.setText(user.getUsername());
                        FloatingActionButton mButton = findViewById(R.id.requestButton);
                        mButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                DatabaseReference renterUIDRef = mReference.child("notificationMessages").child(mItem.userUID);
                                DatabaseReference pushedKey = renterUIDRef.push();

                                pushedKey.setValue(currentUser.getUsername() + " have made you an offer", new DatabaseReference.CompletionListener() {

                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        mReference.child("notifications").child(mItem.userUID).child(currentUser.getUser_id()).setValue(UUID.randomUUID().toString(), new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                if(databaseError == null) {
                                                    DatabaseReference messageRef = mReference.child("messages");
                                                    final DatabaseReference newMessageID = messageRef.push(); //this will be included into both the offered and the renter as well
                                                    newMessageID.setValue(currentUser.getUser_id());
                                                    Message newMessageInsert = new Message(currentUser.getUsername(), " I am interested in your " + mItem.title, "date here", true, currentUser.getUser_id());
                                                    //push message to the message table
                                                    newMessageID.push().setValue(newMessageInsert, new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                            if(databaseError == null) {
                                                                //set message to the current user
                                                                mReference.child("users").child(currentUser.getUser_id()).child("messages_this_user_can_see").push().setValue(newMessageID.getKey(), new DatabaseReference.CompletionListener() {
                                                                    @Override
                                                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                        if(databaseError == null) {
                                                                            //Set the message to the renter
                                                                            mReference.child("users").child(mItem.userUID).child("messages_this_user_can_see").push().setValue(newMessageID.getKey(), new DatabaseReference.CompletionListener() {
                                                                                @Override
                                                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                                    if(databaseError == null) {
                                                                                        int duration = Toast.LENGTH_SHORT;
                                                                                        Toast toast = Toast.makeText(mContext, "Offer sent", duration);
                                                                                        toast.show();
                                                                                    } else {

                                                                                    }
                                                                                }
                                                                            });
                                                                        } else {
                                                                            int duration = Toast.LENGTH_SHORT;
                                                                            Toast toast = Toast.makeText(mContext, "error at pushing new message to the currentUser", duration);
                                                                            toast.show();
                                                                        }
                                                                    }
                                                                });
                                                            } else {
                                                                int duration = Toast.LENGTH_SHORT;
                                                                Toast toast = Toast.makeText(mContext, "error at pushing new message", duration);
                                                                toast.show();
                                                            }
                                                        }
                                                    });
//                                                    CharSequence text = "Offer Sent!";
//                                                    int duration = Toast.LENGTH_SHORT;
//                                                    Toast toast = Toast.makeText(mContext, text, duration);
//                                                    toast.show();
                                                }
                                            }
                                        });
                                    }
                                });
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
