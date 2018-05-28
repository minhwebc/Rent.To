package to.rent.rentto.Messages;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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

import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;
import to.rent.rentto.Listing.ListingActivity;
import to.rent.rentto.Models.Message;
import to.rent.rentto.Models.MessagePost;
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
    private RecyclerView messagesListView;
    private User currentUser;
    private User myUser;
    private String messageID;
    private String messageUID;
    private TextView textView;
    private String ToolBarText;
    private String previewImageURL;
    private String previewPostUID;
    private String previewPostZipcode;
    private ImageView previewPostImageView;
    public ChatActivity() {

    }

    private ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "child listener " + dataSnapshot.getKey());

            if(!dataSnapshot.getKey().equals("post")) {
                Message message = dataSnapshot.getValue(Message.class);
                if (message.getAuthorID().equals(myUser.getUser_id())) {
                    message.belongsToCurrentUser = true;
                } else {
                    message.belongsToCurrentUser = false;
                }
                messageAdapter.add(message);
                messagesListView.scrollToPosition(messageAdapter.messages.size() - 1);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ValueEventListener listener = new ValueEventListener() {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            messageAdapter.messages.clear();
            ProgressDialog pd = new ProgressDialog(ChatActivity.this);
            pd.setMessage("loading");
            pd.show();
            for(DataSnapshot ds : dataSnapshot.getChildren()) {
                if(!ds.getKey().equals("post")) {
                    Message message = ds.getValue(Message.class);
                    //Log.d(TAG, message.getText());
                    if (message.getAuthorID().equals(myUser.getUser_id())) {
                        message.belongsToCurrentUser = true;
                    } else {
                        message.belongsToCurrentUser = false;
                    }
                    messageAdapter.add(message);
                    messagesListView.scrollToPosition(messageAdapter.messages.size() - 1);
                }
            }
            pd.dismiss();

            // If current user has not sent any messages (other than interest message), then shows tooltip
            if(messageAdapter.getMyMessagesCount() == 0) {
                new SimpleTooltip.Builder(mContext)
                        .anchorView(sendMessageButton)
                        .text("Remember to discuss where to meet and how the items will be returned\n\n Tap to dismiss")
                        .showArrow(false)
                        .gravity(Gravity.TOP)
                        .animated(true)
                        .dismissOnOutsideTouch(false)
                        .build()
                        .show();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mContext = ChatActivity.this;

        getCurrentUserInfo();

        //set up the channel info
        messageID = getIntent().getStringExtra("MessageChannelID");
        messageUID = getIntent().getStringExtra("MessageChannelUID");

        messageAdapter = new MessageAdapter(mContext, this.messageID);

        ToolBarText = "";
        textView = (TextView) findViewById(R.id.listingName);
        editText = (EditText) findViewById(R.id.editText);
        sendMessageButton = (ImageButton) findViewById(R.id.SendMessageButton);
        mContext = ChatActivity.this;
        messagesListView = (RecyclerView) findViewById(R.id.messages_view);
        messagesListView.setAdapter(messageAdapter);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //linearLayoutManager.setReverseLayout(true);
        messagesListView.setLayoutManager(linearLayoutManager);


        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(mContext, "Click on send message button" , Toast.LENGTH_SHORT).show();
                sendMessage();
            }
        });



        if(messageID.equals("welcomeMessage")) {
            editText.setEnabled(false);
            editText.setHint("Disabled");
        }
        getListingName();
        previewPostImageView = (ImageView) findViewById(R.id.itemPicturePreview);
        findViewById(R.id.messages_view).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if(getCurrentFocus() == null || getCurrentFocus().getWindowToken() == null) {
                    return false;
                }
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                return false;
            }
        });
        //scrollToBottom();
    }

//    private void scrollToBottom() {
//        messagesListView.post(new Runnable() {
//            @Override
//            public void run() {
//                messagesListView.setSelection(messageAdapter.getCount() -1);
//            }
//        });
//
//        messagesListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
//        messagesListView.setStackFromBottom(true);
//    }

    private void getCurrentUserInfo(){
        Query query1 = mReference.child("users").child(mAuth.getCurrentUser().getUid());
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("This is the user : " + dataSnapshot.getValue());
                myUser = dataSnapshot.getValue(User.class);
                Log.d(TAG, myUser.getUsername());
                Query query = mReference.child("messages").child(messageID);
                //query.addListenerForSingleValueEvent(listener);
                messageAdapter.messages.clear();
                query.addChildEventListener(childEventListener);
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
            String time = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                time = ZonedDateTime.now().toString();
            }
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss yyyy/MM/dd");
            Date currentTime = Calendar.getInstance().getTime();
            Message newMessageInsert = new Message(myUser.getUsername(), message, dateFormat.format(currentTime), true, myUser.getUser_id());
            Log.d(TAG, "messageID is " + messageID + " messageUID: " + messageUID);

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
                                //messagesListView.setSelection(messageAdapter.getCount() - 1);
                            }
                        });
                    }
                }
            });
        }

    }

    private String getRandomColor() {
        Random r = new Random();
        StringBuffer sb = new StringBuffer("#");
        while(sb.length() < 7){
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 7);
    }

    /**
     * Sets fields for imageURL, postUID, and zipcode
     * Shows post image preview in top toolbar
     * Adds onClickListener to item image ImageView to show item details in ListingActivity
     * @param imageURL
     * @param postUID
     * @param postZip
     */
    public void setPostDetails(String imageURL, String postUID, String postZip) {
        this.previewImageURL = imageURL;
        this.previewPostUID = postUID;
        this.previewPostZipcode = postZip;
        Glide.with(mContext)
                .load(previewImageURL)
                .into(previewPostImageView);
        Log.d(TAG, "postUID is" + postUID);
        Log.d(TAG, "zipcode is " + postZip);
        previewPostImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, ListingActivity.class);
                Bundle args = new Bundle();
                args.putString("ITEM_ID", previewPostUID);
                args.putString("CITY", previewPostZipcode);
                intent.putExtras(args);
                startActivity(intent);
            }
        });
    }

    private void getListingName() {
        Query query = mReference.child("messages").child(messageID).child("post");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MessagePost post = dataSnapshot.getValue(MessagePost.class);
                if(post == null || post.getTitle() == null) {
                    Log.d(TAG, "post was null, finishing chatactivity");
                    Toast.makeText(ChatActivity.this, "Could not get Messages. This item may have been deleted", Toast.LENGTH_SHORT);
                    finish();
                } else {
                    Log.d(TAG, "Post was not null");
                    addToTitle(post.getTitle());
                    String imageURL = post.getImageURL();
                    String postUID = post.getPostID();
                    String postZip = post.getZipcode();
                    setPostDetails(imageURL, postUID, postZip);
                    if (post.getUserUID() != null) {
                        Query userQuery = mReference.child("users").child(post.getUserUID());
                        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                currentUser = dataSnapshot.getValue(User.class);
                                addToTitle(currentUser.getUsername());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /**
     * Adds a string for the title bar inside of chatactivity
     * Format should be "{title of post} - {Author Username}"
     * Limits length of title to 28 characters to prevent long title from bleeding over
     * @param str
     */
    private void addToTitle(String str) {
        if (this.ToolBarText.length() == 0) {
            this.ToolBarText = str;
        } else {
            this.ToolBarText += " - " + str;
        }
        String input = limitStringLength(this.ToolBarText, 28);
        this.textView.setText(input);
    }

    /**
     * Limits the length of the given string to the limit, if it is above the limit
     * @param string
     * @param limit
     */
    private String limitStringLength(String string, int limit) {
        if(string == null || limit < 5 || string.length() <= limit) {
            return string;
        } else {
            return string.substring(0, limit) + "...";
        }
    }

    protected void onPause() {
        super.onPause();
        Query query = mReference.child("messages").child(messageID);
        query.removeEventListener(listener);
    }

    protected void onDestroy() {
        super.onDestroy();
        Query query = mReference.child("messages").child(messageID);
        query.removeEventListener(childEventListener);
    }
}