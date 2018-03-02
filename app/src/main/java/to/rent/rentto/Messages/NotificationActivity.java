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
import android.widget.ArrayAdapter;
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

import to.rent.rentto.Models.User;
import to.rent.rentto.R;
import to.rent.rentto.Utils.BottomNavigationViewHelper;

public class NotificationActivity extends AppCompatActivity {
    private static final int ACTIVITY_NUM = 3; // the fourth case in bottomnav (0 index)
    private static final String TAG = "NotificationActivity";
    private Context mContext;
    private ListView messagesListView;
    private ArrayAdapter arrayAdapter;
    private DatabaseReference mReference;
    private FirebaseAuth mAuth;

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

        //Sets up list view
        messagesListView = (ListView) findViewById(R.id.msgview);
        // Dummy array data
        final ArrayList<String> data = new ArrayList<>();
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, data);
        messagesListView.setAdapter(arrayAdapter);

        Query query = mReference.child("notifications").child(mAuth.getCurrentUser().getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                data.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String userKey = ds.getKey();
                    Log.d(TAG, "user key is " + userKey);

                    mReference.child("users").child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dsSnap) {
                            Log.d(TAG, (String) dsSnap.getKey());
                            Log.d(TAG, dsSnap.toString());
                            User user = dsSnap.getValue(User.class);
                            data.add(user.getUsername() + " made you an offer");
                            Log.d(TAG, "size of data " + data.size());
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
                String selectedText = messagesListView.getItemAtPosition(position).toString();
                Toast.makeText(mContext, "You clicked on this message: " + selectedText, Toast.LENGTH_SHORT).show();
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
