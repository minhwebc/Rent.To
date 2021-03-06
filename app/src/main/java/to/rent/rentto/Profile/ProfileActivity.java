package to.rent.rentto.Profile;

import android.app.ProgressDialog;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import to.rent.rentto.Models.Message;
import to.rent.rentto.Models.RemindMessageItem;
import to.rent.rentto.R;
import to.rent.rentto.Utils.BottomNavigationViewHelper;

public class ProfileActivity extends AppCompatActivity implements RatingDialogListener {
    private static final String TAG = "ProfileActivity";
    private Context mContext = ProfileActivity.this;
    private static final int ACTIVITY_NUM = 3;
    private String offerUserID;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private String location;
    private String itemID;
    private String message;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate: starting");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        init();
    }

    private void init(){
        Log.d(TAG, "init: inflating " + getString(R.string.profile_fragment));

        ProfileFragment fragment = new ProfileFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
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

    public void setItem(String itemID, String location, String message) {
        this.itemID = itemID;
        this.location = location;
        this.message = message;
    }

    @Override
    public void onPositiveButtonClicked(int i, final String s) {
        final double newRating = i;
        Log.d(TAG, "This is the user that is going to get rated :" + offerUserID);
        Log.d(TAG, s);
        final String remindMessage = s;
        final ProgressDialog pd = new ProgressDialog(mContext);
        pd.setMessage("Loading...");
        pd.show();
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
                                                                                                    final DatabaseReference newRemindMessage = myRef.child("remind_messages").push();
                                                                                                    final String newRemindMessageKey = newRemindMessage.getKey();
                                                                                                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                                                                                    Date date = new Date();
                                                                                                    String strDate = dateFormat.format(date);
                                                                                                    Message message = new Message("RentTo", remindMessage, strDate, true, "authorID");
                                                                                                    newRemindMessage.push().setValue(message, new DatabaseReference.CompletionListener() {
                                                                                                        @Override
                                                                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                                                            RemindMessageItem remindMessageItem = new RemindMessageItem(location, itemID, mAuth.getCurrentUser().getUid() ,offerUserID, s);
                                                                                                            newRemindMessage.child("item").setValue(remindMessageItem, new DatabaseReference.CompletionListener() {
                                                                                                                @Override
                                                                                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                                                                    myRef.child("users").child(offerUserID).child("return_remind_message_user_can_see").push().setValue(newRemindMessageKey, new DatabaseReference.CompletionListener() {
                                                                                                                        @Override
                                                                                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                                                                            myRef.child("users").child(mAuth.getCurrentUser().getUid()).child("rented_out_remind_message_user_can_see").push().setValue(newRemindMessageKey, new DatabaseReference.CompletionListener() {
                                                                                                                                @Override
                                                                                                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                                                                                    if(databaseError == null) {
                                                                                                                                        pd.dismiss();
                                                                                                                                        Toast.makeText(mContext, "Rating submitted", Toast.LENGTH_SHORT).show();
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            });
                                                                                                                        }
                                                                                                                    });
                                                                                                                }
                                                                                                            });
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
        init();
    }

    @Override
    public void onNeutralButtonClicked() {

    }
}
