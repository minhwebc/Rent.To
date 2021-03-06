package to.rent.rentto.Messages;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import to.rent.rentto.R;

/**
 * Created by Quan Nguyen on 2/26/2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMService";
    private LocalBroadcastManager broadcaster;

    private static final int NOTI_PENDING_INTENT_ID = 1;
    private Context mContext;

    @Override
    public void onCreate() {
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        mContext = this;
        Log.d(TAG, "FROM: " + remoteMessage.getFrom());
        if(remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data: " + remoteMessage.getData());
        }

        //check if the message contains notification
        if(remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message body " + remoteMessage.getNotification().getBody());
        }
        Intent intent = new Intent("MyData");
        intent.putExtra("message", remoteMessage.getNotification().getBody());
        broadcaster.sendBroadcast(intent);



        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference myRef = mFirebaseDatabase.getReference();
        final String[] username = new String[1];

        String userID = remoteMessage.getNotification().getBody();
        myRef.child("users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    if(ds.getKey().equals("username")) {
                        username[0] = ds.getValue(String.class);
                    }
                }
                //Make the notification
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);

                    // Configure the notification channel.
                    notificationChannel.setDescription("Channel description");
                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(Color.RED);
                    notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                    notificationChannel.enableVibration(true);
                    notificationManager.createNotificationChannel(notificationChannel);
                }


                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID);
                notificationBuilder.setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.ic_logo)
                        .setTicker("Hearty365")
                        .setPriority(Notification.PRIORITY_MAX)
                        .setContentTitle("Notification")
                        .setContentInfo("Info");
                String messageToBeSent = "";
                if(remoteMessage.getNotification().getTitle().equals("Offered")){
                    Log.d(TAG, "Offered notification received");
                    Intent notiIntent = new Intent(getApplicationContext(), NotificationActivity.class);
                    // Create a "Shopping Pending Intent"
                    PendingIntent notiPendingIntent = PendingIntent.getActivity(
                            mContext,
                            NOTI_PENDING_INTENT_ID,
                            notiIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
                    messageToBeSent = username[0] + " just offered you ";
                    notificationBuilder.setContentText(messageToBeSent).setContentIntent(notiPendingIntent);;
                } else if(remoteMessage.getNotification().getTitle().equals("Rated")){
                    Intent notiIntent = new Intent(getApplicationContext(), RatingActivity.class);
                    String postID = remoteMessage.getNotification().getIcon();
                    notiIntent.putExtra("userid_to_be_rated", remoteMessage.getNotification().getBody());
                    notiIntent.putExtra("postid", postID);
                    // Create a "Shopping Pending Intent"
                    PendingIntent notiPendingIntent = PendingIntent.getActivity(
                            mContext,
                            NOTI_PENDING_INTENT_ID,
                            notiIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
                    Log.d(TAG, "Rated notification received");
                    messageToBeSent = username[0] + " just rated you ";
                    notificationBuilder.setContentText(messageToBeSent).setContentIntent(notiPendingIntent);;
                }


                notificationManager.notify(/*notification id*/1, notificationBuilder.build());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
