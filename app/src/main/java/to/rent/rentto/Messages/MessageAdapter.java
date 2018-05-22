package to.rent.rentto.Messages;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import to.rent.rentto.Models.Message;
import to.rent.rentto.Models.UserSettings;
import to.rent.rentto.Profile.ProfilePreviewActivity;
import to.rent.rentto.R;
import to.rent.rentto.Utils.FirebaseMethods;

import static android.content.ContentValues.TAG;

public class MessageAdapter extends BaseAdapter {

    public List<Message> messages = new ArrayList<Message>();
    Context context;
    String otherProfilePicURL;
    private String otherProfileUID;
    private String currentUID;
    private DatabaseReference mRef;
    private FirebaseMethods mFirebaseMethods;
    private ImageView authorAvatarPic;
    private int myMessagesCount;


    public MessageAdapter(Context context) {
        myMessagesCount = 0;
        this.context = context;
        currentUID = FirebaseAuth.getInstance().getUid();
        mRef = FirebaseDatabase.getInstance().getReference();

    }

    public void add(Message message) {
        if(!message.getAuthorID().equals(currentUID)) {
            Log.d(TAG, "inside add, authorid =" + message.getAuthorID());
            otherProfileUID = message.getAuthorID();
            getProfilePic();
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
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //retrieve user information from the database
                try {
                    mFirebaseMethods = new FirebaseMethods(context);
                    UserSettings userSettings= mFirebaseMethods.getUserAccountSettings(dataSnapshot, otherProfileUID);
                    otherProfilePicURL = userSettings.getSettings().getProfile_photo();
                    if(otherProfilePicURL != null && otherProfilePicURL.length() > 1) {
                        Glide.with(context)
                                .load(otherProfilePicURL)
                                .into(authorAvatar);
                    } else {
                        authorAvatar.setImageResource(R.drawable.profile_default_pic);
                    }
                    Log.d(TAG, "Setting otherprofilepic to" + otherProfilePicURL);
                } catch(Exception e) {
                    Log.d(TAG, "could not get other profile pic");
                    e.printStackTrace();
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
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public int getMyMessagesCount() {
        return myMessagesCount;
    }

    // This is the backbone of the class, it handles the creation of single ListView row (chat bubble)
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        MessageViewHolder holder = new MessageViewHolder();
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        final Message message = messages.get(i);

        if (message.isBelongsToCurrentUser()) { // this message was sent by us so let's create a basic chat bubble on the right
            Log.d("Hello", "this is in the getView method");
            convertView = messageInflater.inflate(R.layout.my_message, null);
            holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
            convertView.setTag(holder);
            holder.messageBody.setText(message.getText());
        } else { // this message was sent by someone else so let's create an advanced chat bubble on the left
            Log.d("Hello", "this is in the getView method, other guys");
            convertView = messageInflater.inflate(R.layout.their_message, null);
            holder.avatar = (ImageView) convertView.findViewById(R.id.image_message_profile);
            holder.name = (TextView) convertView.findViewById(R.id.text_message_name);
            holder.messageBody = (TextView) convertView.findViewById(R.id.text_message_body);
            holder.messageDate = (TextView) convertView.findViewById(R.id.text_message_time);
            otherProfileUID = message.authorID;
            if(otherProfileUID != null) {
                Log.d(TAG, "trying to get other profile pic, authorID is " + otherProfileUID);
                authorAvatarPic = holder.avatar;
                getProfilePic();
            }
            convertView.setTag(holder);

            //holder.name.setText(message.getData().getName());
            holder.name.setText(message.getAuthor());
            holder.messageBody.setText(message.getText());
            String time = message.date;
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm"); // Changed from HH:MM MM/dd/yy to MM/dd/yy HH:mm
            Date date = new Date(); // Initialize a date object for now
            try {
                date = sdf.parse(message.date); // Parse message creation date into our initialized date
            } catch (ParseException e) {
                e.printStackTrace();
            }
            holder.messageDate.setText(sdf.format(date)); // Format and convert back to string
            Log.d("MsgAdpter",  "authorid is " + message.getAuthorID());
            holder.avatar.setOnClickListener(new View.OnClickListener() {
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
        return convertView;
    }
}

class MessageViewHolder {
    public ImageView avatar;
    public TextView name;
    public TextView messageBody;
    public TextView messageDate;
}
