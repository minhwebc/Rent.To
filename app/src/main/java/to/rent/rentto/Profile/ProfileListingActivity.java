package to.rent.rentto.Profile;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
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
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import to.rent.rentto.Models.Item;
import to.rent.rentto.Models.Message;
import to.rent.rentto.Models.RemindMessageItem;
import to.rent.rentto.Models.User;
import to.rent.rentto.R;
import to.rent.rentto.Utils.BottomNavigationViewHelper;

/**
 * Created by Sora on 2/14/2018.
 */

public class ProfileListingActivity extends AppCompatActivity implements RatingDialogListener{
    private static final String TAG = "ProfileListingActivity";
    private static final int ACTIVITY_NUM = 3;
    private Context mContext;
    private String ITEM_ID;
    private String CITY;
    private boolean RENTED;
    private Item mItem;
    private DatabaseReference mReference;
    private FirebaseAuth mAuth;
    private User currentUser;
    private Button markAsRentedButton;
    private Button markasDeletedButton;
    final ArrayList<String> usersIDArray = new ArrayList<>();
    final ArrayList<String> usersNameArray = new ArrayList<>();
    private String offerUserID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_listing);
        mContext = ProfileListingActivity.this;
        mAuth = FirebaseAuth.getInstance();
        Log.d(TAG, "onCreate: Started.");
        ITEM_ID = getIntent().getStringExtra("ITEM_ID");
        CITY = getIntent().getStringExtra("CITY");
        RENTED = getIntent().getBooleanExtra("RENTED", false);
        mReference = FirebaseDatabase.getInstance().getReference();
        getRentedToList();
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

    private void getRentedToList() {
        final String location = CITY;
        final String itemID = ITEM_ID;
        usersIDArray.add("BLANK");
        usersNameArray.add("Someone outside of Rent To");
        Log.d(TAG, "Whether the item has been rented: " + RENTED);
        // Populates the usersIDArray and usersNameArray, to know who the user may mark the item as rented to
        mReference.child("posts").child(location).child(itemID).child("user_offers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String userID = ds.getValue(String.class);
                    usersIDArray.add(userID);
                    mReference.child("users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                if(ds.getKey().equals("username")) {
                                    usersNameArray.add(ds.getValue(String.class));
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                Log.d(TAG, "the useridarray is " + usersIDArray.toString());
                Log.d(TAG, "The usersnamearray is " + usersNameArray.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
                showOptions(usersNameArray, usersIDArray);


            }
        });
        markasDeletedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Mark as deleted button clicked");
                Log.d("ViewHolder", "delete item here");
                Log.d("ViewHolder", "delete item");
                Log.d("ViewHolder", CITY);
                Log.d("ViewHolder", ITEM_ID);
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(mContext, android.R.style.Theme_Material_Dialog);
                } else {
                    builder = new AlertDialog.Builder(mContext);
                }
                builder.setTitle("Delete post")
                        .setMessage("Are you sure you want to delete this entry?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mReference.child("posts").child(CITY).child(ITEM_ID).child("offer_messages").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                                            Log.d(TAG, ds.getKey());
                                            mReference.child("messages").child(ds.getValue(String.class)).setValue(null, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                    Toast.makeText(mContext, "Message deleted", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                        mReference.child("posts").child(CITY).child(ITEM_ID).setValue(null, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                mReference.child("user_items").child(mAuth.getCurrentUser().getUid()).child(ITEM_ID).setValue(null, new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                        Toast.makeText(mContext, "Item deleted", Toast.LENGTH_SHORT).show();
                                                        Log.d(TAG, "Finishing activity, item deleted");
                                                        finish();
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
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
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


    @Override
    public void onPositiveButtonClicked(int i, final String s) {
        final double newRating = i;
        Log.d(TAG, "This is the user that is going to get rated :" + offerUserID);
        Log.d(TAG, s);
        final String remindMessage = s;
        mReference.child("users").child(offerUserID).addListenerForSingleValueEvent(new ValueEventListener() {
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
                mReference.child("users").child(offerUserID).child("rating").setValue(newAverageRating, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError == null) {
                            mReference.child("users").child(offerUserID).child("totalRating").setValue(finalTotalRating, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if(databaseError == null) {
                                        String id = UUID.randomUUID().toString();
                                        mReference.child("ratingNotifications").child(mAuth.getCurrentUser().getUid()).child(offerUserID).child(ITEM_ID).setValue(id, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                if(databaseError == null) {
                                                    mReference.child("posts").child(CITY).child(ITEM_ID).child("sold").setValue(true, new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                            if (databaseError == null){
                                                                mReference.child(getString(R.string.dbname_user_items)).child(mAuth.getCurrentUser().getUid()).child(ITEM_ID).child("sold").setValue(true, new DatabaseReference.CompletionListener() {
                                                                    @Override
                                                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                        if (databaseError == null){
                                                                            final String[] uniqueID = {mAuth.getCurrentUser().getUid() + offerUserID + ITEM_ID};
                                                                            mReference.child("users").child(mAuth.getCurrentUser().getUid()).child("rating_session").child(uniqueID[0]).setValue(true, new DatabaseReference.CompletionListener() {
                                                                                @Override
                                                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                                    if(databaseError == null) {
                                                                                        uniqueID[0] = offerUserID+mAuth.getCurrentUser().getUid()+ITEM_ID;
                                                                                        mReference.child("users").child(offerUserID).child("rating_session").child(uniqueID[0]).setValue(false, new DatabaseReference.CompletionListener() {
                                                                                            @Override
                                                                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                                                if(databaseError == null){
                                                                                                    final DatabaseReference newRemindMessage = mReference.child("remind_messages").push();
                                                                                                    final String newRemindMessageKey = newRemindMessage.getKey();
                                                                                                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                                                                                    Date date = new Date();
                                                                                                    String strDate = dateFormat.format(date);
                                                                                                    Message message = new Message("RentTo", remindMessage, strDate, true, "authorID");
                                                                                                    newRemindMessage.push().setValue(message, new DatabaseReference.CompletionListener() {
                                                                                                        @Override
                                                                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                                                            RemindMessageItem remindMessageItem = new RemindMessageItem(CITY, ITEM_ID, mAuth.getCurrentUser().getUid() ,offerUserID, s);
                                                                                                            newRemindMessage.child("item").setValue(remindMessageItem, new DatabaseReference.CompletionListener() {
                                                                                                                @Override
                                                                                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                                                                    mReference.child("users").child(offerUserID).child("return_remind_message_user_can_see").push().setValue(newRemindMessageKey, new DatabaseReference.CompletionListener() {
                                                                                                                        @Override
                                                                                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                                                                            mReference.child("users").child(mAuth.getCurrentUser().getUid()).child("rented_out_remind_message_user_can_see").push().setValue(newRemindMessageKey, new DatabaseReference.CompletionListener() {
                                                                                                                                @Override
                                                                                                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                                                                                    if(databaseError == null) {
                                                                                                                                        Toast.makeText(mContext, "Rating submitted", Toast.LENGTH_SHORT).show();
                                                                                                                                        Log.d(TAG, "Finishing activity, rating submitted");
                                                                                                                                        finish();
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
    private void showOptions(final ArrayList<String> usersNameArray, final ArrayList<String> usersIDArray) {
        if(!RENTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle("Who did you rent this to?");
            //String usersIDStringArray[] = usersIDArray.toArray(new String[usersIDArray.size()]);
            String usersNameStringArray[] = usersNameArray.toArray(new String[usersNameArray.size()]);
            builder.setItems(usersNameStringArray, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String userStringSold = usersIDArray.get(which);
                    if(userStringSold.equals("BLANK")){
                        mReference.child("posts").child(CITY).child(ITEM_ID).child("sold").setValue(true, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                mReference.child("user_items").child(mAuth.getCurrentUser().getUid()).child(ITEM_ID).child("sold").setValue(true, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        Toast.makeText(mContext, "Item has been marked rented", Toast.LENGTH_SHORT).show();
                                        Log.d(TAG, "Finishing activity, marked as rented");
                                        finish();
                                    }
                                });
                            }
                        });
                    } else {
                        mReference.child("users").child(userStringSold).child("users_to_be_rated").push().setValue(mAuth.getCurrentUser().getUid(), new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
//                                            ((ProfileActivity) mContext).setUserID(userStringSold);
                                    offerUserID = userStringSold;
//                                            ((ProfileActivity) mContext).setItem(ITEM_ID, CITY, "");
                                    showDialog();
                                }
                            }
                        });
                    }
                }
            });
            builder.show();
        }else{
            Toast.makeText(mContext, "Item has already been marked rented", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Finishing activity, already rented");
            finish();
        }
    }

    private void showDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNeutralButtonText("Later")
                .setNoteDescriptions(Arrays.asList("Very Bad", "Not good", "Quite ok", "Very Good", "Excellent !!!"))
                .setDefaultRating(2)
                .setTitle("How would you rate this person")
                .setDescription("Please select some stars and give a reminder below")
                .setStarColor(R.color.colorAccent)
                .setNoteDescriptionTextColor(R.color.colorPrimary)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Please write something you want to be reminded here about the item. We will send you both a message to remind")
                .setHintTextColor(R.color.black_11)
                .setCommentTextColor(R.color.black)
                .setCommentBackgroundColor(R.color.white)
//                .create((ProfileActivity) mContext)
                .create(ProfileListingActivity.this)
                .show();
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onNeutralButtonClicked() {

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