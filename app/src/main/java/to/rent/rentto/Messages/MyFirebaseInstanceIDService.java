package to.rent.rentto.Messages;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Quan Nguyen on 2/26/2018.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }

    //update the token on firebase
    private void sendRegistrationToServer(String refreshedToken) {
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if(auth != null) {
                FirebaseUser currentUser = auth.getCurrentUser();
                if (currentUser != null) {
                    try {
                        FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid()).child("notificationTokens").child(refreshedToken).setValue(true);
                    } catch (Exception e) {
                        Log.d(TAG, "Could not get notification token");
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e){ // There is no current user signed in
            Log.d(TAG, "Cannot get FirebaseAuth instance");
            e.printStackTrace();
        }
    }
}
