package to.rent.rentto.Listing;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import to.rent.rentto.Messages.PostInMessage;
import to.rent.rentto.Models.Item;
import to.rent.rentto.Models.Message;
import to.rent.rentto.Models.User;
import to.rent.rentto.Models.UserAccountSettings;
import to.rent.rentto.Profile.ProfilePreviewActivity;
import to.rent.rentto.R;
import to.rent.rentto.Utils.BottomNavigationViewHelper;
import to.rent.rentto.Utils.ShareMethods;

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
    private ImageView authorPic;
    private String authorPicURL;
    FloatingActionButton requestButton;
    private ShareMethods shareMethods;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing);
        shareMethods = new ShareMethods(ListingActivity.this);
        mContext = ListingActivity.this;
        requestButton = (FloatingActionButton) findViewById(R.id.requestButton);
        authorPic = (ImageView) findViewById(R.id.author_photo_iv);
        mAuth = FirebaseAuth.getInstance();
        Log.d(TAG, "onCreate: Started.");

        ITEM_ID = getIntent().getStringExtra("ITEM_ID");
        CITY = getIntent().getStringExtra("CITY");

        mReference = FirebaseDatabase.getInstance().getReference();
        try {
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
        } catch (Exception e) {
            Log.d(TAG, "Could not get user");
        }
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
    private String getCurrentLocation(){
        return CITY;
    }

    private void sendOffer(final String offerMessage) {
        Log.d(TAG, "here is the zip of the item " + mItem.zip);
        Log.d(TAG, "here is the item id " + ITEM_ID);
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "You have made offer to this item already, can't make offer again", Toast.LENGTH_SHORT).show();
            }
        });

        mReference.child("posts").child(getCurrentLocation()).child(ITEM_ID).child("user_offers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String value = ds.getValue(String.class);
                    if(value.equals(currentUser.getUser_id())){
                        Toast.makeText(mContext, "You have made offer to this item already, can't make offer again", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
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
                                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                    Date date = new Date();
                                    String strDate = dateFormat.format(date);
                                    Message newMessageInsert = new Message(currentUser.getUsername(), offerMessage, strDate, true, currentUser.getUser_id());
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
                                                                        mReference.child("posts").child(getCurrentLocation()).child(ITEM_ID).child("user_offers").push().setValue(currentUser.getUser_id(), new DatabaseReference.CompletionListener() {
                                                                            @Override
                                                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                                if(databaseError == null) {
                                                                                    mReference.child("posts").child(getCurrentLocation()).child(ITEM_ID).child("offer_messages").push().setValue(newMessageID.getKey(), new DatabaseReference.CompletionListener() {
                                                                                        @Override
                                                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                                            if(databaseError == null) {
                                                                                                PostInMessage post = new PostInMessage(mItem.imageURL, mItem.title, ITEM_ID, getCurrentLocation());
                                                                                                newMessageID.child("post").setValue(post, new DatabaseReference.CompletionListener() {
                                                                                                    @Override
                                                                                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                                                        if(databaseError == null){
                                                                                                            int duration = Toast.LENGTH_SHORT;
                                                                                                            Toast toast = Toast.makeText(mContext, "Offer sent", duration);
                                                                                                            toast.show();
                                                                                                            requestButton.setOnClickListener(new View.OnClickListener() {
                                                                                                                @Override
                                                                                                                public void onClick(View v) {
                                                                                                                    Toast.makeText(mContext, "You have made offer to this item already, can't make offer again", Toast.LENGTH_SHORT).show();
                                                                                                                }
                                                                                                            });
                                                                                                        } else {
                                                                                                            int duration = Toast.LENGTH_SHORT;
                                                                                                            Toast toast = Toast.makeText(mContext, "An error occurred when sending the offer.", duration);
                                                                                                            toast.show();
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                            }
                                                                                        }
                                                                                    });
                                                                                }
                                                                            }
                                                                        });
                                                                    } else {

                                                                    }
                                                                }
                                                            });
                                                        } else {
                                                            int duration = Toast.LENGTH_SHORT;
                                                            Toast toast = Toast.makeText(mContext, "Could not send your message", duration);
                                                            toast.show();
                                                        }
                                                    }
                                                });
                                            } else {
                                                int duration = Toast.LENGTH_SHORT;
                                                Toast toast = Toast.makeText(mContext, "Could not send your message", duration);
                                                toast.show();
                                            }
                                        }
                                    });
                                }
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

    private void grabTheItem(){
        Query query = mReference.child(mContext.getString(R.string.dbname_items)).child(CITY).child(ITEM_ID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, dataSnapshot.getValue()+"");
                mItem = dataSnapshot.getValue(Item.class);
                if(mItem == null) {
                    Log.d(TAG, "grabTheItem/onDataChange mItem is null");
                    Toast.makeText(mContext, "Cannot view this item. It may have been deleted", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                TextView item_name = findViewById(R.id.textView1);
                TextView description = findViewById(R.id.textView2);
                TextView condition = findViewById(R.id.textView3);
                TextView price = findViewById(R.id.textView5);
                ImageView post_image = findViewById(R.id.imageView);
                TextView soldInfo = findViewById(R.id.soldInfo);
                final RequestOptions requestOptions = new RequestOptions()
                        .placeholder(R.drawable.ic_launcher_background);

                try {
                    Glide.with(mContext)
                            .load(mItem.imageURL)
                            .apply(requestOptions)
                            .into(post_image);
                }catch (Exception e){
                    finish();
                    startActivity(getIntent());
                }

                item_name.setText(mItem.title);
                description.setText(mItem.description);
                price.setText(mItem.rate+"");
                condition.setText(mItem.condition);
                if(mItem.sold){
                    soldInfo.setText("RENTED");
                }
                Query query1 = mReference.child("user_account_settings").child(mItem.userUID);
                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserAccountSettings userAccountSettings = dataSnapshot.getValue(UserAccountSettings.class);
                        authorPicURL = userAccountSettings.getProfile_photo();
                        TextView userField = findViewById(R.id.textView4);
                        userField.setText(userAccountSettings.getDisplay_name());
                        if(authorPicURL != null && authorPicURL.length() > 1 && authorPic != null) {
                            Log.d(TAG, "authorPicURL is " + authorPicURL);
                            Glide.with(mContext)
                                    .load(authorPicURL)
                                    .into(authorPic);
                        } else {
                            Log.d(TAG, "using default profile pic");
                            authorPic.setImageResource(R.drawable.profile_default_pic);
                        }
                        authorPic.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d(TAG, "The author pic icon was clicked, finishing");
                                finish();
                                Intent intent1 = new Intent(mContext, ProfilePreviewActivity.class);
                                intent1.putExtra("authorUID", mItem.userUID);
                                intent1.putExtra("ACTIVITY_NUM", 0); // so it will highlight bottom nav as itemlisting
                                startActivity(intent1);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                Query query2 = mReference.child(mContext.getString(R.string.dbname_users)).child(mItem.userUID);
                query2.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if(mItem.sold){
                            requestButton.setVisibility(View.GONE);
                        }

                        mReference.child("posts").child(getCurrentLocation()).child(ITEM_ID).child("user_offers").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    String value = ds.getValue(String.class);
                                    if (value.equals(currentUser.getUser_id())) {
                                        requestButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Toast.makeText(mContext, "You have made offer to this item already, can't make offer again", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        return;
                                    }
                                }
                                setOfferButtonListener();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });





//                        mButton.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                if(mItem == null) {
//                                    Toast.makeText(mContext, "Cannot make an offer for this item. It may have been deleted", Toast.LENGTH_SHORT).show();
//                                    return;
//                                } else if(currentUser.getUser_id().equals(mItem.userUID)){
//                                    Toast.makeText(mContext, "Can't make offer to your own item", Toast.LENGTH_SHORT).show();
//                                    return;
//                                } else if(mItem.sold){
//                                    Toast.makeText(mContext, "Can't make offer to rented item", Toast.LENGTH_SHORT).show();
//                                    return;
//                                }
//                                mButton.setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        Toast.makeText(mContext, "You have made offer to this item already, can't make offer again", Toast.LENGTH_SHORT).show();
//                                    }
//                                }); // so they cannot spam click after first click
//                                Log.d(TAG, "here is the zip of the item " + mItem.zip);
//                                Log.d(TAG, "here is the item id " + ITEM_ID);
//
//                                mReference.child("posts").child(getCurrentLocation()).child(ITEM_ID).child("user_offers").addListenerForSingleValueEvent(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(DataSnapshot dataSnapshot) {
//                                        for(DataSnapshot ds : dataSnapshot.getChildren()){
//                                            String value = ds.getValue(String.class);
//                                            if(value.equals(currentUser.getUser_id())){
//                                                Toast.makeText(mContext, "You have made offer to this item already, can't make offer again", Toast.LENGTH_SHORT).show();
//                                                return;
//                                            }
//                                        }
//                                        DatabaseReference renterUIDRef = mReference.child("notificationMessages").child(mItem.userUID);
//                                        DatabaseReference pushedKey = renterUIDRef.push();
//
//                                        pushedKey.setValue(currentUser.getUsername() + " have made you an offer", new DatabaseReference.CompletionListener() {
//
//                                            @Override
//                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                                                mReference.child("notifications").child(mItem.userUID).child(currentUser.getUser_id()).setValue(UUID.randomUUID().toString(), new DatabaseReference.CompletionListener() {
//                                                    @Override
//                                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                                                        if(databaseError == null) {
//                                                            DatabaseReference messageRef = mReference.child("messages");
//                                                            final DatabaseReference newMessageID = messageRef.push(); //this will be included into both the offered and the renter as well
//                                                            newMessageID.setValue(currentUser.getUser_id());
//                                                            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//                                                            Date date = new Date();
//                                                            String strDate = dateFormat.format(date);
//                                                            Message newMessageInsert = new Message(currentUser.getUsername(), " I am interested in your " + mItem.title, strDate, true, currentUser.getUser_id());
//                                                            //push message to the message table
//                                                            newMessageID.push().setValue(newMessageInsert, new DatabaseReference.CompletionListener() {
//                                                                @Override
//                                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                                                                    if(databaseError == null) {
//                                                                        //set message to the current user
//                                                                        mReference.child("users").child(currentUser.getUser_id()).child("messages_this_user_can_see").push().setValue(newMessageID.getKey(), new DatabaseReference.CompletionListener() {
//                                                                            @Override
//                                                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                                                                                if(databaseError == null) {
//                                                                                    //Set the message to the renter
//                                                                                    mReference.child("users").child(mItem.userUID).child("messages_this_user_can_see").push().setValue(newMessageID.getKey(), new DatabaseReference.CompletionListener() {
//                                                                                        @Override
//                                                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                                                                                            if(databaseError == null) {
//                                                                                                mReference.child("posts").child(getCurrentLocation()).child(ITEM_ID).child("user_offers").push().setValue(currentUser.getUser_id(), new DatabaseReference.CompletionListener() {
//                                                                                                    @Override
//                                                                                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                                                                                                        if(databaseError == null) {
//                                                                                                            mReference.child("posts").child(getCurrentLocation()).child(ITEM_ID).child("offer_messages").push().setValue(newMessageID.getKey(), new DatabaseReference.CompletionListener() {
//                                                                                                                @Override
//                                                                                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                                                                                                                    if(databaseError == null) {
//                                                                                                                        PostInMessage post = new PostInMessage(mItem.imageURL, mItem.title, ITEM_ID, getCurrentLocation());
//                                                                                                                        newMessageID.child("post").setValue(post, new DatabaseReference.CompletionListener() {
//                                                                                                                            @Override
//                                                                                                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                                                                                                                                if(databaseError == null){
//                                                                                                                                    int duration = Toast.LENGTH_SHORT;
//                                                                                                                                    Toast toast = Toast.makeText(mContext, "Offer sent", duration);
//                                                                                                                                    toast.show();
//                                                                                                                                } else {
//                                                                                                                                    int duration = Toast.LENGTH_SHORT;
//                                                                                                                                    Toast toast = Toast.makeText(mContext, "An error occurred when sending the offer.", duration);
//                                                                                                                                    toast.show();
//                                                                                                                                }
//                                                                                                                            }
//                                                                                                                        });
//                                                                                                                    }
//                                                                                                                }
//                                                                                                            });
//                                                                                                        }
//                                                                                                    }
//                                                                                                });
//                                                                                            } else {
//
//                                                                                            }
//                                                                                        }
//                                                                                    });
//                                                                                } else {
//                                                                                    int duration = Toast.LENGTH_SHORT;
//                                                                                    Toast toast = Toast.makeText(mContext, "Could not send your message", duration);
//                                                                                    toast.show();
//                                                                                }
//                                                                            }
//                                                                        });
//                                                                    } else {
//                                                                        int duration = Toast.LENGTH_SHORT;
//                                                                        Toast toast = Toast.makeText(mContext, "Could not send your message", duration);
//                                                                        toast.show();
//                                                                    }
//                                                                }
//                                                            });
//                                                        }
//                                                    }
//                                                });
//                                            }
//                                        });
//                                    }
//
//                                    @Override
//                                    public void onCancelled(DatabaseError databaseError) {
//
//                                    }
//                                });
//
//
//                            }
//                        });
                    }
//
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            private void setOfferButtonListener() {
                // If they are not prohibited from sending an offer, show dialog with default message
                // They can send pretyped message, or type their own
                // They are prohibited from sending an offer if:
                //                      If Item is null
                //                      if it is there own item
                //                      If it is already rented
                //                      If they already made an offer
                requestButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mItem == null) {
                            Toast.makeText(mContext, "Cannot make an offer for this item. It may have been deleted", Toast.LENGTH_SHORT).show();
                            return;
                        } else if(currentUser.getUser_id().equals(mItem.userUID)){
                            Toast.makeText(mContext, "Can't make offer to your own item", Toast.LENGTH_SHORT).show();
                            return;
                        } else if(mItem.sold){
                            Toast.makeText(mContext, "Can't make offer to rented item", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                        alertDialog.setTitle("Send an Offer");
                        alertDialog.setMessage("Type in your message");
                        final EditText input = new EditText(mContext);
                        input.setText("I am interested in your " + mItem.title);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT);
                        input.setLayoutParams(lp);
                        alertDialog.setView(input);
                        alertDialog.setIcon(R.drawable.mail);
                        alertDialog.setPositiveButton("Send",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        sendOffer(input.getText().toString());
                                    }
                                });

                        alertDialog.setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                        alertDialog.show();
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