package to.rent.rentto.Messages;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import to.rent.rentto.Models.Message;
import to.rent.rentto.Models.UserSettings;
import to.rent.rentto.Profile.ProfilePreviewActivity;
import to.rent.rentto.R;
import to.rent.rentto.Utils.FirebaseMethods;

import static android.content.ContentValues.TAG;

public class MessageAdapter extends RecyclerView.Adapter {

    public List<Message> messages = new ArrayList<Message>();
    Context context;
    String otherProfilePicURL;
    private String otherProfileUID;
    private String currentUID;
    private DatabaseReference mRef;
    private FirebaseMethods mFirebaseMethods;
    private ImageView authorAvatarPic;
    private int myMessagesCount;
    private Query mQuery;
    public HashMap<String, String> imageCache;


    public MessageAdapter(Context context, String messageID) {
        myMessagesCount = 0;
        this.context = context;
        currentUID = FirebaseAuth.getInstance().getUid();
        mRef = FirebaseDatabase.getInstance().getReference();
        System.out.println("This is the message ID in messageadapter " + messageID);
        mQuery = mRef.child("messages").child(messageID);
        imageCache = new HashMap<>();
    }

    public void add(Message message) {
        if(!message.getAuthorID().equals(currentUID)) {
            Log.d(TAG, "inside add, authorid =" + message.getAuthorID());
            otherProfileUID = message.getAuthorID();
            if(!imageCache.containsKey(otherProfileUID)) {
                getProfilePic();
            }
        } else { // this user has messaged
            if(!message.text.startsWith("I am interested in your")) {
                myMessagesCount++;
            }
        }
        this.messages.add(message);
//        Collections.sort(this.messages, new Comparator<Message>() {
//            @Override
//            public int compare(Message o1, Message o2) {
//                DateFormat f = new SimpleDateFormat("HH:mm:ss yyyy/MM/dd");
//                try {
//                    Log.d("MessageAdapter", "successfully parse");
//                    return f.parse(o1.date).compareTo(f.parse(o1.date));
//                } catch (ParseException e) {
//                    Log.d("MessageAdapter", e.toString());
//                    return 0;
//                }
//            }
//        });
        notifyDataSetChanged(); // to render the list we need to notify
    }

    public void getProfilePic() {
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!imageCache.containsKey(otherProfileUID)) {//retrieve user information from the database
                    try {
                        System.out.println("It got inside getProfilePic");
                        System.out.println(imageCache.containsKey(otherProfileUID));
                        mFirebaseMethods = new FirebaseMethods(context);
                        UserSettings userSettings = mFirebaseMethods.getUserAccountSettings(dataSnapshot, otherProfileUID);
                        otherProfilePicURL = userSettings.getSettings().getProfile_photo();
                        if (otherProfilePicURL != null && otherProfilePicURL.length() > 1) {
                            System.out.println("Check for UID inside ondatachange : " + otherProfileUID);
                            cacheImage(otherProfileUID, otherProfilePicURL);
                        } else {
                            cacheImage(otherProfileUID, "default");
                        }
                        Log.d(TAG, "Setting otherprofilepic to" + otherProfilePicURL);
                    } catch (Exception e) {
                        Log.d(TAG, "could not get other profile pic");
                        imageCache.clear();
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("getProfilePic exists and the otherProfileUID is: " + otherProfileUID);
                    System.out.println("the value is" + imageCache.get(otherProfileUID));
                    Glide.with(context)
                            .load(imageCache.get(otherProfileUID))
                            .into(authorAvatarPic);
                }

                //retrieve images for the user in question

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

            private ValueEventListener init(ImageView avatar) {
                authorAvatar = avatar;
                return this;
            }

            private ImageView authorAvatar;
        }.init(authorAvatarPic));

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view ;
        Log.d(TAG, "oncreate view holder");
        if(viewType == 1){
            view = messageInflater.inflate(R.layout.my_message, null);
        } else {
            view = messageInflater.inflate(R.layout.their_message, null);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        final Message message = messages.get(position);

        if (message.isBelongsToCurrentUser()) { // this message was sent by us so let's create a basic chat bubble on the right
            ((MessageViewHolder) holder).messageBody.setText(message.getText());
        } else { // this message was sent by someone else so let's create an advanced chat bubble on the left
            Log.d("Hello", "this is in the getView method, other guys");
            otherProfileUID = message.authorID;
            if(otherProfileUID != null) {
                Log.d(TAG, "trying to get other profile pic, authorID is " + otherProfileUID);
                authorAvatarPic = ((MessageViewHolder) holder).avatar;
                if(!imageCache.containsKey(otherProfileUID)) {
                    getProfilePic();
                }
                String tag = imageCache.get(otherProfileUID);
                if(tag != null) {
                    System.out.println("Checking for UID : " + tag);
                    if (tag.equals("default")) {
                        System.out.println("It's running default");
                        authorAvatarPic.setImageResource(R.drawable.profile_default_pic);
                    } else {
                        Glide.with(context)
                                .load(tag)
                                .into(authorAvatarPic);
                    }
                }
            }

            //holder.name.setText(message.getData().getName());
            ((MessageViewHolder) holder).name.setText(message.getAuthor());
            ((MessageViewHolder) holder).messageBody.setText(message.getText());
            String time = message.date;
            System.out.println("The date is this : " + time);
            ((MessageViewHolder) holder).messageDate.setText(time); // Format and convert back to string
            Log.d("MsgAdpter",  "authorid is " + message.getAuthorID());
            ((MessageViewHolder) holder).avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("MsgAdpterProfileClick",  "authorid is " + message.getAuthorID());
                    Log.d(TAG, "The author pic icon was clicked");
                    Intent intent1 = new Intent(context, ProfilePreviewActivity.class);
                    intent1.putExtra("authorUID", message.getAuthorID());
                    intent1.putExtra("ACTIVITY_NUM", 4); // so it will highlight bottom nav as itemlisting
                    context.startActivity(intent1);
                }
            });
            /*
            GradientDrawable drawable = (GradientDrawable) holder.avatar.getBackground();
            drawable.setColor(Color.parseColor("#FFFF00"));
            */
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        final Message message = messages.get(position);
        if (message.isBelongsToCurrentUser()) {
            return 1;
        } else {
            return 0;
        }
    }

    public int getMyMessagesCount() {
        return myMessagesCount;
    }

    private void cacheImage(String id, String url){
        this.imageCache.put(id, url);
    }

}

class MessageViewHolder extends RecyclerView.ViewHolder{
    public ImageView avatar;
    public TextView name;
    public TextView messageBody;
    public TextView messageDate;

    public MessageViewHolder(View itemView) {
        super(itemView);
        avatar = (ImageView) itemView.findViewById(R.id.image_message_profile);
        name = (TextView) itemView.findViewById(R.id.text_message_name);
        messageBody = (TextView) itemView.findViewById(R.id.text_message_body);
        messageDate = (TextView) itemView.findViewById(R.id.text_message_time);

    }
}
