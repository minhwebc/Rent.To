package to.rent.rentto.Messages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

import to.rent.rentto.Models.Message;
import to.rent.rentto.R;
import to.rent.rentto.Utils.BottomNavigationViewHelper;

public class NotificationActivity extends AppCompatActivity {
    private static final int ACTIVITY_NUM = 4; // the fourth case in bottomnav (0 index)
    private static final String TAG = "NotificationActivity";
    private Context mContext;
    private ListView messagesListView;
    private MessagePreviewAdapter arrayAdapter;
    private DatabaseReference mReference;
    private FirebaseAuth mAuth;
    private ArrayList<String> messageIDList;
    private SwipeRefreshLayout swipeLayout;
    private final ArrayList<PostInMessage> data = new ArrayList<>();;


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Message: " + intent.getExtras().getString("message"));
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver),
                new IntentFilter("MyData")
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        mReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mContext = NotificationActivity.this;
        Log.d(TAG, "onCreate: Started.");
        messageIDList = new ArrayList<>();
        //Sets up list view
        messagesListView = (ListView) findViewById(R.id.msgview);
        // Dummy array data
        arrayAdapter = new MessagePreviewAdapter(this, data);
        messagesListView.setAdapter(arrayAdapter);

        //Sets on click listener for listview
        messagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "You clicked on item at position" + position);
                //String selectedText = messageIDList.get(position);
                String selectedText = messageIDList.get(messageIDList.size() - position - 1);
//                Toast.makeText(mContext, "You clicked on this message: " + selectedText, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(NotificationActivity.this, ChatActivity.class);
                intent.putExtra("MessageChannelID", selectedText);
                startActivity(intent);
            }
        });
        loadMessages();
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        messageIDList.clear();
                        data.clear();
                        loadMessages();
                        swipeLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });

        // If list view is not at top, do not refresh on swipe down
        messagesListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    swipeLayout.setEnabled(true);
                } else { swipeLayout.setEnabled(false); }
            }
        });

        // Set up bottom nav bar
        setupBottomNavigationView();
    }

    private void loadMessages(){
        Query query = mReference.child("users").child(mAuth.getCurrentUser().getUid()).child("messages_this_user_can_see");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                messageIDList.clear();
                data.clear();
                for(final DataSnapshot ds : dataSnapshot.getChildren()) {
                    String userKey = ds.getKey();
                    Log.d(TAG, "user key is " + userKey);

                    final String messageId = (String) ds.getValue();
                    final PostInMessage[] message = new PostInMessage[1];
                    final Message[] lastMessageContent = {new Message()};
                    mReference.child("messages").child(messageId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot1) {
                            Log.d(TAG, "this is the message id " + messageId);
                            if(dataSnapshot1.exists()) {
                                final boolean[] postDeleted = {false};
                                boolean validPost = false;
                                for (DataSnapshot ds1 : dataSnapshot1.getChildren()) {
                                    Log.d(TAG, "this is the within post " + ds1.getKey());
                                    if (ds1.getKey().equals("post")) {
                                        message[0] = ds1.getValue(PostInMessage.class);
                                        Log.d(TAG, "message id " + messageId);
                                        validPost = true;


                                    } else {
                                        lastMessageContent[0] = ds1.getValue(Message.class);
                                    }
                                }
                                if(validPost == false) { //the message does not contain a "post" field
                                    return;
                                }
                                if(messageId.equals("welcomeMessage")) {
                                    messageIDList.add(messageId);
                                    try {
                                        message[0].author = lastMessageContent[0].author;
                                        message[0].message = lastMessageContent[0].text;
                                        data.add(message[0]);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    arrayAdapter.notifyDataSetChanged();
                                } else {
                                    mReference.child("posts").child(message[0].zipcode).child(message[0].postID).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (!dataSnapshot.exists()) {
                                                Log.d(TAG, "post deleted");

                                                postDeleted[0] = true;
                                            } else {
                                                Log.d(TAG, "post not deleted");

                                                postDeleted[0] = false;
                                                messageIDList.add(messageId);
                                                try {
                                                    message[0].author = lastMessageContent[0].author;
                                                    message[0].message = lastMessageContent[0].text;
                                                    data.add(message[0]);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                arrayAdapter.notifyDataSetChanged();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            } else {
                                mReference.child("users").child(mAuth.getCurrentUser().getUid()).child("messages_this_user_can_see").child(ds.getKey()).setValue(null, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        return;
                                    }
                                });
                            }
                            arrayAdapter.notifyDataSetChanged();
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

    /**
     * Sets up bottom navigation bar
     */
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