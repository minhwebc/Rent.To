package to.rent.rentto.Messages;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import to.rent.rentto.R;
import to.rent.rentto.Utils.BottomNavigationViewHelper;

public class NotificationActivity extends AppCompatActivity {
    private static final String TAG = "NotificationActivity";
    private Context mContext;
    private ListView messagesListView;
    private ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        mContext = NotificationActivity.this;
        Log.d(TAG, "onCreate: Started.");

        //Sets up list view
        messagesListView = (ListView) findViewById(R.id.msgview);
        // Dummy array data
        String[] dummyData = {"Remember", "To", "Delete", "DummyData", "Array", "In", "NotificationActivity.java",
                "a", "b", "c", "d", "e", "f","g","h","i","j","k","l","m","n","o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y","z"};
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, dummyData);
        messagesListView.setAdapter(arrayAdapter);

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
    }
}
