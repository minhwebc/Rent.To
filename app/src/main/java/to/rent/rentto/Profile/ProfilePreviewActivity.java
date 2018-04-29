package to.rent.rentto.Profile;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.UUID;

import to.rent.rentto.R;
import to.rent.rentto.Utils.BottomNavigationViewHelper;

public class ProfilePreviewActivity extends AppCompatActivity implements RatingDialogListener {
    private static final String TAG = "ProfilePreviewActivity";
    private Context mContext = ProfilePreviewActivity.this;
    private int ACTIVITY_NUM;
    private String offerUserID;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String location;
    private String itemID;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate: starting");

        offerUserID = getIntent().getStringExtra("authorUID");
        ACTIVITY_NUM = getIntent().getIntExtra("ACTIVITY_NUM", 0);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        init();
    }

    private void init(){
        Log.d(TAG, "init: inflating " + getString(R.string.profile_fragment));

        ProfilePreviewFragment fragment = new ProfilePreviewFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("ACTIVITY_NUM", ACTIVITY_NUM);
        bundle.putString("authorUID", offerUserID);
        fragment.setArguments(bundle);
        FragmentTransaction transaction = ProfilePreviewActivity.this.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
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

    public void setUserID(String userID){
        offerUserID = userID;
    }

    public void setItem(String itemID, String location) {
        this.itemID = itemID;
        this.location = location;
    }

    @Override
    public void onPositiveButtonClicked(int i, String s) {
        final double newRating = i;
        Log.d(TAG, "This is the user that is going to get rated :" + offerUserID);
        myRef.child("users").child(offerUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Double ratingNumber = 0.0;
                int totalRating = 0;
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    if(ds.getKey().equals("rating")) {
                        ratingNumber = ds.getValue(Double.class);
                    }
                    if(ds.getKey().equals("totalRating")) {
                        totalRating = ds.getValue(Integer.class);
                    }
                }
                //calculation
                Double latestRating = ratingNumber + newRating;
                totalRating = totalRating + 1;
                Double newAverageRating = latestRating / totalRating;
                newAverageRating = Math.round(newAverageRating * 100.0) / 100.0;
                final int finalTotalRating = totalRating;
                myRef.child("users").child(offerUserID).child("rating").setValue(newAverageRating, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError == null) {
                            myRef.child("users").child(offerUserID).child("totalRating").setValue(finalTotalRating, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if(databaseError == null) {
                                        String id = UUID.randomUUID().toString();
                                        myRef.child("ratingNotifications").child(mAuth.getCurrentUser().getUid()).child(offerUserID).child(itemID).setValue(id, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                if(databaseError == null) {
                                                    myRef.child("posts").child(location).child(itemID).child("sold").setValue(true, new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                            if (databaseError == null){
                                                                myRef.child(getString(R.string.dbname_user_items)).child(mAuth.getCurrentUser().getUid()).child(itemID).child("sold").setValue(true, new DatabaseReference.CompletionListener() {
                                                                    @Override
                                                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                        if (databaseError == null){
                                                                            final String[] uniqueID = {mAuth.getCurrentUser().getUid() + offerUserID + itemID};
                                                                            myRef.child("users").child(mAuth.getCurrentUser().getUid()).child("rating_session").child(uniqueID[0]).setValue(true, new DatabaseReference.CompletionListener() {
                                                                                @Override
                                                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                                    if(databaseError == null) {
                                                                                        uniqueID[0] = offerUserID+mAuth.getCurrentUser().getUid()+itemID;
                                                                                        myRef.child("users").child(offerUserID).child("rating_session").child(uniqueID[0]).setValue(false, new DatabaseReference.CompletionListener() {
                                                                                            @Override
                                                                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                                                if(databaseError == null){
                                                                                                    Toast.makeText(mContext, "Rating submitted", Toast.LENGTH_SHORT).show();
                                                                                                }
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onNeutralButtonClicked() {

    }
}
