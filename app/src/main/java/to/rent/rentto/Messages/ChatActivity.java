package to.rent.rentto.Messages;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import to.rent.rentto.Models.Message;
import to.rent.rentto.Models.User;
import to.rent.rentto.R;

public class ChatActivity extends AppCompatActivity {
    private static String TAG = "ChatActivity";
    private EditText editText;
    private ImageButton sendMessageButton;
    private Context mContext;
    private DatabaseReference mReference;
    private FirebaseAuth mAuth;
    private MessageAdapter messageAdapter;
    private ListView messagesListView;
    private User currentUser;
    private String messageID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mContext = ChatActivity.this;

        getCurrentUserInfo();
        messageAdapter = new MessageAdapter(mContext);

        editText = (EditText) findViewById(R.id.editText);
        sendMessageButton = (ImageButton) findViewById(R.id.SendMessageButton);
        mContext = ChatActivity.this;
        messagesListView = (ListView) findViewById(R.id.messages_view);
        messagesListView.setAdapter(messageAdapter);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "Click on send message button" , Toast.LENGTH_SHORT).show();
                sendMessage();
            }
        });

        //set up the channel info
        messageID = getIntent().getStringExtra("MessageChannelID");
    }

    private void getCurrentUserInfo(){
        Query query1 = mReference.child("users").child(mAuth.getCurrentUser().getUid());
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                Log.d(TAG, currentUser.getUsername());
                Query query = mReference.child("messages").child(messageID);
                query.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        messageAdapter.messages.clear();
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            Message message = ds.getValue(Message.class);
                            Log.d(TAG, message.getText());
                            if(message.getAuthorID().equals(currentUser.getUser_id())) {
                                message.belongsToCurrentUser = true;
                            } else {
                                message.belongsToCurrentUser = false;
                            }
                            messageAdapter.add(message);
                        }
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

    public void sendMessage() {
        String message = editText.getText().toString();
        if (message.length() > 0) {
            Log.d(TAG, "message to be sent: " + message);
            editText.getText().clear();
        }
        String time = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            time = ZonedDateTime.now().toString();
        }
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss yyyy/MM/dd");
        Date currentTime = Calendar.getInstance().getTime();
        Message newMessageInsert = new Message(currentUser.getUsername(), message, dateFormat.format(currentTime), true, currentUser.getUser_id());
        DatabaseReference messageRef = mReference.child("messages").child(messageID);
        messageRef.push().setValue(newMessageInsert, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError != null) {
                    Log.d(TAG, "There is an error in pushing the message");
                } else {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                    messagesListView.post(new Runnable() {
                        @Override
                        public void run() {
                            // Select the last row so it will scroll into view...
                            messagesListView.setSelection(messageAdapter.getCount() - 1);
                        }
                    });
                }
            }
        });

    }

    private String getRandomColor() {
        Random r = new Random();
        StringBuffer sb = new StringBuffer("#");
        while(sb.length() < 7){
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 7);
    }

}
