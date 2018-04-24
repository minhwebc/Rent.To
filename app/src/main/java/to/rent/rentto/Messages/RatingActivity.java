package to.rent.rentto.Messages;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.Arrays;
import java.util.UUID;

import to.rent.rentto.Home.HomeActivity;
import to.rent.rentto.R;

public class RatingActivity extends AppCompatActivity implements RatingDialogListener {

    private static final String TAG = "RatingActivity";
    private String offerUserID;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        Intent intent = getIntent();
        offerUserID = intent.getStringExtra("userid_to_be_rated");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        showDialog();

    }

    private void showDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNeutralButtonText("Later")
                .setNoteDescriptions(Arrays.asList("Very Bad", "Not good", "Quite ok", "Very Good", "Excellent !!!"))
                .setDefaultRating(2)
                .setTitle("How would you rate this person")
                .setDescription("Please select some stars and give your feedback")
                .setStarColor(R.color.colorAccent)
                .setNoteDescriptionTextColor(R.color.colorPrimary)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Please write your comment here ...")
                .setHintTextColor(R.color.black_11)
                .setCommentTextColor(R.color.black)
                .setCommentBackgroundColor(R.color.white)
                .create(RatingActivity.this)
                .show();
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
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getKey().equals("rating")) {
                        ratingNumber = ds.getValue(Double.class);
                    }
                    if (ds.getKey().equals("totalRating")) {
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
                        if (databaseError == null) {
                            myRef.child("users").child(offerUserID).child("totalRating").setValue(finalTotalRating, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        myRef.child("ratingNotifications").child(mAuth.getCurrentUser().getUid()).child(offerUserID).setValue(UUID.randomUUID().toString(), new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                if (databaseError == null) {
                                                    Toast.makeText(RatingActivity.this, "Rating submitted",
                                                            Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(RatingActivity.this, HomeActivity.class);
                                                    startActivity(intent);
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
