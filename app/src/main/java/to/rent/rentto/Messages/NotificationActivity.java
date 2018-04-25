package to.rent.rentto.Messages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

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
        final ArrayList<PostInMessage> data = new ArrayList<>();
        arrayAdapter = new MessagePreviewAdapter(this, data);
        messagesListView.setAdapter(arrayAdapter);
        Query query = mReference.child("users").child(mAuth.getCurrentUser().getUid()).child("messages_this_user_can_see");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                data.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String userKey = ds.getKey();
                    Log.d(TAG, "user key is " + userKey);
                    final String messageId = (String) ds.getValue();
                    final PostInMessage[] message = new PostInMessage[1];
                    final Message[] lastMessageContent = {new Message()};
                    mReference.child("messages").child(messageId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot1) {
                            Log.d(TAG, "here is the message id " + messageId);
                            for(DataSnapshot ds1 : dataSnapshot1.getChildren()) {
                                Log.d(TAG, "here is the message key " + ds1.getKey());
                                if(ds1.getKey().equals("post")){
                                    Log.d(TAG, "got into post " + messageId);
                                    message[0] = ds1.getValue(PostInMessage.class);
                                }else{
                                    lastMessageContent[0] = ds1.getValue(Message.class);
                                }
                            }
                            messageIDList.add(messageId);
                            message[0].author = lastMessageContent[0].author;
                            message[0].message = lastMessageContent[0].text;
                            data.add(message[0]);
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
        //Sets on click listener for listview
        messagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedText = messageIDList.get(position);
                Toast.makeText(mContext, "You clicked on this message: " + selectedText, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(NotificationActivity.this, ChatActivity.class);
                intent.putExtra("MessageChannelID", selectedText);
                startActivity(intent);
            }
        });
        // Set up bottom nav bar
        setupBottomNavigationView();
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