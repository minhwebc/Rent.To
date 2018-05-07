package to.rent.rentto.Remind;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

import to.rent.rentto.Models.Item;
import to.rent.rentto.Models.RemindMessageItem;
import to.rent.rentto.Models.User;
import to.rent.rentto.R;
import to.rent.rentto.Utils.BottomNavigationViewHelper;

public class RemindActivity extends AppCompatActivity {

    private static final String TAG = "RemindActivity";
    private Context mContext = RemindActivity.this;
    private static final int ACTIVITY_NUM = 1;
    private DatabaseReference mReference;
    private FirebaseAuth mAuth;
    private ListView remindReturnList;
    private ListView remindTakeBackList;
    private ArrayList<RemindMessageItem> remindReturnListArray = new ArrayList<>();
    private ArrayList<RemindMessageItem> remindTakeListArray = new ArrayList<>();
    private ArrayList<String> messageR = new ArrayList<>();
    private ArrayList<String> messageT = new ArrayList<>();
    private RemindViewAdapter adapterR;
    private RemindViewAdapter adapterT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remind);
        mAuth = FirebaseAuth.getInstance();
        mReference = FirebaseDatabase.getInstance().getReference();
        remindReturnList = findViewById(R.id.remindReturnList);
        remindTakeBackList = findViewById(R.id.remindTakeBList);
        adapterR = new RemindViewAdapter(this, remindReturnListArray, messageR);
        adapterT = new RemindViewAdapter(this, remindTakeListArray, messageT);
        remindReturnList.setAdapter(adapterR);
        remindTakeBackList.setAdapter(adapterT);
        setupBottomNavigationView();
        updateMessages();
    }

    private void updateMessages(){
        mReference.child("users").child(mAuth.getCurrentUser().getUid()).child("return_remind_message_user_can_see").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    messageR.add(ds.getValue(String.class));
                    mReference.child("remind_messages").child(ds.getValue(String.class)).child("item").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot1) {
                            final RemindMessageItem item = dataSnapshot1.getValue(RemindMessageItem.class);
                            if(item == null) {
                                return;
                            }
                            final User[] borrower = new User[1];
                            final User[] lender = new User[1];
                            final Item[] mItem =  new Item[1];
                            mReference.child("users").child(item.borrower).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    borrower[0] = dataSnapshot.getValue(User.class);
                                    mReference.child("users").child(item.lender).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            lender[0] = dataSnapshot.getValue(User.class);
                                            mReference.child("posts").child(item.zip).child(item.itemID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    mItem[0] = dataSnapshot.getValue(Item.class);
                                                    RemindMessageItem newRemindItem = new RemindMessageItem(mItem[0].title,lender[0].getUsername() ,borrower[0].getUsername(), mItem[0].imageURL, item.reminder, "");
                                                    Log.d(TAG, "This is the borrower name: " + newRemindItem.borrower);
                                                    Log.d(TAG, "This is the borrower name: " + newRemindItem.lender);
                                                    remindReturnListArray.add(newRemindItem);
                                                    adapterR.notifyDataSetChanged();
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

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mReference.child("users").child(mAuth.getCurrentUser().getUid()).child("rented_out_remind_message_user_can_see").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    messageT.add(ds.getValue(String.class));
                    mReference.child("remind_messages").child(ds.getValue(String.class)).child("item").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot1) {
                            final RemindMessageItem item = dataSnapshot1.getValue(RemindMessageItem.class);
                            Log.d(TAG, "The item is " + item.itemID);
                            Log.d(TAG, "The item zip is " + item.zip);
                            Log.d(TAG, "The item lender is " + item.lender);
                            final User[] borrower = new User[1];
                            final User[] lender = new User[1];
                            final Item[] mItem =  new Item[1];
                            try {
                                mReference.child("users").child(item.borrower).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        borrower[0] = dataSnapshot.getValue(User.class);
                                        mReference.child("users").child(item.lender).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                lender[0] = dataSnapshot.getValue(User.class);
                                                mReference.child("posts").child(item.zip).child(item.itemID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        mItem[0] = dataSnapshot.getValue(Item.class);
                                                        RemindMessageItem newRemindItem = new RemindMessageItem(mItem[0].title, lender[0].getUsername(), borrower[0].getUsername(), mItem[0].imageURL, item.reminder, "");
                                                        Log.d(TAG, newRemindItem.borrower);
                                                        Log.d(TAG, newRemindItem.lender);
                                                        remindTakeListArray.add(newRemindItem);
                                                        Log.d(TAG, "notified");
                                                        adapterT.notifyDataSetChanged();
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

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

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

    private User getUserInfo(String userID){
        final User[] returnUser = {null};
        mReference.child("users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                returnUser[0] = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return returnUser[0];
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
