package to.rent.rentto.Messages;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by Quan Nguyen on 2/26/2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "FROM: " + remoteMessage.getFrom());
        if(remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data: " + remoteMessage.getData());
        }

        //check if the message contains notification
        if(remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message body " + remoteMessage.getNotification().getBody());
        }
    }
}
